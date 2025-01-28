package org.Psyholog.Ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.Psyholog.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStorage {
    private static final Logger logger = LoggerFactory.getLogger(DataStorage.class);

    private static final String DATA_FILE_PATH = "ticketData.json"; // JSON format for data storage

    // Singleton instance
    @JsonIgnore
    private static DataStorage instance;

    // Data variables
    private Map<String, String> ticketChannelMap = new HashMap<>();
    private Set<String> closedTickets = new HashSet<>();
    private Map<String, String> ticketPsychologists = new HashMap<>();
    private Map<String, String> userActiveTickets = new HashMap<>();
    private Map<String, String> getTicketDes = new HashMap<>();
    private Map<String, List<Integer>> psychologistRatings = new HashMap<>(); // Новая карта для хранения оценок
    private int ticketCounter = 0;

    // Private constructor to restrict instantiation
    private DataStorage() {
        loadData();
    }

    // Singleton pattern method
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    // Method for loading data using Jackson
    private void loadData() {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(DATA_FILE_PATH);
        if (file.exists()) {
            try {
                DataWrapper data = objectMapper.readValue(file, DataWrapper.class);
                this.ticketChannelMap = data.getTicketChannelMap() != null ? data.getTicketChannelMap() : new HashMap<>();
                this.closedTickets = data.getClosedTickets() != null ? data.getClosedTickets() : new HashSet<>();
                this.ticketPsychologists = data.getTicketPsychologists() != null ? data.getTicketPsychologists() : new HashMap<>();
                this.userActiveTickets = data.getUserActiveTickets() != null ? data.getUserActiveTickets() : new HashMap<>();
                this.ticketCounter = data.getTicketCounter();
                this.getTicketDes = data.getTicketDes() != null ? data.getTicketDes() : new HashMap<>();
                this.psychologistRatings = data.getPsychologistRatings() != null ? data.getPsychologistRatings() : new HashMap<>();
            } catch (IOException e) {
                logger.error("Не удалось загрузить данные тикетов: " + e.getMessage());
                e.printStackTrace();
                // Инициализация пустыми коллекциями в случае ошибки
                this.ticketChannelMap = new HashMap<>();
                this.closedTickets = new HashSet<>();
                this.ticketPsychologists = new HashMap<>();
                this.userActiveTickets = new HashMap<>();
                this.getTicketDes = new HashMap<>();
                this.psychologistRatings = new HashMap<>();
                this.ticketCounter = 0;
            }
        }
    }

    // Method for saving data using Jackson
    public void saveData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(DATA_FILE_PATH);
            file.getAbsoluteFile().getParentFile().mkdirs(); // Создаем директории, если они не существуют
            DataWrapper data = new DataWrapper(this.ticketChannelMap, this.closedTickets,
                    this.ticketPsychologists, this.userActiveTickets,
                    this.getTicketDes, this.psychologistRatings, this.ticketCounter);
            objectMapper.writeValue(file, data);
            logger.info("Данные успешно сохранены в " + DATA_FILE_PATH);
        } catch (IOException e) {
            logger.error("Не удалось сохранить данные тикетов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to add a rating for a psychologist
    public void addPsychologistRating(String psychologistId, int rating) {
        psychologistRatings.computeIfAbsent(psychologistId, k -> new ArrayList<>()).add(rating);
        saveData(); // Сохраняем изменения в базе
    }

    // Method to calculate the average rating of a psychologist
    public double getAverageRating(String psychologistId) {
        List<Integer> ratings = psychologistRatings.get(psychologistId);
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    // Getter methods
    public Map<String, String> getTicketChannelMap() {
        return ticketChannelMap;
    }

    public Map<String, String> getUserActiveTickets() {
        return userActiveTickets;
    }

    public Set<String> getClosedTickets() {
        return closedTickets;
    }

    public Map<String, String> getTicketPsychologists() {
        return ticketPsychologists;
    }

    public int getTicketCounter() {
        return ticketCounter;
    }

    public void incrementTicketCounter() {
        ticketCounter++;
    }

    public Map<String, String> getTicketDes() {
        return getTicketDes;
    }

    public Map<String, List<Integer>> getPsychologistRatings() {
        return psychologistRatings;
    }
}

// Separate wrapper class for the data
class DataWrapper {
    private Map<String, String> ticketChannelMap;
    private Set<String> closedTickets;
    private Map<String, String> ticketPsychologists;
    private Map<String, String> userActiveTickets;
    private Map<String, String> getTicketDes;
    private Map<String, List<Integer>> psychologistRatings; // Новое поле для хранения оценок
    private int ticketCounter;

    // Default constructor for Jackson
    public DataWrapper() {}

    public DataWrapper(Map<String, String> ticketChannelMap, Set<String> closedTickets,
                       Map<String, String> ticketPsychologists, Map<String, String> userActiveTickets,
                       Map<String, String> getTicketDes, Map<String, List<Integer>> psychologistRatings, int ticketCounter) {
        this.ticketChannelMap = ticketChannelMap;
        this.closedTickets = closedTickets;
        this.ticketPsychologists = ticketPsychologists;
        this.userActiveTickets = userActiveTickets;
        this.getTicketDes = getTicketDes;
        this.psychologistRatings = psychologistRatings;
        this.ticketCounter = ticketCounter;
    }

    public Map<String, String> getTicketChannelMap() { return ticketChannelMap; }

    public Set<String> getClosedTickets() {
        return closedTickets;
    }

    public Map<String, String> getTicketPsychologists() {
        return ticketPsychologists;
    }

    public Map<String, String> getUserActiveTickets() {
        return userActiveTickets;
    }

    public Map<String, String> getTicketDes() {
        return getTicketDes;
    }

    public void setTicketDes(Map<String, String> getTicketDes) { // Добавлен сеттер для ticketDes
        this.getTicketDes = getTicketDes;
    }

    public Map<String, List<Integer>> getPsychologistRatings() {
        return psychologistRatings;
    }

    public int getTicketCounter() {
        return ticketCounter;
    }
}
