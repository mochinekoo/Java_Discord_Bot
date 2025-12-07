package mochineko.java_discord_bot.manager.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import mochineko.java_discord_bot.Main;
import mochineko.java_discord_bot.status.FileType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class JsonManager {

    private static Map<FileType, JsonManager> memory_json_map = new HashMap<>();

    private JsonElement raw_element;
    private final FileType type;
    private final Gson gson;

    public JsonManager(FileType type) {
        this.type = type;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * メモリー上からJSONを取得する
     * @return {@link JsonElement}として返す
     */
    public JsonElement getRawElement() {
        createJson();
        if (raw_element == null) updateJson();
        return raw_element;
    }

    /**
     *　デシリアライズしたJSONとして返す
     */
    public DeserializedJson getDeserializedJson() {
        return gson.fromJson(getRawElement(), type.getDeserializedClass());
    }

    /**
     * ファイルからJsonを直接取得する
     * @return {@link JsonElement}として返す
     */
    @Deprecated
    public JsonElement getFileRawElement() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(getFile().toPath()), StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, JsonElement.class);
        }
    }

    /**
     * @return 現在のJSONがどこにあるかを、ファイルとして返す
     */
    public File getFile() {
        return new File(type.getName());
    }

    /**
     * メモリー上のJSONを更新する。
     * @apiNote ファイルの方が大きい場合、メモリー上のJSONはファイルのJSONに置き換えられる
     */
    public void updateJson() {
        try {
            if (!getFile().exists()) createJson(); //無い場合は作成
            if (raw_element == null) {
                raw_element = getFileRawElement();
            }
            memory_json_map.put(type, this);

            try (BufferedWriter write = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(getFile().toPath()), StandardCharsets.UTF_8))) {
                write.write(gson.toJson(raw_element));
                Main.logger.info(getFile().getName() + "の更新に成功しました。");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JSONを作成する関数。
     * @return ファイルの作成に成功した場合 もしくは 既にファイルが存在する場合 は trueを返し、失敗した場合は false を返す。
     */
    public boolean createJson() {
        boolean status = false;
        if (!getFile().exists()) {
            try (InputStream in = Main.class.getResourceAsStream("/" + type.getName())) {
                Files.copy(in, getFile().toPath());
                updateJson();
                status = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            status = true;
        }
        return status;
    }
}