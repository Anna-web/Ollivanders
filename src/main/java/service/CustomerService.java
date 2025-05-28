package service;

import db.DatabaseConnection;
import model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    public boolean createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (first_name, last_name, birth_date, blood_status, " +
                "house, species, wand_license, notes, registration_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getBirthDate());
            stmt.setString(4, customer.getBloodStatus());
            stmt.setString(5, customer.getHouse());
            stmt.setString(6, customer.getSpecies());
            stmt.setString(7, customer.getWandLicense());
            stmt.setString(8, customer.getNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setCustomerId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public List<Customer> findCustomersByName(String name) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
            stmt.setString(2, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = extractCustomerFromResultSet(rs);
                    customers.add(customer);
                }
            }
        }
        return customers;
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, birth_date = ?, " +
                "blood_status = ?, house = ?, species = ?, wand_license = ?, notes = ?, registration_date = ? " +
                "WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getBirthDate());
            stmt.setString(4, customer.getBloodStatus());
            stmt.setString(5, customer.getHouse());
            stmt.setString(6, customer.getSpecies());
            stmt.setString(7, customer.getWandLicense());
            stmt.setString(8, customer.getNotes());
            stmt.setDate(9, new java.sql.Date(System.currentTimeMillis()));
            stmt.setInt(10, customer.getCustomerId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY last_name, first_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        }
        return customers;
    }

    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("birth_date"),
                rs.getString("blood_status"),
                rs.getString("house"),
                rs.getString("species"),
                rs.getString("wand_license"),
                rs.getString("notes")
        );
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setRegistrationDate(rs.getString("registration_date"));
        return customer;
    }

    public boolean validateWandLicense(String license) throws SQLException {
        if (license == null || license.trim().isEmpty()) {
            return true; // No license is valid (for squibs/muggles)
        }

        String sql = "SELECT COUNT(*) FROM customers WHERE wand_license = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, license.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }
}