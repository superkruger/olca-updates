import java.sql.ResultSet as ResultSet

def main():
    global dbUtil

    # add a new column for storing parameter aggregation
    # functions for regionalized LCIA methods
    dbUtil.createColumn(
        'tbl_impact_methods',
        'parameter_mean',
        'parameter_mean VARCHAR(255)')

    dbUtil.createColumn(
        'tbl_processes',
        'last_internal_id',
        'last_internal_id INTEGER')

    dbUtil.createColumn(
        'tbl_exchanges',
        'internal_id',
        'internal_id INTEGER')

    con = db.createConnection()
    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
    rs = stmt.executeQuery('SELECT * FROM tbl_exchanges')
    counter = {}
    while (rs.next()):
        p_id = str(rs.getLong('f_owner'))
        last = counter.get(p_id)
        if not last:
            last = 0
        last = last + 1
        counter[p_id] = last
        rs.updateInt('internal_id', last)
        rs.updateRow()
    rs.close()
    stmt.close()
    con.commit()
    con.close()
    con = db.createConnection()
    stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
    rs = stmt.executeQuery('SELECT * FROM tbl_processes')
    while (rs.next()):
        p_id = str(rs.getLong('id'))
        last = counter.get(p_id)
        rs.updateInt('last_internal_id', last)
        rs.updateRow()
    rs.close()
    stmt.close()
    con.commit()
    con.close()

    # update database schema to version 7
    dbUtil.setVersion(7)


if __name__ == '__main__':
    main()
