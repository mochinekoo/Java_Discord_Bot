package mochineko.java_discord_bot.util;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DrawMapBuilder {

    public static final File EEW_MAP_FILE = new File(".\\src\\main\\resources\\GIS\\AreaForecastLocalM_prefecture_GIS\\府県予報区等.shp");
    public static final File TSUNAMI_MAP_FILE = new File(".\\src\\main\\resources\\GIS\\AreaTsunami_GIS\\津波予報区.shp");
    public static final File CITY_MAP_FILE = new File(".\\src\\main\\resources\\GIS\\AreaInformationCity_quake_GIS\\市町村等.shp");

    private static SimpleFeatureSource EEW_SOURCE;
    private static SimpleFeatureSource CITY_SOURCE;

    public static void Init() {
        try {
            EEW_SOURCE  = MapBuilder.getFileFeatureSource(EEW_MAP_FILE);
            CITY_SOURCE = MapBuilder.getFileFeatureSource(CITY_MAP_FILE);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MapBuilder createCityMap(List<Filter> draw_areaList, Map<Color, List<Filter>> draw_colormap, Color fill, Color stroke) throws IOException {
        //府県予報区
        var eewMap  = new MapBuilder(EEW_SOURCE, 800, 600);
        var cityMap = new MapBuilder(CITY_SOURCE, 800, 600);
        //cityMap.addStroke(Color.WHITE, 0.0f, null);

        if (fill != null) {
            if (draw_colormap == null) {
                cityMap.addFill(fill, 1f, null);
            }
            else {
                FilterFactory filter_factory = CommonFactoryFinder.getFilterFactory();
                for (Map.Entry<Color, List<Filter>> entry : draw_colormap.entrySet()) {
                    Color color = entry.getKey();
                    Filter orFilter = filter_factory.or(entry.getValue());
                    cityMap.addFill(color, 1f, orFilter);
                }
            }
        }

        List<ReferencedEnvelope> envelope_list = new ArrayList<>();
        if (draw_areaList != null) {
            for (Filter filter : draw_areaList) {
                ReferencedEnvelope area = MapBuilder.getAreaEnvelope(eewMap.getMapFeatureSource(), filter); //都道府県ごとのReferencedEnvelopeを取得する
                envelope_list.add(area); //都道府県ごとのReferencedEnvelopeを追加
                eewMap.addFill(Color.WHITE, 1.0f, filter);
                //eewMap.addStroke(Color.BLACK, 1f, filter);
            }
            cityMap.setDrawArea(envelope_list); //描画範囲を決定する
        }

        var im = cityMap.getImage();
        var gra = im.getGraphics();
        var eewIm = eewMap.build();
        File file = new File("tsunamiaa.png");
        ImageIO.write(eewIm, "png", file);
        gra.drawImage(eewIm, 0, 0, null);
        addCredit(im);

        return cityMap;
    }

    public static MapBuilder getPrefectureMap(List<Filter> draw_areaList, Map<Color, List<Filter>> draw_colormap, Color fill, Color stroke) throws IOException {
        //府県予報区
        MapBuilder eew_map = new MapBuilder(EEW_MAP_FILE, 800, 600);
        if (fill != null) {
            if (draw_colormap == null) {
                eew_map.addFill(fill, 1f, null);
            }
            else {
                for (Map.Entry<Color, List<Filter>> entry : draw_colormap.entrySet()) {
                    Color color = entry.getKey();
                    for (Filter filter : entry.getValue()) {
                        eew_map.addFill(color, 1f, filter);
                    }
                }
            }
        }
        if (stroke != null) eew_map.addStroke(stroke, 2f, null);
        List<ReferencedEnvelope> envelope_list = new ArrayList<>();
        if (draw_areaList != null) {
            for (Filter filter : draw_areaList) {
                ReferencedEnvelope area = MapBuilder.getAreaEnvelope(eew_map.getMapFeatureSource(), filter); //都道府県ごとのReferencedEnvelopeを取得する
                envelope_list.add(area); //都道府県ごとのReferencedEnvelopeを追加
            }
            eew_map.setDrawArea(envelope_list); //描画範囲を決定する
        }
        System.out.println(envelope_list.toString());

        return eew_map;
    }

    public static BufferedImage createCustomTsunamiMap(List<Filter> draw_areaList, Map<Color, List<Filter>> draw_colormap) throws IOException {
        MapBuilder prefecture_map = getPrefectureMap(draw_areaList, null, Color.LIGHT_GRAY, null); //府県予報区
        BufferedImage prefecture_image = prefecture_map.getImage();

        //津波予報区
        MapBuilder tsunami_map = new MapBuilder(TSUNAMI_MAP_FILE, prefecture_image.getWidth(), prefecture_image.getHeight())
                .addStroke(Color.LIGHT_GRAY, 3f, null); //全部に色を塗る
        for (Map.Entry<Color, List<Filter>> entry : draw_colormap.entrySet()) {
            Color color = entry.getKey(); //色
            List<Filter> values = entry.getValue(); //津波のタイプごとのフィルダーリスト（塗りたい場所）
            for (Filter filter : values) {
                tsunami_map.addStroke(color, 2f, filter); //津波のタイプごとに色を塗る
            }
        }

        List<Layer> layers = tsunami_map.getMap().layers(); //津波GISから、レイヤーリストを取得する
        for (int n = 1; n < layers.size(); n++) {
            prefecture_map.getMap().addLayer(layers.get(n)); //府県予報区等GISに、津波GISレイヤーを追加する（ふち取りがある状態に）
        }

        BufferedImage buildImage = prefecture_map.build();
        Graphics2D graphics = (Graphics2D) buildImage.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //アンチエイリアシング
        addCredit(buildImage);
        return buildImage;
    }

    public static void addCredit(BufferedImage bufferedImage) {
        final float STROKE_SIZE = 3.0f;
        final int TEXT_SIZE = 30;
        String creditText = "※気象庁「市町村等（地震津波関係）」「府県予報区」を\n加工して利用しています。";

        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        Font font = new Font("SansSerif", Font.PLAIN, TEXT_SIZE);
        GlyphVector glyphVector = font.createGlyphVector(graphics.getFontRenderContext(), creditText);
        Shape outlineShape = glyphVector.getOutline(bufferedImage.getWidth() - (TEXT_SIZE * creditText.length()), 50);
        graphics.setColor(Color.BLACK); //縁は黒色
        graphics.setStroke(new BasicStroke(STROKE_SIZE)); //ふち取りのサイズを決定する
        graphics.draw(outlineShape); //ふち取りのみを描画する
        graphics.setColor(Color.WHITE); //文字の色は白
        graphics.fill(outlineShape); //文字を埋める
    }

}
