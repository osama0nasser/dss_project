package com.mycompany.dss.database;

import com.mycompany.dss.model.Product;
import com.mycompany.dss.model.Scenario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            String url = "jdbc:h2:~/dss_data;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
            connection = DriverManager.getConnection(url, "sa", "");
            createTables();
            if (getProductCount() == 0) insertSampleProducts();
            if (getScenarioCount() == 0) insertSampleScenarios();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private void createTables() throws SQLException {
        String productsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL, " +
                "price DECIMAL(10,2) NOT NULL, cost DECIMAL(10,2) NOT NULL, demand INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String scenariosTable = "CREATE TABLE IF NOT EXISTS scenarios (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL, " +
                "price_change DECIMAL(5,2) NOT NULL, cost_change DECIMAL(5,2) NOT NULL, " +
                "demand_change DECIMAL(5,2) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(productsTable);
            stmt.execute(scenariosTable);
        }
    }

    private int getProductCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getScenarioCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM scenarios")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void insertSampleProducts() throws SQLException {
        String sql = "INSERT INTO products (name, price, cost, demand) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            Object[][] sample = {{"Laptop", 10000.0, 7000.0, 100}, {"Mouse", 500.0, 300.0, 500},
                    {"Keyboard", 1500.0, 800.0, 200}, {"Mobile", 500.0, 300.0, 50}};
            for (Object[] row : sample) {
                ps.setString(1, (String) row[0]);
                ps.setDouble(2, (double) row[1]);
                ps.setDouble(3, (double) row[2]);
                ps.setInt(4, (int) row[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertSampleScenarios() throws SQLException {
        String sql = "INSERT INTO scenarios (name, price_change, cost_change, demand_change) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            Object[][] sample = {{"Optimistic", 10.0, -5.0, 20.0}, {"Pessimistic", -15.0, 10.0, -20.0}, {"Moderate", 5.0, 0.0, 5.0}};
            for (Object[] row : sample) {
                ps.setString(1, (String) row[0]);
                ps.setDouble(2, (double) row[1]);
                ps.setDouble(3, (double) row[2]);
                ps.setDouble(4, (double) row[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Product> loadProducts() {
        List<Product> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, price, cost, demand FROM products ORDER BY id")) {
            while (rs.next()) {
                list.add(new Product(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getDouble("cost"), rs.getInt("demand")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Scenario> loadScenarios() {
        List<Scenario> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, price_change, cost_change, demand_change FROM scenarios ORDER BY id")) {
            while (rs.next()) {
                list.add(new Scenario(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price_change"), rs.getDouble("cost_change"), rs.getDouble("demand_change")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void insertProduct(Product p) {
        String sql = "INSERT INTO products (name, price, cost, demand) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName()); ps.setDouble(2, p.getPrice());
            ps.setDouble(3, p.getCost()); ps.setInt(4, p.getDemand());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateProduct(Product p) {
        String sql = "UPDATE products SET name=?, price=?, cost=?, demand=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getName()); ps.setDouble(2, p.getPrice());
            ps.setDouble(3, p.getCost()); ps.setInt(4, p.getDemand()); ps.setInt(5, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteProduct(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void insertScenario(Scenario s) {
        String sql = "INSERT INTO scenarios (name, price_change, cost_change, demand_change) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName()); ps.setDouble(2, s.getPriceChange());
            ps.setDouble(3, s.getCostChange()); ps.setDouble(4, s.getDemandChange());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateScenario(Scenario s) {
        String sql = "UPDATE scenarios SET name=?, price_change=?, cost_change=?, demand_change=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.getName()); ps.setDouble(2, s.getPriceChange());
            ps.setDouble(3, s.getCostChange()); ps.setDouble(4, s.getDemandChange()); ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteScenario(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM scenarios WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}