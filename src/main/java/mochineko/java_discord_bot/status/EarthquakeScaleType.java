package mochineko.java_discord_bot.status;

import java.util.Map;
import java.util.stream.Collectors;

public enum EarthquakeScaleType {

    ZERO("震度0"),
    ONE("震度1"),
    TWO("震度2"),
    THREE("震度3"),
    FOUR("震度4"),
    FIVE_LOW("震度5弱"),
    FIVE_HIGH("震度5強"),
    SIX_LOW("震度6弱"),
    SIX_HIGH("震度6強"),
    SEVEN("震度7"),
    NODATA("震度5弱以上と推定"),
    UNKNOWN("不明");

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

    private String name;

    EarthquakeScaleType(String name) {
        this.name = name;
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

}
