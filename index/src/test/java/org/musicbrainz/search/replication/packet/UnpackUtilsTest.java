package org.musicbrainz.search.replication.packet;

import java.util.Map;

import org.musicbrainz.search.replication.packet.UnpackUtils;

import junit.framework.TestCase;

public class UnpackUtilsTest extends TestCase {

	public void testSimple() throws Exception {
		String data = "\"id\"='11092484' \"recording\"='11092484' \"tracklist\"='966675' ";
		Map<String, String> map = UnpackUtils.unpackData(data);
		
		assertEquals("11092484", map.get("id"));
		assertEquals("11092484", map.get("recording"));
		assertEquals("966675", map.get("tracklist"));
	}
	
	public void testNullValue() throws Exception {
		String data = "\"id\"='11092484' \"recording\"= ";
		Map<String, String> map = UnpackUtils.unpackData(data);
		
		assertNull(map.get("recording"));
	}
	
	public void testEmptyString() throws Exception {
		String data = "\"id\"='11092484' \"recording\"='' ";
		Map<String, String> map = UnpackUtils.unpackData(data);
		
		assertEquals("", map.get("recording"));
	}
	
	public void testEscapedCharacter() throws Exception {
		String data = "\"id\"='11092484' \"text\"='''4 Minutes'' is a shorter version.\r\n''Ray of Light'' is a radio edit.' ";
		Map<String, String> map = UnpackUtils.unpackData(data);
		
		assertEquals("11092484", map.get("id"));
		assertEquals("'4 Minutes' is a shorter version.\r\n'Ray of Light' is a radio edit.", map.get("text"));
	}
	
}
