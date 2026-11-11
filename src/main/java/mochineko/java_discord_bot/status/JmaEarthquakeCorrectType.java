package mochineko.java_discord_bot.status;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 気象庁が発表する、地震に関する訂正の種類
 */
public enum JmaEarthquakeCorrectType {

    NONE("なし"),
    SCALE_ONLY("震度"),
    DESTINATION_ONLY("震源"),
    SCALE_AND_DESTINATION("震度・震源");

    private final String name;

    public static final Map<String, JmaEarthquakeCorrectType> P2P_API = Map.of(
            "None", JmaEarthquakeCorrectType.NONE,
            "ScaleOnly", JmaEarthquakeCorrectType.SCALE_ONLY,
            "DestinationOnly", JmaEarthquakeCorrectType.DESTINATION_ONLY,
            "ScaleAndDestination", JmaEarthquakeCorrectType.SCALE_AND_DESTINATION
    );

    JmaEarthquakeCorrectType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static JmaEarthquakeCorrectType convertP2PAPI(String name) {
        if (P2P_API.containsKey(name)) {
            return P2P_API.get(name);
        }
        return null;
    }

}
