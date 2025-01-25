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

import java.awt.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.Psyholog.Ticket.CreateTicket.userActiveTicketsMemory;

public class TakeTicketButton extends ListenerAdapter {
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {   // Обработка кнопки взять тикет
        if (event.getButton().getId().startsWith("take-ticket:")) {
            String[] parts = event.getButton().getId().split(":");
            String ticketNumber = parts[1];
            String ticketName = parts[2];
            String ticketId = DataStorage.getInstance().getTicketChannelMap().get(ticketNumber);


            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }

            if (DataStorage.getInstance().getClosedTickets().contains(ticketId)) {
                event.reply("Ошибка: тикет уже закрыт").setEphemeral(true).queue();
                return;
            }

            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: участник не найден.").setEphemeral(true).queue();
                return;
            }

            TextChannel textChannel = guild.getTextChannelById(ticketId);
            Member user = guild.getMemberById(DataStorage.getInstance().getUserActiveTickets().get(ticketId));

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

                DataStorage.getInstance().getClosedTickets().add(ticketId);
                DataStorage.getInstance().getUserActiveTickets().remove(textChannel.getId());
                DataStorage.getInstance().getTicketDes().remove(ticketId);
                DataStorage.getInstance().saveData();

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

                Role psychologistRole = guild.getRoleById(CreateTicket.psyhologRole);
                if (psychologistRole == null) {
                    event.reply("Ошибка: роль психолога не найдена.").setEphemeral(true).queue();
                    return;
                }

                DataStorage.getInstance().getTicketPsychologists().put(ticketNumber, member.getId());
                DataStorage.getInstance().saveData();

                // Получаем текущие разрешения для участника
                PermissionOverride existingPermission = textChannel.getPermissionOverride(member);

                if (existingPermission != null) {
                    // Если разрешение уже существует, обновляем его
                    existingPermission.getManager()
                            .grant(EnumSet.of(Permission.VIEW_CHANNEL))
                            .queue(
                                    success -> System.out.println("Права для участника успешно обновлены."),
                                    error -> System.err.println("Ошибка при обновлении прав для участника: " + error.getMessage())
                            );
                } else {
                    // Если разрешение не существует, создаем новое
                    textChannel.upsertPermissionOverride(member)
                            .setAllowed(EnumSet.of(Permission.VIEW_CHANNEL))
                            .queue(
                                    success -> System.out.println("Права для участника успешно установлены."),
                                    error -> System.err.println("Ошибка при установке прав для участника: " + error.getMessage())
                            );
                }

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.DARK_GRAY)
                        .setTitle("🎉 Психолог найден!")
                        .setDescription("Ваш психолог: " + member.getAsMention() +
                                "\nЕго средний бал: " + DataStorage.getInstance().getAverageRating(member.getId()) +
                                "\nЕго количество оценок: " + DataStorage.getInstance().getPsychologistRatings().get(member.getId()).size())  //Тестить этот код
                        .addField("✨ Поддержка доступна", "Вы можете начать обсуждение.", false)
                        .setFooter("Мы здесь, чтобы помочь вам!")
                        .setTimestamp(Instant.now());
                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

                System.out.println(member.getEffectiveName() + " взял тикет " + ticketId);
            } else {
                event.reply("Ошибка: канал не найден.").setEphemeral(true).queue();
            }
        }
    }
}
