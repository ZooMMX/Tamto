package hello;

import org.ocpsoft.prettytime.PrettyTime;

import java.math.BigInteger;
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
}
