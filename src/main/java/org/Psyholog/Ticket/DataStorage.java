package org.Psyholog.Ticket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import java.sql.*;


public class DataStorage {
    private static final Logger logger = LoggerFactory.getLogger(DataStorage.class);
    private static final String url = Dotenv.load().get("url");
    private static final String user = Dotenv.load().get("user");
    private static final String password = Dotenv.load().get("password");

    // Singleton instance
    private static DataStorage instance;

    private DataStorage() {
        createTables();
        initTicketCounter();
    }

    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    private void createTables() {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            String createTicketsTable = """
                CREATE TABLE IF NOT EXISTS tickets (
                    id INT PRIMARY KEY,
                    user_id VARCHAR(50) NOT NULL,
                    psychologist_id VARCHAR(50),
                    text_channel_id VARCHAR(50),
                    description TEXT,
                    status ENUM('open', 'closed') DEFAULT 'open'
                )""";

            String createPsychologistRatingsTable = """
                CREATE TABLE IF NOT EXISTS psychologist_ratings (
                    id int PRIMARY KEY auto_increment,
                    psychologist_id VARCHAR(50),
                    customer VARCHAR(50),
                    ratings DOUBLE
                )""";

            String createCountersTable = """
                CREATE TABLE IF NOT EXISTS ticket_counters (
                    id VARCHAR(50) PRIMARY KEY,
                    counter int NOT NULL
                )""";

            stmt.executeUpdate(createTicketsTable);
            stmt.executeUpdate(createPsychologistRatingsTable);
            stmt.executeUpdate(createCountersTable);

        } catch (SQLException e) {
            logger.error("Ошибка при создании таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getPsychologistsFromDB() {
        List<String> psychologistIds = new ArrayList<>();
        String query = "SELECT psychologist_id FROM psychologist_ratings";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                psychologistIds.add(rs.getString("psychologist_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return psychologistIds;
    }

    public void removePsychologistsFromDB(List<String> psychologistIds) {
        if (psychologistIds.isEmpty()) return;

        String query = "DELETE FROM psychologist_ratings WHERE psychologist_id IN (" +
                String.join(",", psychologistIds.stream().map(id -> "?").toArray(String[]::new)) + ")";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < psychologistIds.size(); i++) {
                stmt.setString(i + 1, psychologistIds.get(i));
            }
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getClosedTicketsList() {
        List<String> closedTickets = new ArrayList<>();
        String query = "SELECT text_channel_id FROM tickets WHERE status = 'closed'";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                closedTickets.add(rs.getString("text_channel_id"));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка закрытых тикетов: " + e.getMessage());
            e.printStackTrace();
        }
        return closedTickets;
    }

    public String getTicketByMember(String textChannelId) {
        String query = "SELECT user_id FROM tickets WHERE text_channel_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, textChannelId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("psychologist_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getClosedTicketCount(String psychologistId) {
        String query = "SELECT COUNT(*) FROM tickets WHERE psychologist_id = ? AND status = 'closed'";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, psychologistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public void addReview(String psychologist_id, String customer, double counter) {
        String query = "INSERT INTO psychologist_ratings (psychologist_id, customer, ratings) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            System.out.println("Добавляем запись: psychologist_id=" + psychologist_id + ", customer=" + customer + ", counter=" + counter);

            stmt.setString(1, psychologist_id);
            stmt.setString(2, customer);
            stmt.setDouble(3, counter);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Отзыв успешно добавлен.");
            } else {
                System.out.println("Ошибка: отзыв не добавлен.");
            }

        } catch (SQLException e) {
            logger.error("Ошибка при добавлении отзыва: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Double getAverageRating(String psychologistId) {
        String query = "SELECT AVG(ratings) FROM psychologist_ratings WHERE psychologist_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, psychologistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {  // Проверяем, что значение не NULL
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Integer getCountOfRatings(String psychologistId) {
        String query = "SELECT COUNT(ratings) FROM psychologist_ratings WHERE psychologist_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, psychologistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Double> getPsychologistCounters() {
        Map<String, Double> counters = new HashMap<>();
        String query = "SELECT psychologist_id, SUM(ratings) FROM psychologist_ratings GROUP BY psychologist_id";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String psychologistId = rs.getString("psychologist_id");
                Double totalRating = rs.getDouble(2);

                counters.put(psychologistId, totalRating);
                System.out.println("ID: " + psychologistId + ", Total Rating: " + totalRating);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counters;
    }

    public int countOpenTickets() {
        String query = "SELECT COUNT(*) FROM tickets WHERE status = 'open'";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String ticket_counters() {
        String query = "SELECT counter FROM ticket_counters";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("counter");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isTextChannelExists(String textChannelId) {
        String query = "SELECT 1 FROM tickets WHERE text_channel_id = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, textChannelId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPsychologist(int id) {
        String query = "SELECT psychologist_id FROM tickets WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("psychologist_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void assignPsychologist(int ticketId, String psychologistId) {
        String query = "UPDATE tickets SET psychologist_id = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, psychologistId);
            stmt.setInt(2, ticketId);
            int rowsAffected = stmt.executeUpdate(); // ВАЖНО: выполнение запроса

            if (rowsAffected > 0) {
                System.out.println("Психолог " + psychologistId + " назначен на тикет " + ticketId);
            } else {
                System.out.println("Тикет с ID " + ticketId + " не найден.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void closeTicket(String id) {
        String query = "UPDATE tickets SET status = 'closed' WHERE text_channel_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getTicketIdName(String id) {
        String query = "SELECT id FROM tickets WHERE text_channel_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTicketId(int id) {
        String query = "SELECT text_channel_id FROM tickets WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("text_channel_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUser(String ticketId) {
        String query = "SELECT user_id FROM tickets WHERE text_channel_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ticketId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTicketStatus(String id) {
        String query = "SELECT status FROM tickets WHERE text_channel_id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTicketDescription(int id) {
        String query = "SELECT description FROM tickets WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addTicket(int id, String userId, String textChannelId, String description) {
        String query = "INSERT INTO tickets (id, user_id, text_channel_id, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.setString(2, userId);
            stmt.setString(3, textChannelId);
            stmt.setString(4, description);

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении тикета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initTicketCounter() {
        String query = "INSERT IGNORE INTO ticket_counters (id, counter) VALUES (1, 0)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при инициализации счётчика тикетов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getNextTicketNumber() {
        String updateQuery = "UPDATE ticket_counters SET counter = counter + 1 WHERE id = 1";
        String selectQuery = "SELECT counter FROM ticket_counters WHERE id = 1";
        int ticketNumber = -1;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement updateStmt = conn.createStatement();
             Statement selectStmt = conn.createStatement()) {

            updateStmt.executeUpdate(updateQuery);
            ResultSet rs = selectStmt.executeQuery(selectQuery);
            if (rs.next()) {
                ticketNumber = rs.getInt("counter");
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении номера тикета: " + e.getMessage());
            e.printStackTrace();
        }
        return ticketNumber;
    }
}
