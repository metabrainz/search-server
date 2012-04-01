package org.musicbrainz.search.update;

import java.util.Iterator;

import org.musicbrainz.search.index.DatabaseIndexMetadata;
import org.musicbrainz.search.replication.ReplicationPacket;

public class ReplicationPacketIterator implements Iterator<ReplicationPacket> {

	private boolean nextPacketChecked = false;
	private ReplicationPacket nextPacket = null;
	
	private DatabaseIndexMetadata metadataPosition = null;

	public ReplicationPacketIterator(DatabaseIndexMetadata initialState) {
		this.metadataPosition = new DatabaseIndexMetadata();
		this.metadataPosition.replicationSequence = initialState.replicationSequence;
		this.metadataPosition.schemaSequence = initialState.schemaSequence;
		this.metadataPosition.changeSequence = initialState.changeSequence;
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
			metadataPosition.changeSequence = nextPacket.getMaxChangeId();
			metadataPosition.replicationSequence = nextPacket.getReplicationSequence();
			metadataPosition.schemaSequence = nextPacket.getSchemaSequence();
			
			// Reset next packet and check status, since you're moving forward
			nextPacketChecked = false;
			nextPacket = null;
		}
		
		return packet;
	}

	private void checkNextPacket() {
		int packetNo = metadataPosition.replicationSequence + 1;
		
		// First try to load from repository
		nextPacket = ReplicationPacket.loadFromRepository(packetNo, IndexUpdaterOptions.getInstance().getRepositoryPath());
		
		// No packet in repository: let's try with pending changes from database 
		if (nextPacket == null) {
			nextPacket = ReplicationPacket.loadFromDatabase(IndexUpdaterOptions.getInstance().getMainDatabaseConnection(), metadataPosition.changeSequence);
		}
		
		nextPacketChecked = true;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public DatabaseIndexMetadata getMetadataPosition() {
		return metadataPosition;
	}
	
}
