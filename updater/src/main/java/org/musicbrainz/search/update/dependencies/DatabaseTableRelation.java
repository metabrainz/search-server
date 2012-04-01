package org.musicbrainz.search.update.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DatabaseTableRelation {

	public String sourceTableName;
	private String sourceJoinField;
	private DatabaseTableRelation targetTable;
	private String targetJoinField;
	private Set<String> fieldsUsedForIndexing = new HashSet<String>();

	public DatabaseTableRelation(String sourceTable) {
		this.sourceTableName = sourceTable;
	}

	public String getSourceTableName() {
		return sourceTableName;
	}
	
	public DatabaseTableRelation getTargetTable() {
		return targetTable;
	}

	public String getSourceJoinField() {
		return sourceJoinField;
	}

	public String getTargetJoinField() {
		return targetJoinField;
	}
	
	public Set<String> getFieldsUsedForIndexing() {
		return fieldsUsedForIndexing;
	}

	public void setFieldsUsedForIndexing(String[] fieldsUsedForIndexing) {
		for (String field : fieldsUsedForIndexing) {
			this.fieldsUsedForIndexing.add(field);
		}
	}
	
	public void setFieldsUsedForIndexing(Set<String> fieldsUsedForIndexing) {
		this.fieldsUsedForIndexing = fieldsUsedForIndexing;
	}
	
	public void setTargetTable(DatabaseTableRelation targetTable, String targetJoinField, String sourceJoinField) {
		if (targetTable == null || targetJoinField == null || sourceJoinField == null) {
			throw new IllegalArgumentException("arguments can not be null");
		}
		this.targetTable = targetTable;
		this.targetJoinField = targetJoinField;
		this.sourceJoinField = sourceJoinField;
	}

	
	public boolean isHead() {
		return (this.targetTable == null);
	}

	public DatabaseTableRelation getHead() {
		DatabaseTableRelation lt = this;
		while (!lt.isHead()) {
			lt = lt.targetTable;
		}
		return lt;
	}
	
	public String generateSQL(Integer key) {
		Collection<Integer> keys = new HashSet<Integer>();
		keys.add(key);
		return generateSQL(keys);
	}
	
	public String generateSQL(Collection<Integer> keys) {
		
		if (this.isHead()) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		DatabaseTableRelation finalLT = getHead(); 
		sb.append("SELECT " + finalLT.getSourceTableName() + ".id");
		sb.append(" FROM " + finalLT.getSourceTableName());
		
		// JOINs should be added reversely, so we need to first store them before adding them to the SQL query
		List<String> joins = new ArrayList<String>();
		
		if (!isHead()) {
			DatabaseTableRelation lt = this.getTargetTable();
			while (!lt.isHead()) {
				joins.add(" JOIN " + lt.getSourceTableName() + " ON (" + lt.getSourceTableName() + "." + lt.getSourceJoinField() 
						+ " = " + lt.getTargetTable().getSourceTableName() + "." + lt.getTargetJoinField() + ")");
				lt = lt.getTargetTable();
			}
			
			for (int i = joins.size()-1; i >= 0; i--) {
				sb.append(joins.get(i));
			}
		}
		
		sb.append(" WHERE " + this.getTargetTable().getSourceTableName() + "." + this.getTargetJoinField() + " IN (");
		
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(")");
		
		return sb.toString();
	}
	
}
