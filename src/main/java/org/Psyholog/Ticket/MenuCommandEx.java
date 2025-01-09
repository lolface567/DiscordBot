package org.Psyholog.Ticket;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class MenuCommandEx extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        Role role = guild.getRoleById(Dotenv.load().get("psyhologRole"));
        if (event.getName().equals("menu")) {
            if(!member.getRoles().contains(role)){
                event.reply("У вас нет роли психолога").setEphemeral(true).queue();
                return;
            }
            if(member.getRoles().contains(role)){
                MenuSys.execute(event);
            }
        }
    }
}
