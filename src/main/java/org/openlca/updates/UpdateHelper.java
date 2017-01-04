package org.openlca.updates;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.updates.Update.ScriptFile;
import org.openlca.updates.legacy.Upgrades;
import org.openlca.updates.script.CalculationContext;
import org.openlca.updates.script.Python;
import org.python.jline.internal.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class UpdateHelper {

	private static final Logger log = LoggerFactory.getLogger(UpdateHelper.class);
	private final IDatabase database;
	private final UpdateManifestStore store;
	private final Python python;

	public UpdateHelper(IDatabase database, CalculationContext context, File pythonDir) {
		this.python = new Python(database, context, pythonDir);
		this.database = database;
		this.store = new UpdateManifestStore(database);
	}

	public Set<UpdateManifest> getNewAndRequired() {
		Set<UpdateManifest> updates = new HashSet<>();
		for (String refId : getUpdateIds()) {
			Update update = getForRefId(refId);
			UpdateManifest fromDb = store.getForRefId(update.manifest.refId);
			if (fromDb != null && (!update.manifest.required || update.manifest.executed))
				continue;
			updates.add(update.manifest);
		}
		return updates;
	}

	public Set<UpdateManifest> getAllUpdates() {
		Set<UpdateManifest> updates = new HashSet<>();
		for (String refId : getUpdateIds()) {
			Update update = getForRefId(refId);
			updates.add(update.manifest);
		}
		for (UpdateManifest manifest : store.getAll()) {
			if (updates.contains(manifest))
				continue;
			updates.add(manifest);
		}
		return updates;
	}

	public Set<UpdateManifest> getExecuted() {
		Set<UpdateManifest> updates = new HashSet<>();
		for (UpdateManifest manifest : store.getAll()) {
			if (!manifest.executed)
				continue;
			if (updates.contains(manifest))
				continue;
			updates.add(manifest);
		}
		return updates;
	}

	public Update getForRefId(String refId) {
		try {
			Update update = Update.open(getStream(refId + ".zip"));
			UpdateManifest fromDb = store.getForRefId(update.manifest.refId);
			if (fromDb == null)
				return update;
			update.manifest.executed = fromDb.executed;
			return update;
		} catch (IOException e) {
			log.error("Error loading update " + refId, e);
			return null;
		}
	}

	private List<String> getUpdateIds() {
		List<String> paths = new ArrayList<>();
		try (InputStream in = getStream("updates.txt")) {
			if (in == null)
				return paths;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.trim().equals(""))
						continue;
					paths.add(line);
				}
			}
		} catch (IOException e) {
			log.error("Error reading update list", e);
		}
		return paths;
	}

	private InputStream getStream(String name) {
		return UpdateHelper.class.getResourceAsStream("/org/openlca/updates/" + name);
	}

	public Status execute(Update update) {
		Status status = checkIfApplicable(update);
		if (status != null)
			return status;
		log.info("Applying update " + update.manifest.name + " (" + update.manifest.refId + ")");
		initData(update);
		boolean succeeded = python.eval(update.script);
		if (!succeeded)
			return Status.SCRIPT_ERROR;
		update.manifest.executed = true;
		store.save(update.manifest);
		return Status.EXECUTED;
	}

	public void skip(Update update) {
		update.manifest.executed = false;
		store.save(update.manifest);
	}

	private Status checkIfApplicable(Update update) {
		UpdateManifest existing = store.getForRefId(update.manifest.refId);
		if (existing != null && existing.executed) {
			log.info("Skipping update " + update.manifest.refId + " - was executed before");
			return Status.EXECUTED_BEFORE;
		}
		int dbVersion = update.manifest.dbVersion;
		if (dbVersion != database.getVersion()) {
			if (dbVersion < IDatabase.CURRENT_VERSION && dbVersion >= Upgrades.FINAL_UPGRADE) {
				// its safe to assume that this update was executed before
				update.manifest.executed = true;
				store.save(update.manifest);
				log.info("Skipping update " + update.manifest.refId + " - was executed before");
				return Status.EXECUTED_BEFORE;
			}
			log.info("Skipping update " + update.manifest.refId + " - db version mismatch");
			return Status.DB_MISMATCH;
		}
		for (String depRefId : update.manifest.dependencies) {
			existing = store.getForRefId(depRefId);
			if (existing != null && existing.executed)
				continue;
			log.info("Skipping update " + update.manifest.refId + " - missing dependency " + depRefId);
			return Status.MISSING_DEPENDENCY;
		}
		return null;
	}

	private void initData(Update update) {
		if (update.files.isEmpty())
			return;
		File tmpDir = Files.createTempDir();
		tmpDir.deleteOnExit();
		for (ScriptFile file : update.files) {
			File tmpFile = new File(tmpDir, file.filename);
			try {
				java.nio.file.Files.copy(new ByteArrayInputStream(file.data), tmpFile.toPath());
			} catch (IOException e) {
				log.error("Error while copying update data to tmp file", e);
			}
			tmpFile.deleteOnExit();
		}
		python.setDataDir(tmpDir);
	}

	public enum Status {

		EXECUTED_BEFORE(false),
		DB_MISMATCH(false),
		MISSING_DEPENDENCY(false),
		SCRIPT_ERROR(false),
		EXECUTED(true);

		public final boolean executed;

		private Status(boolean executed) {
			this.executed = executed;
		}

	}

}
