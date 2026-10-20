package mochineko.java_discord_bot.api;

import com.google.gson.Gson;
import mochineko.java_discord_bot.status.EarthquakeScaleType;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

public class P2PEarthquakeAPI {

    private final static Gson gson = new Gson();

    private Earthquake earthquake;
    private Point[] points;
    private String time;

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

    public Earthquake getEarthquake() {
        return earthquake;
    }

    public Point[] getPoints() {
        return points;
    }

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

    public String getTime() {
        return time;
    }

    public LocalDateTime getLocalDateTime() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy/MM/dd HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 2, 3, true)
                .toFormatter();
        return LocalDateTime.parse(time, formatter);
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
        public String getDomesticTsunami() {
            return domesticTsunami;
        }

        /**
         * 海外での津波の有無を返す関数
         */
        public String getForeignTsunami() {
            return foreignTsunami;
        }

        public Hypocenter getHypocenter() {
            return hypocenter;
        }

        /**
         * 最大震度を返す関数。
         * @apiNote 震度情報が存在しない場合は、-1を返す。
         */
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
        public String getTime() {
            return time;
        }

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
             */
            public int getDepth() {
                return depth;
            }

            /**
             * 緯度を返す関数
             */
            public float getLatitude() {
                return latitude;
            }

            /**
             * 経度を返す関数
             */
            public float getLongitude() {
                return longitude;
            }

            /**
             *　マグニチュードを返す関数
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
