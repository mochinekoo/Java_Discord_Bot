package mochineko.java_discord_bot.manager.music;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MusicManager {

    private static final List<Emoji> emoji_list = Arrays.asList(
            Emoji.fromUnicode("1\uFE0F"),
            Emoji.fromUnicode("2\uFE0F"),
            Emoji.fromUnicode("3\uFE0F"),
            Emoji.fromUnicode("4\uFE0F"),
            Emoji.fromUnicode("5\uFE0F")
    );

    private static MusicManager instance;
    public final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
    YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager();

    public MusicManager() {
        //AudioSourceManagers.registerLocalSource(playerManager);

        playerManager.registerSourceManager(yt);
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new GetyarnAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        playerManager.registerSourceManager(new NicoAudioSourceManager());
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        }

        return musicManager;
    }


    public static String minToString(long n) {
        long time = n / 1000;
        int hour = (int) (time / 3600);
        int min = (int) (time / 60);
        int sec = (int) (time % 60);

        return hour + "時間" + min + "分" + sec + "秒";
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo info = track.getInfo();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("音楽が再生されます");
                embed.addField("タイトル：", info.title, false);
                embed.addField("URL：", info.uri, false);
                embed.addField("時間：", minToString(info.length), false);
                embed.setColor(Color.GREEN);
                channel.sendMessageEmbeds(embed.build()).complete();

                play(channel.getGuild(), channel, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    List<AudioTrack> track_list = playlist.getTracks();
                    getGuildAudioPlayer(channel.getGuild()).setWaitTrack(track_list);
                    EmbedBuilder embed = new EmbedBuilder();
                    int get_size = Math.min(track_list.size(), 5);
                    embed.setTitle("再生したい音楽を選択してください");
                    for (int n = 0; n < get_size; n++) {
                        AudioTrack track = track_list.get(n);
                        AudioTrackInfo info = track.getInfo();
                        embed.addField( emoji_list.get(n).getFormatted() + info.title + "(" + info.author + ")", info.uri, true);
                    }
                    embed.setColor(Color.GREEN);
                    channel.sendMessageEmbeds(embed.build())
                            .addActionRow(get_size >= 1 ? Button.primary("button_1", "1") : Button.primary("button_1", "1").asDisabled(),
                                    get_size >= 2 ? Button.primary("button_2", "2") : Button.primary("button_2", "2").asDisabled(),
                                    get_size >= 3 ? Button.primary("button_3", "3") : Button.primary("button_3", "3").asDisabled(),
                                    get_size >= 4 ? Button.primary("button_4", "4") : Button.primary("button_4", "4").asDisabled(),
                                    get_size == 5 ? Button.primary("button_5", "5") : Button.primary("button_5", "5").asDisabled())
                            .complete();
                }
                else {
                    AudioTrack track = playlist.getTracks().get(0);
                    AudioTrackInfo info = track.getInfo();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("音楽が再生されます");
                    embed.addField("タイトル：", info.title, false);
                    embed.addField("URL：", info.uri, false);
                    embed.addField("時間：", minToString(info.length), false);
                    embed.setColor(Color.GREEN);
                    channel.sendMessageEmbeds(embed.build()).complete();

                    play(channel.getGuild(), channel, musicManager, track);
                }
            }

            @Override
            public void noMatches() {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("エラー", trackUrl + " は見つかりませんでした", false);
                embed.setColor(Color.RED);
                channel.sendMessageEmbeds(embed.build()).complete();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("エラー", "エラーが発生しました：" + exception.getMessage(), false);
                embed.setColor(Color.RED);
                channel.sendMessageEmbeds(embed.build()).complete();
                exception.printStackTrace();
            }
        });
    }

    public void selectPlay(TextChannel channel, int index) {
        GuildMusicManager guild_manager = getGuildAudioPlayer(channel.getGuild());
        AudioTrack track = guild_manager.getWaitTrack().get(index);
        play(channel.getGuild(), channel, guild_manager, track);
        guild_manager.setWaitTrack(null);
    }

    public void play(Guild guild, TextChannel channel, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        AudioTrackInfo info = track.getInfo();
        if (musicManager.player.getPlayingTrack() == null) {
            System.out.print(track.getInfo().author);
            musicManager.player.playTrack(track);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("音楽が再生されます");
            embed.addField("タイトル：", info.title, false);
            embed.addField("URL：", info.uri, false);
            embed.addField("時間：", minToString(info.length), false);
            embed.setColor(Color.GREEN);
            channel.sendMessageEmbeds(embed.build()).complete();
        } else {
            musicManager.scheduler.queue(track);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("キューに追加されました。");
            embed.addField("タイトル：", info.title, false);
            embed.addField("URL：", info.uri, false);
            embed.addField("時間：", minToString(info.length), false);
            embed.setColor(Color.GREEN);
            channel.sendMessageEmbeds(embed.build()).complete();
        }
    }

    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        EmbedBuilder embed = new EmbedBuilder();

        AudioTrackInfo info = musicManager.player.getPlayingTrack().getInfo();
        embed.setTitle("音楽はスキップされます");
        embed.addField("次のタイトル：", info.title, false);
        embed.addField("URL：", info.uri, false);
        embed.addField("時間：", minToString(info.length), false);
        embed.setColor(Color.GREEN);
        channel.sendMessageEmbeds(embed.build()).complete();

        musicManager.scheduler.nextTrack();
    }

    public void setVolume(TextChannel channel, int volume) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.setVolume(volume);
    }

    public int getVolume(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        return musicManager.player.getVolume();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }


    public static class GuildMusicManager {
        public final AudioPlayer player;
        public final TrackScheduler scheduler;
        public List<AudioTrack> wait_track;

        public GuildMusicManager(AudioPlayerManager manager) {
            player = manager.createPlayer();
            scheduler = new TrackScheduler(player);
            wait_track = new ArrayList<>();
            player.addListener(scheduler);
        }

        public AudioPlayerSendHandler getSendHandler() {
            return new AudioPlayerSendHandler(player);
        }

        public AudioTrack getTrack() {
            return player.getPlayingTrack();
        }

        public List<AudioTrack> getWaitTrack() {
            return wait_track;
        }

        public void setWaitTrack(List<AudioTrack> track) {
            this.wait_track = track;
        }
    }

}
