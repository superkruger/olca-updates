package org.openlca.updates;

import org.openlca.core.database.IDatabase;
import org.openlca.updates.legacy.Upgrades;

/**
 * The result of a version check of a database.
 */
public enum VersionState {

	/**
	 * The database is up to date.
	 */
	UP_TO_DATE,

	/**
	 * The database requires updates (provided by new update concept).
	 */
	NEEDS_UPDATE,

	/**
	 * The database requires updates (provided by legacy upgrade concept).
	 */
	NEEDS_UPGRADE,

	/**
	 * The database is newer than the version of openLCA.
	 */
	HIGHER_VERSION,

	/**
	 * Could not get the version because of an error.
	 */
	ERROR;

	public static VersionState checkVersion(IDatabase database) {
		int version = database.getVersion();
		if (version < 1)
			return VersionState.ERROR;
		if (version == IDatabase.CURRENT_VERSION)
			return VersionState.UP_TO_DATE;
		if (version < IDatabase.CURRENT_VERSION)
			if (version < Upgrades.FINAL_UPGRADE)
				return VersionState.NEEDS_UPGRADE;
			else
				return VersionState.NEEDS_UPDATE;
		return VersionState.HIGHER_VERSION;
	}

}
