package mochineko.java_discord_bot.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import mochineko.java_discord_bot.manager.music.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import java.net.MalformedURLException;
import java.net.URL;

public class MusicCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        MessageChannelUnion channel = event.getChannel();
        MusicManager musicManager = MusicManager.getInstance();

        if ("play".equalsIgnoreCase(cmd)) {
            OptionMapping option_url = event.getOption("url");
            String url_string = option_url.getAsString();
            try {
                URL url = new URL(url_string);
                musicManager.loadAndPlay(channel.asTextChannel(), url.toString());
            } catch (MalformedURLException e) {
                musicManager.loadAndPlay(channel.asTextChannel(), "ytsearch:" + url_string);
            }
        }
        else if ("skip".equalsIgnoreCase(cmd)) {
            musicManager.skipTrack(channel.asTextChannel());
        }
        else if ("volume".equalsIgnoreCase(cmd)) {
            OptionMapping option_url = event.getOption("volume");
            musicManager.setVolume(channel.asTextChannel(), option_url.getAsInt());
        }
        else if ("play-info".equalsIgnoreCase(cmd)) {
            MusicManager.GuildMusicManager guild = musicManager.getGuildAudioPlayer(event.getGuild());

            EmbedBuilder embed = new EmbedBuilder();
            if (guild != null) {
                AudioTrack track = guild.getTrack();
                AudioTrackInfo info = track.getInfo();
                embed.addField("タイトル", info.title, false);
            }
            else {
                embed.addField("情報", "何も再生していません", false);
            }
            event.deferReply().addEmbeds(embed.build()).queue();
        }
        else if ("select-track".equalsIgnoreCase(cmd)) {
            OptionMapping option = event.getOption("index");
            musicManager.selectPlay(channel.asTextChannel(), option.getAsInt());
        }
    }

}

