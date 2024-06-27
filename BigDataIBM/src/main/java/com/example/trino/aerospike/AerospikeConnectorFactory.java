package com.example.trino.aerospike;

import io.trino.spi.connector.*;

import java.util.Map;

public class AerospikeConnectorFactory implements ConnectorFactory {
    @Override
    public String getName() {
        return "aerospike";
    }

    @Override
    public Connector create(String catalogName, Map<String, String> config, ConnectorContext context) {
        AerospikeConfig aerospikeConfig = new AerospikeConfig(config);
        return new AerospikeConnector(aerospikeConfig);
    }
}
