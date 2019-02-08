import org.openlca.util.DQSystems as DQSystems


def callback(rs):
    global db, olca
    systemId = DQSystems.ecoinvent(db).id
    olca.updateSql("UPDATE tbl_processes SET f_exchange_dq_system = " +
                   str(systemId) + " WHERE id IN (SELECT DISTINCT f_owner " +
                   "FROM tbl_exchanges WHERE dq_entry IS NOT NULL)")


olca.querySql(
    "SELECT count(id) FROM tbl_exchanges WHERE dq_entry IS NOT NULL", callback)
