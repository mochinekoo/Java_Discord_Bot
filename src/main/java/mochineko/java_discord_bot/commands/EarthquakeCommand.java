package mochineko.java_discord_bot.commands;

import mochineko.java_discord_bot.api.P2PEEWAPI;
import mochineko.java_discord_bot.api.P2PEarthquakeAPI;
import mochineko.java_discord_bot.api.P2PTsunamiAPI;
import mochineko.java_discord_bot.status.EarthquakeScaleType;
import mochineko.java_discord_bot.status.TsunamiGrade;
import mochineko.java_discord_bot.util.DrawMapBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.PropertyName;
import org.geotools.factory.CommonFactoryFinder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class EarthquakeCommand extends ListenerAdapter {

    private static final FilterFactory filter_factory = CommonFactoryFinder.getFilterFactory();

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

                new Thread(() -> {
                    long t = System.nanoTime();
                    PropertyName nameProperty = filter_factory.property("regionname".trim());
                    Set<String> prefSet = new LinkedHashSet<>();
                    try {
                        P2PEarthquakeAPI api = P2PEarthquakeAPI.GetAPI()[2];
                        P2PEarthquakeAPI.Earthquake earthquake1 = api.getEarthquake();
                        var hy = earthquake1.getHypocenter();
                        Map<Color, List<Filter>> drawColorMap = new HashMap<>();
                        for (var entry : api.getScaleMap().entrySet()) {
                            EarthquakeScaleType scaleType = entry.getKey();

                            if (!drawColorMap.containsKey(scaleType.getColor())) {
                                drawColorMap.put(scaleType.getColor(), new ArrayList<>());
                            }

                            for (P2PEarthquakeAPI.Point point : entry.getValue()) {
                                var colorFilter = drawColorMap.get(scaleType.getColor());
                                String pref = point.getPref();
                                String addr = point.getAddr();
                                String addrSplit = "";
                                if (addr.contains("市")) {
                                    addrSplit = addr.substring(0, addr.indexOf("市") + 1);
                                }
                                else if (addr.contains("町")) {
                                    addrSplit = addr.substring(0, addr.indexOf("町") + 1);
                                }
                                else if (addr.contains("村")) {
                                    addrSplit = addr.substring(0, addr.indexOf("村") + 1);
                                }
                                else if (addr.contains("区")) {
                                    addrSplit = addr.substring(0, addr.indexOf("区") + 1);
                                }
                                String format = (pref + addrSplit).trim();
                                prefSet.add(pref.trim());

                                System.out.println(pref + "\n" + addr + "\n" + addrSplit + "\n" + format);

                                colorFilter.add(filter_factory.equals(nameProperty, filter_factory.literal(format)));
                            }
                        }

                        List<Filter> filterList = prefSet.stream()
                                .<Filter>map(p -> filter_factory.equals(filter_factory.property("name"), filter_factory.literal(p)))
                                .toList();
                        var map = DrawMapBuilder.createCityMap(filterList, drawColorMap, hy.getLatitude(), hy.getLongitude());
                        //BufferedImage image = map.build();
                        File file = new File("tsunami.png");
                        ImageIO.write(map, "png", file);

                        //ByteArrayOutputStream os = new ByteArrayOutputStream();
                        //ImageIO.write(image, "png", os);
                        //InputStream is = new ByteArrayInputStream(os.toByteArray());
                        channel.sendFiles(FileUpload.fromData(file)).queue();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
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
                event.replyEmbeds(embed.build()).queue();

                new Thread(() -> {
                    PropertyName nameProperty = filter_factory.property("name".trim());
                    List<Filter> prefSet = new ArrayList<>();
                    try {
                        P2PEEWAPI api = P2PEEWAPI.getAPI()[1];
                        var earthquake1 = api.getEarthquake();
                        //var hy = earthquake1.getHypocenter();
                        Map<Color, List<Filter>> drawColorMap = new HashMap<>();
                        for (var area : api.getAreas()) {
                            var name = area.getName();
                            var fromScale = area.getScaleFrom();
                            var toScale = area.getScaleTo();
                            var fromConvertScale = EarthquakeScaleType.convertP2PAPI(fromScale);
                            var toConvertScale = EarthquakeScaleType.convertP2PAPI(toScale);

                            if (!drawColorMap.containsKey(fromConvertScale.getColor())) {
                                drawColorMap.put(fromConvertScale.getColor(), new ArrayList<>());
                            }
                            var drawFilter = drawColorMap.get(fromConvertScale.getColor());

                            drawFilter.add(filter_factory.equals(nameProperty, filter_factory.literal(name)));
                            prefSet.add(filter_factory.equals(nameProperty, filter_factory.literal(name)));
                        }

                        var map = DrawMapBuilder.createEEWMap(prefSet, drawColorMap);
                        //map.addImage(new File("point.png"), hy.getLatitude(), hy.getLongitude());
                        BufferedImage image = map.build();
                        File file = new File("tsunami.png");
                        ImageIO.write(image, "png", file);

                        //ByteArrayOutputStream os = new ByteArrayOutputStream();
                        //ImageIO.write(image, "png", os);
                        //InputStream is = new ByteArrayInputStream(os.toByteArray());
                        channel.sendFiles(FileUpload.fromData(file)).queue();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

            }
        }
    }

}
