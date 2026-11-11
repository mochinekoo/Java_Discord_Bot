package mochineko.java_discord_bot.status;

import mochineko.java_discord_bot.api.P2PEarthquakeAPI;

import java.util.Map;

/**
 * 気象庁が発表する、地震に関する発表の種類
 */
public enum JmaEarthquakeIssueType {

    SCALE_PROMPT("震度速報"),
    DESTINATION("震源に関する情報"),
    SCALE_AND_DESTINATION("震度・震源に関する情報"),
    DETAIL_SCALE("各地の震度に関する情報"),
    FOREIGN("遠地地震に関する情報"),
    OTHER("その他");

    private final String name;

    public static final Map<String, JmaEarthquakeIssueType> P2PAPI_TYPE = Map.of(
            "ScalePrompt", JmaEarthquakeIssueType.SCALE_PROMPT,
            "Destination", JmaEarthquakeIssueType.DESTINATION,
            "ScaleAndDestination", JmaEarthquakeIssueType.SCALE_AND_DESTINATION,
            "DetailScale", JmaEarthquakeIssueType.DETAIL_SCALE,
            "Foreign", JmaEarthquakeIssueType.FOREIGN,
            "Other", JmaEarthquakeIssueType.OTHER
    );

    JmaEarthquakeIssueType(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public static JmaEarthquakeIssueType convertP2PAPI(String name) {
        if (P2PAPI_TYPE.containsKey(name)) {
            return P2PAPI_TYPE.get(name);
        }
        return null;
    }
}
