package org.Psyholog.DevCommands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClearKickedPsyholog extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClearKickedPsyholog.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("clear-baned-psyholog")) {
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (guild == null) {
                event.reply("Не удалось получить гильдию.").setEphemeral(true).queue();
                return;
            }

            // Получаем роль психолога
            Role psychologistRole = guild.getRoleById(Dotenv.load().get("psyhologRole"));
            if (psychologistRole == null) {
                event.reply("Роль психолога не найдена. Проверьте настройки.").setEphemeral(true).queue();
                return;
            }

            assert member != null;
            logger.info(member.getEffectiveName() + " Прописал команду /clear-baned-psyholog");

            // Получаем хранилище оценок
            Map<String, List<Integer>> psychologistRatings = DataStorage.getInstance().getPsychologistRatings();

            // Итератор для изменения Map во время обхода
            Iterator<Map.Entry<String, List<Integer>>> iterator = psychologistRatings.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, List<Integer>> entry = iterator.next();
                String psychologistId = entry.getKey();

                // Проверяем, есть ли у участника роль психолога
                Member psychologist = guild.getMemberById(psychologistId);

                if (psychologist == null || !psychologist.getRoles().contains(psychologistRole)) {
                    iterator.remove(); // Удаляем психолога из хранилища
                    DataStorage.getInstance().saveData();
                }
            }

            event.reply("Очищены все психологи без роли.").setEphemeral(true).queue();
        }
    }
}
