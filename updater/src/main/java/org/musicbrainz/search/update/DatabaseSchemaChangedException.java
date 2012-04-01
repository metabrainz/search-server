package org.musicbrainz.search.update;

public class DatabaseSchemaChangedException extends Exception {

	private String message;
	
	public DatabaseSchemaChangedException(int indexSchemaSequence, int changesetSchemaSequence) {
		this.message = "Search index built with schema " + indexSchemaSequence + 
				", changes come from schema "+changesetSchemaSequence;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
}
