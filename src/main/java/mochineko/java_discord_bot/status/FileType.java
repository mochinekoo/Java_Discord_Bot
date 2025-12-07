package mochineko.java_discord_bot.status;

import mochineko.java_discord_bot.manager.json.ConfigJson;
import mochineko.java_discord_bot.manager.json.DeserializedJson;

public enum FileType {
    CONFIG("config.json", ConfigJson.class);

    private final String name;
    private final Class<? extends DeserializedJson> deserialized_class;

    FileType(String name, Class<? extends DeserializedJson> deserialized_class) {
        this.name = name;
        this.deserialized_class = deserialized_class;
    }

    public String getName() {
        return name;
    }

    public Class<? extends DeserializedJson> getDeserializedClass() {
        return deserialized_class;
    }
}
