package org.Psyholog.CheakPsyholog;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TopPsyhologCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TopPsyhologCommand.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("top")) {
            Guild guild = event.getGuild();
            Map<String, Double> psychologistCounters = DataStorage.getInstance().getPsychologistCounters();

            // Создаем эмбед-сообщение
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("🔹 Топ психологов");
            embedBuilder.setColor(0x00ADEF);

            psychologistCounters.entrySet().stream()
                    .limit(10)
                    .forEach(entry -> {
                        String id = entry.getKey();

                        try {
                            assert guild != null;
                            Member psychologist = guild.retrieveMemberById(id).complete();
                            Double averageRating = DataStorage.getInstance().getAverageRating(id);
                            Integer ratingCount = DataStorage.getInstance().getCountOfRatings(id);
                            Integer closedTickets = DataStorage.getInstance().getClosedTicketCount(id);

                            if (psychologist != null) {
                                embedBuilder.addField(
                                        "⭐ " + psychologist.getEffectiveName(),
                                        String.format(
                                                "\uD83D\uDCCA Средний рейтинг: %.2f\n" +
                                                        "\uD83D\uDDF3\uFE0F Количество оценок: %d\n" +
                                                        "\uD83D\uDD12 Количество закрытых тикетов: %d",
                                                averageRating, ratingCount, closedTickets),
                                        false
                                );
                            } else {
                                embedBuilder.addField(
                                        "Психолог не найден",
                                        String.format("ID: %s", id),
                                        false
                                );
                            }
                        } catch (Exception e) {
                            logger.error("Ошибка при получении данных о психологе с ID " + id + ": " + e.getMessage());
                            embedBuilder.addField(
                                    "Психолог не найден",
                                    String.format("ID: %s", id),
                                    false
                            );
                        }
                    });

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }
    }
}
