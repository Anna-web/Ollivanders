package service;

import db.DatabaseConnection;
import model.Delivery;
import model.DeliveryItem;
import model.Wand;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryService {

    public boolean recordDelivery(Delivery delivery) throws SQLException {
        String deliverySql = "INSERT INTO inventory_deliveries (supplier_name, received_by, notes) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO delivery_items (delivery_id, item_type, material_id, quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int deliveryId;
                try (PreparedStatement pstmt = conn.prepareStatement(deliverySql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, delivery.getSupplierName());
                    pstmt.setString(2, delivery.getReceivedBy());
                    pstmt.setString(3, delivery.getNotes());
                    pstmt.executeUpdate();

                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (!rs.next()) {
                        throw new SQLException("Failed to get delivery ID");
                    }
                    deliveryId = rs.getInt(1);
                }

                try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                    for (DeliveryItem item : delivery.getItems()) {
                        pstmt.setInt(1, deliveryId);
                        pstmt.setString(2, item.getItemType());
                        pstmt.setInt(3, item.getMaterialId());
                        pstmt.setInt(4, item.getQuantity());
                        pstmt.addBatch();

                        updateStock(item.getItemType(), item.getMaterialId(), item.getQuantity(), conn);
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void updateStock(String itemType, int materialId, int quantity, Connection conn) throws SQLException {
        String updateSql = "UPDATE component_inventory SET quantity = quantity + ? " +
                "WHERE item_type = ? AND material_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, itemType);
            pstmt.setInt(3, materialId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // Item doesn't exist, insert new record
                String insertSql = "INSERT INTO component_inventory (item_type, material_id, quantity) " +
                        "VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, itemType);
                    insertStmt.setInt(2, materialId);
                    insertStmt.setInt(3, quantity);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public List<Delivery> getDeliveryHistory() throws SQLException {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT d.*, di.item_type, di.material_id, di.quantity, " +
                "CASE WHEN di.item_type = 'wood' THEN w.name ELSE c.material END AS material_name " +
                "FROM inventory_deliveries d " +
                "LEFT JOIN delivery_items di ON d.delivery_id = di.delivery_id " +
                "LEFT JOIN wood_types w ON di.item_type = 'wood' AND di.material_id = w.wood_id " +
                "LEFT JOIN cores c ON di.item_type = 'core' AND di.material_id = c.core_id " +
                "ORDER BY d.delivery_date DESC, d.delivery_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Map<Integer, Delivery> deliveryMap = new HashMap<>();

            while (rs.next()) {
                try {
                    int deliveryId = rs.getInt("delivery_id");
                    Delivery delivery = deliveryMap.computeIfAbsent(deliveryId, id -> {
                        try {
                            Delivery d = new Delivery();
                            d.setDeliveryId(id);
                            d.setDeliveryDate(rs.getString("delivery_date"));
                            d.setSupplierName(rs.getString("supplier_name"));
                            d.setReceivedBy(rs.getString("received_by"));
                            d.setNotes(rs.getString("notes"));
                            d.setItems(new ArrayList<>());
                            return d;
                        } catch (SQLException e) {
                            throw new RuntimeException("Error creating delivery", e);
                        }
                    });

                    if (rs.getString("item_type") != null) {
                        delivery.getItems().add(new DeliveryItem(
                                rs.getString("item_type"),
                                rs.getInt("material_id"),
                                rs.getInt("quantity")
                        ));
                    }
                } catch (SQLException e) {
                    throw new SQLException("Error processing delivery row", e);
                }
            }
            deliveries.addAll(deliveryMap.values());
        }
        return deliveries;
    }

    private List<DeliveryItem> getDeliveryItems(int deliveryId) throws SQLException {
        String sql = "SELECT di.*, " +
                "CASE WHEN di.item_type = 'wood' THEN w.name ELSE c.material END AS material_name " +
                "FROM delivery_items di " +
                "LEFT JOIN wood_types w ON di.item_type = 'wood' AND di.material_id = w.wood_id " +
                "LEFT JOIN cores c ON di.item_type = 'core' AND di.material_id = c.core_id " +
                "WHERE di.delivery_id = ?";

        List<DeliveryItem> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, deliveryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new DeliveryItem(
                            rs.getString("item_type"),
                            rs.getInt("material_id"),
                            rs.getInt("quantity")
                    ));
                }
            }
        }
        return items;
    }

    public Delivery getDeliveryById(int deliveryId) throws SQLException {
        String sql = "SELECT d.* FROM inventory_deliveries d WHERE d.delivery_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, deliveryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Delivery delivery = new Delivery();
                    delivery.setDeliveryId(rs.getInt("delivery_id"));
                    delivery.setDeliveryDate(rs.getString("delivery_date"));
                    delivery.setSupplierName(rs.getString("supplier_name"));
                    delivery.setReceivedBy(rs.getString("received_by"));
                    delivery.setNotes(rs.getString("notes"));
                    delivery.setItems(getDeliveryItems(deliveryId));
                    return delivery;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            throw e;
        }
        return null;
    }
}
