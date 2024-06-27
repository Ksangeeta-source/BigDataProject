package com.example.trino.aerospike;

import java.util.List;
import java.util.Map;

public class AerospikeConfig {
    private final List<String> hosts;
    private final String namespace;

    public AerospikeConfig(Map<String, String> config) {
        this.hosts = List.of(config.get("aerospike.hosts").split(","));
        this.namespace = config.get("aerospike.namespace");
    }

    public List<String> getHosts() {
        return hosts;
    }

    public String getNamespace() {
        return namespace;
    }
}