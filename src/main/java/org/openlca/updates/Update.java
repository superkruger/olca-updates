package org.openlca.updates;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class Update implements Comparable<Update> {

	public final UpdateMetaInfo metaInfo;
	public final String script;
	public final byte[] attachment;
	public final List<ScriptFile> files;

	public static Update open(InputStream updateData) throws IOException {
		ZipInputStream stream = new ZipInputStream(updateData);
		ZipEntry entry = null;
		UpdateMetaInfo metaInfo = null;
		String script = null;
		byte[] attachment = null;
		List<ScriptFile> files = new ArrayList<>();
		while ((entry = stream.getNextEntry()) != null) {
			switch (entry.getName()) {
			case "meta-info.json":
				metaInfo = readMetaInfo(stream);
				break;
			case "script.py":
				script = readScript(stream);
				break;
			case "attachment.pdf":
				attachment = readEntry(stream);
				break;
			default:
				files.add(new ScriptFile(entry.getName(), readEntry(stream)));
				break;
			}
			stream.closeEntry();
		}
		stream.close();
		return new Update(metaInfo, script, files, attachment);
	}
	
	public static Update wrap(UpdateMetaInfo metaInfo) {
		return new Update(metaInfo);
	}

	private Update(UpdateMetaInfo metaInfo) {
		this.metaInfo = metaInfo;
		this.script = null;
		this.files = new ArrayList<>();
		this.attachment = null;
	}

	private Update(UpdateMetaInfo metaInfo, String script, List<ScriptFile> files, byte[] attachment) {
		this.metaInfo = metaInfo;
		this.script = script;
		this.files = files;
		this.attachment = attachment;
	}

	public static UpdateMetaInfo readMetaInfo(InputStream stream) throws IOException {
		byte[] data = readEntry(stream);
		String json = new String(data, Charset.forName("utf-8"));
		JsonReader reader = new JsonReader(new StringReader(json));
		reader.setLenient(true);
		return new GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(reader, UpdateMetaInfo.class);
	}

	private static String readScript(ZipInputStream stream) throws IOException {
		byte[] data = readEntry(stream);
		return new String(data, Charset.forName("utf-8"));
	}

	private static byte[] readEntry(InputStream stream) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024];
		int len = -1;
		while ((len = stream.read(bytes)) != -1) {
			out.write(bytes, 0, len);
		}
		return out.toByteArray();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Update))
			return false;
		Update update = (Update) obj;
		return update.metaInfo.equals(metaInfo);
	}
	
	@Override
	public int hashCode() {
		return metaInfo.hashCode();
	}

	@Override
	public int compareTo(Update o) {
		return metaInfo.compareTo(o.metaInfo);
	}
	
	public static class ScriptFile {

		public final String filename;
		public final byte[] data;

		private ScriptFile(String filename, byte[] data) {
			this.filename = filename;
			this.data = data;
		}

	}

}
