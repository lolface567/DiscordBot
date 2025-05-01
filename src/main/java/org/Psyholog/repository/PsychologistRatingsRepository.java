package org.Psyholog.repository;

import org.Psyholog.entity.PsychologistRatings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsychologistRatingsRepository extends JpaRepository<PsychologistRatings, Integer> {

    // 🔍 Средний рейтинг по психологу
    @Query("SELECT AVG(r.rating) FROM PsychologistRatings r WHERE r.psychologist = :psychologistId")
    Double findAverageRatingByPsychologistId(@Param("psychologistId") String psychologistId);

    // 🔍 Кол-во отзывов по психологу
    @Query("SELECT COUNT(r) FROM PsychologistRatings r WHERE r.psychologist = :psychologistId")
    Long countByPsychologistId(@Param("psychologistId") String psychologistId);

    // 🔍 Кол-во отзывов по всем психологам
    @Query("SELECT r.psychologist, COUNT(r) FROM PsychologistRatings r GROUP BY r.psychologist")
    List<Object[]> countAllGroupedByPsychologist();

    // 🔍 Получить всех психологов из базы
    @Query("SELECT DISTINCT r.psychologist FROM PsychologistRatings r")
    List<String> findAllPsychologistIds();

    // ❌ Удалить всех психологов по списку ID
    void deleteByPsychologistIn(List<String> psychologistIds);
}