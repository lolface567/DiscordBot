package org.Psyholog.Economyc;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static final String url = Dotenv.load().get("url");
    private static final String user = Dotenv.load().get("user");
    private static final String password = Dotenv.load().get("password");

    public static void initializeDatabase() {
        String createShopRolesTable = "CREATE TABLE IF NOT EXISTS shop_roles (" +
                "id BIGINT PRIMARY KEY, " +
                "cost INT NOT NULL" +
                ")";

        String createUserCoinsTable = "CREATE TABLE IF NOT EXISTS user_coins (" +
                "id BIGINT PRIMARY KEY, " +
                "coins INT NOT NULL DEFAULT 0" +
                ")";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createShopRolesTable);
            stmt.executeUpdate(createUserCoinsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Добавление роли в магазин
    public static void addRoleToShop(long roleId, int cost) {
        String query = "INSERT INTO shop_roles (id, cost) VALUES (?, ?) ON DUPLICATE KEY UPDATE cost = VALUES(cost)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, roleId);
            stmt.setInt(2, cost);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получение всех ролей из магазина
    public static Map<Long, Integer> getShopRoles() {
        Map<Long, Integer> roles = new HashMap<>();
        String query = "SELECT id, cost FROM shop_roles";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                roles.put(rs.getLong("id"), rs.getInt("cost"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    public static int getBalance(String userId) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement("SELECT coins FROM user_coins WHERE id = ?")) {
            stmt.setLong(1, Long.parseLong(userId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("coins");
            } else {
                createUser(Long.parseLong(userId));
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void addCoins(long userId, int amount) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO user_coins (id, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = coins + ?")) {
            stmt.setLong(1, userId);
            stmt.setInt(2, amount);
            stmt.setInt(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getRoleCost(long roleId) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement("SELECT cost FROM shop_roles WHERE id = ?")) {
            stmt.setLong(1, roleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("cost");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Если роль не найдена
    }

    public static void removeCoins(long userId, int amount) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE user_coins SET coins = GREATEST(coins - ?, 0) WHERE id = ?")) {
            stmt.setInt(1, amount);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createUser(long userId) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement("INSERT IGNORE INTO user_coins (id, coins) VALUES (?, 0)")) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
