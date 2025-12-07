package mochineko.java_discord_bot.commands;

import mochineko.java_discord_bot.manager.SQLManager;
import mochineko.java_discord_bot.manager.music.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.Date;

public class UserCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        MessageChannelUnion channel = event.getChannel();
        MusicManager musicManager = MusicManager.getInstance();
        OptionMapping option = event.getOption("database");
        SQLManager sqlManager = new SQLManager();
        if (cmd.equalsIgnoreCase("user_info")) {
            if (option.getAsString().equalsIgnoreCase("database")) {
                sqlManager.addName(event.getMember());
                Object[] get = sqlManager.getName(event.getUser().getName());
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("名前", get[0].toString(), true);
                embed.addField("id", String.valueOf(get[1]), true);
                embed.addField("joinDate", ((Date)get[2]).toLocalDate().toString(), true);
                channel.sendMessageEmbeds(embed.build()).queue();

            }
        }
    }
}
