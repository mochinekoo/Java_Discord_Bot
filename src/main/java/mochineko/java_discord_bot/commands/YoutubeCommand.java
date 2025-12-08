package mochineko.java_discord_bot.commands;

import mochineko.java_discord_bot.api.YoutubeDataAPI;
import mochineko.java_discord_bot.manager.SQLManager;
import mochineko.java_discord_bot.manager.music.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.Date;
import java.util.List;

public class YoutubeCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        MessageChannelUnion channel = event.getChannel();

        //api
        YoutubeDataAPI api = YoutubeDataAPI.getInstance();
        List<YoutubeDataAPI.Items> itemList = api.getItems();
        YoutubeDataAPI.Items item = itemList.get(0);
        YoutubeDataAPI.Items.Snippet snippet = item.getSnippet();

        if (cmd.equalsIgnoreCase("youtube_search")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.addField("配信タイトル", snippet.getTitle(), true);
            embed.addField("時間", snippet.getPublishedAt(), true);
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

}
