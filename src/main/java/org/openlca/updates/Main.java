package org.openlca.updates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * This class builds the update zip files from the script sources
 */
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private static final File TARGET = new File("src/main/resources/org/openlca/updates/");

	public static void main(String[] args) {
		clear();
		File file = new File("update-src");
		Set<String> uuids = new HashSet<>();
		for (File updateDir : file.listFiles()) {
			uuids.add(pack(updateDir));
		}
		writeSummary(uuids);
	}

	private static void clear() {
		if (!TARGET.exists())
			return;
		for (File file : TARGET.listFiles()) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}
	}

	private static String pack(File dir) {
		String uuid = uuid(dir);
		if (!TARGET.exists()) {
			TARGET.mkdirs();
		}
		ZipUtil.pack(dir, new File(TARGET, uuid + ".zip"));
		return uuid;
	}

	private static String uuid(File dir) {
		File f = new File(dir, "meta-info.json");
		Gson gson = new Gson();
		try (
				FileInputStream fis = new FileInputStream(f);
				Reader reader = new InputStreamReader(fis, "utf-8");
				BufferedReader buffer = new BufferedReader(reader)) {
			JsonObject json = gson.fromJson(buffer, JsonObject.class);
			return json.get("refId").getAsString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeSummary(Set<String> uuids) {
		File file = new File(TARGET, "updates.txt");
		if (file.exists()) {
			file.delete();
		}
		try (FileWriter fw = new FileWriter(file);
				BufferedWriter writer = new BufferedWriter(fw)) {
			boolean first = true;
			for (String uuid : uuids) {
				if (first) {
					first = false;
				} else {
					writer.newLine();
				}
				writer.write(uuid);
			}
		} catch (IOException e) {
			log.error("Error writing updates.txt", e);
		}
	}

}
