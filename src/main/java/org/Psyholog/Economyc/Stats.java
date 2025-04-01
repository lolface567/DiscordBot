package org.Psyholog.Economyc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class Stats extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Stats.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getName().equals("stats")) {
            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: не удалось получить информацию о пользователе.").setEphemeral(true).queue();
                return;
            }

            String balance = String.valueOf(DatabaseManager.getBalance(member.getId())); // Получаем баланс
            LocalDate joinDate = member.getTimeJoined().toLocalDate();
            LocalDate today = LocalDate.now();
            long daysOnServer = ChronoUnit.DAYS.between(joinDate, today); // Считаем дни

            String creationDate = member.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String roles = member.getRoles().isEmpty() ? "Нет ролей" :
                    member.getRoles().stream().map(Role::getName).collect(Collectors.joining(", "));

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("👤 Статистика пользователя: " + member.getEffectiveName())
                    .setThumbnail(member.getEffectiveAvatarUrl()) // Аватарка
                    .setColor(Color.CYAN) // Цвет рамки
                    .addField("💰 Баланс:", balance + " монет", false)
                    .addField("⏳ Вы с нами уже:", daysOnServer + " дней", false)
                    .addField("📆 Дата создания аккаунта:", creationDate, false)
                    .addField("🏅 Роли:", roles, false)
                    .setFooter("ID: " + member.getId(), member.getEffectiveAvatarUrl());

            event.replyEmbeds(embedBuilder.build()).queue();
        }

        if (event.getName().equals("add_coins")) {
            long memberId;
            if (event.getOption("id") != null) {
                memberId = event.getOption("id").getAsLong();
            } else {
                event.reply("Нужно передать id пользователя").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }
            int coinsCount;
            if (event.getOption("coins") != null) {
                coinsCount = event.getOption("coins").getAsInt();
            } else {
                event.reply("Нужно передать количество коинов").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }
            DatabaseManager.addCoins(memberId, coinsCount);
            event.reply("Пользователю успешно добавлено: " + coinsCount + " коинов").setEphemeral(true).queue();
        }
    }
}
