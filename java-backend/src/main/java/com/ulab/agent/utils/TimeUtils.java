package com.ulab.agent.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = ONE_SECOND * 60;
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

    public static String formatDuration(long millis) {
        if(millis == 0) return "0 seconds";

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append(" day").append(days == 1 ? "" : "s").append(" ");
        if (hours > 0) sb.append(hours).append(" hour").append(hours == 1 ? "" : "s").append(" ");
        if (minutes > 0) sb.append(minutes).append(" minute").append(minutes == 1 ? "" : "s").append(" ");
        if (seconds > 0) sb.append(seconds).append(" second").append(seconds == 1 ? "" : "s");

        return sb.toString().trim();
    }

    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) return 0;

        long totalMillis = 0;
        input = input.toLowerCase().replaceAll("\\s+", "");

        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "d": totalMillis += value * ONE_DAY; break;
                case "h": totalMillis += value * ONE_HOUR; break;
                case "m": totalMillis += value * ONE_MINUTE; break;
                case "s": totalMillis += value * ONE_SECOND; break;
            }
        }
        return totalMillis;
    }

    public static String getTimeNow() {
        return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
    }

    public static String getFileSafeTime() {
        return FILE_NAME_FORMAT.format(new Date(System.currentTimeMillis()));
    }

    public static String getDate(long millis) {
        return DATE_FORMAT.format(new Date(millis));
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("dd/MM/yy").format(new Date());
    }

    public static String getTimeSince(long pastTimestamp) {
        long diff = System.currentTimeMillis() - pastTimestamp;
        return formatDuration(diff);
    }
}
