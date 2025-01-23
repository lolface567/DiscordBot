package org.Psyholog.Ticket;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReTakeTicketOnModal extends ListenerAdapter {
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().startsWith("changePsyhologModal:")) {
            String[] parts = event.getModalId().split(":");
            String ticketId = parts[1];
            String ticketName = parts[2];
            String[] channel = ticketName.split("-");
            String ticketIdname = channel[0];

            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }

            TextChannel adminTextChannel = guild.getTextChannelById(CreateTicket.adminChannel);

            if (ticketId == null) {
                event.reply("Ошибка: тикет не найден.").setEphemeral(true).queue();
                return;
            }

            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: мембер не найден.").setEphemeral(true).queue();
                return;
            }

            Role role = guild.getRoleById(CreateTicket.psyhologRole);
            if (role == null) {
                event.reply("Ошибка: роль не найдена.").setEphemeral(true).queue();
                return;
            }

            if (member.getRoles().contains(role)) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("🚨 Психолог запрашивает смену 🚨")
                        .setColor(Color.RED)
                        .addField("👤 Сообщение от", member.getAsMention(), false)
                        .addField("📄 Описание от психолога", event.getValue("why").getAsString(), false)
                        .addField("📄 Тикет", "Номер: " + ticketName + "\n Айди: " + ticketId, false)
                        .addField("📄 Описание тикета", DataStorage.getInstance().getTicketDes().get(ticketName), false)
                        .addField("🔔 Действие", "Нажмите на кнопку ниже, чтобы забрать тикет", false)
                        .setTimestamp(Instant.now());

                adminTextChannel.sendMessageEmbeds(embedBuilder.build()).addActionRow(
                        Button.primary("re-ticket:" + ticketIdname + ":" + ticketId, "Взять тикет")
                                .withEmoji(Emoji.fromUnicode("🎫"))
                ).queue();

                // Properly acknowledge the modal interaction to prevent it from hanging
                event.reply("Запрос на смену тикета был отправлен.").setEphemeral(true).queue();

                // Schedule the deletion of the message
                scheduler.schedule(() -> {
                    event.getMessage().delete().queue();
                }, 10, TimeUnit.MINUTES);
            } else {
                event.reply("Ошибка: у вас нет прав для выполнения этого действия.").setEphemeral(true).queue();
            }
        }
    }
}
