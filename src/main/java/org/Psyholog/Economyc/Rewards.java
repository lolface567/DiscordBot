package org.Psyholog.Economyc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Rewards extends ListenerAdapter {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Set<String> claimedMessages = new HashSet<>();
    private JDA jda = null;
    private String rewardChannelId = "";

    public void RewardScheduler(JDA jda, String rewardChannelId) {
        this.jda = jda;
        this.rewardChannelId = rewardChannelId;

        startScheduler();
    }

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            TextChannel channel = jda.getTextChannelById(rewardChannelId);
            if (channel != null) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🎁 Успей первым!")
                        .setDescription("Нажми на кнопку первым и получи **200 монет**!")
                        .setColor(Color.YELLOW)
                        .setTimestamp(Instant.now());

                channel.sendMessageEmbeds(embed.build())
                        .setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.primary("claim_reward", "💰 Забрать монеты"))
                        .queue();
            }
        }, 0, 4, TimeUnit.HOURS);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("claim_reward")) {

            String messageId = event.getMessageId();

            if (claimedMessages.contains(messageId)) {
                event.reply("❌ Награда уже была забрана. Ждите следующей!").setEphemeral(true).queue();
                return;
            }

            claimedMessages.add(messageId);

            DatabaseManager.addCoins(event.getUser().getIdLong(), 200);

            EmbedBuilder claimedEmbed = new EmbedBuilder()
                    .setTitle("🎉 Монеты забраны!")
                    .setDescription(event.getUser().getAsMention() + " забрал **200 монет** 💸")
                    .setColor(Color.GREEN)
                    .setTimestamp(Instant.now());

            event.editMessageEmbeds(claimedEmbed.setColor(Color.RED).build())
                    .setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.danger("claimed_reward", "❌ Монеты забрали"))
                    .queue();
        }
        if (event.getComponentId().equals("claimed_reward")) {
            event.reply("❌ Ты не успел забрать награду!").setEphemeral(true).queue();
        }
    }
}
