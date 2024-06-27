package org.example;

import java.sql.*;

class TrinoAerospikeExample {
    public static void main(String[] args) {
        String url = "jdbc:trino://localhost:8080/aerospike/test";

        try (Connection connection = DriverManager.getConnection(url, "user", null);
             Statement statement = connection.createStatement()) {

            // List tables
            try (ResultSet rs = statement.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            }

            // Select query
            try (ResultSet rs = statement.executeQuery("SELECT * FROM your_set LIMIT 10")) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rs.getString(i) + "\t");
                    }
                    System.out.println();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}