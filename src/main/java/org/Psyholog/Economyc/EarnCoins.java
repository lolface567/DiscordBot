package org.Psyholog.Economyc;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EarnCoins extends ListenerAdapter {
    private static final String ChannelForSpam = Dotenv.load().get("ChannelForSpam");
    private static final String systemMessages = Dotenv.load().get("systemMessages");
    private final Map<Long, Long> voiceJoinTimes = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        TextChannel eventChannel = event.getChannel().asTextChannel();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        TextChannel coinChannel = guild.getTextChannelById(ChannelForSpam);
        assert member != null;
        if (!member.getUser().isBot()) {
            assert coinChannel != null;
            if (coinChannel.getId().equals(eventChannel.getId())) {
                DatabaseManager.createUser(Long.parseLong(member.getId())); // Запрос в бд на создание нового юзера если его нету в бд

                DatabaseManager.addCoins(Long.parseLong(member.getId()), 2); // Добавляет юзеру 2 коинов
            }
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();

        // Пользователь зашел в войс
        if (event.getChannelJoined() != null) {
            voiceJoinTimes.put(userId, System.currentTimeMillis());
        }

        // Пользователь вышел из войса или перешел в другой гильдии
        if (event.getChannelLeft() != null) {
            if (voiceJoinTimes.containsKey(userId)) {
                long joinTime = voiceJoinTimes.remove(userId);
                long timeSpentMillis = System.currentTimeMillis() - joinTime;
                int minutesSpent = (int) (timeSpentMillis / 60000); // Конвертация в минуты

                if (minutesSpent > 0) {
                    int earnedCoins = minutesSpent * 3; // 3 монеты за минуту
                    DatabaseManager.addCoins(userId, earnedCoins);
                    Objects.requireNonNull(event.getGuild().getTextChannelById(systemMessages))
                            .sendMessage(event.getMember().getAsMention() + " получил " + earnedCoins + " монет за " + minutesSpent + " минут в голосовом чате! 💰")
                            .queue();
                }
            }
        }
    }
}
