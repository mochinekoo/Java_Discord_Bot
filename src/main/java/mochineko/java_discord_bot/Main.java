package mochineko.java_discord_bot;

import com.google.gson.Gson;
import mochineko.java_discord_bot.api.P2PEEWAPI;
import mochineko.java_discord_bot.api.P2PEarthquakeAPI;
import mochineko.java_discord_bot.commands.*;
import mochineko.java_discord_bot.listener.ButtonListener;
import mochineko.java_discord_bot.manager.json.ConfigJson;
import mochineko.java_discord_bot.manager.json.DeserializedJson;
import mochineko.java_discord_bot.manager.json.JsonManager;
import mochineko.java_discord_bot.status.EarthquakeScaleType;
import mochineko.java_discord_bot.status.FileType;
import mochineko.java_discord_bot.util.DrawMapBuilder;
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
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public class Main extends ListenerAdapter {

    public static final Gson gson = new Gson();
    public static final Logger logger = Logger.getLogger("java_discordBot");
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);
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
                .addEventListeners(new WeatherCommand())
                .build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        DrawMapBuilder.Init();
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
                .addCommands(Commands.slash("weather", "天気を見るコマンド"))
                .queue();
    }

    public static void startEarthquakeService() {
        JsonManager jsonManager = new JsonManager(FileType.CONFIG);
        ConfigJson configJson = (ConfigJson) jsonManager.getDeserializedJson();
        ConfigJson.Earthquake earthquakeConfig = configJson.getEarthquake();
        String sendChannelID = earthquakeConfig.getSendChannelID();

        P2PEarthquakeAPI.startWebSockAPI(new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                webSocket.request(1);
                logger.info("地震速報APIの接続に成功しました");
            }
            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                logger.warning("接続に閉じられました：" + statusCode + "(" + reason + ")");
                return null;
            }
            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                webSocket.request(1);
                logger.warning("APIの接続に問題が発生しました：" + error);
            }
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                webSocket.request(1);
                P2PEarthquakeAPI api = gson.fromJson(data.toString(), P2PEarthquakeAPI.class);

                logger.info("地震速報APIからの受信データ: " + data.toString());
                logger.info("送信するチャンネル：" + sendChannelID);

                if (sendChannelID == null || sendChannelID.isEmpty()) {
                    logger.warning("IDが設定されていません");
                    return null;
                }
                if (api.getCode() == 555 || api.getCode() == 561 || api.getCode() == 9611) {
                    logger.warning("受信対象外なため、サーバーには送られません：" + api.getCode());
                    return null;
                }

                TextChannel channel = jda.getTextChannelById(sendChannelID);
                channel.sendMessage(data.toString()).queue();

                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("発生時刻", api.getTime(), false);
                for (Map.Entry<EarthquakeScaleType, List<P2PEarthquakeAPI.Point>> entry : api.getScaleMap().entrySet()) {
                    EarthquakeScaleType scaleType = entry.getKey();
                    List<String> pointAddr = entry.getValue().stream().map(P2PEarthquakeAPI.Point::getAddr).toList();
                    String pointText = String.join(",", pointAddr);
                    embed.addField(scaleType.getName(), pointText, true);
                }
                channel.sendMessageEmbeds(embed.build()).queue();
                return null;
            }
        });

        P2PEEWAPI.startWebSockAPI(new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                webSocket.request(10);
                System.out.println("緊急地震速報のAPIに接続されました。");
            }
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                webSocket.request(5);
                logger.info("緊急地震速報APIからの受信データ: " + data.toString());
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

                return null;
            }
        });
    }

}
