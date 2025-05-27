package org.Psyholog.dev_commands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.DiscordBot;
import org.Psyholog.service.PsychologistProfileService;
import org.Psyholog.service.PsychologistRatingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateNames extends ListenerAdapter {

    private final PsychologistRatingsService ratingsService;
    private final PsychologistProfileService profileService;

    public UpdateNames(PsychologistRatingsService ratingsService, PsychologistProfileService profileService) {
        this.ratingsService = ratingsService;
        this.profileService = profileService;
    }
    private static final DotenvConfig DOTENV_CONFIG = new DotenvConfig();
    private static final Dotenv DOTENV = DOTENV_CONFIG.dotenv();
    private static final Logger logger = LoggerFactory.getLogger(UpdateNames.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!"update_names".equals(event.getName())) return;

        event.deferReply(true).queue();
        Guild guild = event.getGuild();
        Role role = guild.getRoleById(DOTENV.get("TICKET_ROLE"));

        if (role == null) {
            event.getHook().sendMessage("❌ Роль не знайдено").queue();
            return;
        }

        guild.findMembersWithRoles(role)
            .onSuccess(members -> {
                logger.info("Знайдено {} учасників з роллю {}", members.size(), role.getName());
                int updated = 0;

                try {
                    for (Member member : members) {
                        String id = member.getId();
                        String name = member.getEffectiveName();
                        profileService.saveOrUpdate(id, name);
                        updated++;
                    }
                    event.getHook().sendMessage("✅ Оновлено ніків: " + updated).queue();
                } catch (Exception e) {
                    logger.error("Помилка при оновленні профілів", e);
                    event.getHook().sendMessage("❌ Виникла помилка при оновленні профілів").queue();
                }
            })
            .onError(error -> {
                logger.error("Помилка при отриманні списку учасників", error);
                event.getHook().sendMessage("❌ Не вдалося отримати список учасників").queue();
            });
    }
}