package gui;

import db.DatabaseCleaner;
import db.DatabaseInitializer;
import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUI extends JFrame {
    // Color scheme
    private final Color PRIMARY_COLOR = new Color(53, 101, 77);  // Dark green
    private final Color SECONDARY_COLOR = new Color(218, 165, 32);  // Gold
    private final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private final Color TABLE_HEADER_COLOR = new Color(70, 130, 180);  // Steel blue
    private final Color TABLE_ROW_COLOR = new Color(245, 245, 245);
    private final Color TABLE_ALT_ROW_COLOR = new Color(230, 230, 230);

    // Services
    private final WandService wandService = new WandService();
    private final CustomerService customerService = new CustomerService();
    private final InventoryService inventoryService = new InventoryService();
    private final DeliveryService deliveryService = new DeliveryService();

    public GUI() {
        initializeUI();
    }

    private void initializeUI() {
        // Main Window Setup
        setTitle("Ollivanders Wand Shop Inventory System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Main Menu Panel
        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Create buttons with consistent styling
        JButton wandsButton = createMenuButton("Wand Inventory", () -> showWandInventory());
        JButton customersButton = createMenuButton("Customer Registry", () -> showCustomerRegistry());
        JButton deliveriesButton = createMenuButton("Components & Deliveries", () -> showDeliveriesInventory());
        JButton salesButton = createMenuButton("Sales Tracking", () -> showSales());

        // Add all buttons
        mainPanel.add(wandsButton);
        mainPanel.add(customersButton);
        mainPanel.add(deliveriesButton);
        mainPanel.add(salesButton);

        // Add reset button
        JButton resetButton = new JButton("Reset All Data");
        styleButton(resetButton, PRIMARY_COLOR, Color.WHITE, 16, true);
        resetButton.setForeground(Color.RED);
        resetButton.addActionListener(e -> resetDatabase());

        add(mainPanel, BorderLayout.CENTER);
        add(resetButton, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String text, Runnable action) {
        JButton button = new JButton(text);
        styleButton(button, PRIMARY_COLOR, Color.WHITE, 18, true);
        button.addActionListener(e -> action.run());  // Wrap the Runnable
        return button;
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor, int fontSize, boolean bold) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, fontSize));
        button.setPreferredSize(new Dimension(300, 80));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
    }

    private void styleButton(JButton button) {
        styleButton(button, PRIMARY_COLOR, Color.WHITE, 18, true);
    }

    private JTable createStyledTable(Object[][] data, String[] columns) {
        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(Color.LIGHT_GRAY);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Alternate row coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW_COLOR : TABLE_ALT_ROW_COLOR);
                }
                return c;
            }
        });

        return table;
    }

    private void showWandInventory() {
        JFrame frame = new JFrame("Wand Inventory");
        frame.setSize(1200, 700);
        centerWindow(frame);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField(20);
        styleTextField(searchField);

        JLabel searchLabel = createStyledLabel("Search:");

        JButton searchButton = createMenuButton("Search", () -> {
            try {
                refreshWandTable(frame, searchField.getText());
            } catch (SQLException ex) {
                showError("Search failed: " + ex.getMessage());
            }
        });
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Main table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);

        frame.add(searchPanel, BorderLayout.NORTH);
        frame.add(tablePanel, BorderLayout.CENTER);

        try {
            refreshWandTable(frame, "");
        } catch (SQLException e) {
            showError("Failed to load wands: " + e.getMessage());
        }

        frame.setVisible(true);
    }

    private void refreshWandTable(JFrame parentFrame, String searchQuery) throws SQLException {
        JPanel tablePanel = (JPanel) parentFrame.getContentPane().getComponent(1);
        tablePanel.removeAll();

        List<Wand> wands = wandService.searchWands(searchQuery);

        String[] columns = {"ID", "Wood", "Core", "Length", "Flexibility", "Condition", "Price", "Status"};
        Object[][] data = new Object[wands.size()][columns.length];

        for (int i = 0; i < wands.size(); i++) {
            Wand w = wands.get(i);
            data[i] = new Object[]{
                    w.getId(),
                    wandService.getWoodName(w.getWoodId()),
                    wandService.getCoreMaterial(w.getCoreId()),
                    w.getLength(),
                    w.getFlexibility(),
                    w.getCondition(),
                    String.format("%,.2f", w.getPrice()),
                    w.getStatus()
            };
        }

        JTable table = createStyledTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton editButton = createMenuButton("Edit", () -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int wandId = (int) table.getValueAt(selectedRow, 0);
                try {
                    showEditWandDialog(parentFrame, wandId);
                } catch (SQLException ex) {
                    showError("Failed to edit wand: " + ex.getMessage());
                }
            } else {
                showError("Please select a wand first");
            }
        });

        editButton.setPreferredSize(new Dimension(180, 40));

        JButton deleteButton = createMenuButton("Delete", () -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int wandId = (int) table.getValueAt(selectedRow, 0);
                confirmAndDeleteWand(parentFrame, wandId);
            } else {
                showError("Please select a wand first");
            }
        });

        deleteButton.setPreferredSize(new Dimension(180, 40));

        JButton addButton = createMenuButton("Add New", () -> {
            showAddWandDialog(parentFrame);
            parentFrame.dispose();
            showWandInventory();
        });

        addButton.setPreferredSize(new Dimension(180, 40));

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(addButton);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        parentFrame.revalidate();
        parentFrame.repaint();
    }

    private void showAddWandDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Add New Wand", true);
        dialog.setSize(500, 650);
        centerWindow(dialog);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(BACKGROUND_COLOR);

        try {
            // Wood selection
            formPanel.add(createStyledLabel("Wood Type:"));
            JComboBox<String> woodCombo = new JComboBox<>(wandService.getAllWoodNames().toArray(new String[0]));
            styleComboBox(woodCombo);
            formPanel.add(woodCombo);

            // Core selection
            formPanel.add(createStyledLabel("Core Material:"));
            JComboBox<String> coreCombo = new JComboBox<>(wandService.getAllCoreMaterials().toArray(new String[0]));
            styleComboBox(coreCombo);
            formPanel.add(coreCombo);

            // Other fields
            formPanel.add(createStyledLabel("Length (inches):"));
            JTextField lengthField = new JTextField();
            styleTextField(lengthField);
            formPanel.add(lengthField);

            formPanel.add(createStyledLabel("Flexibility:"));
            JComboBox<String> flexibilityCombo = new JComboBox<>(
                    new String[]{"rigid", "unyielding", "solid", "stiff", "flexible", "whippy", "supple"});
            styleComboBox(flexibilityCombo);
            formPanel.add(flexibilityCombo);

            formPanel.add(createStyledLabel("Condition:"));
            JComboBox<String> conditionCombo = new JComboBox<>(
                    new String[]{"new", "used", "refurbished", "damaged"});
            styleComboBox(conditionCombo);
            formPanel.add(conditionCombo);

            formPanel.add(createStyledLabel("Special Features:"));
            JTextField featuresField = new JTextField();
            styleTextField(featuresField);
            formPanel.add(featuresField);

            formPanel.add(createStyledLabel("Notes:"));
            JTextField notesField = new JTextField();
            styleTextField(notesField);
            formPanel.add(notesField);

            formPanel.add(createStyledLabel("Price:"));
            JTextField priceField = new JTextField();
            styleTextField(priceField);
            formPanel.add(priceField);

            formPanel.add(createStyledLabel("Status:"));
            JComboBox<String> statusCombo = new JComboBox<>(
                    new String[]{"in_stock", "sold", "reserved", "defective"});
            styleComboBox(statusCombo);
            formPanel.add(statusCombo);

            JButton saveButton = createMenuButton("Save Wand", () -> {
                try {
                    String woodName = (String) woodCombo.getSelectedItem();
                    String coreMaterial = (String) coreCombo.getSelectedItem();

                    Wand newWand = new Wand(
                            wandService.getWoodIdByName(woodName),
                            wandService.getCoreIdByMaterial(coreMaterial),
                            Double.parseDouble(lengthField.getText()),
                            (String) flexibilityCombo.getSelectedItem(),
                            (String) conditionCombo.getSelectedItem(),
                            featuresField.getText(),
                            Double.parseDouble(priceField.getText()),
                            (String) statusCombo.getSelectedItem(),
                            notesField.getText()
                    );

                    if (wandService.createWand(newWand)) {
                        JOptionPane.showMessageDialog(dialog, "Wand added successfully!");
                        dialog.dispose();
                    }
                } catch (Exception ex) {
                    showError("Invalid input: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            saveButton.setPreferredSize(new Dimension(150, 40));

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(saveButton, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (SQLException e) {
            showError("Failed to load wood/core data: " + e.getMessage());
        }
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        area.setBackground(Color.WHITE);
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }


    private void styleComboBoxInt(JComboBox<Integer> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }


    private void showCustomerRegistry() {
        JFrame frame = new JFrame("Customer Registry");
        frame.setSize(1200, 700); // Wider for additional columns
        centerWindow(frame);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Search Panel - now with proper styling
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search components with consistent styling
        JLabel searchLabel = createStyledLabel("Search:");
        JTextField searchField = new JTextField(20);
        styleTextField(searchField);

        JButton searchButton = createMenuButton("Search", () -> {
            try {
                refreshCustomerTable(frame, searchField.getText());
            } catch (SQLException ex) {
                showError("Search failed: " + ex.getMessage());
            }
        });
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Main table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);

        frame.add(searchPanel, BorderLayout.NORTH);
        frame.add(tablePanel, BorderLayout.CENTER);

        try {
            refreshCustomerTable(frame, "");
        } catch (SQLException e) {
            showError("Failed to load customers: " + e.getMessage());
        }

        frame.setVisible(true);
    }


    private void refreshCustomerTable(JFrame parentFrame, String searchQuery) throws SQLException {
        JPanel tablePanel = (JPanel) parentFrame.getContentPane().getComponent(1);
        tablePanel.removeAll();

        List<Customer> customers = customerService.findCustomersByName(searchQuery);

        // All columns including birth date and registration date
        String[] columns = {"ID", "First Name", "Last Name", "Birth Date", "Blood Status",
                "House", "Species", "Wand License", "Registration Date"};

        Object[][] data = new Object[customers.size()][columns.length];

        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            data[i] = new Object[]{
                    c.getCustomerId(),
                    c.getFirstName(),
                    c.getLastName(),
                    c.getBirthDate(),
                    c.getBloodStatus(),
                    c.getHouse(),
                    c.getSpecies(),
                    c.getWandLicense(),
                    c.getRegistrationDate()
            };
        }

        JTable table = createStyledTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Button panel with both edit and add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton editButton = createMenuButton("Edit", () -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int customerId = (int) table.getValueAt(selectedRow, 0);
                try {
                    showEditCustomerDialog(parentFrame, customerId);
                } catch (SQLException ex) {
                    showError("Failed to edit customer: " + ex.getMessage());
                }
            } else {
                showError("Please select a customer first");
            }
        });
        editButton.setPreferredSize(new Dimension(150, 40));

        JButton addButton = createMenuButton("Add New", () -> {
            showAddCustomerDialog(parentFrame);
            parentFrame.dispose();
        });
        addButton.setPreferredSize(new Dimension(180, 40));

        buttonPanel.add(editButton);
        buttonPanel.add(addButton);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        parentFrame.revalidate();
        parentFrame.repaint();
    }


    private void showAddCustomerDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Add New Customer", true);
        dialog.setSize(500, 600);
        centerWindow(dialog);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(BACKGROUND_COLOR);

        // First Name
        formPanel.add(createStyledLabel("First Name:"));
        JTextField firstNameField = new JTextField();
        styleTextField(firstNameField);
        formPanel.add(firstNameField);

        // Last Name
        formPanel.add(createStyledLabel("Last Name:"));
        JTextField lastNameField = new JTextField();
        styleTextField(lastNameField);
        formPanel.add(lastNameField);

        // Birth Date
        formPanel.add(createStyledLabel("Birth Date (YYYY-MM-DD):"));
        JTextField birthDateField = new JTextField();
        styleTextField(birthDateField);
        formPanel.add(birthDateField);

        // Blood Status
        formPanel.add(createStyledLabel("Blood Status:"));
        JComboBox<String> bloodStatusCombo = new JComboBox<>(
                new String[]{"pure", "half", "muggle", "unknown"});
        styleComboBox(bloodStatusCombo);
        formPanel.add(bloodStatusCombo);

        // House
        formPanel.add(createStyledLabel("House:"));
        JComboBox<String> houseCombo = new JComboBox<>(
                new String[]{"Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin", "Other"});
        styleComboBox(houseCombo);
        formPanel.add(houseCombo);

        // Species
        formPanel.add(createStyledLabel("Species:"));
        JTextField speciesField = new JTextField("human"); // Default value
        styleTextField(speciesField);
        formPanel.add(speciesField);

        // Wand License
        formPanel.add(createStyledLabel("Wand License:"));
        JTextField licenseField = new JTextField();
        styleTextField(licenseField);
        formPanel.add(licenseField);

        // Notes
        formPanel.add(createStyledLabel("Notes:"));
        JTextArea notesArea = new JTextArea();
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        styleTextArea(notesArea);
        formPanel.add(notesScroll);

        // Save Button
        JButton saveButton = createMenuButton("Save Customer", () -> {
            try {
                Customer newCustomer = new Customer(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        birthDateField.getText(),
                        (String) bloodStatusCombo.getSelectedItem(),
                        (String) houseCombo.getSelectedItem(),
                        speciesField.getText(),
                        licenseField.getText(),
                        notesArea.getText()
                );

                if (customerService.createCustomer(newCustomer)) {
                    JOptionPane.showMessageDialog(dialog, "Customer added successfully!");
                    dialog.dispose();
                    parent.dispose();
                    // Refresh the customer list
                    showCustomerRegistry();
                }
            } catch (Exception ex) {
                showError("Invalid input: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        saveButton.setPreferredSize(new Dimension(150, 40));

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditCustomerDialog(JFrame parent, int customerId) throws SQLException {
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            showError("Customer not found");
            return;
        }

        JDialog dialog = new JDialog(parent, "Edit Customer", true);
        dialog.setSize(500, 600);
        centerWindow(dialog);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(BACKGROUND_COLOR);

        // First Name
        formPanel.add(createStyledLabel("First Name:"));
        JTextField firstNameField = new JTextField(customer.getFirstName());
        styleTextField(firstNameField);
        formPanel.add(firstNameField);

        // Last Name
        formPanel.add(createStyledLabel("Last Name:"));
        JTextField lastNameField = new JTextField(customer.getLastName());
        styleTextField(lastNameField);
        formPanel.add(lastNameField);

        // Birth Date
        formPanel.add(createStyledLabel("Birth Date:"));
        JTextField birthDateField = new JTextField(customer.getBirthDate());
        styleTextField(birthDateField);
        formPanel.add(birthDateField);

        // Blood Status
        formPanel.add(createStyledLabel("Blood Status:"));
        JComboBox<String> bloodStatusCombo = new JComboBox<>(
                new String[]{"pure", "half", "muggle", "unknown"});
        bloodStatusCombo.setSelectedItem(customer.getBloodStatus());
        styleComboBox(bloodStatusCombo);
        formPanel.add(bloodStatusCombo);

        // House
        formPanel.add(createStyledLabel("House:"));
        JComboBox<String> houseCombo = new JComboBox<>(
                new String[]{"Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin", "Other"});
        houseCombo.setSelectedItem(customer.getHouse());
        styleComboBox(houseCombo);
        formPanel.add(houseCombo);

        // Species
        formPanel.add(createStyledLabel("Species:"));
        JTextField speciesField = new JTextField(customer.getSpecies());
        styleTextField(speciesField);
        formPanel.add(speciesField);

        // Wand License
        formPanel.add(createStyledLabel("Wand License:"));
        JTextField licenseField = new JTextField(customer.getWandLicense());
        styleTextField(licenseField);
        formPanel.add(licenseField);

        // Notes
        formPanel.add(createStyledLabel("Notes:"));
        JTextArea notesArea = new JTextArea(customer.getNotes());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        styleTextArea(notesArea);
        formPanel.add(notesScroll);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = createMenuButton("Save Changes", () -> {
            try {
                Customer updatedCustomer = new Customer(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        birthDateField.getText(),
                        (String) bloodStatusCombo.getSelectedItem(),
                        (String) houseCombo.getSelectedItem(),
                        speciesField.getText(),
                        licenseField.getText(),
                        notesArea.getText()
                );
                updatedCustomer.setCustomerId(customerId);

                if (customerService.updateCustomer(updatedCustomer)) {
                    JOptionPane.showMessageDialog(dialog, "Customer updated successfully!");
                    dialog.dispose();
                    refreshCustomerTable(parent, "");
                }
            } catch (Exception ex) {
                showError("Error updating customer: " + ex.getMessage());
            }
        });

        JButton deleteButton = createMenuButton("Delete Customer", () -> {
            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "Are you sure you want to delete this customer?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (customerService.deleteCustomer(customerId)) {
                        JOptionPane.showMessageDialog(dialog, "Customer deleted successfully!");
                        dialog.dispose();
                        refreshCustomerTable(parent, "");
                    }
                } catch (SQLException ex) {
                    showError("Error deleting customer: " + ex.getMessage());
                }
            }
        });

        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showSales() {
        try {
            List<Map<String, Object>> sales = ReportingService.getAllSales();

            JFrame frame = new JFrame("Sales Records");
            frame.setSize(1200, 700);
            centerWindow(frame);
            frame.getContentPane().setBackground(BACKGROUND_COLOR);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Search Panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            searchPanel.setBackground(BACKGROUND_COLOR);
            searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField searchField = new JTextField(20);
            styleTextField(searchField);

            // Main table panel
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBackground(BACKGROUND_COLOR);

            // Column names
            String[] columnNames = {
                    "Sale ID", "Date", "Price", "Payment Method", "Customer",
                    "Wood Type", "Core Material", "Length", "Flexibility"
            };

            // Create table data
            Object[][] data = new Object[sales.size()][columnNames.length];
            for (int i = 0; i < sales.size(); i++) {
                Map<String, Object> sale = sales.get(i);
                data[i][0] = sale.get("sale_id");
                data[i][1] = sale.get("sale_date");
                data[i][2] = String.format("%.2f", sale.get("sale_price"));
                data[i][3] = sale.get("payment_method");
                data[i][4] = sale.get("customer_name");
                data[i][5] = sale.get("wood_type");
                data[i][6] = sale.get("core_material");
                data[i][7] = sale.get("length");
                data[i][8] = sale.get("flexibility");
            }

            JTable table = new JTable(data, columnNames);
            table.setAutoCreateRowSorter(true);
            table.setFillsViewportHeight(true);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            // Format price column to right-align
            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
            table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

            // Add scroll pane
            JScrollPane scrollPane = new JScrollPane(table);
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            frame.add(searchPanel, BorderLayout.NORTH);
            frame.add(tablePanel, BorderLayout.CENTER);

            // Add button to add new sale
            JButton addSaleButton = createMenuButton("Add Sale", () -> showAddSaleDialog(frame));
            addSaleButton.setPreferredSize(new Dimension(150, 40));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            buttonPanel.add(addSaleButton);

            // Add refresh button
            JButton refreshButton = createMenuButton("Refresh", () -> {
                frame.dispose();
                showSales();
            });
            refreshButton.setPreferredSize(new Dimension(180, 50));
            buttonPanel.add(refreshButton);

            frame.add(buttonPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error loading sales data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void showAddSaleDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Add New Purchase", true);
        dialog.setSize(500, 600);
        centerWindow(dialog);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(BACKGROUND_COLOR);

        try {
            // Wand selection
            formPanel.add(createStyledLabel("Wand:"));
            List<Integer> wandIds = ReportingService.getAllWandIds(); // Ensure this returns List<Integer>
            JComboBox<Integer> wandCombo = new JComboBox<>(wandIds.toArray(new Integer[0])); // Convert List<Integer> to Integer[]
            styleComboBoxInt(wandCombo); // Calls the overloaded method for Integer
            wandCombo.setPreferredSize(new Dimension(150, 30)); // Set preferred size for the JComboBox
            formPanel.add(wandCombo);

            // Customer selection
            formPanel.add(createStyledLabel("Customer:"));
            List<Integer> customerIds = ReportingService.getAllCustomerIds(); // Ensure this returns List<Integer>
            JComboBox<Integer> customerCombo = new JComboBox<>(customerIds.toArray(new Integer[0])); // Convert List<Integer> to Integer[]
            styleComboBoxInt(customerCombo); // Calls the overloaded method for Integer
            customerCombo.setPreferredSize(new Dimension(150, 30)); // Set preferred size for the JComboBox
            formPanel.add(customerCombo);

            // Sale Price
            formPanel.add(createStyledLabel("Sale Price (galleons):"));
            JTextField priceField = new JTextField();
            styleTextField(priceField);
            priceField.setPreferredSize(new Dimension(150, 30)); // Set preferred size for the JTextField
            formPanel.add(priceField);

            // Payment Method
            formPanel.add(createStyledLabel("Payment Method:"));
            JComboBox<String> paymentMethodCombo = new JComboBox<>(
                    new String[]{"Galleons", "Gringotts", "Credit"});
            styleComboBox(paymentMethodCombo);
            paymentMethodCombo.setPreferredSize(new Dimension(150, 30)); // Set preferred size for the JComboBox
            formPanel.add(paymentMethodCombo);

            JButton saveButton = createMenuButton("Save Purchase", () -> {
                try {
                    // Get the selected wand ID directly
                    int wandId = (Integer) wandCombo.getSelectedItem();
                    // Get the selected customer ID directly
                    int customerId = (Integer) customerCombo.getSelectedItem();
                    double salePrice = Double.parseDouble(priceField.getText());
                    String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

                    // Create a new Purchase object
                    Purchase newPurchase = new Purchase(wandId, customerId, salePrice);
                    newPurchase.setPaymentMethod(paymentMethod);

                    // Call the service to save the new purchase
                    if (ReportingService.createPurchase(newPurchase)) {
                        JOptionPane.showMessageDialog(dialog, "Purchase added successfully!");
                        dialog.dispose();
                    }
                } catch (Exception ex) {
                    showError("Invalid input: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            saveButton.setPreferredSize(new Dimension(150, 40));

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(saveButton, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (SQLException e) {
            showError("Failed to load wand/customer data: " + e.getMessage());
        }

    }



    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "This will DELETE ALL DATA. Continue?",
                "Warning",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Reset the database
                DatabaseCleaner.resetDatabase(true);
                // Initialize the database schema and data
                DatabaseInitializer.initializeDatabase();
                JOptionPane.showMessageDialog(this, "Database reset successfully");
            } catch (Exception e) {
                showError("Reset failed: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
    }

    private void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void confirmAndDeleteWand(JFrame parentFrame, int wandId) {
        int confirm = JOptionPane.showConfirmDialog(
                parentFrame,
                "Are you sure you want to delete this wand?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (wandService.deleteWand(wandId)) {
                    JOptionPane.showMessageDialog(parentFrame, "Wand deleted successfully!");
                    refreshWandTable(parentFrame, "");
                }
            } catch (SQLException ex) {
                showError("Error deleting wand: " + ex.getMessage());
            }
        }
    }

    private void showEditWandDialog(JFrame parent, int wandId) throws SQLException {
        Wand wand = wandService.getWandById(wandId);
        if (wand == null) {
            showError("Wand not found");
            return;
        }

        JDialog dialog = new JDialog(parent, "Edit Wand", true);
        dialog.setSize(500, 700); // Slightly taller for better spacing
        centerWindow(dialog);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(BACKGROUND_COLOR);

        try {
            // Wood selection (pre-selected)
            formPanel.add(createStyledLabel("Wood Type:"));
            List<String> woodNames = wandService.getAllWoodNames();
            JComboBox<String> woodCombo = new JComboBox<>(woodNames.toArray(new String[0]));
            woodCombo.setSelectedItem(wandService.getWoodName(wand.getWoodId()));
            styleComboBox(woodCombo);
            formPanel.add(woodCombo);

            // Core selection (pre-selected)
            formPanel.add(createStyledLabel("Core Material:"));
            List<String> coreMaterials = wandService.getAllCoreMaterials();
            JComboBox<String> coreCombo = new JComboBox<>(coreMaterials.toArray(new String[0]));
            coreCombo.setSelectedItem(wandService.getCoreMaterial(wand.getCoreId()));
            styleComboBox(coreCombo);
            formPanel.add(coreCombo);

            // Length
            formPanel.add(createStyledLabel("Length (inches):"));
            JTextField lengthField = new JTextField(String.valueOf(wand.getLength()));
            styleTextField(lengthField);
            formPanel.add(lengthField);

            // Flexibility (pre-selected)
            formPanel.add(createStyledLabel("Flexibility:"));
            JComboBox<String> flexibilityCombo = new JComboBox<>(
                    new String[]{"rigid", "unyielding", "solid", "stiff", "flexible", "whippy", "supple"});
            flexibilityCombo.setSelectedItem(wand.getFlexibility());
            styleComboBox(flexibilityCombo);
            formPanel.add(flexibilityCombo);

            // Condition (pre-selected)
            formPanel.add(createStyledLabel("Condition:"));
            JComboBox<String> conditionCombo = new JComboBox<>(
                    new String[]{"new", "used", "refurbished", "damaged"});
            conditionCombo.setSelectedItem(wand.getCondition());
            styleComboBox(conditionCombo);
            formPanel.add(conditionCombo);

            // Special Features
            formPanel.add(createStyledLabel("Special Features:"));
            JTextField featuresField = new JTextField(wand.getSpecialFeatures());
            styleTextField(featuresField);
            formPanel.add(featuresField);

            // Notes
            formPanel.add(createStyledLabel("Notes:"));
            JTextArea notesArea = new JTextArea(wand.getNotes());
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(notesArea);
            styleTextArea(notesArea);
            formPanel.add(notesScroll);

            // Price
            formPanel.add(createStyledLabel("Price:"));
            JTextField priceField = new JTextField(String.format("%.2f", wand.getPrice()));
            styleTextField(priceField);
            formPanel.add(priceField);

            // Status (pre-selected)
            formPanel.add(createStyledLabel("Status:"));
            JComboBox<String> statusCombo = new JComboBox<>(
                    new String[]{"in_stock", "sold", "reserved", "defective"});
            statusCombo.setSelectedItem(wand.getStatus());
            styleComboBox(statusCombo);
            formPanel.add(statusCombo);

            // Button Panel
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            buttonPanel.setBackground(BACKGROUND_COLOR);

            // Delete Button
            JButton deleteButton = createMenuButton("Delete Wand", () -> {
                int confirm = JOptionPane.showConfirmDialog(
                        dialog,
                        "Are you sure you want to delete this wand?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (wandService.deleteWand(wandId)) {
                            JOptionPane.showMessageDialog(dialog, "Wand deleted successfully!");
                            dialog.dispose();
                            parent.dispose(); // Close the list window
                            showWandInventory(); // Refresh the list
                        }
                    } catch (SQLException ex) {
                        showError("Error deleting wand: " + ex.getMessage());
                    }
                }
            });
            deleteButton.setBackground(new Color(220, 80, 80)); // Red color for delete

            // Save Button
            JButton saveButton = createMenuButton("Save Changes", () -> {
                try {
                    Wand updatedWand = new Wand(
                            wandService.getWoodIdByName((String)woodCombo.getSelectedItem()),
                            wandService.getCoreIdByMaterial((String)coreCombo.getSelectedItem()),
                            Double.parseDouble(lengthField.getText()),
                            (String) flexibilityCombo.getSelectedItem(),
                            (String) conditionCombo.getSelectedItem(),
                            featuresField.getText(),
                            Double.parseDouble(priceField.getText()),
                            (String) statusCombo.getSelectedItem(),
                            notesArea.getText()
                    );
                    updatedWand.setId(wandId);

                    if (wandService.updateWand(updatedWand)) {
                        JOptionPane.showMessageDialog(dialog, "Wand updated successfully!");
                        dialog.dispose();
                        parent.dispose(); // Close the list window
                        showWandInventory(); // Refresh the list
                    }
                } catch (Exception ex) {
                    showError("Error updating wand: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            buttonPanel.add(deleteButton);
            buttonPanel.add(saveButton);

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (SQLException e) {
            showError("Failed to load wood/core data: " + e.getMessage());
        }
    }


    private void showDeliveriesInventory() {
        JFrame frame = new JFrame("Components & Deliveries");
        frame.setSize(1200, 700);
        centerWindow(frame);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create tabbed pane for Inventory and Deliveries
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Inventory Panel
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBackground(BACKGROUND_COLOR);
        setupInventoryTable(inventoryPanel);
        tabbedPane.addTab("Current Inventory", inventoryPanel);

        // Deliveries Panel
        JPanel deliveriesPanel = new JPanel(new BorderLayout());
        deliveriesPanel.setBackground(BACKGROUND_COLOR);
        setupDeliveriesTable(deliveriesPanel);
        tabbedPane.addTab("Delivery History", deliveriesPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Add button for new delivery
        JButton newDeliveryButton = createMenuButton("New Delivery", () -> showNewDeliveryDialog(frame));
        newDeliveryButton.setPreferredSize(new Dimension(180, 50));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(newDeliveryButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void setupInventoryTable(JPanel parentPanel) {
        try {
            List<InventoryItem> inventory = inventoryService.getFullInventory();

            String[] columns = {"ID", "Type", "Material", "Quantity", "Last Updated"};
            Object[][] data = new Object[inventory.size()][columns.length];

            for (int i = 0; i < inventory.size(); i++) {
                InventoryItem item = inventory.get(i);
                data[i] = new Object[]{
                        item.getItemId(),
                        item.getItemType(),
                        item.getMaterialName(),
                        item.getQuantity(),
                        item.getLastUpdated()
                };
            }

            JTable table = createStyledTable(data, columns);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            parentPanel.add(scrollPane, BorderLayout.CENTER);

            // Add refresh button
            JButton refreshButton = createMenuButton("Refresh", () -> {
                setupInventoryTable(parentPanel);
                parentPanel.revalidate();
                parentPanel.repaint();
            });
            refreshButton.setPreferredSize(new Dimension(120, 40));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            buttonPanel.add(refreshButton);

            parentPanel.add(buttonPanel, BorderLayout.SOUTH);

        } catch (SQLException e) {
            showError("Failed to load inventory: " + e.getMessage());
        }
    }

    private void setupDeliveriesTable(JPanel parentPanel) {
        try {
            List<Delivery> deliveries = deliveryService.getDeliveryHistory();

            String[] columns = {"Delivery ID", "Date", "Supplier", "Received By", "Items Count"};
            Object[][] data = new Object[deliveries.size()][columns.length];

            for (int i = 0; i < deliveries.size(); i++) {
                Delivery delivery = deliveries.get(i);
                data[i] = new Object[]{
                        delivery.getDeliveryId(),
                        delivery.getDeliveryDate(),
                        delivery.getSupplierName(),
                        delivery.getReceivedBy(),
                        delivery.getItems().size()
                };
            }

            JTable table = createStyledTable(data, columns);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            // Add double-click to view details
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        int row = table.rowAtPoint(evt.getPoint());
                        int deliveryId = (int) table.getValueAt(row, 0);
                        showDeliveryDetails(deliveryId);
                    }
                }
            });

            parentPanel.add(scrollPane, BorderLayout.CENTER);

        } catch (SQLException e) {
            showError("Failed to load deliveries: " + e.getMessage());
        }
    }

    private void showNewDeliveryDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "New Delivery", true);
        dialog.setSize(800, 600);
        centerWindow(dialog);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));

        // Main form panel
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(BACKGROUND_COLOR);

        // Delivery info panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(BACKGROUND_COLOR);

        // Received by
        infoPanel.add(createStyledLabel("Received By:"));
        JTextField receivedByField = new JTextField();
        styleTextField(receivedByField);
        infoPanel.add(receivedByField);

        // Notes
        infoPanel.add(createStyledLabel("Notes:"));
        JTextArea notesArea = new JTextArea();
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        styleTextArea(notesArea);
        infoPanel.add(notesScroll);

        formPanel.add(infoPanel, BorderLayout.NORTH);

        // Items panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(BACKGROUND_COLOR);

        // Table for delivery items
        String[] itemColumns = {"Type", "Material", "Quantity", ""};
        Object[][] itemData = {};
        JTable itemsTable = new JTable(itemData, itemColumns);
        //styleTable(itemsTable);

        // Add button column
        TableColumn buttonColumn = itemsTable.getColumnModel().getColumn(3);
        buttonColumn.setCellRenderer(new ButtonRenderer());
        buttonColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsPanel.add(itemsScroll, BorderLayout.CENTER);

        // Panel for adding new items
        JPanel addItemPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        addItemPanel.setBackground(BACKGROUND_COLOR);

        // Item type selection
        JComboBox<String> itemTypeCombo = new JComboBox<>(new String[]{"wood", "core"});
        styleComboBox(itemTypeCombo);
        addItemPanel.add(itemTypeCombo);

        // Material selection (will be populated based on type)
        JComboBox<String> materialCombo = new JComboBox<>();
        styleComboBox(materialCombo);
        addItemPanel.add(materialCombo);

        // Quantity
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        addItemPanel.add(quantitySpinner);

        // Add item button
        JButton addItemButton = createMenuButton("Add Item", () -> {
            // Implementation to add item to the table
        });
        addItemButton.setPreferredSize(new Dimension(120, 30));
        addItemPanel.add(addItemButton);

        itemsPanel.add(addItemPanel, BorderLayout.SOUTH);
        formPanel.add(itemsPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = createMenuButton("Save Delivery", () -> {
            try {
                // Create delivery object
                Delivery delivery = new Delivery();
                delivery.setReceivedBy(receivedByField.getText());
                delivery.setNotes(notesArea.getText());

                // Get items from table
                List<DeliveryItem> items = new ArrayList<>();
                // (Implementation to get items from table)
                delivery.setItems(items);

                // Save delivery
                if (deliveryService.recordDelivery(delivery)) {
                    JOptionPane.showMessageDialog(dialog, "Delivery recorded successfully!");
                    dialog.dispose();
                    parent.dispose();
                    showDeliveriesInventory();
                }
            } catch (SQLException e) {
                showError("Error saving delivery: " + e.getMessage());
            }
        });

        buttonPanel.add(saveButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Update materials combo when type changes
        itemTypeCombo.addActionListener(e -> {
            updateMaterialCombo(itemTypeCombo.getSelectedItem().toString(), materialCombo);
        });

        // Initial population of materials
        updateMaterialCombo(itemTypeCombo.getSelectedItem().toString(), materialCombo);

        dialog.setVisible(true);
    }

    private void updateMaterialCombo(String itemType, JComboBox<String> materialCombo) {
        materialCombo.removeAllItems();
        try {
            if (itemType.equals("wood")) {
                List<String> woodNames = wandService.getAllWoodNames();
                for (String name : woodNames) {
                    materialCombo.addItem(name);
                }
            } else {
                List<String> coreMaterials = wandService.getAllCoreMaterials();
                for (String material : coreMaterials) {
                    materialCombo.addItem(material);
                }
            }
        } catch (SQLException e) {
            showError("Failed to load materials: " + e.getMessage());
        }
    }

    private void showDeliveryDetails(int deliveryId) {
        try {
            Delivery delivery = deliveryService.getDeliveryById(deliveryId);
            if (delivery == null) {
                showError("Delivery not found");
                return;
            }

            JDialog dialog = new JDialog(this, "Delivery Details", true);
            dialog.setSize(600, 500);
            centerWindow(dialog);
            dialog.getContentPane().setBackground(BACKGROUND_COLOR);
            dialog.setLayout(new BorderLayout(10, 10));

            // Info panel
            JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            infoPanel.setBackground(BACKGROUND_COLOR);

            infoPanel.add(createStyledLabel("Delivery ID:"));
            infoPanel.add(createStyledLabel(String.valueOf(delivery.getDeliveryId())));

            infoPanel.add(createStyledLabel("Date:"));
            infoPanel.add(createStyledLabel(delivery.getDeliveryDate()));

            infoPanel.add(createStyledLabel("Supplier:"));
            infoPanel.add(createStyledLabel(delivery.getSupplierName()));

            infoPanel.add(createStyledLabel("Received By:"));
            infoPanel.add(createStyledLabel(delivery.getReceivedBy()));

            infoPanel.add(createStyledLabel("Notes:"));
            JTextArea notesArea = new JTextArea(delivery.getNotes());
            notesArea.setEditable(false);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(notesArea);
            styleTextArea(notesArea);
            infoPanel.add(notesScroll);

            dialog.add(infoPanel, BorderLayout.NORTH);

            // Items table
            String[] columns = {"Type", "Material", "Quantity"};
            Object[][] data = new Object[delivery.getItems().size()][columns.length];

            for (int i = 0; i < delivery.getItems().size(); i++) {
                DeliveryItem item = delivery.getItems().get(i);
                String materialName = item.getItemType().equals("wood") ?
                        wandService.getWoodName(item.getMaterialId()) :
                        wandService.getCoreMaterial(item.getMaterialId());

                data[i] = new Object[]{
                        item.getItemType(),
                        materialName,
                        item.getQuantity()
                };
            }

            JTable itemsTable = createStyledTable(data, columns);
            JScrollPane itemsScroll = new JScrollPane(itemsTable);
            dialog.add(itemsScroll, BorderLayout.CENTER);

            // Close button
            JButton closeButton = createMenuButton("Close", dialog::dispose);
            closeButton.setPreferredSize(new Dimension(120, 40));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            buttonPanel.add(closeButton);

            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (SQLException e) {
            showError("Failed to load delivery details: " + e.getMessage());
        }
    }


    // Add these as inner classes to your GUI class
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // Action is handled by the action listener we'll add
            }
            isPushed = false;
            return label;
        }

        public void addActionListener(ActionListener listener) {
            button.addActionListener(listener);
        }
    }

    private void styleTableButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database (using our existing class)
                new GUI().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to initialize: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}