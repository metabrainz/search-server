package org.musicbrainz.search.replication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EntityDependencyTree {

	protected String rootTableName;
	protected Map<String, LinkedTable> dependencies = new HashMap<String, LinkedTable>();

	public String getRootTableName() {
		return rootTableName;
	}

	public void addDependency(LinkedTable linkedTable) {

		LinkedTable head = linkedTable.getHead();
		// If this is the first dependency, use the table of the head of the path as root table
		if (rootTableName == null) {
			rootTableName = head.getTableName();
		// Otherwise, check that the new path has the same root table
		} else {
			if (!rootTableName.equals(head.getTableName())) {
				throw new IllegalArgumentException("This linked table is not compatible, " +
						"its head table (" + head.getTableName() + ") " +
						"is different from the actual root table (" + rootTableName + ")");
			}
		}
		
		// Add also all intermediate tables in path
		LinkedTable lt = linkedTable;
		while (lt != null) {
			if (!dependencies.containsKey(lt.getTableName())) {
				dependencies.put(lt.getTableName(), lt);
			}
			lt = lt.getTargetTable();
		}
	}
	
	public LinkedTable getDependency(String tableName) {
		return dependencies.get(tableName);
	}

	public Set<String> getTables() {
		return dependencies.keySet();
	}
	
	
	
}
