package hello;

import org.ocpsoft.prettytime.PrettyTime;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 14/05/15
 * Time: 20:06
 */
public class Util {
    public static String timestamp2String(BigInteger timestamp) {
        Date date = Date.from(Instant.ofEpochMilli( timestamp.longValue() ));
        return getElapsedTimeString(date);
    }

    public static String getElapsedTimeString(Date d) {
        PrettyTime p = new PrettyTime(new Locale("es"));
        return p.format(d);
    }

    public static String formatFilesize(String filesize) {
        long size = Long.parseLong(filesize);
        return formatFilesize(size);
    }

    public static String formatFilesize(Long size) {


        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
