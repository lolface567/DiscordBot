package org.Psyholog.check_psyholog;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.PsychologistRatingsService;
import org.Psyholog.service.TicketsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class CheckPsyhologCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CheckPsyhologCommand.class);
    private static PsychologistRatingsService psychologistRatingsService;
    private static TicketsService ticketsService;

    @Autowired
    public CheckPsyhologCommand(PsychologistRatingsService psychologistRatingsService, TicketsService ticketsService) {
        this.psychologistRatingsService = psychologistRatingsService;
        this.ticketsService = ticketsService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("rating")) {
            Guild guild = event.getGuild();

            if (event.getOption("name") == null) {
                event.reply("Нужно передать упоминание психолога!").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }

            // Извлекаем ID психолога без лишних символов
            String psychologistId = event.getOption("name").getAsString().replaceAll("[<@>]", "");
            Member member = guild.getMemberById(psychologistId);

            if (member == null) {
                event.reply("Психолог с таким ID не найден.").setEphemeral(true).queue();
                return;
            }

            Role role = guild.getRoleById(Dotenv.load().get("TICKET_ROLE"));
            if (role == null || !member.getRoles().contains(role)) {
                event.reply("У этого пользователя нет роли психолога.").setEphemeral(true).queue();
                return;
            }

            // Получаем данные о психологе
            Double averageRating = psychologistRatingsService.getAverageRating(psychologistId);
            Integer ratingCount = Math.toIntExact(psychologistRatingsService.getCountOfRatings(psychologistId));
            Integer closedTickets = Math.toIntExact(ticketsService.getClosedTicketsCountByPsychologist(psychologistId));

            // Создаем эмбед
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎓 Средний балл психолога")
                    .setDescription(String.format(
                            "🔹 Психолог: **%s**\n" +
                                    "📊 Средний балл: **%.2f**\n" +
                                    "🗳 Количество оценок: **%d**\n" +
                                    "🔒 Количество закрытых тикетов: **%d**",
                            member.getEffectiveName(), averageRating, ratingCount, closedTickets))
                    .setColor(Color.CYAN)
                    .setThumbnail(member.getEffectiveAvatarUrl());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
