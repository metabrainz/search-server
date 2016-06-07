package org.musicbrainz.replication;

import java.sql.Connection;
import java.util.Iterator;

import org.musicbrainz.search.index.ReplicationInformation;
import org.musicbrainz.search.update.LiveDataFeedIndexUpdaterOptions;

public class ReplicationPacketIterator implements Iterator<ReplicationPacket> {

	private boolean nextPacketChecked = false;
	private boolean useLocalDatabase = false;
	private ReplicationPacket nextPacket = null;
	private Connection databaseConnection = null;

	private ReplicationInformation currentReplicationPosition = null;

	/**
	 * 
	 * @param initialReplicationInfo
	 *            Replication information serving as starting point for the iteration.
	 * @param useLocalDatabase
	 *            Whether to try to load changes in the local database that have not yet been packaged in a replication packet
	 * 
	 */
	public ReplicationPacketIterator(final ReplicationInformation initialReplicationInfo, boolean useLocalDatabase) {
		this.currentReplicationPosition = initialReplicationInfo;
		this.currentReplicationPosition.replicationSequence = initialReplicationInfo.replicationSequence;
		this.currentReplicationPosition.schemaSequence = initialReplicationInfo.schemaSequence;
		this.currentReplicationPosition.changeSequence = initialReplicationInfo.changeSequence;
		this.useLocalDatabase = useLocalDatabase;
	}

	public void setDatabaseConnection(Connection databaseConnection) {
		this.databaseConnection = databaseConnection;
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
		nextPacket = ReplicationPacket.loadFromRepository(
			packetNo,
			LiveDataFeedIndexUpdaterOptions.getInstance().getRepositoryPath(),
			LiveDataFeedIndexUpdaterOptions.getInstance().getAccessToken()
		);

		// No packet in repository: let's try with pending changes from database
		if (useLocalDatabase && databaseConnection != null && nextPacket == null && currentReplicationPosition.changeSequence != null) {
			nextPacket = ReplicationPacket.loadFromDatabase(databaseConnection, currentReplicationPosition.changeSequence);
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
