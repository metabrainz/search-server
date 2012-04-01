package org.musicbrainz.search.update.dependencies;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;


public class DatabaseIndexDependencies {

	private String indexName;
	private String rootTableName;
	private Map<String, DatabaseTableRelation> dependencies = new HashMap<String, DatabaseTableRelation>();

	public DatabaseIndexDependencies(String indexName) {
		this.indexName = indexName;
	}

	public String getIndexName() {
		return indexName;
	}
	
	public String getRootTableName() {
		return rootTableName;
	}
	
	public void addDependency(DatabaseTableRelation linkedTable) {

		DatabaseTableRelation head = linkedTable.getHead();
		// If this is the first dependency, use the table of the head of the path as root table
		if (rootTableName == null) {
			rootTableName = head.getSourceTableName();
		// Otherwise, check that the new path has the same root table
		} else {
			if (!rootTableName.equals(head.getSourceTableName())) {
				throw new IllegalArgumentException("This linked table is not compatible, " +
						"its head table (" + head.getSourceTableName() + ") " +
						"is different from the actual root table (" + rootTableName + ")");
			}
		}
		
		// Add also all intermediate tables in path
		DatabaseTableRelation tr = linkedTable;
		while (tr != null) {
			if (!dependencies.containsKey(tr.getSourceTableName())) {
				dependencies.put(tr.getSourceTableName(), tr);
			}
			tr = tr.getTargetTable();
		}
	}
	
	public DatabaseTableRelation getDependency(String tableName) {
		return dependencies.get(tableName);
	}

	public Set<String> getTables() {
		return dependencies.keySet();
	}
	
	/* Output of dependencies, mainly for debug purpose
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (DatabaseTableRelation tr : dependencies.values()) {
			if (tr.isHead()) {
				sb.append(tr.getSourceTableName());
				continue;
			}
			sb.append(indexName + " ==> ");
			while (tr.getTargetTable() != null) {
				sb.append(tr.getSourceTableName() + " (" + tr.getSourceJoinField() + ")");
				sb.append(" -> " + tr.getTargetTable().getSourceTableName() + " (" + tr.getTargetJoinField() + ")");
				tr = tr.getTargetTable();
				if (tr.getTargetTable() != null) sb.append(" -> ");
			}
		}
		return sb.toString();
	}
	
	public void loadFromConfigFile(InputStream InputStream) throws JDOMException, IOException {
		
		org.jdom.Document document;
    	
    	SAXBuilder sxb = new SAXBuilder();
		document = sxb.build(InputStream);

		// Define the filter that allows to get the beginning of the path
		// (i.e. the deepest <table> element)
		Filter filter = new Filter() {
			public boolean matches(Object ob) {
				if (!(ob instanceof Element)) { return false; }
				Element element = (Element) ob;
				if ("table".equals(element.getName()) && element.getChildren().isEmpty()) {
					return true;
				} 
				return false;
			}
		};

		// Iterate over each group
		Element root = document.getRootElement();
		List groups = root.getChildren("index");
				
		for (Iterator indexesIterator = groups.iterator(); indexesIterator.hasNext();) {
			
			Element group = (Element) indexesIterator.next();
			String indexName = group.getAttributeValue("name");		

			if (!indexName.equals(this.indexName)) {
				continue;
			}
			
			// Iterate over each path for the group 
			for (Iterator tablesIterator = group.getDescendants(filter); tablesIterator.hasNext();) {

				Element currElement = (Element) tablesIterator.next();
				
				DatabaseTableRelation firstLinkedtable = null;
				DatabaseTableRelation lastLinkedTable = null;
				Element lastJoin = null;
				
				while ( "table".equals(currElement.getName()) ) {
					
					DatabaseTableRelation newlt = new DatabaseTableRelation(currElement.getAttributeValue("name"));
					Attribute usedFields = currElement.getAttribute("used_fields"); 
					if (usedFields != null && !usedFields.getValue().isEmpty()) {
						newlt.setFieldsUsedForIndexing(usedFields.getValue().replace(" ", "").split(","));	
					}
					
					if (lastLinkedTable == null) {
						firstLinkedtable = newlt;
					} else {
						
						// targetJoinKey -> src_field and sourceJoinKey -> target_field
						// because we're reading it the other way
						String targetJoinKey = lastJoin.getAttributeValue("src_field");
						String sourceJoinKey = lastJoin.getAttributeValue("target_field");
						
						lastLinkedTable.setTargetTable(newlt, targetJoinKey, sourceJoinKey);
					}
					lastLinkedTable = newlt;
					lastJoin = currElement.getParentElement();
					currElement = lastJoin.getParentElement();
				}
				
				// Finally declare the whole path
				addDependency(firstLinkedtable);
			}
						
		}
		
	}
	
}
