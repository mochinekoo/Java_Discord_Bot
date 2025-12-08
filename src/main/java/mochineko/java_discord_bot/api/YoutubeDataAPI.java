package mochineko.java_discord_bot.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import mochineko.java_discord_bot.manager.json.ConfigJson;
import mochineko.java_discord_bot.manager.json.DeserializedJson;
import mochineko.java_discord_bot.manager.json.JsonManager;
import mochineko.java_discord_bot.status.FileType;
import mochineko.java_discord_bot.util.NetworkUtil;

import java.util.List;

public class YoutubeDataAPI {
    
    private static final String URL = "https://www.googleapis.com/youtube/v3/search" +
            "?key=%s" +
            "&part=snippet" +
            "&type=video" +
            "&eventType=live" +
            "&channelId=%s";

    private List<Items> items;

    public List<Items> getItems() {
        return items;
    }

    public static class Items {
        private Snippet snippet;

        public Snippet getSnippet() {
            return snippet;
        }

        public static class Snippet {
            private String publishedAt;
            private String title;

            public String getPublishedAt() {
                return publishedAt;
            }

            public String getTitle() {
                return title;
            }
        }
    }

    public static YoutubeDataAPI getInstance() {
        ConfigJson json = (ConfigJson) new JsonManager(FileType.CONFIG).getDeserializedJson();
        ConfigJson.WebAPI.YoutubeDataAPI youtubeAPI = json.getWebAPISetting().getYoutubeDataAPISetting();
        JsonElement youtubeJson = NetworkUtil.getJson(URL.formatted(youtubeAPI.getAPIKey(), youtubeAPI.getChannelID()));
        return new Gson().fromJson(youtubeJson, YoutubeDataAPI.class);
    }
}
