package org.musicbrainz.search.replication.packet;

import java.io.IOException;

import org.musicbrainz.search.replication.packet.ReplicationPacket;

import junit.framework.TestCase;

public class ReplicationPacketTest extends TestCase {

	int REPLICATION_SEQUENCE = 104;
	int SCHEMA_SEQUENCE = 12;
	
	
	
	public ReplicationPacket loadTestPacket() {
		ReplicationPacket packet = null;
		try {
			packet = ReplicationPacket.loadFromRepository(REPLICATION_SEQUENCE, "http://test.musicbrainz.org:82/pub/musicbrainz/data/replication/");
		} catch (IOException e) {
			System.err.println("Unable to load test packet, test won't be run: " + e.getMessage());
		}
		return packet;
	}
	
	public void testSchemaSequence() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals(SCHEMA_SEQUENCE, packet.getSchemaSequence());
		}
	}

    /*
	public void testReplicationSequence() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals(REPLICATION_SEQUENCE, packet.getReplicationSequence());
		}
	}

	public void testFirstChange() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals("release", packet.getChanges().get(0).getTableName());
			assertEquals(DatabaseOperation.UPDATE, packet.getChanges().get(0).getOperation());
		}
	}

	public void testGetMaxChangeId() throws Exception {
		ReplicationPacket packet = loadTestPacket();
		if (packet != null) {
			assertEquals(new Integer(54), packet.getMaxChangeId());
		}
	}
    */

	public void testGetMaxChangeIdWithNoChanges() throws Exception {
		ReplicationPacket packet = new ReplicationPacket();
		assertNull(packet.getMaxChangeId());
	}

    /*
	public void testBigPacket() throws Exception {
        
		ReplicationPacket bigPacket;
		try {
			long t0 = System.currentTimeMillis();
			bigPacket = ReplicationPacket.loadFromRepository(52110, "http://ftp.musicbrainz.org/pub/musicbrainz/data/replication/");
	        long t1 = System.currentTimeMillis();
	        
	        double elapsedTimeSeconds = (t1 - t0)/1000.0;
	        System.out.println("It tooks " + elapsedTimeSeconds + " s to load " + bigPacket.getChanges().size() + " changes");
		} catch (IOException e) {
			System.out.println("Unable to load big packet, test won't be run: " + e.getMessage());
		}
	}

	public void testNotExistingPacket() throws Exception {
		ReplicationPacket nullPacket;
		try {
			nullPacket = ReplicationPacket.loadFromRepository(99999999, "http://ftp.musicbrainz.org/pub/musicbrainz/data/replication/");
			assertNull(nullPacket);
		} catch (IOException e) {
			System.err.println("Proble during attempt to load non existing packet, test won't be run: " + e.getMessage());
		}
		
	}
	*/
	
}
