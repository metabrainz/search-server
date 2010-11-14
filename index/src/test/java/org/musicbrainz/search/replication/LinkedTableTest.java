package org.musicbrainz.search.replication;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class LinkedTableTest extends TestCase {

	public void testEndOfPath() {
		
		LinkedTable artistlt = new LinkedTable("artist");
    	LinkedTable acnlt = new LinkedTable("artist_credit_name");
    	LinkedTable worklt = new LinkedTable("work");
    	
    	artistlt.setTargetTable(acnlt, "artist", "id");
    	acnlt.setTargetTable(worklt, "artist_credit", "artist_credit");
		
		assertTrue(worklt.isHead());
		assertFalse(artistlt.isHead());
		assertFalse(acnlt.isHead());
    	
	}

	public void testGetFinalTargetTable() {
		
		LinkedTable artistlt = new LinkedTable("artist");
    	LinkedTable acnlt = new LinkedTable("artist_credit_name");
    	LinkedTable worklt = new LinkedTable("work");
    	
    	artistlt.setTargetTable(acnlt, "artist", "id");
    	acnlt.setTargetTable(worklt, "artist_credit", "artist_credit");
		
		assertEquals(worklt, acnlt.getHead());
		assertEquals(worklt, artistlt.getHead());
    	
	}

	
	public void testSQLGeneration() {
		
		// Work
		EntityDependencyTree dependencyTree = new EntityDependencyTree();

    	for (String table : new String[]{"work_alias", "work_tag"}) {
        	LinkedTable lt = new LinkedTable(table);
        	lt.setTargetTable(new LinkedTable("work"), "id", "work");
        	dependencyTree.addDependency(lt);
    	}
    	
    	LinkedTable artistlt = new LinkedTable("artist");
    	LinkedTable acnlt = new LinkedTable("artist_credit_name");
    	LinkedTable worklt = new LinkedTable("work");
    	
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
