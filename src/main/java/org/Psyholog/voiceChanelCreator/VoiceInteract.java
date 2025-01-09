package org.Psyholog.voiceChanelCreator;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoiceInteract extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        VoiceChannel voice = event.getChannelJoined().asVoiceChannel();
        System.out.println("Тут");

        if (event.getChannelJoined() != null) {
            VoiceChannel joinedChannel = event.getChannelJoined().asVoiceChannel();
            String userName = event.getMember().getEffectiveName();
            System.out.println(userName + " подключился к голосовому каналу " + joinedChannel.getName());
        }

        String voiceChanel = "1319490985033470013";
        if(voice.getId().equals(voiceChanel)){
            String categoryVoice = "1319490947842834553";
            guild.createVoiceChannel("Приват " + member.getEffectiveName(), guild.getCategoryById(categoryVoice)).queue();
            System.out.println("Работает");
        }
    }
}
