package org.Psyholog.DevCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.CreateTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CahingRolle extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CahingRolle.class);
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Member member = event.getMember(); // Участник, которому выдали роль
        List<Role> addedRoles = event.getRoles(); // Список новых ролей

        for (Role role : addedRoles) {
            logger.info("Роль " + role.getName() + " была выдана пользователю " + member.getEffectiveName());
        }
    }
}
