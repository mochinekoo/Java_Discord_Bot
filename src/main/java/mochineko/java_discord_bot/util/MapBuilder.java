package mochineko.java_discord_bot.util;

import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.filter.Filter;
import org.geotools.api.style.*;
import org.geotools.api.style.Stroke;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.StyleBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MapBuilder {

    public record MapImage(BufferedImage image, double latitude, double longitude, int width, int height) {}

    //マップ情報
    private SimpleFeatureSource featureSource;
    private MapContent map;
    private SimpleFeatureCollection map_featureCollection;
    private ReferencedEnvelope envelope;

    //画像情報
    private final List<MapImage> mapImageList = new ArrayList<>();
    private final int width;
    private final int height;
    private BufferedImage image;
    private Graphics2D graphics;

    /**
     * @param file 使用したいマップファイル
     * @param width　描画したいマップ幅
     * @param height 描画したいマップ高さ
     */
    public MapBuilder(File file, int width, int height) {
        try {
            //マップ情報
            this.featureSource = getFileFeatureSource(file);
            this.map = new MapContent();
            this.map_featureCollection = featureSource.getFeatures();
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); //透明で描画する
            this.graphics = image.createGraphics();

            //画像情報
            this.width = width;
            this.height = height;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MapBuilder(SimpleFeatureSource source, int width, int height) {
        try {
            this.featureSource = source;
            this.map = new MapContent();
            this.map_featureCollection = source.getFeatures();
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            this.graphics = image.createGraphics();
            this.width = width;
            this.height = height;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MapBuilder addStroke(Color color, float thickness, Filter filter) {
        if (filter != null) {
            map.addLayer(new FeatureLayer(getMapFeatureCollection(filter), createLineStyle(color, thickness)));
        }
        else {
            map.addLayer(new FeatureLayer(featureSource, createLineStyle(color, thickness)));
        }
        return this;
    }

    public MapBuilder addFill(Color color, float opacity, Filter filter) {
        if (filter != null) {
            map.addLayer(new FeatureLayer(getMapFeatureCollection(filter), createStyle(null, 0f, color, opacity)));
        }
        else {
            map.addLayer(new FeatureLayer(featureSource, createStyle(null, 0f, color, opacity)));
        }
        return this;
    }

    public MapBuilder addImage(File file, float latitude, float longitude) {
        try {
            BufferedImage addImage = ImageIO.read(file);
            mapImageList.add(new MapImage(
               addImage, latitude, longitude, (int) (addImage.getWidth()), (int) (addImage.getHeight())
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void drawImage() {
        if (mapImageList.isEmpty()) return;

        AffineTransform affineTransform = RendererUtilities.worldToScreenTransform(envelope, new Rectangle(width, height));

        for (MapImage mapImage : mapImageList) {
            Point2D point2D = affineTransform.transform(new Point2D.Double(mapImage.longitude(), mapImage.latitude), null);
            int x = (int) Math.round(point2D.getX() - mapImage.width() / 2.0f);
            int y = (int) Math.round(point2D.getY() - mapImage.height() / 2.0f);
            graphics.drawImage(mapImage.image(), x, y, mapImage.width(), mapImage.height(), null);
        }
    }

    public MapBuilder setDrawArea(List<ReferencedEnvelope> drawAreas) {
        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;

        for (ReferencedEnvelope env : drawAreas) {
            if (env.getMinX() < xMin)  xMin = env.getMinX();
            if (env.getMaxX() > xMax) xMax = env.getMaxX();
            if (env.getMinY() < yMin) yMin = env.getMinY();
            if (env.getMaxY() > yMax) yMax = env.getMaxY();
        }

        ReferencedEnvelope envelope = new ReferencedEnvelope(
                xMin, xMax,
                yMin, yMax,
                DefaultGeographicCRS.WGS84
        );

        setEnvelope(envelope);

        return this;
    }

    public BufferedImage getImage() {
        return image;
    }

    public MapBuilder setEnvelope(ReferencedEnvelope envelope) {
        this.envelope = envelope;
        System.out.println(featureSource.getName() + ":" + envelope);
        return this;
    }

    public BufferedImage build() {
        if (envelope == null) envelope = map.layers().getFirst().getBounds();
        graphics.setPaint(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, width, height);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);
        renderer.paint(graphics, new Rectangle(width, height), envelope);

        drawImage();

        return image;
    }

    public SimpleFeatureCollection getMapFeatureCollection(Filter filter) {
        if (filter != null) {
            try {
                return featureSource.getFeatures(filter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return map_featureCollection;
        }
    }

    public MapContent getMap() {
        return map;
    }

    public SimpleFeatureSource getMapFeatureSource() {
        return featureSource;
    }

    public static Style createLineStyle(Color stroke_color, float stroke_thickness) {
        StyleBuilder style_builder = new StyleBuilder();
        LineSymbolizer symbolizer = style_builder.createLineSymbolizer(stroke_color, stroke_thickness);
        Rule rule = style_builder.createRule(symbolizer);
        FeatureTypeStyle featureTypeStyle = style_builder.createFeatureTypeStyle(null, rule);
        Style style = style_builder.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);
        return style;
    }

    public static Style createStyle(Color stroke_color, float stroke_thickness, Color fill_color, float fill_opacity) {
        StyleBuilder style_builder = new StyleBuilder();

        Stroke stroke = null;
        if (stroke_color != null) {
            stroke = style_builder.createStroke(stroke_color, stroke_thickness);
        }
        Fill fill = style_builder.createFill(fill_color, fill_opacity);
        PolygonSymbolizer symbolizer = style_builder.createPolygonSymbolizer(stroke, fill);
        Rule rule = style_builder.createRule(symbolizer);
        FeatureTypeStyle featureTypeStyle = style_builder.createFeatureTypeStyle(null, rule);
        Style style = style_builder.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);
        return style;
    }

    public static SimpleFeatureSource getFileFeatureSource(File file) throws IOException {
        ShapefileDataStore store = (ShapefileDataStore) FileDataStoreFinder.getDataStore(file);
        store.setCharset(StandardCharsets.UTF_8);
        SimpleFeatureCollection all = store.getFeatureSource().getFeatures();
        return DataUtilities.source(DataUtilities.collection(all));
    }

    public static ReferencedEnvelope getAreaEnvelope(SimpleFeatureSource source, Filter filter) throws IOException {
        if (filter != null) {
            SimpleFeatureCollection features = source.getFeatures(filter);
            System.out.println(features.getBounds());
            return features.getBounds();
        }
        return source.getBounds();
    }

}
