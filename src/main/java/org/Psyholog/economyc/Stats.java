package org.Psyholog.economyc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.UserCoinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class Stats extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Stats.class);
    private final UserCoinService userCoinService;

    @Autowired
    public Stats(UserCoinService userCoinService) {
        this.userCoinService = userCoinService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("stats")) {
            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: не удалось получить информацию о пользователе.").setEphemeral(true).queue();
                return;
            }

            String balance = String.valueOf(userCoinService.getBalance(member.getIdLong())); // Получаем баланс
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

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }

        if (event.getName().equals("add_coins")) {
            long id_user;
            if (event.getOption("user") != null) {
                id_user = Objects.requireNonNull(event.getOption("user")).getAsUser().getIdLong();
            } else {
                event.reply("Нужно упомянуть пользователя").setEphemeral(true).queue();
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
            userCoinService.addCoins(id_user, coinsCount);
            event.reply("Пользователю успешно добавлено: " + coinsCount + " коинов").setEphemeral(true).queue();
        }
    }
}
