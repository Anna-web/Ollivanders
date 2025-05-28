import db.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        String sqlFilePath = "src/main/resources/sql_scripts/schema_init.sql";

        DatabaseConnection.initializeDatabase(sqlFilePath);
    }
}
