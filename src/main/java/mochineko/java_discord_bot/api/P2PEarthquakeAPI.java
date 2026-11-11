package mochineko.java_discord_bot.api;

import com.google.gson.Gson;
import mochineko.java_discord_bot.Main;
import mochineko.java_discord_bot.status.EarthquakeScaleType;
import mochineko.java_discord_bot.status.JmaEarthquakeCorrectType;
import mochineko.java_discord_bot.status.JmaEarthquakeIssueType;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

public class P2PEarthquakeAPI {

    private final static Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private Earthquake earthquake;
    private Issue issue;
    private Point[] points;
    private String time;
    private int code;

    @Nullable
    public static P2PEarthquakeAPI[] GetAPI() {
        try {
            URL url = new URL("https://api.p2pquake.net/v2/history?codes=551");
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            con.disconnect();
            return gson.fromJson(content.toString(), P2PEarthquakeAPI[].class);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public static void startWebSockAPI(WebSocket.Listener listener) {
        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create("wss://api.p2pquake.net/v2/ws?codes=551"), listener)
                .exceptionally(e -> {
                    Main.logger.warning("地震速報APIの接続に失敗しました。");
                    return null;
                });
    }

    @Nonnull
    public Earthquake getEarthquake() {
        return earthquake;
    }

    /**
     * 震度観測点の情報を返す関数
     * @return {@link Point} で返す。
     */
    @Nullable
    public Point[] getPoints() {
        return points;
    }

    /**
     * 震度ごとの観測地点のデータです。
     * @return {@link Map} で返します。
     */
    @Nullable
    public Map<EarthquakeScaleType, List<Point>> getScaleMap() {
        Map<EarthquakeScaleType, List<Point>> map = new TreeMap<>(Comparator.comparingInt(EarthquakeScaleType::getP2PRawData));
        for (Point point : points) {
            if (!map.containsKey(point.getScaleType())) {
                map.put(point.getScaleType(), new ArrayList<>());
            }
            List<Point> list = map.get(point.getScaleType());
            list.add(point);
        }
        return map;
    }

    /**
     * 震度ごと+都道府県ごとのデータです。
     * @return {@link Map} で返します。
     */
    @Nullable
    public Map<EarthquakeScaleType, Map<String, List<Point>>> getScalePrefMap() {
        Map<EarthquakeScaleType, Map<String, List<Point>>> map = new TreeMap<>(Comparator.comparingInt(EarthquakeScaleType::getP2PRawData).reversed());

        var scaleMap = getScaleMap();
        for (var entry : scaleMap.entrySet()) {
            var scale = entry.getKey();
            if (!map.containsKey(scale)) {
                map.put(scale, new TreeMap<>());
            }
            var pointList = entry.getValue();
            for (var point : pointList) {
                var prefMapData = map.get(scale);
                var pref = point.getPref();
                if (!prefMapData.containsKey(pref)) {
                    prefMapData.put(pref, new ArrayList<>());
                }
                var prefPointData = prefMapData.get(pref);
                prefPointData.add(point);
            }
        }
        return map;
    }

    /**
     * 震度ごと+都道府県ごとのデータを、Discordの形式（Embed）として返す関数
     * @return
     */
    @Nullable
    public static List<EmbedBuilder> createScalePrefMapEmbed(P2PEarthquakeAPI api, String title) {
        List<EmbedBuilder> embedList = new ArrayList<>();

        EmbedBuilder embed = new EmbedBuilder();

        if (title != null) {
            embed.setTitle(title);
        }
        embed.addField("発生時刻", api.earthquake.getTime(), false);

        for (var scalePrefEntry : api.getScalePrefMap().entrySet()) {
            EarthquakeScaleType scaleType = scalePrefEntry.getKey();
            var value = scalePrefEntry.getValue();

            StringBuilder builder = new StringBuilder();
            for (var prefPointEntry : value.entrySet()) {
                var pref = prefPointEntry.getKey();
                builder.append("<" + pref + ">");
                var pointList = prefPointEntry.getValue();
                List<String> pointAddr = pointList.stream().map(P2PEarthquakeAPI.Point::getAddr).toList();  // 観測地点だけに

                for (String addr : pointAddr) {
                    builder.append(addr).append(",");

                    if (builder.length() >= 1024) {
                        Main.logger.info(String.valueOf(builder.length()) + ":" + builder.toString());
                        builder.delete(builder.length() - 1 - addr.length(), builder.length());
                        Main.logger.info(String.valueOf(builder.length()));
                        embed.addField(scaleType.getName(), builder.toString(), true);
                        builder = new StringBuilder();
                        embedList.add(embed);
                        embed = new EmbedBuilder();
                        if (title != null) {
                            embed.setTitle(title + "(続き)");
                        }
                        builder.append("<" + pref +">" + addr).append(",");
                    }
                }

                builder.append("\n");
            }
            embed.addField(scaleType.getName(), builder.toString(), true);
        }

        embedList.add(embed);
        return embedList;
    }

    public String getTime() {
        return time;
    }
    public int getCode() { return code; }

    @Nonnull
    public Issue getIssue() { return issue; }

    public LocalDateTime getLocalDateTime() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy/MM/dd HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 2, 3, true)
                .toFormatter();
        return LocalDateTime.parse(time, formatter);
    }

    public static class Issue {
        private String source;
        private String time;
        private String type;
        private String correct;

        /**
         *　情報の発表元
         */
        @Nullable
        public String getSource() {
            return source;
        }

        /**
         * 情報の発表時間
         * @return {@link String} で返す
         */
        @Nonnull
        public String getRawTime() {
            return time;
        }

        /**
         * 発表の種類
         * @return {@link JmaEarthquakeIssueType} で返す。
         */
        @Nonnull
        public JmaEarthquakeIssueType getIssueType() {
            return JmaEarthquakeIssueType.convertP2PAPI(type);
        }

        /**
         * 訂正の有無
         * @return {@link JmaEarthquakeIssueType} で返す
         */
        public JmaEarthquakeCorrectType getCorrectType() {
            return JmaEarthquakeCorrectType.convertP2PAPI(correct);
        }
    }

    public static class Earthquake {
        private String domesticTsunami;
        private String foreignTsunami;
        private Hypocenter hypocenter;
        private int maxScale;
        private String time;

        /**
         * 国内への津波の有無を返す関数
         */
        @Nullable
        public String getDomesticTsunami() {
            return domesticTsunami;
        }

        /**
         * 海外での津波の有無を返す関数
         */
        @Nullable
        public String getForeignTsunami() {
            return foreignTsunami;
        }

        @Nullable
        public Hypocenter getHypocenter() {
            return hypocenter;
        }

        /**
         * 最大震度を返す関数。
         * @apiNote 震度情報が存在しない場合は、-1を返す。
         */
        @Nullable
        public int getMaxScale() {
            return maxScale;
        }

        /**
         * 最大震度を変換された形で返す関数
         */
        @Nullable
        public EarthquakeScaleType getMaxScaleType() {
            return EarthquakeScaleType.convertP2PAPI(maxScale);
        }

        /**
         * 地震の発生時刻
         */
        @Nullable
        public String getTime() {
            return time;
        }

        @Nullable
        public LocalDateTime getDateTime() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            return LocalDateTime.parse(time, formatter);
        }

        public static class Hypocenter {
            private int depth;
            private float latitude;
            private float longitude;
            private float magnitude;
            private String name;

            /**
             * 深さを返す関数
             * @return int型で返し、震源情報が存在しない場合は-1を返す。ごく浅いは0。
             */
            public int getDepth() {
                return depth;
            }

            /**
             * 緯度を返す関数
             * @return float型で返し、震源情報が存在しない場合は-200を返す
             */
            public float getLatitude() {
                return latitude;
            }

            /**
             * 経度を返す関数
             * @return float型で返し、震源情報が存在しない場合は-200を返す
             */
            public float getLongitude() {
                return longitude;
            }

            /**
             *　マグニチュードを返す関数
             * @return float型で返し、震源情報が存在しない場合は-1を返す
             */
            public float getMagnitude() {
                return magnitude;
            }

            /**
             * 震源名を返す関数
             */
            public String getName() {
                return name;
            }
        }
    }

    public static class Point {
        private String addr; //観測点名
        private String pref; //都道府県
        private int scale; //震度

        /**
         * 都道府県を返す関数
         * @return 文字列で返す（例：石川県）
         */
        public String getPref() {
            return pref;
        }

        /**
         *  観測点名を取得する関数
         * @return 文字列で返す（例：輪島市鳳至町）
         */
        public String getAddr() {
            return addr;
        }

        /**
         * 震度を生のデータで返す関数
         * @return intで返す
         */
        public int getScale() {
            return scale;
        }

        /**
         * 震度を変換されたデータで返す関数
         * @return {@link EarthquakeScaleType}　で返す
         */
        public EarthquakeScaleType getScaleType() {
            return EarthquakeScaleType.convertP2PAPI(scale);
        }

    }

}
