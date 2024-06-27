package com.example.trino.aerospike;

import com.aerospike.client.Info;
import io.trino.spi.Node;
import io.trino.spi.connector.*;
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.query.Statement;
import com.aerospike.client.Record;
import io.trino.spi.type.*;

import java.util.*;

public class AerospikeMetadata implements ConnectorMetadata {
    private final AerospikeClient client;
    private final String namespace;

    public AerospikeMetadata(AerospikeClient client, AerospikeConfig config) {
        this.client = client;
        this.namespace = config.getNamespace();
    }

    @Override
    public List<String> listSchemaNames(ConnectorSession session) {
        return List.of(namespace);
    }

    @Override
    public List<SchemaTableName> listTables(ConnectorSession session, Optional<String> schemaName) {
        List<SchemaTableName> tables = new ArrayList<>();

        // Get the first node in the cluster
        Node[] nodes = (Node[]) client.getNodes();
        if (nodes.length == 0) {
            throw new RuntimeException("No Aerospike nodes available");
        }
        Node node = nodes[0];

        // Request info about sets
        String infoString = Info.request((com.aerospike.client.cluster.Node) node, "sets");
        if (infoString == null) {
            return tables;
        }

        // Parse the info string
        String[] setsInfo = infoString.split(";");
        for (String setInfo : setsInfo) {
            String[] parts = setInfo.split(":");
            if (parts.length > 1 && parts[0].equals(namespace)) {
                String setName = parts[1];
                tables.add(new SchemaTableName(namespace, setName));
            }
        }

        return tables;
    }
    @Override
    public ConnectorTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName) {
        return new AerospikeTableHandle(tableName.getSchemaName(), tableName.getTableName());
    }

    @Override
    public ConnectorTableMetadata getTableMetadata(ConnectorSession session, ConnectorTableHandle table) {
        AerospikeTableHandle tableHandle = (AerospikeTableHandle) table;
        List<ColumnMetadata> columns = new ArrayList<>();

        Statement stmt = new Statement();
        stmt.setNamespace(tableHandle.getSchemaName());
        stmt.setSetName(tableHandle.getTableName());

        client.query(null, stmt).forEach(keyRecord -> {
            Record record = keyRecord.record;
            for (String binName : record.bins.keySet()) {
                columns.add(ColumnMetadata.builder()
                        .setName(binName)
                        .setType(getTrinoType(record.bins.get(binName)))
                        .build());
            }
        });

        return new ConnectorTableMetadata(
                new SchemaTableName(tableHandle.getSchemaName(), tableHandle.getTableName()),
                columns);
    }

    private Type getTrinoType(Object value) {
        if (value instanceof String) return VarcharType.VARCHAR;
        if (value instanceof Integer) return IntegerType.INTEGER;
        if (value instanceof Long) return BigintType.BIGINT;
        if (value instanceof Double) return DoubleType.DOUBLE;
        return VarcharType.VARCHAR;
    }
}