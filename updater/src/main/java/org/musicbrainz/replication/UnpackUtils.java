package org.musicbrainz.replication;

import java.util.HashMap;
import java.util.Map;
import jregex.Pattern;
import jregex.Matcher;

public class UnpackUtils {

	private static Pattern UNPACK_PATTERN = new Pattern("\"([^\"]+)\"=('(?:''|[^'])*')? ");
	
	static Map<String, String> unpackData(String data) {

		Map<String, String> map = new HashMap<String, String>();
		
		Matcher m = UNPACK_PATTERN.matcher(data);
		while (m.find()) {	
			String name = m.group(1);
			String value = m.group(2);
			
			if (value != null) {
				value = value.substring(1, value.length()-1) // remove leading white space
						.replace("''", "'")					 // remove escaping of ' 
						.replace("\\\\", "\\");				 // remove escaping of \
			}
			
			map.put(name, value);
		}
		
		return map;
	}
	
}
