package com.example.trino.aerospike;

import io.trino.spi.connector.ConnectorTableHandle;

public class AerospikeTableHandle implements ConnectorTableHandle {
    private final String schemaName;
    private final String tableName;

    public AerospikeTableHandle(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }
}