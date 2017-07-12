
def main():
    global dbUtil

    # add a new column for storing parameter aggregation
    # functions for regionalized LCIA methods
    dbUtil.createColumn(
        'tbl_impact_methods',
        'parameter_mean',
        'parameter_mean VARCHAR(255)')

    # update database schema to version 7
    dbUtil.setVersion(7)


if __name__ == '__main__':
    main()
