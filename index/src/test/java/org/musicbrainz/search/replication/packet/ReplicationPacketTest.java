package org.musicbrainz.search.replication.packet;

import org.musicbrainz.search.replication.packet.ReplicationPacket;

import junit.framework.TestCase;

public class ReplicationPacketTest extends TestCase {

	int REPLICATION_SEQUENCE = 104;
	int SCHEMA_SEQUENCE = 12;
	
	ReplicationPacket packet;
	
	public void setUp() throws Exception {
		packet = ReplicationPacket.loadFromRepository(REPLICATION_SEQUENCE, "http://test.musicbrainz.org:82/pub/musicbrainz/data/replication/");
	}
	
	public void testSchemaSequence() throws Exception {
		assertEquals(SCHEMA_SEQUENCE, packet.getSchemaSequence());
	}

	public void testReplicationSequence() throws Exception {
		assertEquals(REPLICATION_SEQUENCE, packet.getReplicationSequence());
	}

	public void testFirstChange() throws Exception {
		assertEquals("release", packet.getChanges().get(0).getTableName());
		assertEquals(DatabaseOperation.UPDATE, packet.getChanges().get(0).getOperation());
	}

	public void testGetMaxChangeId() throws Exception {
		assertEquals(new Integer(54), packet.getMaxChangeId());
	}

	public void testGetMaxChangeIdWithNoChanges() throws Exception {
		ReplicationPacket packet = new ReplicationPacket();
		assertNull(packet.getMaxChangeId());
	}
	
	public void testBigPacket() throws Exception {
        long t0 = System.currentTimeMillis();
		ReplicationPacket bigPacket = ReplicationPacket.loadFromRepository(33209, "http://ftp.musicbrainz.org/pub/musicbrainz/data/replication/");
        long t1 = System.currentTimeMillis();
        
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("It tooks " + elapsedTimeSeconds + " s to load " + bigPacket.getChanges().size() + " changes");
	}

	public void testNotExistingPacket() throws Exception {
		ReplicationPacket nullPacket = ReplicationPacket.loadFromRepository(99999999, "http://ftp.musicbrainz.org/pub/musicbrainz/data/replication/");
		assertNull(nullPacket);
	}
	
}
