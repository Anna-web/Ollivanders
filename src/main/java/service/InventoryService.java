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
                "ORDER BY i.item_type, material_name";

        List<InventoryItem> inventory = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inventory.add(new InventoryItem(
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

    public boolean updateStock(String itemType, int materialId, int quantityChange) throws SQLException {
        String sql = "UPDATE component_inventory " +
                "SET quantity = quantity + ?, last_updated = datetime('now') " +
                "WHERE item_type = ? AND material_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantityChange);
            pstmt.setString(2, itemType);
            pstmt.setInt(3, materialId);

            return pstmt.executeUpdate() > 0;
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

    public boolean updateInventory(String itemType, int materialId, int quantityChange) throws SQLException {
        // First check if item exists
        int currentQuantity = getQuantity(itemType, materialId);

        if (currentQuantity == 0 && quantityChange > 0) {
            // Item doesn't exist and we're adding - create new record
            String insertSql = "INSERT INTO component_inventory (item_type, material_id, quantity) " +
                    "VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

                pstmt.setString(1, itemType);
                pstmt.setInt(2, materialId);
                pstmt.setInt(3, quantityChange);

                return pstmt.executeUpdate() > 0;
            }
        } else {
            // Item exists - update quantity
            return updateStock(itemType, materialId, quantityChange);
        }
    }


}
