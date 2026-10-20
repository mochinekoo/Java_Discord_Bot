package mochineko.java_discord_bot.status;

import java.util.Map;

public enum TsunamiGrade {
    MAJOR_WARNING("大津波警報"),
    WARNING("津波警報"),
    WATCH("津波注意報"),
    SLIGHT("若干の海面変動"),
    NONE(null),
    UNKNOWN("不明");

    public static final Map<String, TsunamiGrade> p2p_convert = Map.ofEntries(
            Map.entry("MajorWarning", TsunamiGrade.MAJOR_WARNING),
            Map.entry("Warning", TsunamiGrade.WARNING),
            Map.entry("Watch", TsunamiGrade.WATCH),
            Map.entry("Unknown", TsunamiGrade.UNKNOWN)
    );

    private String name;

    TsunamiGrade(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TsunamiGrade getGradeType(float meter) {
        if (meter > 3.0) {
            return MAJOR_WARNING;
        }
        else if (meter > 1.0 && meter <= 3.0) {
            return WARNING;
        }
        else if (meter > 0.2 && meter <= 1.0) {
            return WATCH;
        }
        return null;
    }

    public static TsunamiGrade convertP2PAPI(String grade) {
        return p2p_convert.getOrDefault(grade, UNKNOWN);
    }
}
