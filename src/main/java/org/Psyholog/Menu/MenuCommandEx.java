package org.Psyholog.Menu;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;


public class MenuCommandEx extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        TextChannel textChannel = event.getChannel().asTextChannel();
        boolean curentText = DataStorage.getInstance().isTextChannelExists(textChannel.getId());
        assert guild != null;
        Role role = guild.getRoleById(Dotenv.load().get("psyhologRole"));

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
