package com.example.trino.aerospike;

import io.trino.spi.HostAddress;
import io.trino.spi.connector.*;
import com.aerospike.client.AerospikeClient;

import java.util.ArrayList;
import java.util.List;

public class AerospikeSplitManager implements ConnectorSplitManager {
    private final AerospikeClient client;
    private final String namespace;

    public AerospikeSplitManager(AerospikeClient client, AerospikeConfig config) {
        this.client = client;
        this.namespace = config.getNamespace();
    }

    @Override
    public ConnectorSplitSource getSplits(
            ConnectorTransactionHandle transaction,
            ConnectorSession session,
            ConnectorTableHandle table,
            SplitSchedulingStrategy splitSchedulingStrategy) {
        AerospikeTableHandle tableHandle = (AerospikeTableHandle) table;
        List<ConnectorSplit> splits = new ArrayList<>();
        splits.add(new AerospikeSplit(tableHandle.getSchemaName(), tableHandle.getTableName()));
        return new FixedSplitSource(splits);
    }

    private static class AerospikeSplit implements ConnectorSplit {
        private final String schemaName;
        private final String tableName;

        public AerospikeSplit(String schemaName, String tableName) {
            this.schemaName = schemaName;
            this.tableName = tableName;
        }

        @Override
        public boolean isRemotelyAccessible() {
            return true;
        }

        @Override
        public List<HostAddress> getAddresses() {
            return List.of();
        }

        @Override
        public Object getInfo() {
            return this;
        }
    }
}