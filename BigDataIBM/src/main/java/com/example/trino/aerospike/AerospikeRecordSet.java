package com.example.trino.aerospike;

import com.aerospike.client.query.KeyRecord;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.trino.spi.connector.*;
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.query.Statement;
import com.aerospike.client.Record;
import io.trino.spi.type.Type;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AerospikeRecordSet implements RecordSet {
    private final AerospikeClient client;
    private final String namespace;
    private final String setName;
    private final List<AerospikeColumnHandle> columns;

    public AerospikeRecordSet(AerospikeClient client, String namespace, String setName, List<AerospikeColumnHandle> columns) {
        this.client = client;
        this.namespace = namespace;
        this.setName = setName;
        this.columns = columns;
    }

    @Override
    public List<Type> getColumnTypes() {
        return columns.stream().map(AerospikeColumnHandle::getType).collect(Collectors.toList());
    }

    @Override
    public RecordCursor cursor() {
        return new AerospikeRecordCursor(client, namespace, setName, columns);
    }

    private static class AerospikeRecordCursor implements RecordCursor {
        private final AerospikeClient client;
        private final String namespace;
        private final String setName;
        private final List<AerospikeColumnHandle> columns;
        private Iterator<KeyRecord> recordIterator;
        private Record currentRecord;

        public AerospikeRecordCursor(AerospikeClient client, String namespace, String setName, List<AerospikeColumnHandle> columns) {
            this.client = client;
            this.namespace = namespace;
            this.setName = setName;
            this.columns = columns;
            Statement stmt = new Statement();
            stmt.setNamespace(namespace);
            stmt.setSetName(setName);
            this.recordIterator = client.query(null, stmt).iterator();
        }

        @Override
        public long getCompletedBytes() {
            return 0;
        }

        @Override
        public long getReadTimeNanos() {
            return 0;
        }

        @Override
        public Type getType(int field) {
            return columns.get(field).getType();
        }

        @Override
        public boolean advanceNextPosition() {
            if (recordIterator.hasNext()) {
                currentRecord = recordIterator.next().record;
                return true;
            }
            return false;
        }

        @Override
        public boolean getBoolean(int field) {
            return (boolean) getValue(field);
        }

        @Override
        public long getLong(int field) {
            return (long) getValue(field);
        }

        @Override
        public double getDouble(int field) {
            return (double) getValue(field);
        }

        @Override
        public Slice getSlice(int field) {
            Object value = getValue(field);
            return value != null ? Slices.utf8Slice(value.toString()) : Slices.EMPTY_SLICE;
        }

        @Override
        public Object getObject(int field) {
            return getValue(field);
        }

        @Override
        public boolean isNull(int field) {
            return getValue(field) == null;
        }

        private Object getValue(int field) {
            return currentRecord.getValue(columns.get(field).getName());
        }

        @Override
        public void close() {
            // No resources to close
        }
    }
}