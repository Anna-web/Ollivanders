package service;

import db.DatabaseConnection;
import model.Purchase;
import model.Wand;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportingService {

    // Method to get all wand names
    public static List<String> getAllWandNames() throws SQLException {
        List<String> wandNames = new ArrayList<>();
        String query = "SELECT wand_id FROM wands"; // Adjust the table name and column as necessary

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                wandNames.add(rs.getString("wand_id"));
            }
        }
        return wandNames;
    }

    public static List<Integer> getAllCustomerIds() throws SQLException {
        List<Integer> customerIds = new ArrayList<>();
        String sql = "SELECT customer_id FROM customers"; // Adjust the query if needed

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customerIds.add(rs.getInt("customer_id"));
            }
        }
        return customerIds;
    }

    public static List<Integer> getAllWandIds() throws SQLException {
        List<Integer> wandIds = new ArrayList<>();
        String sql = "SELECT wand_id FROM wands"; // Adjust the query if needed

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                wandIds.add(rs.getInt("wand_id"));
            }
        }
        return wandIds;
    }

    // Method to get wand ID by name
    public static int getWandIdByName(int wand_id) throws SQLException {
        String query = "SELECT id FROM wands WHERE wand_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)){
            preparedStatement.setInt(1, wand_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return -1; // Return -1 if not found
    }

    public static List<Map<String, Object>> getAllSales() throws SQLException {
        List<Map<String, Object>> sales = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.sale_date, s.sale_price, s.payment_method, " +
                "c.first_name || ' ' || c.last_name AS customer_name, " +
                "wt.name AS wood_type, co.material AS core_material, " +
                "w.length, w.flexibility " +
                "FROM sales s " +
                "JOIN wands w ON s.wand_id = w.wand_id " +
                "JOIN customers c ON s.customer_id = c.customer_id " +
                "JOIN wood_types wt ON w.wood_id = wt.wood_id " +
                "JOIN cores co ON w.core_id = co.core_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> saleMap = new HashMap<>();
                saleMap.put("sale_id", rs.getInt("sale_id"));
                saleMap.put("sale_date", rs.getString("sale_date"));
                saleMap.put("sale_price", rs.getDouble("sale_price"));
                saleMap.put("payment_method", rs.getString("payment_method"));
                saleMap.put("customer_name", rs.getString("customer_name"));
                saleMap.put("wood_type", rs.getString("wood_type"));
                saleMap.put("core_material", rs.getString("core_material"));
                saleMap.put("length", rs.getDouble("length"));
                saleMap.put("flexibility", rs.getString("flexibility"));
                sales.add(saleMap);
            }
        }
        return sales;
    }

    private static Purchase extractPurchaseFromResultSet(ResultSet rs) throws SQLException {
        Purchase purchase = new Purchase();
        purchase.setPurchaseId(rs.getInt("purchaseId"));
        purchase.setWandId(rs.getInt("wandId"));
        purchase.setCustomerId(rs.getInt("customerId"));
        purchase.setSaleDate(rs.getString("saleDate"));
        purchase.setSalePrice(rs.getDouble("salePrice"));
        purchase.setPaymentMethod(rs.getString("paymentMethod"));
        return purchase;
    }


    // Method to get customer ID by name
    public static int getCustomerIdByName(String customerFirstName, String customerLastName) throws SQLException {
        String query = "SELECT id FROM customers WHERE first_name = ? AND last_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)){
            preparedStatement.setString(1, customerFirstName);
            preparedStatement.setString(2, customerLastName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return -1; // Return -1 if not found
    }

    // Method to create a new purchase
    public static boolean createPurchase(Purchase purchase) throws SQLException {
        String query = "INSERT INTO sales (wand_id, customer_id, sale_date, sale_price, payment_method) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)){
            preparedStatement.setInt(1, purchase.getWandId());
            preparedStatement.setInt(2, purchase.getCustomerId());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = dateFormat.format(new java.util.Date());
            preparedStatement.setString(3, currentDate);
            preparedStatement.setDouble(4, purchase.getSalePrice());
            String paymentMethod = purchase.getPaymentMethod();
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                paymentMethod = Character.toLowerCase(paymentMethod.charAt(0)) + paymentMethod.substring(1);
            }
            preparedStatement.setString(5, paymentMethod);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the purchase was created successfully
        }
    }
}

