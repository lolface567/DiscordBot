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
            // Получаем строку с ID психолога и убираем лишние символы
            String psyholog = "";
            if (event.getOption("name") != null) {
                psyholog = event.getOption("name").getAsString().replaceAll("[<@>]", "");
            } else {
                event.reply("Нужно передать упонминание психолога!").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }

            assert guild != null;
            Member member = guild.getMemberById(psyholog);
            if (member == null) {
                event.reply("Психолог с таким ID не найден.").setEphemeral(true).queue();
                return;
            }

            Role role = guild.getRoleById(Dotenv.load().get("psyhologRole"));

            if (member.getRoles().contains(role)) {
                // Проверяем, что ID действительно числовой
                if (!psyholog.matches("\\d+")) {
                    event.reply("Неверный формат ID психолога.").setEphemeral(true).queue();
                    return;
                }

                String averageRating = String.format("%.2f", DataStorage.getInstance().getAverageRating(psyholog));

                // Проверяем наличие оценок
                Map<String, List<Integer>> psychologistRatings = DataStorage.getInstance().getPsychologistRatings();
                List<Integer> ratings = psychologistRatings.get(member.getId());
                int ratingCount = (ratings != null) ? ratings.size() : 0;
                Integer closeTicketsCount = DataStorage.getInstance().getPsychologCloseCount(member.getId());

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle("🎓 Средний балл психолога");
                embed.setDescription("🔹 Психолог: **" + member.getEffectiveName() + "**\n"
                        + "📊 Средний балл: **" + averageRating + "**\n"
                        + "\uD83D\uDDF3\uFE0F Количество оценок: **" + ratingCount + "**\n"
                        + "\uD83D\uDD12 Количество закрытых тикетов: **" + closeTicketsCount + "**");
                embed.setColor(0x00ADEF); // Устанавливаем цвет (например, синий)
                embed.setThumbnail(member.getEffectiveAvatarUrl());
                embed.build();

                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            } else {
                event.reply("У этого юзера нету роли психолога").setEphemeral(true).queue();
            }
        }
    }
}
