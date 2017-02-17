package org.openlca.updates.script;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.openlca.core.database.IDatabase;
import org.openlca.updates.DbUtil;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Python {

	private final static Logger log = LoggerFactory.getLogger(Python.class);
	private final File pythonDir;
	private final Map<String, Object> properties = new HashMap<>();
	private IDatabase database;
	private CalculationContext context;

	public Python(File pythonDir) {
		this.pythonDir = pythonDir;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	public void setContext(CalculationContext context) {
		this.context = context;
	}

	public void setDataDir(File dataDir) {
		properties.put("__datadir__", dataDir.getAbsolutePath());
	}

	public void register(String key, Object value) {
		properties.put(key, value);
	}

	public boolean eval(String script) {
		try {
			System.setProperty("python.path", pythonDir.getAbsolutePath());
			System.setProperty("python.home", pythonDir.getAbsolutePath());
			String fullScript = prependImports(script);
			doEval(fullScript);
			return true;
		} catch (Exception e) {
			log.error("failed to evaluate script", e);
			return false;
		}
	}

	private void doEval(String script) {
		try (PythonInterpreter interpreter = new PythonInterpreter()) {
			interpreter.set("log", LoggerFactory.getLogger(Python.class));
			interpreter.set("db", database);
			interpreter.set("dbUtil", new DbUtil(database));
			interpreter.set("olca", new ScriptApi(database, context));
			for (Entry<String, Object> entry : properties.entrySet()) {
				interpreter.set(entry.getKey(), entry.getValue());
			}
			interpreter.exec(script);
		}
	}

	private static String prependImports(String script) throws IOException {
		StringBuilder builder = new StringBuilder();
		Properties properties = new Properties();
		properties.load(Python.class.getResourceAsStream("bindings.properties"));
		properties.forEach((name, fullName) -> {
			builder.append("import ")
					.append(fullName)
					.append(" as ")
					.append(name)
					.append("\n");
		});
		builder.append(script);
		return builder.toString();
	}

}
