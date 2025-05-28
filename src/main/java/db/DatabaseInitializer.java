package db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String SCHEMA_SCRIPT = "/sql_scripts/schema_init.sql";
    private static final String SAMPLE_DATA_SCRIPT = "/sql_scripts/sample_data.sql";

    public static void initializeDatabase() throws SQLException, IOException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("CREATING TABLES...");
            executeSqlScript(conn, SCHEMA_SCRIPT);

            System.out.println("EMPTY DB, POPULATING...");
            executeSqlScript(conn, SAMPLE_DATA_SCRIPT);

            System.out.println("DONE!");
        }
    }

    private static void executeSqlScript(Connection conn, String scriptPath)
            throws SQLException, IOException {
        try (InputStream is = DatabaseInitializer.class.getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            String[] queries = sb.toString().split(";(?=([^']*'[^']*')*[^']*$)");

            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query);
                }
            }
        }
    }

    private static boolean isDatabaseEmpty(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "%", null)) {
            return !rs.next();
        }
    }
}