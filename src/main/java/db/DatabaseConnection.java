package db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                String dbPath = findDatabasePath();
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found", e);
            }
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void initializeDatabase(String sqlFilePath) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader br = new BufferedReader(new FileReader(sqlFilePath))) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sql.append(line).append("\n");
            }
            stmt.execute(sql.toString());
            System.out.println("Database initialized successfully.");
        } catch (IOException e) {
            System.err.println("Error reading SQL file: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    private static String findDatabasePath() {
        String[] possiblePaths = {
                "ollivanders.db",
                "../ollivanders.db",
                "db/ollivanders.db",
                System.getProperty("user.dir") + "/ollivanders.db",
        };

        for (String path : possiblePaths) {
            File dbFile = new File(path);
            if (dbFile.exists()) {
                return dbFile.getPath();
            }
        }

        throw new RuntimeException("Could not find 'ollivanders.db' in:\n" +
                String.join("\n", possiblePaths));
    }
}
