import db.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        // Path to your schema_init.sql file
        String sqlFilePath = "src/main/resources/sql_scripts/schema_init.sql"; // Change this to your actual path

        // Initialize the database
        DatabaseConnection.initializeDatabase(sqlFilePath);
    }
}
