package mochineko.java_discord_bot.manager.json;

import javax.xml.crypto.Data;

public class ConfigJson implements DeserializedJson {

    private Bot bot;
    private DataBase database;

    public Bot getBotSetting() {
        return bot;
    }

    public DataBase getDatabaseSetting() {
        return database;
    }

    public static class Bot {
        private String token;

        public String getToken() {
            return token;
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
