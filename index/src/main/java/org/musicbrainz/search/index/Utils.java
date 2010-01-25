package org.musicbrainz.search.index;

import java.util.Formatter;

public class Utils {

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

}
