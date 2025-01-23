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
        assert guild != null;
        Role role = guild.getRoleById(Dotenv.load().get("psyhologRole"));

        if (event.getName().equals("menu")) {
            if (textChannel.getId().equals(DataStorage.getInstance().getTicketChannelMap().containsKey(textChannel.getId()))) { // Тут может быть ошибка
                if (member.getRoles().contains(role)) {
                    MenuSystem.execute(event);
                    System.out.println("Меню вызвал " + member.getEffectiveName());
                }else {
                    event.reply("У вас нет роли психолога").setEphemeral(true).queue();
                }
            } else {
                event.reply("Меню можно вызвать только в тикете!").setEphemeral(true).queue();
            }
        }
    }
}
