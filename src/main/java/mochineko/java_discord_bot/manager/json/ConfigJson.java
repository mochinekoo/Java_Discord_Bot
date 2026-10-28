package mochineko.java_discord_bot.manager.json;

import javax.xml.crypto.Data;

public class ConfigJson implements DeserializedJson {

    private Bot bot;
    private DataBase database;
    private WebAPI api;
    private Earthquake earthquake;

    public Bot getBotSetting() {
        return bot;
    }

    public DataBase getDatabaseSetting() {
        return database;
    }

    public WebAPI getWebAPISetting() {
        return api;
    }

    public Earthquake getEarthquake() {return  earthquake;}

    public static class Bot {
        private String token;

        public String getToken() {
            return token;
        }
    }

    public static class Earthquake {
        private String sendChannelID;

        public String getSendChannelID() {return sendChannelID;}
    }

    public static class WebAPI {
        private YoutubeDataAPI youtube_data_api;
        private OpenWeather openweather;

        public WebAPI.YoutubeDataAPI getYoutubeDataAPISetting() {
            return youtube_data_api;
        }

        public WebAPI.OpenWeather getOpenWeatherAPI() { return openweather;}

        public static class YoutubeDataAPI {
            private String apiKey;
            private String channelID;

            public String getAPIKey() {
                return apiKey;
            }

            public String getChannelID() {
                return channelID;
            }
        }

        public static class OpenWeather {
            private String apiKey;

            public String getAPIKey() {
                return apiKey;
            }
        }
    }

    public static class DataBase {
        private String host;
        private int port;
        private String db_name;
        private String user_id;
        private String user_password;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getDatabaseName() {
            return db_name;
        }

        public String getUserID() {
            return user_id;
        }

        public String getUserPassword() {
            return user_password;
        }
    }
}
