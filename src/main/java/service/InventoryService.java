package service;

import db.DatabaseConnection;
import model.InventoryItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class InventoryService {

    public List<InventoryItem> getFullInventory() throws SQLException {
        String sql = "SELECT i.*, " +
                "CASE WHEN i.item_type = 'wood' THEN w.name ELSE c.material END AS material_name " +
                "FROM component_inventory i " +
                "LEFT JOIN wood_types w ON i.item_type = 'wood' AND i.material_id = w.wood_id " +
                "LEFT JOIN cores c ON i.item_type = 'core' AND i.material_id = c.core_id " +
                "ORDER BY item_id";

        List<InventoryItem> inventory = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inventory.add(new InventoryItem(
                        rs.getInt("item_id"),
                        rs.getString("item_type"),
                        rs.getInt("material_id"),
                        rs.getString("material_name"),
                        rs.getInt("quantity"),
                        rs.getString("last_updated")
                ));
            }
        }
        return inventory;
    }

    public boolean updateStock(String itemType, int materialId, int quantityChange, Connection conn) throws SQLException {
        String updateSql = "UPDATE component_inventory " +
                "SET quantity = quantity + ?, last_updated = datetime('now') " +
                "WHERE item_type = ? AND material_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, quantityChange);
            pstmt.setString(2, itemType);
            pstmt.setInt(3, materialId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0 && quantityChange > 0) {
                // Item doesn't exist - insert new record if adding stock
                String insertSql = "INSERT INTO component_inventory (item_type, material_id, quantity) " +
                        "VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, itemType);
                    insertStmt.setInt(2, materialId);
                    insertStmt.setInt(3, quantityChange);
                    insertStmt.executeUpdate();
                }
            }
            return true;
        }
    }

    public int getQuantity(String itemType, int materialId) throws SQLException {
        String sql = "SELECT quantity FROM component_inventory " +
                "WHERE item_type = ? AND material_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemType);
            pstmt.setInt(2, materialId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("quantity") : 0;
            }
        }
    }

}
