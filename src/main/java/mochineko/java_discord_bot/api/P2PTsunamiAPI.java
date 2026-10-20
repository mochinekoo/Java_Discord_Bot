package mochineko.java_discord_bot.api;

import com.google.gson.Gson;
import mochineko.java_discord_bot.status.TsunamiGrade;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P2PTsunamiAPI {

    private final static Gson gson = new Gson();

    private Area[] areas;
    private String time;

    public static P2PTsunamiAPI[] getAPI() {
        try {
            URL url = new URL("https://api.p2pquake.net/v2/history?codes=552");
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
            return  gson.fromJson(content.toString(), P2PTsunamiAPI[].class);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return null;
    }

    public Area[] getArea() {
        return areas;
    }

    public Map<TsunamiGrade, List<Area>> getGradeMap() {
        Map<TsunamiGrade, List<Area>> gradeMap = new HashMap<>();
        for (Area area : areas) {
            if (!gradeMap.containsKey(area.getGradeType())) {
                gradeMap.put(area.getGradeType(), new ArrayList<>());
            }
            gradeMap.get(area.getGradeType()).add(area);
        }
        return gradeMap;
    }

    public String getTime() {
        return time;
    }

    public LocalDateTime getConvertTime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return LocalDateTime.parse(time, format);
    }

    public static class Area {
        private String name;
        private String grade;
        private boolean immediate;
        private FirstHeight firstHeight;
        private MaxHeight maxHeight;

        public String getName() {
            return name;
        }

        public String getGrade() {
            return grade;
        }

        public TsunamiGrade getGradeType() {
            return TsunamiGrade.convertP2PAPI(grade);
        }

        public boolean isImmediate() {
            return immediate;
        }

        public FirstHeight getFirstHeight() {
            return firstHeight;
        }

        public MaxHeight getMaxHeight() {
            return maxHeight;
        }

        public static class FirstHeight {
            private String arrivalTime;
            private String condition;

            /**
             * 第一波の到達予想時刻を返す関数
             */
            public String getArrivalTime() {
                return arrivalTime;
            }

            /**
             * 津波の状態を返す関数
             * @return 「ただちに津波来襲と予測」「津波到達中と推測」「第一波の到達を確認」のいずれかを返す
             */
            public String getCondition() {
                return condition;
            }
        }

        public static class MaxHeight {
            private String description;
            private int number;

            /**
             * 予想される津波の高さを文字として返す
             * @return 「巨大」「高い」「10m超」「10m」「5m」「3m」「1m」「0.2m未満」のいずれかを返す
             */
            public String getDescription() {
                return description;
            }

            /**
             * 予想される津波の高さを数値として返す
             * @return
             */
            public int getNumber() {
                return number;
            }
        }
    }
}
