package com.example.trino.aerospike;

import io.trino.spi.connector.*;
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.policy.ClientPolicy;
import io.trino.spi.transaction.IsolationLevel;

public class AerospikeConnector implements Connector {
    private final AerospikeClient client;
    private final AerospikeMetadata metadata;
    private final AerospikeSplitManager splitManager;

    public AerospikeConnector(AerospikeConfig config) {
        ClientPolicy policy = new ClientPolicy();
        this.client = new AerospikeClient(policy, config.getHosts().get(0), 3000);
        this.metadata = new AerospikeMetadata(client, config);
        this.splitManager = new AerospikeSplitManager(client, config);
    }

    @Override
    public ConnectorTransactionHandle beginTransaction(IsolationLevel isolationLevel, boolean readOnly) {
        return new AerospikeTransactionHandle();
    }

    @Override
    public ConnectorMetadata getMetadata(ConnectorTransactionHandle transactionHandle) {
        return metadata;
    }

    @Override
    public ConnectorSplitManager getSplitManager() {
        return splitManager;
    }

    @Override
    public void shutdown() {
        client.close();
    }

    private static class AerospikeTransactionHandle implements ConnectorTransactionHandle {}
}