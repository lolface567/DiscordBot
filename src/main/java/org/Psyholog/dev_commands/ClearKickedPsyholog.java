package org.Psyholog.dev_commands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.PsychologistRatingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClearKickedPsyholog extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClearKickedPsyholog.class);
    private static PsychologistRatingsService psychologistRatingsService;
    private static final DotenvConfig DOTENV_CONFIG = new DotenvConfig();
    private static final Dotenv DOTENV = DOTENV_CONFIG.dotenv();

    @Autowired
    public ClearKickedPsyholog(PsychologistRatingsService psychologistRatingsService) {
        this.psychologistRatingsService = psychologistRatingsService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("clear-baned-psyholog")) {
            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Не удалось получить гильдию.").setEphemeral(true).queue();
                return;
            }

            // Получаем роль психолога
            Role psychologistRole = guild.getRoleById(DOTENV.get("TICKET_ROLE"));
            if (psychologistRole == null) {
                event.reply("Роль психолога не найдена. Проверьте настройки.").setEphemeral(true).queue();
                return;
            }

            // Получаем список психологов из базы данных
            List<String> psychologistIds = psychologistRatingsService.getPsychologistsFromDB();
            List<String> toRemove = new ArrayList<>();

            for (String psychologistId : psychologistIds) {
                Member psychologist = guild.getMemberById(psychologistId);
                if (psychologist == null || !psychologist.getRoles().contains(psychologistRole)) {
                    toRemove.add(psychologistId);
                }
            }

            // Удаляем из базы данных психологов, которые не соответствуют требованиям
            psychologistRatingsService.removePsychologistsFromDB(toRemove);

            event.reply("Очищены все психологи без роли.").setEphemeral(true).queue();
        }
    }
}
