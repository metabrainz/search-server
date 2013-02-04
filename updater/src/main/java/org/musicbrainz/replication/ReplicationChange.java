package org.musicbrainz.replication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReplicationChange {

	private int id;
	private String schemaName;
	private String tableName;
	private DatabaseOperation operation;
	private Map<String, String> oldValues = new HashMap<String, String>();
	private Map<String, String> newValues = new HashMap<String, String>();
	
	public ReplicationChange(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String table) {
		this.tableName = table;
	}
	
	public DatabaseOperation getOperation() {
		return operation;
	}
	public void setOperation(DatabaseOperation operation) {
		this.operation = operation;
	}
	public void setOperation(String operationString) {

    	if ("u".equals(operationString)) {
    		this.operation = DatabaseOperation.UPDATE;
    	} else if ("d".equals(operationString)) {
    		this.operation = DatabaseOperation.DELETE;
    	} else if ("i".equals(operationString)) {
    		this.operation = DatabaseOperation.INSERT;
    	} 
	}
	
	public Map<String, String> getOldValues() {
		return oldValues;
	}
	public void setOldValues(Map<String, String> oldValues) {
		this.oldValues = oldValues;
	}
	
	public Map<String, String> getNewValues() {
		return newValues;
	}
	public void setNewValues(Map<String, String> newValues) {
		this.newValues = newValues;
	}
	
	public Set<String> getChangedFields() {
		Set<String> results = new HashSet<String>();
		for (String field : oldValues.keySet()) {
			if (!newValues.containsKey(field)) { continue; }
			
			String newValue = newValues.get(field);
			String oldValue = oldValues.get(field);
			if ( (newValue == null && oldValue == null)
					|| (newValue != null && newValue.equals(oldValue))) 
			{
				continue;
			} else {
				results.add(field);
			}
		}
		return results;
	}
	
}
