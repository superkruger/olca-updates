package org.openlca.updates;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateMetaInfo implements Comparable<UpdateMetaInfo> {

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
		if (!(obj instanceof UpdateMetaInfo))
			return false;
		UpdateMetaInfo meta = (UpdateMetaInfo) obj;
		return meta.refId.equals(refId);
	}

	@Override
	public int hashCode() {
		return refId.hashCode();
	}
	
	@Override
	public int compareTo(UpdateMetaInfo o) {
		return Integer.compare(dbVersion, o.dbVersion);
	}

}
