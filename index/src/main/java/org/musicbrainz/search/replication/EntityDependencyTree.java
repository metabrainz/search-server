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

	public void setRootTableName(String rootTableName) {
		this.rootTableName = rootTableName;
	}

	public void addDependency(LinkedTable linkedTable) {
		
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
