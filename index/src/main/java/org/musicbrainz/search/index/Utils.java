package org.musicbrainz.search.index;

import org.apache.commons.lang.time.StopWatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class Utils {

    private static SimpleDateFormat TIME_OUTPUT = new SimpleDateFormat("HH:mm:ss");

	public static String formatDate(Integer year, Integer month, Integer day)
	{
		StringBuffer sb = new StringBuffer();
		Formatter formatter = new Formatter(sb);

		StringBuffer formatStringBuffer = new StringBuffer();
		if (year != null && !year.equals(0)) {
			formatStringBuffer.append("%04d");
			
			if (month != null && !month.equals(0)) {
				formatStringBuffer.append("-%02d");
				
				if (day != null && !day.equals(0)) {
					formatStringBuffer.append("-%02d");
					return formatter.format(formatStringBuffer.toString(), year, month, day).toString();
				} else {
					return formatter.format(formatStringBuffer.toString(), year, month).toString();
				}
				
			} else {
				return formatter.format(formatStringBuffer.toString(), year).toString();
			}
		}
		
		return "";
	}

    /**
     * Format clock for output
     *
     * @param clock
     * @return
     */
    public static String formatClock(StopWatch clock)
    {
        return Float.toString(clock.getTime()/1000) + " secs";
    }

    /**
     * Format current date as time for output
     *
     * (Must be synchronized because DateFormat is not thread safe)
     *
     * @return
     */
    public synchronized static String formatCurrentTimeForOutput()
    {
        return TIME_OUTPUT.format(new Date());
    }

}
