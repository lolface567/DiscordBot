package org.Psyholog.Ticket;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;

public class MenuSys {
    public static void execute(SlashCommandInteractionEvent event) {
        // Отправка предварительного ответа, чтобы избежать ошибки "Приложение не отвечает"
        event.deferReply().queue();

        try {
            String ticketName = event.getChannel().asTextChannel().getName();
            String ticketId = event.getChannel().asTextChannel().getId();

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("🧠 Меню психолога")
                    .setDescription("📋 Используйте кнопки ниже для управления тикетом.")
                    .addField("🆔 Ticket ID", ticketName, false)
                    .setColor(Color.DARK_GRAY)
                    .setTimestamp(Instant.now());

            TextChannel textChannel = event.getChannel().asTextChannel();
            textChannel.sendMessageEmbeds(embedBuilder.build())
                    .addActionRow(Button.danger("close-ticket:" + ticketId + ":" + ticketName, "Закрыть тикет").withEmoji(Emoji.fromUnicode("\uD83D\uDD11")),
                            Button.primary("change:" + ticketName, "Смена психолога").withEmoji(Emoji.fromUnicode("\uD83D\uDD04")))
                            .addActionRow(Button.success("voice:" + ticketName, "Создать войс").withEmoji(Emoji.fromUnicode("\uD83D\uDD0A")))
                    .queue(
                            success -> {
                                // Успешный ответ после отправки меню
                                event.getHook().sendMessage("Меню отправлено успешно!").setEphemeral(true).queue();
                            },
                            error -> {
                                // Обработка ошибки при отправке меню
                                event.getHook().sendMessage("Ошибка при отправке меню: " + error.getMessage()).setEphemeral(true).queue();
                                error.printStackTrace();
                            }
                    );
        } catch (Exception e) {
            event.getHook().sendMessage("Ошибка: " + e.getMessage()).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }
}