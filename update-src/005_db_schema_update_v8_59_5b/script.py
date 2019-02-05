def main():
    global dbUtil, db

    dbUtil.createTable('tbl_source_links', '''
    CREATE TABLE tbl_source_links (
      f_owner  BIGINT,
      f_source BIGINT
    ) ''')

    dbUtil.createColumn(
        'tbl_process_links',
        'is_system_link',
        'is_system_link SMALLINT default 0')

    dbUtil.createColumn(
        'tbl_impact_methods',
        'f_author',
        'f_author BIGINT')

    dbUtil.createColumn(
        'tbl_impact_methods',
        'f_generator',
        'f_generator BIGINT')

    dbUtil.createColumn(
        'tbl_process_docs',
        'preceding_dataset',
        'preceding_dataset VARCHAR(255)')

    dbUtil.createColumn(
        'tbl_project_variants',
        'is_disabled',
        'is_disabled SMALLINT default 0')

    con = db.createConnection()
    stmt = con.createStatement()
    query = 'SELECT f_process_doc, f_source FROM tbl_process_sources'
    rs = stmt.executeQuery(query)

    insert = 'INSERT INTO tbl_source_links (f_owner, f_source) VALUES (?, ? )'
    batch = con.prepareStatement(insert)

    while (rs.next()):
        doc = rs.getLong(1)
        source = rs.getLong(2)
        batch.setLong(1, doc)
        batch.setLong(2, source)
        batch.addBatch()

    batch.executeBatch()
    batch.close()
    rs.close()
    stmt.close()
    con.commit()
    con.close()

    dbUtil.setVersion(8)


if __name__ == '__main__':
    main()
