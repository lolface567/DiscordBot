package org.Psyholog.CheakPsyholog;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TopPsyhologCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TopPsyhologCommand.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("top")) {
            Guild guild = event.getGuild();
            Map<String, List<Integer>> psychologistRatings = DataStorage.getInstance().getPsychologistRatings();

            // Сортируем психологов по количеству оценок
            Map<String, Long> sortedPsychologists = psychologistRatings.entrySet().stream()
                    .sorted((entry1, entry2) -> Long.compare(
                            entry2.getValue().size(), // Сравниваем по количеству оценок
                            entry1.getValue().size()
                    ))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, // Ключ — это ID психолога
                            entry -> (long) entry.getValue().size(), // Значение — это количество оценок
                            (e1, e2) -> e1, // На случай дубликатов
                            LinkedHashMap::new // Сохраняем порядок сортировки
                    ));

            // Создаем эмбед-сообщение
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("🔹 Топ психологов");
            embedBuilder.setColor(0x00ADEF);

            sortedPsychologists.entrySet().stream()
                    .limit(10) // Ограничиваем вывод до топ-10
                    .forEach(entry -> {
                        String id = entry.getKey();
                        Long reviewCount = entry.getValue();
                        Integer closeTicketsCount = DataStorage.getInstance().getPsychologCloseCount(id);
                        Double averageRating = DataStorage.getInstance().getAverageRating(id); // Получаем средний рейтинг

                        try {
                            // Получаем участника напрямую через API
                            assert guild != null;
                            Member psychologist = guild.retrieveMemberById(id).complete();

                            if (psychologist != null) {
                                embedBuilder.addField(
                                        "⭐ " + psychologist.getEffectiveName(),
                                        String.format("📊 Средний рейтинг: %.2f\n\uD83D\uDDF3\uFE0F Количество оценок: %d\n\uD83D\uDD12 Количество закрытых тикетов: %d", averageRating, reviewCount, closeTicketsCount),
                                        false
                                );
                            } else {
                                embedBuilder.addField(
                                        "Психолог не найден",
                                        String.format("ID: %s\nКоличество оценок: %d", id, reviewCount),
                                        false
                                );
                            }
                        } catch (Exception e) {
                            logger.error("Ошибка при получении данных о психологе с ID " + id + ": " + e.getMessage());
                            embedBuilder.addField(
                                    "Психолог не найден",
                                    String.format("ID: %s\nКоличество оценок: %d", id, reviewCount),
                                    false
                            );
                        }
                    });

            // Отправляем эмбед-сообщение
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }
    }
}
