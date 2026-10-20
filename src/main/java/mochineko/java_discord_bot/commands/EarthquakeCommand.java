package mochineko.java_discord_bot.commands;

import mochineko.java_discord_bot.api.P2PEEWAPI;
import mochineko.java_discord_bot.api.P2PEarthquakeAPI;
import mochineko.java_discord_bot.api.P2PTsunamiAPI;
import mochineko.java_discord_bot.manager.music.MusicManager;
import mochineko.java_discord_bot.status.EarthquakeScaleType;
import mochineko.java_discord_bot.status.TsunamiGrade;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.*;
import java.util.List;

public class EarthquakeCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        MessageChannelUnion channel = event.getChannel();
        P2PEarthquakeAPI[] earthquakeAPIList = P2PEarthquakeAPI.GetAPI();

        if ("history_earthquake".equalsIgnoreCase(cmd)) {
            P2PEarthquakeAPI earthquakeAPI = earthquakeAPIList[0];
            EmbedBuilder embed = new EmbedBuilder();
            if (earthquakeAPI == null) {
                embed.setTitle("エラー");
                embed.addField("", "地震情報の取得に失敗しました", true);
                embed.setColor(Color.RED);
                channel.sendMessageEmbeds(embed.build()).queue();
            }
            else {
                P2PEarthquakeAPI.Earthquake earthquake = earthquakeAPI.getEarthquake();
                P2PEarthquakeAPI.Point[] pointList = earthquakeAPI.getPoints();
                var scalePointMap = earthquakeAPI.getScaleMap();

                embed.setTitle("過去の地震情報");
                embed.addField("発生時刻", earthquake.getTime(), false);
                for (Map.Entry<EarthquakeScaleType, List<P2PEarthquakeAPI.Point>> entry : scalePointMap.entrySet()) {
                    EarthquakeScaleType scaleType = entry.getKey();
                    List<String> pointAddr = entry.getValue().stream().map(P2PEarthquakeAPI.Point::getAddr).toList();
                    String pointText = String.join(",", pointAddr);
                    embed.addField(scaleType.getName(), pointText, true);
                }
                embed.setColor(Color.GREEN);
                event.replyEmbeds(embed.build()).queue();
            }

        }
        else if ("history_tsunami".equalsIgnoreCase(cmd)) {
            EmbedBuilder embed = new EmbedBuilder();
            P2PTsunamiAPI[] tsunamiAPIList = P2PTsunamiAPI.getAPI();
            P2PTsunamiAPI tsunamiAPI = tsunamiAPIList[0];
            if (tsunamiAPI == null) {
                embed.setTitle("エラー");
                embed.addField("", "津波情報の取得に失敗しました", true);
                embed.setColor(Color.RED);
                channel.sendMessageEmbeds(embed.build()).queue();
            }
            else {
                embed.setTitle("過去の津波情報");
                Map<TsunamiGrade, List<P2PTsunamiAPI.Area>> areaGradeMap = tsunamiAPI.getGradeMap();
                for (Map.Entry<TsunamiGrade, List<P2PTsunamiAPI.Area>> entry : areaGradeMap.entrySet()) {
                    TsunamiGrade grade = entry.getKey();
                    //List<P2PTsunamiAPI.Area> areaList = entry.getValue();
                    List<String> areaNameList = entry.getValue().stream().map(P2PTsunamiAPI.Area::getName).toList();
                    String areaNameText = String.join(",", areaNameList);
                    embed.addField(grade.getName(), areaNameText, true);
                }

                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }
        else if ("history_eew".equalsIgnoreCase(cmd)) {
            EmbedBuilder embed = new EmbedBuilder();
            P2PEEWAPI[] eewAPIList = P2PEEWAPI.getAPI();
            if (eewAPIList == null) {
                embed.setTitle("エラー");
                embed.addField("", "緊急地震速報の取得に失敗しました", true);
                embed.setColor(Color.RED);
                channel.sendMessageEmbeds(embed.build()).queue();
            }
            else {
                P2PEEWAPI eewapi = eewAPIList[1];
                var earthquake = eewapi.getEarthquake();
                if (earthquake != null) {
                    var earthquakeArrivalTime = earthquake.getArrivalTime();
                    var hypocenter = earthquake.getHypocenter();

                    embed.setTitle("過去の緊急地震速報");
                    embed.addField("地震発生時刻", earthquakeArrivalTime, true);
                    embed.addField("地震", hypocenter.getName() + "," + hypocenter.getDepth() + "," + hypocenter.getMagnitude(), true);
                }

                var areaList = eewapi.getAreas();
                for (int i = 0; i < 20; i++) {
                    var area = areaList[i];
                    String name = area.getName();
                    String arrivalTime = area.getArrivalTime();
                    int scaleFrom = area.getScaleFrom();
                    int scaleTo = area.getScaleTo();
                    embed.addField(name, arrivalTime + "," + scaleFrom + ":" + scaleTo, true);
                }
                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }

}
