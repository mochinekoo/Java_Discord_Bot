package mochineko.java_discord_bot.commands;

import mochineko.java_discord_bot.api.OpenWeatherAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WeatherCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        MessageChannelUnion channel = event.getChannel();
        if (cmd.equalsIgnoreCase("weather")) {
            var embed = new EmbedBuilder();

            float lat = 35.68F;
            float lon = 139.69F;
            var api = OpenWeatherAPI.getInstance(lat, lon);
            var weatherList = api.getWeatherList();
            if (weatherList.isEmpty()) {
                embed.setTitle("エラー");
                embed.addField("", "天気情報を取得できませんでした", true);
            }
            else {
                var weatherFirst = weatherList.getFirst();
                var mainWeather = weatherFirst.getMain();
                var weatherInfo = weatherFirst.getWeather().getFirst();
                var time = weatherFirst.getStringDate(false);
                embed.setTitle(lat + "," + lon + "の" + time + "の天気");
                embed.addField("天気", weatherInfo.getDescription(), true);
                embed.addField("最高気温", mainWeather.getTempMax(false) + "℃", true);
                embed.addField("最小気温", mainWeather.getTempMin(false) + "℃", true);
            }

            event.replyEmbeds(embed.build()).queue();
        }
    }

}
