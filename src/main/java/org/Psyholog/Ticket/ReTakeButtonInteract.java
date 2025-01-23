package org.Psyholog.Ticket;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.Psyholog.Ticket.CreateTicket.scheduler;

public class ReTakeButtonInteract extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().startsWith("re-ticket:")) {
            String[] parts = event.getButton().getId().split(":");
            String ticketIdName = parts[1]; // Извлекаем ID тикета из имени
            String ticketId = parts[2]; // Извлекаем айди канала

            Member member = event.getMember();
            Guild guild = event.getGuild();
            TextChannel mainChanel = guild.getTextChannelById(ticketId);
            if(mainChanel == null){
                event.reply("Ошибка: текстовый канал не найден.").setEphemeral(true).queue();
                return;
            }

            Role psyhologRole = guild.getRoleById(CreateTicket.psyhologRole);
            Member pshyholog = guild.getMemberById(DataStorage.getInstance().getTicketPsychologists().get(ticketIdName));
            if(pshyholog == null){
                event.reply("Ошибка: психолог не найден.").setEphemeral(true).queue();
                return;
            }
            System.out.println(pshyholog);
            if(psyhologRole == null){
                event.reply("Ошибка: роль психолога не найдена.").setEphemeral(true).queue();
                return;
            }

            Message message = event.getMessage();
            scheduler.schedule(() -> {
                message.delete().queue();
            }, 10, TimeUnit.MINUTES);

            if (member.getRoles().contains(psyhologRole)) {
                String newChannelName = "ticket-" + ticketIdName + "-" + member.getEffectiveName();
                mainChanel.getManager().setName(newChannelName).queue(
                        success -> {
                            // Обновляем кнопку, чтобы показать, что тикет взят
                            event.editComponents(
                                    ActionRow.of(
                                            Button.danger("taken-reticket", "Взял: " + member.getEffectiveName()).asDisabled() // Изменяем кнопку на красную и отключаем её
                                    )
                            ).queue();
                        },
                        error -> event.reply("Ошибка при обновлении имени канала: " + error.getMessage()).setEphemeral(true).queue()
                );

                mainChanel.upsertPermissionOverride(pshyholog)
                                .setDenied(EnumSet.of(Permission.VIEW_CHANNEL))
                                        .queue(success -> System.out.println("Права успешно сняты."),
                                                error -> System.err.println("Ошибка при установке прав: " + error.getMessage()));

                DataStorage.getInstance().getTicketPsychologists().remove(ticketIdName);
                DataStorage.getInstance().saveData(); // Сохраняем данные в файл
                DataStorage.getInstance().getTicketPsychologists().put(ticketIdName, member.getId());
                DataStorage.getInstance().saveData(); // Сохраняем данные в файл

                mainChanel.upsertPermissionOverride(member)
                        .setAllowed(EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))
                        .queue(
                                success -> System.out.println("Права успешно установлены."),
                                error -> System.err.println("Ошибка при установке прав: " + error.getMessage())
                        );

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("✨ У вас новый психолог! ✨")
                        .setColor(Color.DARK_GRAY)
                        .setThumbnail(member.getAvatarUrl())
                        .setDescription("Ваш новый психолог будет рад помочь вам.")
                        .addField("👤 Новый психолог", member.getAsMention(), false)
                        .addField("🕒 Часы работы", "9:00 - 22:00 по МСК", false)
                        .setFooter("Будьте уверены, мы всегда готовы помочь!")
                        .setTimestamp(java.time.Instant.now());

                mainChanel.sendMessageEmbeds(embedBuilder.build()).queue();
                System.out.println("Психолог " + member.getEffectiveName() + " хочет заменить психолога!");
            }
            else event.reply("Ошибка. у вас нету прав").setEphemeral(true).queue();
        }
    }
}
