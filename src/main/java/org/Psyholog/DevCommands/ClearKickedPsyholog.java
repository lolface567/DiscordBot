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

import java.util.ArrayList;
import java.util.List;

public class ClearKickedPsyholog extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClearKickedPsyholog.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("clear-baned-psyholog")) {
            Guild guild = event.getGuild();
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

            // Получаем список психологов из базы данных
            List<String> psychologistIds = DataStorage.getInstance().getPsychologistsFromDB();
            List<String> toRemove = new ArrayList<>();

            for (String psychologistId : psychologistIds) {
                Member psychologist = guild.getMemberById(psychologistId);
                if (psychologist == null || !psychologist.getRoles().contains(psychologistRole)) {
                    toRemove.add(psychologistId);
                }
            }

            // Удаляем из базы данных психологов, которые не соответствуют требованиям
            DataStorage.getInstance().removePsychologistsFromDB(toRemove);

            event.reply("Очищены все психологи без роли.").setEphemeral(true).queue();
        }
    }
}
