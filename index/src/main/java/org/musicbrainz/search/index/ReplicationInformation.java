package org.musicbrainz.search.index;

public class ReplicationInformation implements Comparable<ReplicationInformation> {

	public Integer schemaSequence;
	public Integer replicationSequence;
	public Integer changeSequence;
	
	@Override
	public int compareTo(ReplicationInformation other) {
		int result;
		
		// SchemaSequence
		result = this.schemaSequence.compareTo(other.schemaSequence);
		if (result != 0) {
			return result;
		}
		
		// ReplicationSequence
		result = this.replicationSequence.compareTo(other.replicationSequence);
		if (result != 0) {
			return result;
		}
		
		// ReplicationSequence (might be null)
		if (this.changeSequence == null && other.changeSequence == null) {
			result = 0;
		} else if (this.changeSequence == null) {
			result = -1;
		} else if (other.changeSequence == null) {
			result = 1;
		} else {
			result = this.changeSequence.compareTo(other.changeSequence);
		}
		return result;
	}

	
}
