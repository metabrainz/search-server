package org.musicbrainz.search.update.dependencies;

import java.util.HashSet;
import java.util.Set;

import org.musicbrainz.search.update.dependencies.DatabaseIndexDependencies;
import org.musicbrainz.search.update.dependencies.DatabaseTableRelation;

import junit.framework.TestCase;

public class DatabaseTableRelationTest extends TestCase {

	public void testEndOfPath() {
		
		DatabaseTableRelation artistlt = new DatabaseTableRelation("artist");
    	DatabaseTableRelation acnlt = new DatabaseTableRelation("artist_credit_name");
    	DatabaseTableRelation worklt = new DatabaseTableRelation("work");
    	
    	artistlt.setTargetTable(acnlt, "artist", "id");
    	acnlt.setTargetTable(worklt, "artist_credit", "artist_credit");
		
		assertTrue(worklt.isHead());
		assertFalse(artistlt.isHead());
		assertFalse(acnlt.isHead());
    	
	}

	public void testGetFinalTargetTable() {
		
		DatabaseTableRelation artistlt = new DatabaseTableRelation("artist");
    	DatabaseTableRelation acnlt = new DatabaseTableRelation("artist_credit_name");
    	DatabaseTableRelation worklt = new DatabaseTableRelation("work");
    	
    	artistlt.setTargetTable(acnlt, "artist", "id");
    	acnlt.setTargetTable(worklt, "artist_credit", "artist_credit");
		
		assertEquals(worklt, acnlt.getHead());
		assertEquals(worklt, artistlt.getHead());
    	
	}

	
	public void testSQLGeneration() {
		
		// Work
		DatabaseIndexDependencies dependencyTree = new DatabaseIndexDependencies("test");

    	for (String table : new String[]{"work_alias", "work_tag"}) {
        	DatabaseTableRelation lt = new DatabaseTableRelation(table);
        	lt.setTargetTable(new DatabaseTableRelation("work"), "id", "work");
        	dependencyTree.addDependency(lt);
    	}
    	
    	DatabaseTableRelation artistlt = new DatabaseTableRelation("artist");
    	DatabaseTableRelation acnlt = new DatabaseTableRelation("artist_credit_name");
    	DatabaseTableRelation worklt = new DatabaseTableRelation("work");
    	
    	acnlt.setTargetTable(worklt, "artist_credit", "artist_credit");
    	artistlt.setTargetTable(acnlt, "artist", "id");
    	dependencyTree.addDependency(artistlt);

    	
		Set<Integer> keys = new HashSet<Integer>();
		keys.add(1);
		keys.add(2);
		keys.add(3);
    	assertEquals("SELECT work.id FROM work JOIN artist_credit_name ON (artist_credit_name.artist_credit = work.artist_credit) WHERE artist_credit_name.artist IN (1,2,3)",
    			dependencyTree.getDependency("artist").generateSQL(keys));
    	assertEquals("SELECT work.id FROM work WHERE work.artist_credit IN (1)", 
    			dependencyTree.getDependency("artist_credit_name").generateSQL(1));
    	assertNull(worklt.generateSQL(1));
    	
		
	}
	
}
