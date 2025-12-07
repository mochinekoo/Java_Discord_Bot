package mochineko.java_discord_bot;

import mochineko.java_discord_bot.commands.MusicCommand;
import mochineko.java_discord_bot.commands.UserCommand;
import mochineko.java_discord_bot.listener.ButtonListener;
import mochineko.java_discord_bot.manager.json.ConfigJson;
import mochineko.java_discord_bot.manager.json.DeserializedJson;
import mochineko.java_discord_bot.manager.json.JsonManager;
import mochineko.java_discord_bot.status.FileType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Main extends ListenerAdapter {

    public final static Logger logger = Logger.getLogger("java_discordBot");
    private static JDA jda;

    public static void main(String[] strings) {
        if (jda == null) {
            initBot();
        }
    }

    public static JDA getJDA() {
        return jda;
    }

    public static void initBot() {
        for (FileType fileType : FileType.values()) {
            new JsonManager(fileType).createJson();
        }

        ConfigJson configJson = (ConfigJson) new JsonManager(FileType.CONFIG).getDeserializedJson();
        jda = JDABuilder.createDefault(configJson.getBotSetting().getToken())
                .addEventListeners(new Main())
                .addEventListeners(new MusicCommand())
                .addEventListeners(new ButtonListener())
                .addEventListeners(new UserCommand())
                .build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        jda.updateCommands()
                .addCommands(Commands.slash("play", "音楽を再生するコマンド")
                        .addOption(OptionType.STRING, "url", "再生したいURL　もしくは　再生したいキーワード", true))
                .addCommands(Commands.slash("play-info", "再生情報を表示するコマンド"))
                .addCommands(Commands.slash("skip", "音楽をスキップするコマンド"))
                .addCommands(Commands.slash("volume", "音楽の音量を設定するコマンド")
                        .addOption(OptionType.INTEGER, "volume", "音量"))
                .addCommands(Commands.slash("select-track", "再生したいトラックを選ぶ")
                        .addOption(OptionType.INTEGER, "index", "再生したいトラック", true))
                .addCommands(Commands.slash("user_info", "aa")
                        .addOption(OptionType.STRING, "database", "aa"))
                .queue();
    }

}
