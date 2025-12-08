package mochineko.java_discord_bot.manager.json;

import javax.xml.crypto.Data;

public class ConfigJson implements DeserializedJson {

    private Bot bot;
    private DataBase database;
    private WebAPI api;

    public Bot getBotSetting() {
        return bot;
    }

    public DataBase getDatabaseSetting() {
        return database;
    }

    public WebAPI getWebAPISetting() {
        return api;
    }

    public static class Bot {
        private String token;

        public String getToken() {
            return token;
        }
    }

    public static class WebAPI {
        private YoutubeDataAPI youtube_data_api;

        public WebAPI.YoutubeDataAPI getYoutubeDataAPISetting() {
            return youtube_data_api;
        }

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
