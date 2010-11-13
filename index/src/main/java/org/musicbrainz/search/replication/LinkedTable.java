package org.musicbrainz.search.replication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class LinkedTable {

	public String tableName;
	private String sourceJoinField;
	private LinkedTable targetTable;
	private String targetJoinField;
	
	public LinkedTable(String sourceTable) {
		this.tableName = sourceTable;
	}

	public String getTableName() {
		return tableName;
	}
	
	public LinkedTable getTargetTable() {
		return targetTable;
	}

	public String getSourceJoinField() {
		return sourceJoinField;
	}

	public String getTargetJoinField() {
		return targetJoinField;
	}

	public void setTargetTable(LinkedTable targetTable, String targetJoinField, String sourceJoinField) {
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

	public LinkedTable getHead() {
		LinkedTable lt = this;
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
		LinkedTable finalLT = getHead(); 
		sb.append("SELECT " + finalLT.getTableName() + ".id");
		sb.append(" FROM " + finalLT.getTableName());
		
		if (!isHead()) {
			LinkedTable lt = this.getTargetTable();
			while (!lt.isHead()) {
				sb.append(" JOIN " + lt.getTableName() + " ON (" + lt.getTableName() + "." + lt.getSourceJoinField() 
						+ " = " + lt.getTargetTable().getTableName() + "." + lt.getTargetJoinField() + ")");
				lt = lt.getTargetTable();
			}
		}
		
		sb.append(" WHERE " + this.getTargetTable().getTableName() + "." + this.getTargetJoinField() + " IN (");
		
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