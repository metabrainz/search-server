package org.musicbrainz.search.replication.packet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class ReplicationPacket {

	private List<ReplicationChange> changes = new ArrayList<ReplicationChange>();
	private int replicationSequence;
	private int schemaSequence;
	
	public List<ReplicationChange> getChanges() {
		return changes;
	}

	public int getReplicationSequence() {
		return replicationSequence;
	}

	private void setReplicationSequence(int replicationSequence) {
		this.replicationSequence = replicationSequence;
	}

	public int getSchemaSequence() {
		return schemaSequence;
	}

	private void setSchemaSequence(int schemaSequence) {
		this.schemaSequence = schemaSequence;
	}
	
	public Integer getMaxChangeId() {
		Integer result = null;
		
		for(ReplicationChange change : getChanges()) {
			if (result == null || change.getId() > result) {
				result = change.getId();
			}
		}
		return result;
	}

	public static ReplicationPacket loadFromDatabase(Connection dbConnection, int lastChangeId) throws IOException, SQLException {
		
		ReplicationPacket packet = new ReplicationPacket();

        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery(
        		"SELECT p.seqid, p.tablename, p.op, pd.iskey, pd.data, " +
        		"	 	r.current_schema_sequence, r.current_replication_sequence " +
        		" FROM dbmirror_pending p " +
        		"  JOIN dbmirror_pendingdata pd ON (p.seqid = pd.seqid) " +
        		"  CROSS JOIN replication_control r " +        		
        		" WHERE p.seqid > " + lastChangeId +
        		" ORDER BY p.seqid");
        
        ReplicationChange change = null;
        while (rs.next()) {
        	
        	// Fill replication and schema sequences on first result
        	if (change == null) {
        		packet.setSchemaSequence(rs.getInt("current_schema_sequence"));
        		packet.setReplicationSequence(rs.getInt("current_replication_sequence"));
        	}
        	
            int seqId = rs.getInt("seqid");
            
            if (change == null || change.getId() != seqId) {
	            change = new ReplicationChange(seqId);
	            change.setTableName(rs.getString("tablename"));
	            change.setOperation(rs.getString("op"));
	            packet.getChanges().add(change);
            } 
            
            String values = rs.getString("data");
            String opcode = rs.getString("iskey");
        	switch (change.getOperation()) {
	    		case INSERT:
	    			change.setNewValues(UnpackUtils.unpackData(values));
	    			break;
	    		case UPDATE:
	    			if ("f".equals(opcode)) change.setOldValues(UnpackUtils.unpackData(values));
	    			if ("t".equals(opcode)) change.setNewValues(UnpackUtils.unpackData(values));
	    			break;
	    		case DELETE:
	    			change.setOldValues(UnpackUtils.unpackData(values));
	    			break;
        	}
        }
        
        // If we've no changes, there's not point, return null
		return packet.getChanges().isEmpty() ? null : packet;
	}	
	
	public static ReplicationPacket loadFromRepository(int sequence, String repositoryPath) throws IOException {
		
		URL url = new URL(repositoryPath + "/" + "replication-"+sequence+".tar.bz2");
		InputStream input;
		try {
			input = url.openStream();
		} catch (FileNotFoundException e) {
			return null;
		}
		return ReplicationPacket.loadFromInputStream(input);
	}	
	
	public static ReplicationPacket loadFromInputStream(InputStream input) throws IOException {
		
		ReplicationPacket packet = new ReplicationPacket();
		
        // Create the archive input stream from the dump, assuming it's a tar.bz2 file
        BufferedInputStream fileInput = new BufferedInputStream(input);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(fileInput);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn);

        SortedMap<Integer,ReplicationChange> changes = new TreeMap<Integer,ReplicationChange>();
        
        ArchiveEntry entry;
        while ((entry = tarIn.getNextEntry()) != null) {
        	
        	// REPLICATION_SEQUENCE
            if (entry.getName().equals("REPLICATION_SEQUENCE") ) {
            
                byte[] content = new byte[(int) entry.getSize()];
                tarIn.read(content, 0, (int) entry.getSize());

                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
                String line = reader.readLine();
                packet.setReplicationSequence(Integer.parseInt(line));
		
           	// SCHEMA_SEQUENCE
            } else if (entry.getName().equals("SCHEMA_SEQUENCE") ) {
                
                byte[] content = new byte[(int) entry.getSize()];
                tarIn.read(content, 0, (int) entry.getSize());

                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
                String line = reader.readLine();
                packet.setSchemaSequence(Integer.parseInt(line));
		
            // dbmirror_pending
            } else if (entry.getName().equals("mbdump/dbmirror_pending")
            		|| entry.getName().equals("mbdump/Pending")) {
                
                byte[] content = new byte[(int) entry.getSize()];
                tarIn.read(content, 0, (int) entry.getSize());

                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
                String line;
                while ((line = reader.readLine()) != null) {
              	   
                	StringTokenizer st = new StringTokenizer(line, "\t");
                	int id = Integer.parseInt(st.nextToken());
                	
                	ReplicationChange change;
                	if (changes.containsKey(id)) {
                		change = changes.get(id);
                	} else {
                		change = new ReplicationChange(id);
                		changes.put(id, change);
                	}
                	
                	String tableName = st.nextToken().replace("\"public\".", "").replace("\"musicbrainz\".", "");
                	tableName = tableName.substring(1, tableName.length()-1);
                	change.setTableName(tableName);
                	change.setOperation(st.nextToken());
                	
                	changes.put(change.getId(), change);
                }
		
             // dbmirror_pendingdata
            } else if (entry.getName().equals("mbdump/dbmirror_pendingdata") 
            		|| entry.getName().equals("mbdump/PendingData")) {
                
                byte[] content = new byte[(int) entry.getSize()];
                tarIn.read(content, 0, (int) entry.getSize());

                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
                String line;
                while ((line = reader.readLine()) != null) {
                	 
                	StringTokenizer st = new StringTokenizer(line, "\t");
                	int id = Integer.parseInt(st.nextToken());
                	
                	ReplicationChange change;
                	if (changes.containsKey(id)) {
                		change = changes.get(id);
                	} else {
                		change = new ReplicationChange(id);
                		changes.put(id, change);
                	}
                	
                	String opcode = st.nextToken();
                	String values = st.nextToken();
                	
                	switch (change.getOperation()) {
                		case INSERT:
                			change.setNewValues(UnpackUtils.unpackData(values));
                			break;
                		case UPDATE:
                			if ("f".equals(opcode)) change.setOldValues(UnpackUtils.unpackData(values));
                			if ("t".equals(opcode)) change.setNewValues(UnpackUtils.unpackData(values));
                			break;
                		case DELETE:
                			change.setOldValues(UnpackUtils.unpackData(values));
                			break;
                	}
                	
                }
		
            }
        }
        
        for (Iterator<Integer> iterator = changes.keySet().iterator(); iterator.hasNext();) {
        	packet.getChanges().add(changes.get(iterator.next()));
		}
        
		return packet;
	}
	
}
