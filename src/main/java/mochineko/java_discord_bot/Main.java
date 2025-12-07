package mochineko.java_discord_bot;

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

    public static void main(String[] strings) throws InterruptedException, IOException, SQLException {
        if (jda == null) {
            jda = JDABuilder.createDefault("")
                    .addEventListeners(new Main())
                    .build();
            jda.awaitReady();

            jda.updateCommands()
                    .queue();
        }
    }

    public static JDA getJDA() {
        return jda;
    }

}
