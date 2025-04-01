package org.Psyholog.CheakPsyholog;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class CheckPsyhologCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CheckPsyhologCommand.class);

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

            Role role = guild.getRoleById(Dotenv.load().get("psyhologRole"));
            if (role == null || !member.getRoles().contains(role)) {
                event.reply("У этого пользователя нет роли психолога.").setEphemeral(true).queue();
                return;
            }

            // Получаем данные о психологе
            Double averageRating = DataStorage.getInstance().getAverageRating(psychologistId);
            Integer ratingCount = DataStorage.getInstance().getCountOfRatings(psychologistId);
            Integer closedTickets = DataStorage.getInstance().getClosedTicketCount(psychologistId);

            // Создаем эмбед
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎓 Средний балл психолога")
                    .setDescription(String.format(
                            "🔹 Психолог: **%s**\n" +
                                    "📊 Средний балл: **%.2f**\n" +
                                    "🗳 Количество оценок: **%d**\n" +
                                    "🔒 Количество закрытых тикетов: **%d**",
                            member.getEffectiveName(), averageRating, ratingCount, closedTickets))
                    .setColor(0x00ADEF)
                    .setThumbnail(member.getEffectiveAvatarUrl());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
