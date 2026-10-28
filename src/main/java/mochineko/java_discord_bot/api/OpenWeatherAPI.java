package mochineko.java_discord_bot.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import mochineko.java_discord_bot.manager.json.ConfigJson;
import mochineko.java_discord_bot.manager.json.JsonManager;
import mochineko.java_discord_bot.status.FileType;
import mochineko.java_discord_bot.util.NetworkUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class OpenWeatherAPI {

    private static final Gson gson = new Gson();
    private static final double KELVIN = 273.15;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @SerializedName("list")
    List<WeatherList> weatherList;

    private OpenWeatherAPI() {

    }

    public static OpenWeatherAPI getInstance(double lat, double lon) {
        ConfigJson configJson = (ConfigJson) new JsonManager(FileType.CONFIG).getDeserializedJson();
        JsonObject raw_json = NetworkUtil.getJson("https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + configJson.getWebAPISetting().getOpenWeatherAPI().getAPIKey() + "&lang=ja").getAsJsonObject();
        return gson.fromJson(raw_json, OpenWeatherAPI.class);
    }

    /**
     * 時間ごとの予報を取得する関数
     * @return {@link  List<WeatherList>}で返します
     */
    public List<WeatherList> getWeatherList() {
        return weatherList;
    }

    public static class WeatherList {
        Main main;
        List<Weather> weather;
        String dt_txt;

        public Main getMain() {
            return main;
        }

        /**
         * 天気を取得する関数
         * @return {@link List<Weather>} で返します
         */
        public List<Weather> getWeather() {
            return weather;
        }

        public String getStringDate(boolean raw_data) {
            if (raw_data) {
                return dt_txt;
            }
            else {
                SimpleDateFormat raw_format = new SimpleDateFormat(DATE_FORMAT);
                SimpleDateFormat convert_format = new SimpleDateFormat("MM/dd");
                try {
                    return convert_format.format(raw_format.parse(dt_txt));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public LocalDateTime getLocalDateTime() {
            SimpleDateFormat raw_format = new SimpleDateFormat(DATE_FORMAT);
            try {
                Date date = raw_format.parse(dt_txt);
                return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public static class Main {
            private float temp_min;
            private float temp_max;

            /**
             * 最小気温を返します
             * @param raw_data ケルビン（K）として返す場合はtrue、摂氏（C）として返す場合はfalse
             * @return doubleで最小気温を返します
             */
            public double getTempMin(boolean raw_data) {
                if (raw_data) {
                    return temp_min;
                }
                else {
                    return (new BigDecimal(temp_min)
                            .subtract(new BigDecimal(KELVIN))
                            .setScale(2, RoundingMode.HALF_DOWN)
                            .doubleValue());
                }
            }

            /**
             * 最高気温を返します
             * @param raw_data ケルビン（K）として返す場合はtrue、摂氏（C）として返す場合はfalse
             * @return doubleで最高気温を返します
             */
            public double getTempMax(boolean raw_data) {
                if (raw_data) {
                    return temp_max;
                }
                else {
                    return (new BigDecimal(temp_max)
                            .subtract(new BigDecimal(KELVIN))
                            .setScale(2, RoundingMode.HALF_DOWN)
                            .doubleValue());
                }
            }
        }

        public static class Weather {
            String main;
            String description;

            /**
             * 天気を返す関数
             * @return 文字列として返します（例：Clouds　など）
             */
            public String getMain() {
                return main;
            }

            /**
             * 天気の説明を返す関数
             * @return 文字列として返します（例：曇りがち、厚い雲　など）
             */
            public String getDescription() {
                return description;
            }
        }
    }

}
