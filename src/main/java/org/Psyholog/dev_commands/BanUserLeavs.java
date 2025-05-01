package org.Psyholog.dev_commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.PsychologistRatingsService;
import org.Psyholog.service.TicketsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.Psyholog.ticket.CreateTicket.userActiveTicketsMemory;

@Service
public class BanUserLeavs extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BanUserLeavs.class);
    private static TicketsService ticketsService;

    @Autowired
    public BanUserLeavs(TicketsService ticketsService) {
        this.ticketsService = ticketsService;
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) { // Если чубрик ливнул с активным тикетом НУЖНО ПРОВЕРИТЬ ЭТОТ КОД
        Member leavMember = event.getMember();  // Получаем пользователя

        if (leavMember == null) {
            logger.error("Member object is null, possibly due to member already leaving the server.");
            return; // Выход, если нет информации о пользователе
        }

        String stringMember = leavMember.getId();
        Guild guild = event.getGuild();

        // Проверяем, есть ли активные тикеты у пользователя
        if (ticketsService.getTicketByUserId(stringMember).isPresent()) {
            TextChannel textChannel = guild.getTextChannelById(userActiveTicketsMemory.get(stringMember));

            if (textChannel != null) { // Проверяем, что канал не null
                EmbedBuilder embedBuilder1 = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("\uD83D\uDCA2 Пользователь покинул сервер")
                        .setDescription("Можно закрыть тикет, пользователь покинул сервер")
                        .setFooter("Фарту масти")
                        .setTimestamp(Instant.now());

                textChannel.sendMessageEmbeds(embedBuilder1.build()).queue();

                // Удаляем активный тикет пользователя
                ticketsService.closeTicket(Integer.parseInt(textChannel.getId()));
                userActiveTicketsMemory.remove(stringMember);
            } else {
                logger.error("Текстовый канал не найден.");
            }
        }
        // Баним пользователя на 10 дней
        guild.ban(leavMember, 0, TimeUnit.DAYS).queue(
                success -> logger.info("Юзер забанен"),
                error -> logger.error("Не удалось забанить пользователя: " + error.getMessage())
        );
    }
}
