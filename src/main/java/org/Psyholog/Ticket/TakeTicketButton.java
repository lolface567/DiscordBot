package org.Psyholog.Ticket;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.Psyholog.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TakeTicketButton extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TakeTicketButton.class);
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {   // Обработка кнопки взять тикет
        if (event.getButton().getId().startsWith("take-ticket:")) {
            String[] parts = event.getButton().getId().split(":");
            String ticketNumber = parts[1];
            String ticketName = parts[2];
            String ticketId = DataStorage.getInstance().getTicketId(Integer.parseInt(ticketNumber));


            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }

            if (DataStorage.getInstance().getTicketStatus(ticketId).equals("closed")) {
                event.reply("Ошибка: тикет уже закрыт").setEphemeral(true).queue();
                return;
            }

            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: участник не найден.").setEphemeral(true).queue();
                return;
            }

            event.getGuild().retrieveMemberById(member.getId()).queue(updatedMember -> {
                if (updatedMember.getRoles().stream().anyMatch(role -> role.getId().equals(CreateTicket.PSYCHOLOGY_ROLE))) {
                    TextChannel textChannel = guild.getTextChannelById(ticketId);
                    Member user = guild.getMemberById(DataStorage.getInstance().getUser(ticketId));

                    if (user == null) {
                        event.editComponents(
                                ActionRow.of(
                                        Button.danger("taken-ticket", "Тикет был закрыт, юзер не найден").asDisabled().withEmoji(Emoji.fromUnicode("❌")),
                                        Button.link(textChannel.getJumpUrl(), "Перейти к тикету")
                                )
                        ).queue();
                        scheduler.schedule(() -> {
                            event.getMessage().delete().queue();
                        }, 10, TimeUnit.MINUTES);

                        DataStorage.getInstance().closeTicket(ticketId);

                        textChannel.delete().queue();
                        return;
                    }

                    if (textChannel != null) {
                        String newChannelName = ticketName + "-" + member.getEffectiveName();
                        textChannel.getManager().setName(newChannelName).queue(
                                success -> {
                                    event.editComponents(
                                            ActionRow.of(
                                                    Button.danger("taken-ticket", "Взял: " + member.getEffectiveName()).asDisabled()
                                                            .withEmoji(Emoji.fromUnicode("✅")),
                                                    Button.link(textChannel.getJumpUrl(), "Перейти к тикету")
                                            )
                                    ).queue();

                                    scheduler.schedule(() -> {
                                        event.getMessage().delete().queue();
                                    }, 10, TimeUnit.MINUTES);
                                },
                                error -> event.reply("Ошибка при обновлении имени канала: " + error.getMessage()).setEphemeral(true).queue()
                        );

                        Role psychologistRole = guild.getRoleById(CreateTicket.PSYCHOLOGY_ROLE);
                        if (psychologistRole == null) {
                            event.reply("Ошибка: роль психолога не найдена.").setEphemeral(true).queue();
                            return;
                        }

                        DataStorage.getInstance().assignPsychologist(Integer.parseInt(ticketNumber), member.getId());

                        // Получаем текущие разрешения для участника
                        PermissionOverride existingPermission = textChannel.getPermissionOverride(member);

                        if (existingPermission != null) {
                            // Если разрешение уже существует, обновляем его
                            existingPermission.getManager()
                                    .grant(EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_MANAGE))
                                    .queue(
                                            success -> logger.info("Права для участника успешно обновлены."),
                                            error -> logger.error("Ошибка при обновлении прав для участника: " + error.getMessage())
                                    );
                        } else {
                            // Если разрешение не существует, создаем новое
                            textChannel.upsertPermissionOverride(member)
                                    .setAllowed(EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_MANAGE))
                                    .queue(
                                            success -> logger.info("Права для участника успешно обновлены."),
                                            error -> logger.error("Ошибка при обновлении прав для участника: " + error.getMessage())
                                    );
                        }

                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setColor(Color.DARK_GRAY)
                                .setTitle("🎉 Психолог найден!")
                                .setDescription("Ваш психолог: " + member.getAsMention())
                                .addField("✨ Поддержка доступна", "Вы можете начать обсуждение.", false)
                                .setFooter("Мы здесь, чтобы помочь вам!")
                                .setTimestamp(Instant.now());

                        Double rating = DataStorage.getInstance().getAverageRating(member.getId());
                        Integer countOfRatings = DataStorage.getInstance().getCountOfRatings(member.getId());

                        if (rating != null && countOfRatings != null) {
                            embedBuilder.setDescription("Ваш психолог: " + member.getAsMention() +
                                    "\nЕго средний бал: " + rating +
                                    "\nЕго количество оценок: " + countOfRatings);
                        }

                        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

                        logger.info(member.getEffectiveName() + " взял тикет " + ticketId);
                    } else {
                        event.reply("Ошибка: канал не найден.").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("У вас нету роли психолога!").setEphemeral(true).queue();
                }
            });
        }
    }
}
