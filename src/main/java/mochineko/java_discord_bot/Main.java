package mochineko.java_discord_bot;

import com.google.gson.Gson;
import mochineko.java_discord_bot.api.P2PEEWAPI;
import mochineko.java_discord_bot.commands.EarthquakeCommand;
import mochineko.java_discord_bot.commands.MusicCommand;
import mochineko.java_discord_bot.commands.UserCommand;
import mochineko.java_discord_bot.commands.YoutubeCommand;
import mochineko.java_discord_bot.listener.ButtonListener;
import mochineko.java_discord_bot.manager.json.ConfigJson;
import mochineko.java_discord_bot.manager.json.DeserializedJson;
import mochineko.java_discord_bot.manager.json.JsonManager;
import mochineko.java_discord_bot.status.FileType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.WebSocket;
import java.sql.SQLException;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public class Main extends ListenerAdapter {

    public static final Gson gson = new Gson();
    public static final Logger logger = Logger.getLogger("java_discordBot");
    private static JDA jda;

    public static void main(String[] strings) {
        if (jda == null) {
            initBot();
        }
    }

    public static JDA getJDA() {
        return jda;
    }

    public static void initBot() {
        for (FileType fileType : FileType.values()) {
            new JsonManager(fileType).createJson();
        }

        ConfigJson configJson = (ConfigJson) new JsonManager(FileType.CONFIG).getDeserializedJson();
        jda = JDABuilder.createDefault(configJson.getBotSetting().getToken())
                .addEventListeners(new Main())
                .addEventListeners(new MusicCommand())
                .addEventListeners(new ButtonListener())
                .addEventListeners(new UserCommand())
                .addEventListeners(new YoutubeCommand())
                .addEventListeners(new EarthquakeCommand())
                .build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        startEarthquakeService();

        jda.updateCommands()
                .addCommands(Commands.slash("play", "音楽を再生するコマンド")
                        .addOption(OptionType.STRING, "url", "再生したいURL　もしくは　再生したいキーワード", true))
                .addCommands(Commands.slash("play-info", "再生情報を表示するコマンド"))
                .addCommands(Commands.slash("skip", "音楽をスキップするコマンド"))
                .addCommands(Commands.slash("volume", "音楽の音量を設定するコマンド")
                        .addOption(OptionType.INTEGER, "volume", "音量"))
                .addCommands(Commands.slash("select-track", "再生したいトラックを選ぶ")
                        .addOption(OptionType.INTEGER, "index", "再生したいトラック", true))
                .addCommands(Commands.slash("youtube_search", "Youtubeを検索するコマンド"))
                .addCommands(Commands.slash("user_info", "aa")
                        .addOption(OptionType.STRING, "database", "aa"))
                .addCommands(Commands.slash("history_earthquake", "地震情報の履歴を表示するコマンド"))
                .addCommands(Commands.slash("history_tsunami", "津波情報の履歴を表示するコマンド"))
                .addCommands(Commands.slash("history_eew", "緊急地震速報の履歴を表示するコマンド"))
                .queue();
    }

    public static void startEarthquakeService() {
        JsonManager jsonManager = new JsonManager(FileType.CONFIG);
        P2PEEWAPI.startWebSockAPI(new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                logger.info("緊急地震速報のAPIに接続されました。");
                webSocket.request(10);
            }
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                logger.info("緊急地震速報APIからの受信データ: " + data.toString());

                ConfigJson configJson = (ConfigJson) jsonManager.getDeserializedJson();
                ConfigJson.Earthquake earthquakeConfig = configJson.getEarthquake();
                String sendChannelID = earthquakeConfig.getSendChannelID();
                if (sendChannelID == null || sendChannelID.isEmpty()) {
                    logger.warning("IDが設定されていません");
                }

                TextChannel channel = jda.getTextChannelById(sendChannelID);
                P2PEEWAPI api = gson.fromJson(data.toString(), P2PEEWAPI.class);
                EmbedBuilder embed = new EmbedBuilder();
                var earthquake = api.getEarthquake();
                if (earthquake != null) {
                    var earthquakeArrivalTime = earthquake.getArrivalTime();
                    var hypocenter = earthquake.getHypocenter();

                    embed.setTitle("過去の緊急地震速報");
                    embed.addField("地震発生時刻", earthquakeArrivalTime, true);
                    embed.addField("地震", hypocenter.getName() + "," + hypocenter.getDepth() + "," + hypocenter.getMagnitude(), true);
                }

                var areaList = api.getAreas();
                for (int i = 0; i < 20; i++) {
                    var area = areaList[i];
                    String name = area.getName();
                    String arrivalTime = area.getArrivalTime();
                    int scaleFrom = area.getScaleFrom();
                    int scaleTo = area.getScaleTo();
                    embed.addField(name, arrivalTime + "," + scaleFrom + ":" + scaleTo, true);
                }
                channel.sendMessageEmbeds(embed.build()).queue();

                webSocket.request(1);
                return null;
            }
        });
    }

}
