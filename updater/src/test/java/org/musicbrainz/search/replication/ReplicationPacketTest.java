package org.musicbrainz.search.replication;

import org.musicbrainz.search.replication.ReplicationPacket;

import junit.framework.TestCase;

public class ReplicationPacketTest extends TestCase {

	int REPLICATION_SEQUENCE = 51992;
	int SCHEMA_SEQUENCE = 13;
	
	
	
	public ReplicationPacket loadTestPacket() {
		ReplicationPacket packet = null;
		packet = ReplicationPacket.loadFromRepository(REPLICATION_SEQUENCE);
		if (packet == null) {
			System.err.println("Unable to load test packet, test won't be run.");
		}
		return packet;
	}

	public void testCopy() {
		
	}
	
	public void testSchemaSequence() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals(SCHEMA_SEQUENCE, packet.getSchemaSequence());
		}
	}


	public void testReplicationSequence() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals(REPLICATION_SEQUENCE, packet.getReplicationSequence());
		}
	}

	public void testFirstChange() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals("release_meta", packet.getChanges().get(0).getTableName());
			assertEquals(DatabaseOperation.UPDATE, packet.getChanges().get(0).getOperation());
		}
	}

	public void testGetMaxChangeId() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals(new Integer(6243972), packet.getMaxChangeId());
		}
	}

	public void testGetMaxChangeIdWithNoChanges() throws Exception {
		ReplicationPacket packet = new ReplicationPacket();
		assertNull(packet.getMaxChangeId());
	}

    /*
	public void testBigPacket() throws Exception {
        
		// #57157 => 11M
		// #52955 => 1.6M
		// #52991 => 724K
		
		ReplicationPacket bigPacket;
		long t0 = System.currentTimeMillis();
		bigPacket = ReplicationPacket.loadFromRepository(52955);
        long t1 = System.currentTimeMillis();
        
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("It tooks " + elapsedTimeSeconds + " s to load " + bigPacket.getChanges().size() + " changes");
        assertNotNull(bigPacket);
	}
	*/ 
	
	public void testNotExistingPacket() throws Exception {
		ReplicationPacket nullPacket;
		nullPacket = ReplicationPacket.loadFromRepository(99999999);
		assertNull(nullPacket);
	}
	
}
