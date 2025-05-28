package db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseCleaner {
    private static final String RESET_SCRIPT = "/sql_scripts/reset_db.sql";
    private static final String CONFIRMATION_MESSAGE =
            "Are you sure you want to reset the entire database? This cannot be undone!";

    public static boolean resetDatabase(boolean confirmed)
            throws SQLException, IOException {
        if (!confirmed) {
            throw new IllegalStateException("Reset operation not confirmed");
        }

        System.out.println("RESETTING DB...");

        executeResetScript();
        return true;
    }

    private static void executeResetScript() throws SQLException, IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     DatabaseCleaner.class.getResourceAsStream(RESET_SCRIPT)))) {

            String line;
            StringBuilder sqlCommand = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("--")) {
                    continue;
                }
                sqlCommand.append(line).append("\n");

                if (line.trim().endsWith(";")) {
                    statement.execute(sqlCommand.toString());
                    sqlCommand.setLength(0);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error executing reset script: " + e.getMessage(), e);
        }
    }



    public static String getConfirmationMessage() {
        return CONFIRMATION_MESSAGE;
    }
}
