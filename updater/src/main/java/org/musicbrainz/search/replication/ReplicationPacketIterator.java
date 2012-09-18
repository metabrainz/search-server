package org.musicbrainz.search.replication;

import java.util.Iterator;

import org.musicbrainz.search.index.ReplicationInformation;
import org.musicbrainz.search.update.LiveDataFeedIndexUpdaterOptions;

public class ReplicationPacketIterator implements Iterator<ReplicationPacket> {

	private boolean nextPacketChecked = false;
	private ReplicationPacket nextPacket = null;
	
	private ReplicationInformation currentReplicationPosition = null;

	public ReplicationPacketIterator(ReplicationInformation initialReplicationInfo) {
		this.currentReplicationPosition = new ReplicationInformation();
		this.currentReplicationPosition.replicationSequence = initialReplicationInfo.replicationSequence;
		this.currentReplicationPosition.schemaSequence = initialReplicationInfo.schemaSequence;
		this.currentReplicationPosition.changeSequence = initialReplicationInfo.changeSequence;
	}

	@Override
	public boolean hasNext() {
		if (!nextPacketChecked) {
			checkNextPacket();
		}
		return nextPacket != null;
	}

	@Override
	public ReplicationPacket next() {
		if (!nextPacketChecked) {
			checkNextPacket();
		}
		
		ReplicationPacket packet = nextPacket;
		
		// Update 
		if (nextPacket != null) {
			currentReplicationPosition.changeSequence = nextPacket.getMaxChangeId();
			currentReplicationPosition.replicationSequence = nextPacket.getReplicationSequence();
			currentReplicationPosition.schemaSequence = nextPacket.getSchemaSequence();
			
			// Reset next packet and check status, since you're moving forward
			nextPacketChecked = false;
			nextPacket = null;
		}
		
		return packet;
	}

	private void checkNextPacket() {
		int packetNo = currentReplicationPosition.replicationSequence + 1;
		
		// First try to load from repository
		nextPacket = ReplicationPacket.loadFromRepository(packetNo, LiveDataFeedIndexUpdaterOptions.getInstance().getRepositoryPath());
		
		// No packet in repository: let's try with pending changes from database 
		if (nextPacket == null && currentReplicationPosition.changeSequence != null) {
			nextPacket = ReplicationPacket.loadFromDatabase(LiveDataFeedIndexUpdaterOptions.getInstance().getMainDatabaseConnection(), currentReplicationPosition.changeSequence);
		}
		
		nextPacketChecked = true;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public ReplicationInformation getCurrentReplicationPosition() {
		return currentReplicationPosition;
	}
	
}
