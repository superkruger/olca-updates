import org.openlca.core.database.CategoryDao as CategoryDao

dbUtil.createTable("tbl_dq_systems",
		"CREATE TABLE tbl_dq_systems ( "
				+ "id BIGINT NOT NULL, "
				+ "name VARCHAR(255), "
				+ "ref_id VARCHAR(36), "
				+ "version BIGINT, "
				+ "last_change BIGINT, "
				+ "f_category BIGINT, "
				+ "f_source BIGINT, "
				+ "description CLOB(64 K), "
				+ "has_uncertainties SMALLINT default 0, "
				+ "PRIMARY KEY (id)) ")

dbUtil.createTable("tbl_dq_indicators",
		"CREATE TABLE tbl_dq_indicators ( "
				+ "id BIGINT NOT NULL, "
				+ "name VARCHAR(255), "
				+ "position INTEGER NOT NULL, "
				+ "f_dq_system BIGINT, "
				+ "PRIMARY KEY (id)) ")

dbUtil.createTable("tbl_dq_scores", "CREATE TABLE tbl_dq_scores ( "
		+ "id BIGINT NOT NULL, "
		+ "position INTEGER NOT NULL, "
		+ "description CLOB(64 K), "
		+ "label VARCHAR(255), "
		+ "uncertainty DOUBLE default 0, "
		+ "f_dq_indicator BIGINT, "
		+ "PRIMARY KEY (id)) ")
		
dbUtil.createColumn("tbl_processes", "dq_entry", "dq_entry VARCHAR(50)")
dbUtil.createColumn("tbl_processes", "f_dq_system", "f_dq_system BIGINT")
dbUtil.createColumn("tbl_processes", "f_exchange_dq_system", "f_exchange_dq_system BIGINT")
dbUtil.createColumn("tbl_processes", "f_social_dq_system", "f_social_dq_system BIGINT")
dbUtil.renameColumn("tbl_exchanges", "pedigree_uncertainty", "dq_entry", "VARCHAR(50)")
dbUtil.createColumn("tbl_product_systems", "cutoff", "cutoff DOUBLE")

dao = CategoryDao(db)
roots = dao.getRootCategories()
for category in roots:
	dao.update(category)
	
dbUtil.setVersion(6)