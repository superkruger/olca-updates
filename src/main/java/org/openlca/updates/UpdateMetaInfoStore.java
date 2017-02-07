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

public class UpdateMetaInfoStore {

	private final static Logger log = LoggerFactory.getLogger(UpdateMetaInfoStore.class);
	private final File store;

	public UpdateMetaInfoStore(IDatabase database) {
		store = new File(database.getFileStorageLocation(), "updates");
		if (store.exists())
			return;
		store.mkdirs();
	}

	public UpdateMetaInfo getForRefId(String refId) {
		File file = new File(store, refId + ".MF");
		if (!file.exists())
			return null;
		try (InputStream stream = new FileInputStream(file)) {
			return Update.readMetaInfo(stream);
		} catch (IOException e) {
			log.error("Error loading meta info " + refId, e);
			return null;
		}
	}

	public Set<UpdateMetaInfo> getAll() {
		File[] files = store.listFiles();
		if (files == null)
			return new HashSet<>();
		Set<UpdateMetaInfo> all = new HashSet<>();
		for (File file : files) {
			try (InputStream stream = new FileInputStream(file)) {
				all.add(Update.readMetaInfo(stream));
			} catch (IOException e) {
				log.error("Error loading meta infos", e);
			}
		}
		return all;
	}

	public UpdateMetaInfo save(UpdateMetaInfo metaInfo) {
		UpdateMetaInfo fromDb = getForRefId(metaInfo.refId);
		if (fromDb != null && fromDb.executed)
			return fromDb;
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		File file = new File(store, metaInfo.refId + ".MF");
		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(metaInfo, writer);
		} catch (IOException e) {
			log.error("Error saving meta info " + metaInfo.refId, e);
		}
		return metaInfo;
	}

}
