package org.Psyholog.DevCommands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class UserLogs extends ListenerAdapter {
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        User user = event.getUser();
        TextChannel logChannel = guild.getTextChannelById(Dotenv.load().get("logsForUser"));

        guild.retrieveAuditLogs().type(ActionType.BAN).limit(1).queue(logs -> {
            boolean wasBanned = false;

            if (!logs.isEmpty()) {
                AuditLogEntry entry = logs.get(0);
                String targetId = entry.getTargetId();

                if (targetId != null && targetId.equals(user.getId())) {
                    wasBanned = true;
                }
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
                    .setColor(wasBanned ? Color.RED : Color.ORANGE)
                    .setThumbnail(user.getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            if (wasBanned) {
                embed.setTitle("🚫 Пользователь забанен")
                        .setDescription("**" + user.getAsTag() + "** был **забанен** с сервера.");
            } else {
                embed.setTitle("👋 Пользователь вышел")
                        .setDescription("**" + user.getAsTag() + "** вышел с сервера.");
            }

            if (logChannel != null)
                logChannel.sendMessageEmbeds(embed.build()).queue();
        });
    }
    // Создаем кэш
    private final Map<Long, Message> messageCache = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        messageCache.put(event.getMessageIdLong(), event.getMessage());

        // Ограничение кеша
        if (messageCache.size() > 2000) {
            Long firstKey = messageCache.keySet().iterator().next();
            messageCache.remove(firstKey);
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Message deletedMessage = messageCache.get(event.getMessageIdLong());
        TextChannel logChannel = event.getGuild().getTextChannelById(Dotenv.load().get("logsForUser"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🗑️ Сообщение удалено")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .setFooter("Message ID: " + event.getMessageId());

        if (deletedMessage != null) {
            embed.setAuthor(deletedMessage.getAuthor().getAsTag(), null, deletedMessage.getAuthor().getEffectiveAvatarUrl());

            StringBuilder description = new StringBuilder();
            description.append("**Сообщение отправил** ").append(deletedMessage.getAuthor().getAsMention())
                    .append(" **Удалено в** <#").append(event.getChannel().getId()).append(">\n")
                    .append(deletedMessage.getContentDisplay());

            // Добавим инфу о вложениях
            if (!deletedMessage.getAttachments().isEmpty()) {
                description.append("\n\n📎 **Вложения:**\n");
                for (Message.Attachment attachment : deletedMessage.getAttachments()) {
                    description.append(attachment.getUrl()).append("\n");
                }
            }

            embed.setDescription(description.toString());
        }

        if (logChannel != null) {
            logChannel.sendMessageEmbeds(embed.build()).queue();
        }

        // Удаляем из кэша
        messageCache.remove(event.getMessageIdLong());
    }
}
