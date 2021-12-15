package me.drex.itsours.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    private static Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
    private static final int maxYears = 100000;
    private static SimpleCommandExceptionType TIME_ARGUMENT_INVALID = new SimpleCommandExceptionType(new LiteralText("Invalid time argument, correct syntax: 1w, 1d12h, 10m").formatted(Formatting.RED));


    public static String convertSecondsToString(int seconds) {
        int day = seconds / (24 * 3600);
        seconds = seconds % (24 * 3600);
        int hour = seconds / 3600;
        seconds %= 3600;
        int min = seconds / 60;
        seconds %= 60;
        String s = ((day != 0) ? day > 1 ? day + " Days " : day + " Day " : "") +
                ((hour != 0) ? hour > 1 ? hour + " Hours " : hour + " Hour " : "") +
                ((min != 0) ? min > 1 ? min + " Minutes " : min + " Minute " : "")  +
                ((seconds != 0) ? seconds > 1 ? seconds + " Seconds " : seconds +  " Second " : "");
        if(s.length() > 0){
            return s.substring(0, s.length()-1);
        }
        return "";
    }

    public static long convertStringToSeconds(String time) throws CommandSyntaxException {
        long current = (int) (new Date().getTime());
        long until = convertStringToUnixSeconds(time);
        return until - current;
    }

    private static long convertStringToUnixSeconds(String time) throws CommandSyntaxException {
        Date date = new Date();
        Matcher matcher = timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (matcher.find()) {
            if (matcher.group() == null || matcher.group().isEmpty())
                continue;

            for (int i = 0; i < matcher.groupCount(); i++) {
                if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw TIME_ARGUMENT_INVALID.create();
            }

            if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
                years = Integer.parseInt(matcher.group(1));
            }
            if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
                months = Integer.parseInt(matcher.group(2));
            }
            if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
                weeks = Integer.parseInt(matcher.group(3));
            }
            if (matcher.group(4) != null && !matcher.group(4).isEmpty()) {
                days = Integer.parseInt(matcher.group(4));
            }
            if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
                hours = Integer.parseInt(matcher.group(5));
            }
            if (matcher.group(6) != null && !matcher.group(6).isEmpty()) {
                minutes = Integer.parseInt(matcher.group(6));
            }
            if (matcher.group(7) != null && !matcher.group(7).isEmpty()) {
                seconds = Integer.parseInt(matcher.group(7));
            }
            break;
        }

        Calendar c = new GregorianCalendar();
        if (years > 0) {
            if (years > maxYears) {
                years = maxYears;
            }
            c.add(Calendar.YEAR, years);
        }
        if (months > 0) {
            c.add(Calendar.MONTH, months);
        }
        if (weeks > 0) {
            c.add(Calendar.WEEK_OF_YEAR, weeks);
        }
        if (days > 0) {
            c.add(Calendar.DAY_OF_MONTH, days);
        }
        if (hours > 0) {
            c.add(Calendar.HOUR_OF_DAY, hours);
        }
        if (minutes > 0) {
            c.add(Calendar.MINUTE, minutes);
        }
        if (seconds > 0) {
            c.add(Calendar.SECOND, seconds);
        }
        Calendar max = new GregorianCalendar();
        max.add(Calendar.YEAR, 10);
        if (c.after(max)) {
            return (int) max.getTimeInMillis() / 1000;
        }

        if (date.getTime() == c.getTimeInMillis()) {
            throw TIME_ARGUMENT_INVALID.create();
        }

        return c.getTimeInMillis();
    }

}