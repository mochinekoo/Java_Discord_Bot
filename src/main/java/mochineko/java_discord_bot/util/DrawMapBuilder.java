package mochineko.java_discord_bot.util;

import mochineko.java_discord_bot.status.EarthquakeScaleType;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DrawMapBuilder {

    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final FilterFactory FILTER_FACTORY = CommonFactoryFinder.getFilterFactory();

    public static final File EEW_MAP_FILE = new File(".\\src\\main\\resources\\GIS\\AreaForecastLocalE_GIS\\地震情報_細分区域.shp");
    public static final File PREF_MAP_FILE = new File(".\\src\\main\\resources\\GIS\\AreaForecastLocalM_prefecture_GIS\\府県予報区等.shp");
    public static final File TSUNAMI_MAP_FILE = new File(".\\src\\main\\resources\\GIS\\AreaTsunami_GIS\\津波予報区.shp");
    public static final File CITY_MAP_FILE = new File(".\\src\\main\\resources\\GIS\\AreaInformationCity_quake_GIS\\市町村等.shp");

    private static SimpleFeatureSource EEW_SOURCE;
    private static SimpleFeatureSource PREF_SOURCE;
    private static SimpleFeatureSource CITY_SOURCE;

    public static void Init() {
        try {
            EEW_SOURCE = MapBuilder.getFileFeatureSource(EEW_MAP_FILE);
            PREF_SOURCE = MapBuilder.getFileFeatureSource(PREF_MAP_FILE);
            CITY_SOURCE = MapBuilder.getFileFeatureSource(CITY_MAP_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MapBuilder createMap(MapBuilder mapBuilder, List<Filter> drawAreaList, Map<Color, List<Filter>> drawColorMap) throws IOException {
        var mapSource = mapBuilder.getMapFeatureSource();

        if (drawColorMap != null) {
            for (var drawColorEntry : drawColorMap.entrySet()) {
                var color = drawColorEntry.getKey();
                Filter filter = FILTER_FACTORY.or(drawColorEntry.getValue());
                mapBuilder.addFill(color, 1f, filter);
            }
        }

        if (drawAreaList != null) {
            List<ReferencedEnvelope> envelopeList = new ArrayList<>();
            for (Filter filter : drawAreaList) {
                ReferencedEnvelope area = MapBuilder.getAreaEnvelope(mapSource, filter);
                envelopeList.add(area);
            }
            mapBuilder.setDrawArea(envelopeList); //描画範囲を決定する
        }

        var buildImage = mapBuilder.build();
        var image = mapBuilder.getImage();
        var graphics = image.getGraphics();

        graphics.drawImage(buildImage, 0, 0, null);
        MapBuilder.addCredit(image);

        File file = new File("eew1.png");
        ImageIO.write(buildImage, "png", file);
        return mapBuilder;
    }

    public static MapBuilder createEEWMap(List<Filter> drawAreaList, Map<Color, List<Filter>> drawColorMap) throws IOException {
        var eewMapBuilder = new MapBuilder(EEW_SOURCE, MAP_WIDTH, MAP_HEIGHT);
        return createMap(eewMapBuilder, drawAreaList, drawColorMap);
    }

    public static BufferedImage createCityMap(List<Filter> drawAreaList, Map<Color, List<Filter>> drawColorMap, float latitude, float longitude) throws IOException {
        var cityMapBuilder = new MapBuilder(CITY_SOURCE, MAP_WIDTH, MAP_HEIGHT);
        var prefMapBuilder = new MapBuilder(PREF_SOURCE, MAP_WIDTH, MAP_HEIGHT);
        var prefMapSource = prefMapBuilder.getMapFeatureSource();

        Filter filter = FILTER_FACTORY.or(drawAreaList);
        ReferencedEnvelope envelope = MapBuilder.getAreaEnvelope(prefMapSource, filter);
        envelope.expandToInclude(longitude, latitude);
        envelope.expandBy(envelope.getWidth() * 0.1, envelope.getHeight() * 0.1);
        prefMapBuilder.setDrawArea(Arrays.asList(envelope));
        prefMapBuilder.addFill(Color.WHITE, 1.0f, null);
        prefMapBuilder.addStroke(Color.BLACK, 0.5f, null);

        var prefImage = prefMapBuilder.build();
        var cityImage = createMap(cityMapBuilder, drawAreaList, drawColorMap)
                .setDrawArea(Arrays.asList(envelope))
                .addImage(new File("point.png"), latitude, longitude)
                .build();
        prefImage.getGraphics().drawImage(cityImage, 0, 0, null);

        return prefImage;
    }

}
