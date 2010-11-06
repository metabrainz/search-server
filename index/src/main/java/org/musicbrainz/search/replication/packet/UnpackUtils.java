package org.musicbrainz.search.replication.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnpackUtils {

	private static Pattern UNPACK_PATTERN = Pattern.compile("\"([^\"]+)\"=('(?:''|[^'])*')? ");
	
	static Map<String, String> unpackData(String data) {

		Map<String, String> map = new HashMap<String, String>();
		
		Matcher m = UNPACK_PATTERN.matcher(data);
		while (m.find()) {	
			String name = m.group(1);
			String value = m.group(2);
			
			if (value != null) {
				value = value.substring(1, value.length()-1).replace("''", "'").replace("\\\\", "\\");				
			}
			
			map.put(name, value);
		}
		
		return map;
	}
	
}
