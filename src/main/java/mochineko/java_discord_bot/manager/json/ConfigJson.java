package mochineko.java_discord_bot.manager.json;

public class ConfigJson implements DeserializedJson {

    private Bot bot;

    public Bot getBotSetting() {
        return bot;
    }

    public static class Bot {
        private String token;

        public String getToken() {
            return token;
        }
    }
}
