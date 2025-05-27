package org.Psyholog.menu;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.dev_commands.DotenvConfig;
import org.Psyholog.service.TicketsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuCommandEx extends ListenerAdapter {
    private static TicketsService ticketsService = null;
    private static final DotenvConfig DOTENV_CONFIG = new DotenvConfig();
    private static final Dotenv DOTENV = DOTENV_CONFIG.dotenv();

    @Autowired
    public MenuCommandEx(TicketsService ticketsService) {
        this.ticketsService = ticketsService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        Guild guild = event.getGuild();
        Member member = event.getMember();
        TextChannel textChannel = event.getChannel().asTextChannel();
        boolean curentText = ticketsService.channelExists(textChannel.getId());
        assert guild != null;
        Role role = guild.getRoleById(DOTENV.get("TICKET_ROLE"));

        if (event.getName().equals("menu")) {
            if(curentText){
                assert member != null;
                if (member.getRoles().contains(role)) {
                    MenuSystem.execute(event);
                } else {
                    event.reply("У вас нет роли психолога").setEphemeral(true).queue();
                }
            }else {
                event.reply("Команду menu можно прописывать только в тикете!").setEphemeral(true).queue();
            }
        }
    }
}
