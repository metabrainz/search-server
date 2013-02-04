package org.musicbrainz.search.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.musicbrainz.replication.ReplicationChange;
import org.musicbrainz.replication.ReplicationPacket;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.update.dependencies.DatabaseIndexDependencies;
import org.musicbrainz.search.update.dependencies.DatabaseTableRelation;

public class ChangesAnalyzer {

	private final Logger LOGGER = Logger.getLogger(ChangesAnalyzer.class.getName());
	
	private Set<Integer> deletedIds = new HashSet<Integer>();
	private Set<Integer> insertedOrUpdatedIds = new HashSet<Integer>();
	
	private DatabaseIndex databaseIndex;
	private DatabaseIndexDependencies dependencyTree;
    
    public ChangesAnalyzer(DatabaseIndex databaseIndex, DatabaseIndexDependencies dependencyTree) {
    	this.databaseIndex = databaseIndex;
    	this.dependencyTree = dependencyTree;
    }

	public Set<Integer> getDeletedIds() {
		return deletedIds;
	}

	public Set<Integer> getInsertedOrUpdatedIds() {
		return insertedOrUpdatedIds;
	}
    
	public boolean hasChanges() {
		return !deletedIds.isEmpty() || !insertedOrUpdatedIds.isEmpty();
	}
	
	public void reset() {
		deletedIds.clear();
		insertedOrUpdatedIds.clear();
	}
	
    public void analyze(ReplicationPacket packet, Integer lastChangeSequence) throws SQLException, InvalidReplicationChangeException {
    	
    	Map<String, Set<Integer>> changedTables = new HashMap<String, Set<Integer>>();
    	
    	// Initialize deleted and inserted maps of 
    	for (String tableName : dependencyTree.getTables()) {
    		if (tableName.equals(dependencyTree.getRootTableName())) {
    			continue;
    		}
    		changedTables.put(tableName, new HashSet<Integer>());
    	}
    	
    	// Process changes in replication packet
    	for (ReplicationChange change : packet.getChanges()) {

    		if (lastChangeSequence != null && change.getId() <= lastChangeSequence ) {
    			LOGGER.finer("Skipping change #" + change.getId() + " because it has already been applied");
    			continue;
    		}
    		
    		if (!dependencyTree.getTables().contains(change.getTableName())) {
    			LOGGER.finest("Skipping change #" + change.getId() + " on unrelated table " + change.getTableName().toUpperCase());
    			continue;
    		}
    		
    		DatabaseTableRelation lt = dependencyTree.getDependency(change.getTableName());
    		
    		LOGGER.finer("Analyzing change #" + change.getId() + " on table " + change.getTableName().toUpperCase());
    		switch (change.getOperation()) {
    			case INSERT:
        			{
        				Map<String,String> values = change.getNewValues();
        				if (lt.isHead()) {
        					insertedOrUpdatedIds.add( Integer.parseInt(values.get("id")) );
        				} else {
        					// Check that the replication packet has all the info (in case the table doesn't have a verbose replication trigger
        		    		if ( values.get(lt.getSourceJoinField()) == null ) {
        		    			String errMsg = "Replication packet doesn't have verbose information for table '" + change.getTableName() + "': field '" + lt.getSourceJoinField() + "' is missing (change #" + change.getId() +")";
        		    			throw new InvalidReplicationChangeException(errMsg);
        		    		}
        					changedTables.get(change.getTableName()).add( 
        							Integer.parseInt( values.get(lt.getSourceJoinField()) )); 
        				}
        			}
        			break;
    			case UPDATE:
        			{
        				Map<String,String> oldValues = change.getOldValues();
        				Map<String,String> newValues = change.getNewValues();
        				if (lt.isHead()) {
        					// TODO: fix hardcoding of "id"?
        					insertedOrUpdatedIds.add( Integer.parseInt(newValues.get("id")) );
        				} else {
        					
        					// By default, skip this change unless no fields used for indexing 
        					// have been declared
        					boolean skipChange = lt.getFieldsUsedForIndexing().isEmpty() ? false : true;
        					
        					// Now check that at least one of the used fields has been changed
        					for (String usedField : lt.getFieldsUsedForIndexing()) {
        						if (change.getChangedFields().contains(usedField)) {
        							skipChange = false;
        							break;
        						}
        					}
        					
        					if (skipChange) {
        						LOGGER.finer("Skipping change #" + change.getId() + " on table " + change.getTableName().toUpperCase()
        								+ " because none of the fields used for indexing has been changed");
        					} else {
            					// Check that the replication packet has all the info (in case the table doesn't have a verbose replication trigger
            		    		if ( newValues.get(lt.getSourceJoinField()) == null ) {
            		    			String errMsg = "Replication packet doesn't have verbose information for table '" + change.getTableName() + "': field '" + lt.getSourceJoinField() + "' is missing (change #" + change.getId() +")";
            		    			throw new InvalidReplicationChangeException(errMsg);
            		    		}
	        					changedTables.get(change.getTableName()).add( 
	        							Integer.parseInt(newValues.get(lt.getSourceJoinField()) ));
	        					
	          					// Check that the replication packet has all the info (in case the table doesn't have a verbose replication trigger
            		    		if ( oldValues.get(lt.getSourceJoinField()) == null ) {
            		    			String errMsg = "Replication packet doesn't have verbose information for table '" + change.getTableName() + "': field '" + lt.getSourceJoinField() + "' is missing (change #" + change.getId() +")";
            		    			throw new InvalidReplicationChangeException(errMsg);
            		    		}
	        					changedTables.get(change.getTableName()).add( 
	        							Integer.parseInt(oldValues.get(lt.getSourceJoinField()) ));
        					}
        				}
        			}
    				break;
    			case DELETE:
        			{
        				Map<String,String> values = change.getOldValues();
        				if (lt.isHead()) {
        					deletedIds.add( Integer.parseInt(values.get("id")) );
        				} else {
        					changedTables.get(change.getTableName()).add( 
        							Integer.parseInt(values.get(lt.getSourceJoinField()) )); 
        				}
        			}
    				break;
    		}
    		
    	}
    	
    	// Now determine the ids of our entity
    	for (String tableName : changedTables.keySet()) {
    		
    		Set<Integer> tmpIds = changedTables.get(tableName);
    		if (tmpIds.isEmpty()) {
    			continue;
    		}
    		DatabaseTableRelation lt = dependencyTree.getDependency(tableName);
    		String sql = lt.generateSQL(tmpIds);
    		
    		LOGGER.finer("Resolution of affected ids for table " + tableName + ": " + sql);
    		
    		Statement st = this.databaseIndex.getDbConnection().createStatement();
    		ResultSet rs = st.executeQuery(sql);
    		
    		while (rs.next()) {
    			insertedOrUpdatedIds.add(rs.getInt(1));
    		}
    		
    	}
    	
    }
    
}
