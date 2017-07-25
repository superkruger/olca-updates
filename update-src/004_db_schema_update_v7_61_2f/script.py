def setInternalIds(process):
    for exchange in process.getExchanges():
        exchange.internalId = process.drawNextInternalId()  
    olca.updateProcess(process)

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

    olca.eachProcess(setInternalIds)

    # update database schema to version 7
    dbUtil.setVersion(7)


if __name__ == '__main__':
    main()
