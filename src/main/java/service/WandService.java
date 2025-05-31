    package service;

    import db.DatabaseConnection;
    import model.*;
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    public class WandService {

        private final InventoryService inventoryService;

        public WandService() {
            this.inventoryService = new InventoryService();
        }

        public boolean createWand(Wand wand) throws SQLException {
            if (!hasSufficientInventory(wand.getWoodId(), wand.getCoreId())) {
                throw new SQLException("Insufficient inventory to create this wand");
            }

            String sql = "INSERT INTO wands (wood_id, core_id, length, flexibility, " +
                    "condition, special_features, price, status, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setInt(1, wand.getWoodId());
                        stmt.setInt(2, wand.getCoreId());
                        stmt.setDouble(3, wand.getLength());
                        stmt.setString(4, wand.getFlexibility());
                        stmt.setString(5, wand.getCondition());
                        stmt.setString(6, wand.getSpecialFeatures());
                        stmt.setDouble(7, wand.getPrice());
                        stmt.setString(8, wand.getStatus());
                        stmt.setString(9, wand.getNotes());

                        int affectedRows = stmt.executeUpdate();

                        if (affectedRows == 0) {
                            throw new SQLException("Creating wand failed, no rows affected");
                        }

                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new SQLException("Creating wand failed, no ID obtained");
                            }
                            wand.setId(rs.getInt(1));
                        }
                    }

                    inventoryService.updateStock("wood", wand.getWoodId(), -1, conn);
                    inventoryService.updateStock("core", wand.getCoreId(), -1, conn);

                    conn.commit();
                    return true;

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        }

        private boolean hasSufficientInventory(int woodId, int coreId) throws SQLException {
            return inventoryService.getQuantity("wood", woodId) > 0 &&
                    inventoryService.getQuantity("core", coreId) > 0;
        }

        public static WandWithDetails getWandDetails(int wandId) throws SQLException {
            String sql = "SELECT w.*, wt.name as wood_name, wt.rarity as wood_rarity, " +
                    "wt.description as wood_desc, c.material as core_material, " +
                    "c.power_level as core_power, c.description as core_desc " +
                    "FROM wands w " +
                    "JOIN wood_types wt ON w.wood_id = wt.wood_id " +
                    "JOIN cores c ON w.core_id = c.core_id " +
                    "WHERE w.wand_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, wandId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Wand wand = extractWandFromResultSet(rs);

                        WoodType wood = new WoodType(
                                rs.getInt("wood_id"),
                                rs.getString("wood_name"),
                                rs.getString("wood_rarity"),
                                rs.getString("wood_desc")
                        );

                        WandCore core = new WandCore(
                                rs.getInt("core_id"),
                                rs.getString("core_material"),
                                rs.getString("core_desc"),
                                0
                        );

                        return new WandWithDetails(wand, wood, core, null);
                    }
                }
            }
            return null;
        }

        public static Wand extractWandFromResultSet(ResultSet rs) throws SQLException {
            Wand wand = new Wand();
            wand.setId(rs.getInt("wand_id"));
            wand.setWoodId(rs.getInt("wood_id"));
            wand.setCoreId(rs.getInt("core_id"));
            wand.setLength(rs.getDouble("length"));
            wand.setFlexibility(rs.getString("flexibility"));
            wand.setProductionDate(rs.getString("production_date"));
            wand.setCondition(rs.getString("condition"));
            wand.setSpecialFeatures(rs.getString("special_features"));
            wand.setPrice(rs.getDouble("price"));
            wand.setStatus(rs.getString("status"));
            wand.setNotes(rs.getString("notes"));
            return wand;
        }

        private static void setWandParameters(PreparedStatement stmt, Wand wand) throws SQLException {
            stmt.setInt(1, wand.getWoodId());
            stmt.setInt(2, wand.getCoreId());
            stmt.setDouble(3, wand.getLength());
            stmt.setString(4, wand.getFlexibility());
            stmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));
            stmt.setString(6, wand.getCondition());
            stmt.setDouble(8, wand.getPrice());
            stmt.setString(7, wand.getSpecialFeatures());
            stmt.setString(9, wand.getStatus());
        }

        public static List<Wand> getAllWands() throws SQLException {
            List<Wand> wands = new ArrayList<>();
            String sql = "SELECT * FROM wands";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Wand wand = extractWandFromResultSet(rs);
                    wands.add(wand);
                }
            }
            return wands;
        }

        public List<Wand> searchWands(String query) throws SQLException {
            List<Wand> wands = new ArrayList<>();
            String sql = "SELECT w.* FROM wands w " +
                    "JOIN wood_types wt ON w.wood_id = wt.wood_id " +
                    "JOIN cores c ON w.core_id = c.core_id " +
                    "WHERE wt.name LIKE ? OR c.material LIKE ? OR w.status LIKE ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, "%" + query + "%");
                stmt.setString(2, "%" + query + "%");
                stmt.setString(3, "%" + query + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        wands.add(extractWandFromResultSet(rs));
                    }
                }
            }
            return wands;
        }

        public Wand getWandById(int wandId) throws SQLException {
            String sql = "SELECT * FROM wands WHERE wand_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, wandId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return extractWandFromResultSet(rs);
                    }
                }
            }
            return null;
        }

        public boolean updateWand(Wand wand) throws SQLException {
            String sql = "UPDATE wands SET wood_id = ?, core_id = ?, length = ?, " +
                    "flexibility = ?, condition = ?, special_features = ?, " +
                    "price = ?, status = ?, notes = ? WHERE wand_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, wand.getWoodId());
                stmt.setInt(2, wand.getCoreId());
                stmt.setDouble(3, wand.getLength());
                stmt.setString(4, wand.getFlexibility());
                stmt.setString(5, wand.getCondition());
                stmt.setString(6, wand.getSpecialFeatures());
                stmt.setDouble(7, wand.getPrice());
                stmt.setString(8, wand.getStatus());
                stmt.setString(9, wand.getNotes());
                stmt.setInt(10, wand.getId());

                return stmt.executeUpdate() > 0;
            }
        }

        public boolean deleteWand(int wandId) throws SQLException {
            String sql = "DELETE FROM wands WHERE wand_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, wandId);
                return stmt.executeUpdate() > 0;
            }
        }

        public String getWoodName(int woodId) throws SQLException {
            String sql = "SELECT name FROM wood_types WHERE wood_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, woodId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("name");
                    }
                }
            }
            return "Unknown Wood";
        }

        public String getCoreMaterial(int coreId) throws SQLException {
            String sql = "SELECT material FROM cores WHERE core_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, coreId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("material");
                    }
                }
            }
            return "Unknown Core";
        }

        public List<String> getAllWoodNames() throws SQLException {
            List<String> names = new ArrayList<>();
            String sql = "SELECT name FROM wood_types ORDER BY name";

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
            return names;
        }

        public List<String> getAllCoreMaterials() throws SQLException {
            List<String> materials = new ArrayList<>();
            String sql = "SELECT material FROM cores ORDER BY material";

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    materials.add(rs.getString("material"));
                }
            }
            return materials;
        }

        public int getWoodIdByName(String name) throws SQLException {
            String sql = "SELECT wood_id FROM wood_types WHERE name = ?";
            int woodId = -1;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        woodId = rs.getInt("wood_id");
                    }
                }
            }

            if (woodId == -1) {
                throw new SQLException("Wood type not found: " + name);
            }
            return woodId;
        }

        public int getCoreIdByMaterial(String material) throws SQLException {
            String sql = "SELECT core_id FROM cores WHERE material = ?";
            int coreId = -1;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, material);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        coreId = rs.getInt("core_id");
                    }
                }
            }

            if (coreId == -1) {
                throw new SQLException("Core material not found: " + material);
            }
            return coreId;
        }
    }