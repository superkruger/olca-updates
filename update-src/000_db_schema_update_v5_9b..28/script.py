import gnu.trove.map.hash.TObjectLongHashMap as TObjectLongHashMap;
import gnu.trove.impl.Constants as Constants;
import org.openlca.core.matrix.cache.FlowTypeTable as FlowTypeTable;
import org.openlca.core.matrix.LongPair as LongPair;

dbUtil.renameColumn("tbl_sources", "doi", "url", "VARCHAR(255)");
dbUtil.renameColumn("tbl_process_links", "f_recipient", "f_process", "BIGINT");
dbUtil.createColumn("tbl_process_links", "f_exchange", "f_exchange BIGINT");

flowTypes = None
idx = None

def callback(rs):
	exchangeId = rs.getLong(1);
	processId = rs.getLong(2);
	flowId = rs.getLong(3);
	isInput = rs.getBoolean(4);
	if not isInput or flowTypes.get(flowId) == FlowType.ELEMENTARY_FLOW:
		return;
	idx.put(LongPair.of(processId, flowId), exchangeId);
	return;

def updateLinks(rs):
	if rs.getInt("noOfLinks") < 1:
		return
	global idx
	idx = TObjectLongHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
	global flowTypes
	flowTypes = FlowTypeTable.create(db);
	sql = "SELECT id, f_owner, f_flow, is_input from tbl_exchanges";
	olca.querySql(sql, callback);
	con = db.createConnection();
	con.setAutoCommit(False);
	query = con.createStatement();
	query.setCursorName("UPDATE_LINKS");
	cursor = query.executeQuery("SELECT f_process, f_flow FROM tbl_process_links FOR UPDATE of f_exchange");
	update = con.prepareStatement("UPDATE tbl_process_links SET f_exchange = ? WHERE CURRENT OF UPDATE_LINKS");
	while cursor.next():
		processId = cursor.getLong(1);
		flowId = cursor.getLong(2);
		exchangeId = idx.get(LongPair.of(processId, flowId));
		if exchangeId > 0:
			update.setLong(1, exchangeId);
			update.executeUpdate();
	cursor.close();
	query.close();
	update.close();
	con.commit();
	con.close();

olca.querySql("SELECT count(f_product_system) AS noOfLinks FROM tbl_process_links", updateLinks)

dbUtil.setVersion(5);