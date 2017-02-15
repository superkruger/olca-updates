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
	private final UpdateMetaInfoStore store;
	private final Python python;

	public UpdateHelper(IDatabase database, CalculationContext context, File pythonDir) {
		this.python = new Python(context, pythonDir);
		this.python.setDatabase(database);
		this.database = database;
		this.store = new UpdateMetaInfoStore(database);
	}

	public Set<UpdateMetaInfo> getNewAndRequired() {
		Set<UpdateMetaInfo> updates = new HashSet<>();
		for (String refId : getUpdateIds()) {
			Update update = getForRefId(refId);
			UpdateMetaInfo fromDb = store.getForRefId(update.metaInfo.refId);
			if (fromDb != null && (!update.metaInfo.required || update.metaInfo.executed))
				continue;
			updates.add(update.metaInfo);
		}
		return updates;
	}

	public Set<UpdateMetaInfo> getAllUpdates() {
		Set<UpdateMetaInfo> updates = new HashSet<>();
		for (String refId : getUpdateIds()) {
			Update update = getForRefId(refId);
			updates.add(update.metaInfo);
		}
		for (UpdateMetaInfo metaInfo : store.getAll()) {
			if (updates.contains(metaInfo))
				continue;
			updates.add(metaInfo);
		}
		return updates;
	}

	public Set<UpdateMetaInfo> getExecuted() {
		Set<UpdateMetaInfo> updates = new HashSet<>();
		for (UpdateMetaInfo metaInfo : store.getAll()) {
			if (!metaInfo.executed)
				continue;
			if (updates.contains(metaInfo))
				continue;
			updates.add(metaInfo);
		}
		return updates;
	}

	public Update getForRefId(String refId) {
		try {
			InputStream stream = getStream(refId + ".zip");
			if (stream == null)
				return null;
			Update update = Update.open(stream);
			UpdateMetaInfo fromDb = store.getForRefId(update.metaInfo.refId);
			if (fromDb == null)
				return update;
			update.metaInfo.executed = fromDb.executed;
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
		log.info("Applying update " + update.metaInfo.name + " (" + update.metaInfo.refId + ")");
		initData(update);
		boolean succeeded = python.eval(update.script);
		if (!succeeded)
			return Status.SCRIPT_ERROR;
		update.metaInfo.executed = true;
		store.save(update.metaInfo);
		return Status.EXECUTED;
	}

	public void skip(Update update) {
		update.metaInfo.executed = false;
		store.save(update.metaInfo);
	}

	private Status checkIfApplicable(Update update) {
		UpdateMetaInfo existing = store.getForRefId(update.metaInfo.refId);
		if (existing != null && existing.executed) {
			log.info("Skipping update " + update.metaInfo.refId + " - was executed before");
			return Status.EXECUTED_BEFORE;
		}
		int dbVersion = update.metaInfo.dbVersion;
		if (dbVersion != database.getVersion()) {
			if (dbVersion < IDatabase.CURRENT_VERSION && dbVersion >= Upgrades.FINAL_UPGRADE) {
				// its safe to assume that this update was executed before
				update.metaInfo.executed = true;
				store.save(update.metaInfo);
				log.info("Skipping update " + update.metaInfo.refId + " - was executed before");
				return Status.EXECUTED_BEFORE;
			}
			log.info("Skipping update " + update.metaInfo.refId + " - db version mismatch");
			return Status.DB_MISMATCH;
		}
		for (String depRefId : update.metaInfo.dependencies) {
			existing = store.getForRefId(depRefId);
			if (existing != null && existing.executed)
				continue;
			log.info("Skipping update " + update.metaInfo.refId + " - missing dependency " + depRefId);
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
