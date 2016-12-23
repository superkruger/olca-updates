package org.openlca.updates;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateManifest {

	public String refId;
	public String name;
	public String description;
	public int dbVersion;
	public boolean required;
	public Date releaseDate;
	public boolean executed;
	public final List<String> dependencies = new ArrayList<>();

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof UpdateManifest))
			return false;
		UpdateManifest manifest = (UpdateManifest) obj;
		return manifest.refId.equals(refId);
	}

	@Override
	public int hashCode() {
		return refId.hashCode();
	}

}
