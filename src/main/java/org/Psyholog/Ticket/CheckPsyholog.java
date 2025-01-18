package org.Psyholog.Ticket;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CheckPsyholog extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("rating")) {
            Guild guild = event.getGuild();
            // Получаем строку с ID психолога и убираем лишние символы
            String psyholog = event.getOption("name").getAsString().replaceAll("[<@>]", "");

            Member member = guild.getMemberById(psyholog);
            if (member == null) {
                event.reply("Психолог с таким ID не найден.").setEphemeral(true).queue();
                return;
            }

            Role role = guild.getRoleById(Dotenv.load().get("psyhologRole"));

            if (member.getRoles().contains(role)){
                // Проверяем, что ID действительно числовой
                if (!psyholog.matches("\\d+")) {
                    event.reply("Неверный формат ID психолога.").setEphemeral(true).queue();
                    return;
                }

                String averageRating = String.format("%.2f", DataStorage.getInstance().getAverageRating(psyholog));

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle("🎓 Средний балл психолога");
                embed.setDescription("🔹 Психолог: **" + member.getEffectiveName() + "**\n"
                        + "📊 Средний балл: **" + averageRating + "**" +
                        "\n📊 Количество оценок: " + "**" + DataStorage.getInstance().getPsychologistRatings().get(member.getId()).size() + "**");
                embed.setColor(0x00ADEF);  // Устанавливаем цвет (например, синий)
                embed.setThumbnail(member.getEffectiveAvatarUrl());
                embed.build();

                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            }else {
                event.reply("У этого юзера нету роли психолога").setEphemeral(true).queue();
            }
        }
        if (event.getName().equals("top")) {
            Guild guild = event.getGuild();
            Map<String, List<Integer>> psychologistRatings = DataStorage.getInstance().getPsychologistRatings();

            Map<String, Long> sortedPsychologists = psychologistRatings.entrySet().stream()
                    .sorted((entry1, entry2) -> Long.compare(
                            entry2.getValue().size(), // Сравниваем по количеству оценок
                            entry1.getValue().size()
                    ))
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey, // Ключ — это ID психолога
                                    entry -> (long) entry.getValue().size(), // Значение — это количество оценок
                                    (e1, e2) -> e1, // На случай дубликатов
                                    LinkedHashMap::new // Сохраняем порядок сортировки
                            )
                    );

            // Создаем эмбед-сообщение
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Топ психологов");
            embedBuilder.setColor(0x00ADEF);

            sortedPsychologists.entrySet().stream()
                    .limit(10) // Ограничиваем вывод до топ-10
                    .forEach(entry -> {
                        String id = entry.getKey();
                        Long reviewCount = entry.getValue();
                        Double averageRating = DataStorage.getInstance().getAverageRating(id); // Получаем средний рейтинг
                        Member psychologist = guild.getMemberById(id); // Получаем члена гильдии по ID психолога
                        if (psychologist != null) { // Проверяем, что психолог существует
                            embedBuilder.addField(
                                    psychologist.getEffectiveName(),
                                    String.format("Средний рейтинг: %.2f\nКоличество оценок: %d", averageRating, reviewCount),
                                    false
                            );
                        } else {
                            System.out.println("Психолог с ID " + id + " не найден!"); // Отладка
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
