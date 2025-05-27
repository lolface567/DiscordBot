package org.Psyholog.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.Psyholog.entity.PsychologistRatings;
import org.Psyholog.repository.PsychologistRatingsRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PsychologistRatingsService {

    private final PsychologistRatingsRepository repository;

    // 📊 Получить средний рейтинг психолога
    public double getAverageRating(String psychologistId) {
        Double avg = repository.findAverageRatingByPsychologistId(psychologistId);
        return avg != null ? avg : 0.0;
    }

    // 🔢 Получить количество оценок психолога
    public long getCountOfRatings(String psychologistId) {
        return repository.countByPsychologistId(psychologistId);
    }

    public Map<String, Double> getPsychologistCounters() {
        List<Object[]> results = repository.countAllGroupedByPsychologist();
        Map<String, Double> map = new HashMap<>();

        for (Object[] row : results) {
            String psychologistId = (String) row[0];
            Long count = (Long) row[1];
            map.put(psychologistId, count.doubleValue());
        }

        return map;
    }

    @Transactional
    public void addRating(String psychologistId, String customerId, int rating) {
        PsychologistRatings ratingEntity = new PsychologistRatings();
        ratingEntity.setPsychologist(psychologistId);
        ratingEntity.setCustomer(customerId);
        ratingEntity.setRating((double) rating); // Приводим к double, так как в БД поле Double

        repository.save(ratingEntity);
    }

    // 🔍 Получить список всех психологов из базы
    public List<String> getPsychologistsFromDB() {
        return repository.findAllPsychologistIds();
    }

    // ❌ Удалить всех психологов по списку ID
    public void removePsychologistsFromDB(List<String> toRemove) {
        repository.deleteByPsychologistIn(toRemove);
    }
}