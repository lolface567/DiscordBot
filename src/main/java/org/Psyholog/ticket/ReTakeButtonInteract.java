package org.Psyholog.ticket;

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
import org.Psyholog.service.TicketsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.Psyholog.ticket.CreateTicket.scheduler;

@Service
public class ReTakeButtonInteract extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ReTakeButtonInteract.class);
    private TicketsService ticketsService;

    @Autowired
    public ReTakeButtonInteract(TicketsService ticketsService) {
        this.ticketsService = ticketsService;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().startsWith("re-ticket:")) {
            String[] parts = event.getButton().getId().split(":");
            String ticketIdName = parts[1];
            String ticketId = parts[2];

            Member member = event.getMember();
            Guild guild = event.getGuild();
            TextChannel mainChanel = guild.getTextChannelById(ticketId);
            if(mainChanel == null){
                event.reply("Ошибка: текстовый канал не найден.").setEphemeral(true).queue();
                return;
            }

            Role psyhologRole = guild.getRoleById(CreateTicket.PSYCHOLOGY_ROLE);
            Member pshyholog = guild.getMemberById(ticketsService.getPsychologistIdByChannelId(ticketId));
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
                                        .queue(success -> logger.info("Права успешно сняты."),
                                                error -> logger.error("Ошибка при установке прав: " + error.getMessage()));

                ticketsService.assignPsychologist(Integer.parseInt(ticketIdName), member.getId());

                mainChanel.upsertPermissionOverride(member)
                        .setAllowed(EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))
                        .queue(
                                success -> logger.info("Права успешно установлены."),
                                error -> logger.error("Ошибка при установке прав: " + error.getMessage())
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
                logger.info("Психолог " + member.getEffectiveName() + " хочет заменить психолога!");
            }
            else event.reply("Ошибка. у вас нету прав").setEphemeral(true).queue();
        }
    }
}