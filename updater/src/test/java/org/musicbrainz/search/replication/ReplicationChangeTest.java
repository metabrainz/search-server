package org.musicbrainz.search.replication;

import org.musicbrainz.search.replication.ReplicationChange;
import org.musicbrainz.search.replication.UnpackUtils;

import junit.framework.TestCase;

public class ReplicationChangeTest extends TestCase {

	public void testChangedFields() {
		ReplicationChange change = new ReplicationChange(0);
		String strOldValues = "\"id\"='1' \"recording\"='1' \"tracklist\"='1' ";
		String strNewValues = "\"id\"='1' \"recording\"='2' \"tracklist\"='1' ";
		change.setOldValues(UnpackUtils.unpackData(strOldValues));
		change.setNewValues(UnpackUtils.unpackData(strNewValues));
		
		assertEquals(1, change.getChangedFields().size(), 1);
		assertEquals("recording", change.getChangedFields().iterator().next());
	}

	public void testChangedFieldsWithNullValues() {
		ReplicationChange change = new ReplicationChange(0);
		String strOldValues = "\"id\"= \"recording\"= \"tracklist\"='1' ";
		String strNewValues = "\"id\"='1' \"recording\"= \"tracklist\"='1' ";
		change.setOldValues(UnpackUtils.unpackData(strOldValues));
		change.setNewValues(UnpackUtils.unpackData(strNewValues));
		
		assertEquals(1, change.getChangedFields().size());
		assertEquals("id", change.getChangedFields().iterator().next());
	}

	public void testChangedFieldsWithMissingField() {
		ReplicationChange change = new ReplicationChange(0);
		String strOldValues = "\"id\"='1' \"recording\"='' ";
		String strNewValues = "\"id\"='1' \"recording\"='1' \"tracklist\"='1' ";
		change.setOldValues(UnpackUtils.unpackData(strOldValues));
		change.setNewValues(UnpackUtils.unpackData(strNewValues));
		
		assertEquals(1, change.getChangedFields().size());
		assertEquals("recording", change.getChangedFields().iterator().next());
	}
	
}
