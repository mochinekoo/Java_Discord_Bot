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
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class P2PEEWAPI {

    public static final String API_URL = "https://api.p2pquake.net/v2/history?codes=556";
    private final static Gson gson = new Gson();

    private Area[] areas;
    private Earthquake earthquake;

    public static P2PEEWAPI[] getAPI() {
        try {
            URL url = new URL(API_URL);
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
            return  gson.fromJson(content.toString(), P2PEEWAPI[].class);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return null;
    }

    public static void startWebSockAPI(WebSocket.Listener listener) {
        try (HttpClient httpClient = HttpClient.newHttpClient();){
            WebSocket.Builder wsb = httpClient.newWebSocketBuilder();

            /*
            new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    webSocket.request(10);
                }
                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    System.out.println("onText");
                    System.out.println(data);
                    webSocket.request(1);
                    return null;
                }
            };
             */

            CompletableFuture<WebSocket> comp = wsb.buildAsync(URI.create("wss://api.p2pquake.net/v2/ws?codes=556"), listener);
            WebSocket webSocket = comp.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public Area[] getAreas() {
        return areas;
    }

    @Nullable
    public Earthquake getEarthquake() {
        return earthquake;
    }

    /**
     * 緊急地震速報のエリアのクラス
     */
    public static class Area {
        private String name;
        private String pref;
        private int scaleFrom;
        private int scaleTo;
        private String arrivalTime;

        /**
         * 地域名を返す関数
         * @return （例：岩手県内陸北部）
         */
        public String getName() {
            return name;
        }

        /**
         * 府県予報区を返す関数
         * @return （例：岩手）
         */
        public String getPref() {
            return pref;
        }

        /**
         * 予測震度の最小震度を、生のデータで返す関数。
         * @return （例：10）
         */
        public int getScaleFrom() {
            return scaleFrom;
        }

        /**
         * 予想震度の最小震度を、列挙型で返す関数。
         * @return （例：10の場合は、震度1({@link EarthquakeScaleType#ONE})を返す）
         */
        public EarthquakeScaleType getScaleType() {
            return EarthquakeScaleType.convertP2PAPI(scaleFrom);
        }

        /**
         * 予想震度の最大震度を、生のデータで返す関数。
         * @return （例：10）
         */
        public int getScaleTo() {
            return scaleTo;
        }

        /**
         * 主要動の到達予想時刻を返す関数。
         * @return yyyy/MM/dd HH:mm:ss で返す。（例：2026/01/11 13:15:00）
         */
        @Nullable
        public String getArrivalTime() {
            return arrivalTime;
        }
    }

    public static class Earthquake {
        private String arrivalTime;
        private Hypocenter hypocenter;
        private String originTime;

        /**
         * 地震の発現の時刻
         * @return
         */
        public String getArrivalTime() {
            return arrivalTime;
        }

        /**
         * 震源の情報を取得する関数
         */
        public Hypocenter getHypocenter() {
            return hypocenter;
        }

        /**
         * 地震の発生時刻を返す関数。
         * @return yyyy/MM/dd HH:mm:ss で返す。（例：2026/01/11 13:15:00）
         * @apiNote {@link #getOriginLocalDateTime()} でLocalDateTimeに変換されたものを取得できる。
         */
        public String getOriginTime() {
            return originTime;
        }

        /**
         * 地震の発生時刻を返す関数
         * @return LocalDateTimeで返す。
         */
        public LocalDateTime getOriginLocalDateTime() {
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            return LocalDateTime.parse(originTime, timeFormat);
        }

        /**
         * 震源の情報のクラス
         */
        public static class Hypocenter {
            private String name;
            private String reduceName;
            private int depth;
            private float latitude;
            private float longitude;
            private float magnitude;

            /**
             * 震央の名前を返す関数
             * @return （例：岩手県沿岸北部）
             */
            @Nullable
            public String getName() {
                return name;
            }

            /**
             * 震央の短い名前を返す関数
             * @return （例：岩手県）
             */
            @Nullable
            public String getReduceName() {
                return reduceName;
            }

            /**
             * 深さを返す関数
             * @return int型で返す
             */
            public int getDepth() {
                return depth;
            }

            /**
             * 緯度を返す関数
             * @return float型で返す
             */
            public float getLatitude() {
                return latitude;
            }

            /**
             * 経度を返す関数
             * @return float型で返す
             */
            public float getLongitude() {
                return longitude;
            }

            /**
             * マグニチュードを返す関数
             * @return float型で返す
             */
            public float getMagnitude() {
                return magnitude;
            }
        }
    }

}
