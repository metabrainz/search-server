package org.musicbrainz.search.replication.packet;

import junit.framework.TestCase;

public class ReplicationChangeTest extends TestCase {

	public void testChangedFields() {
		ReplicationChange change = new ReplicationChange(0);
		String strOldValues = "\"id\"='1' \"recording\"='1' \"tracklist\"='1' ";
		String strNewValues = "\"id\"='1' \"recording\"='2' \"tracklist\"='1' ";
		change.setOldValues(UnpackUtils.unpackData(strOldValues));
		change.setNewValues(UnpackUtils.unpackData(strNewValues));
		
		assertEquals(change.getChangedFields().size(), 1);
		assertEquals(change.getChangedFields().iterator().next(), "recording");
	}

	public void testChangedFieldsWithNullValues() {
		ReplicationChange change = new ReplicationChange(0);
		String strOldValues = "\"id\"= \"recording\"= \"tracklist\"='1' ";
		String strNewValues = "\"id\"='1' \"recording\"= \"tracklist\"='1' ";
		change.setOldValues(UnpackUtils.unpackData(strOldValues));
		change.setNewValues(UnpackUtils.unpackData(strNewValues));
		
		assertEquals(change.getChangedFields().size(), 1);
		assertEquals(change.getChangedFields().iterator().next(), "id");
	}

	
}
