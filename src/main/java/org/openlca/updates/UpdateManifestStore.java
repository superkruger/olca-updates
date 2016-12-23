package org.openlca.updates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UpdateManifestStore {

	private final static Logger log = LoggerFactory.getLogger(UpdateManifestStore.class);
	private final File store;

	public UpdateManifestStore(IDatabase database) {
		store = new File(database.getFileStorageLocation(), "updates");
		if (store.exists())
			return;
		store.mkdirs();
	}

	public UpdateManifest getForRefId(String refId) {
		File file = new File(store, refId + ".MF");
		if (!file.exists())
			return null;
		try (InputStream stream = new FileInputStream(file)) {
			return Update.readManifest(stream);
		} catch (IOException e) {
			log.error("Error loading manifest " + refId, e);
			return null;
		}
	}

	public Set<UpdateManifest> getAll() {
		File[] files = store.listFiles();
		if (files == null)
			return new HashSet<>();
		Set<UpdateManifest> all = new HashSet<>();
		for (File file : files) {
			try (InputStream stream = new FileInputStream(file)) {
				all.add(Update.readManifest(stream));
			} catch (IOException e) {
				log.error("Error loading manifests", e);
			}
		}
		return all;
	}

	public UpdateManifest save(UpdateManifest manifest) {
		UpdateManifest fromDb = getForRefId(manifest.refId);
		if (fromDb != null && fromDb.executed)
			return fromDb;
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		File file = new File(store, manifest.refId + ".MF");
		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(manifest, writer);
		} catch (IOException e) {
			log.error("Error saving manifest " + manifest.refId, e);
		}
		return manifest;
	}

}
