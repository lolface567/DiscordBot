package org.Psyholog.DevCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.Psyholog.Ticket.CreateTicket.userActiveTicketsMemory;

public class BanUserLeavs extends ListenerAdapter {
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) { // Если чубрик ливнул с активным тикетом НУЖНО ПРОВЕРИТЬ ЭТОТ КОД
        Member leavMember = event.getMember();  // Получаем пользователя

        if (leavMember == null) {
            System.out.println("Member object is null, possibly due to member already leaving the server.");
            return; // Выход, если нет информации о пользователе
        }

        String stringMember = leavMember.getId();
        Guild guild = event.getGuild();

        // Проверяем, есть ли активные тикеты у пользователя
        if (DataStorage.getInstance().getUserActiveTickets().containsValue(stringMember)) {
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
                DataStorage.getInstance().getUserActiveTickets().remove(textChannel.getId());
                userActiveTicketsMemory.remove(stringMember);
            } else {
                System.out.println("Текстовый канал не найден.");
            }
        }
        // Баним пользователя на 10 дней
        guild.ban(leavMember, 0, TimeUnit.DAYS).queue(
                success -> System.out.println("Юзер забанен"),
                error -> System.err.println("Не удалось забанить пользователя: " + error.getMessage())
        );
    }
}
