package org.musicbrainz.search.update.dependencies;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jdom.JDOMException;

import junit.framework.TestCase;

public class DatabaseIndexDependenciesTest extends TestCase {

	public void testLoadingFromXML() throws IOException, JDOMException {
		
		String xml = "<table_dependencies>" +
					 "	<index name='label'>" +
					 "		<table name='label'>" +
					 "			<join src_field='id' target_field='label'>" +
					 "				<table name='label_alias' />" +
					 "			</join>" +
					 "			<join src_field='id' target_field='label'>" +
					 "				<table name='label_tag' />" +
					 "			</join>" +
					 "			<join src_field='id' target_field='label'>" +
					 "				<table name='label_ipi' />" +
					 "			</join>" +
					 "		</table>"+
					 "	</index>" +
					 "</table_dependencies>";
		
        DatabaseIndexDependencies dependencies = new DatabaseIndexDependencies("label");
		dependencies.loadFromConfigFile( new ByteArrayInputStream(xml.getBytes()) );
		
		assertEquals(dependencies.getRootTableName(), "label");
		assertTrue(dependencies.getTables().contains("label_tag"));
		assertTrue(dependencies.getTables().contains("label_ipi"));
		assertTrue(dependencies.getTables().contains("label_alias"));
		
		assertNotNull(dependencies.getDependency("label"));
		assertTrue(dependencies.getDependency("label").isHead());
		
		assertNotNull(dependencies.getDependency("label_ipi"));
		assertFalse(dependencies.getDependency("label_ipi").isHead());
	}
	
}
