package mochineko.java_discord_bot.status;

import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

public enum EarthquakeScaleType {

    ZERO("震度0", Color.WHITE),
    ONE("震度1", Color.GRAY),
    TWO("震度2", new Color(30, 110, 230)),
    THREE("震度3", new Color(21, 98, 101)),
    FOUR("震度4", new Color(247, 247, 98)),
    FIVE_LOW("震度5弱", new Color(251, 176, 3)),
    FIVE_HIGH("震度5強", new Color(234, 173, 102)),
    SIX_LOW("震度6弱", new Color(230, 0, 2)),
    SIX_HIGH("震度6強", new Color(158, 1, 2)),
    SEVEN("震度7", Color.PINK),
    NODATA("震度5弱以上と推定", Color.WHITE),
    UNKNOWN("不明", Color.WHITE);

    private static final Map<Integer, EarthquakeScaleType> p2pAPI_convert = Map.ofEntries(
            Map.entry(-1, UNKNOWN),
            Map.entry(10, ONE),
            Map.entry(20, TWO),
            Map.entry(30, THREE),
            Map.entry(40, FOUR),
            Map.entry(45, FIVE_LOW),
            Map.entry(46, NODATA),
            Map.entry(50, FIVE_HIGH),
            Map.entry(55, SIX_LOW),
            Map.entry(60, SIX_HIGH),
            Map.entry(70, SEVEN)
    );

    private final String name;
    private final Color color;

    EarthquakeScaleType(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getP2PRawData() {
        Map<EarthquakeScaleType, Integer> map = p2pAPI_convert.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return map.getOrDefault(this, -1);
    }

    public static EarthquakeScaleType convertP2PAPI(int scale) {
        return p2pAPI_convert.getOrDefault(scale, UNKNOWN);
    }

    public Color getColor() {
        return color;
    }
}
