/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Aurelien Mino

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.musicbrainz.search.index;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.replication.packet.ReplicationPacket;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;

/**
 * An abstract Index specialized in indexing information from a Database
 */
public abstract class DatabaseIndex implements Index {

    /* This is appended to the getName() method of each index to create the index folder  */
    private static final String INDEX_SUFFIX = "_index";

    protected HashMap<String, PreparedStatement> preparedStatements;
    protected Connection dbConnection;
    
    public String getFilename() {
        return getName() + INDEX_SUFFIX;
    }

    protected DatabaseIndex(Connection dbConnection) {
        this.preparedStatements = new HashMap<String, PreparedStatement>();
        this.dbConnection = dbConnection;
    }

    protected DatabaseIndex() {

    }

	@Override
	public void addMetaInformation(IndexWriter indexWriter) throws IOException {
    	MbDocument doc = new MbDocument();
    	doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
        doc.addField(MetaIndexField.LAST_UPDATED, NumericUtils.longToPrefixCoded(new Date().getTime()));
        
        Statement st;
		try {
			st = dbConnection.createStatement();
	        ResultSet rs = st.executeQuery(
	        		"SELECT current_schema_sequence, current_replication_sequence, " +
	        		"	(SELECT MAX(seqid) FROM dbmirror_pending) as max_seq_id " +
	        		"FROM replication_control;");
	        rs.next();
	        doc.addField(MetaIndexField.SCHEMA_SEQUENCE, rs.getString("current_schema_sequence"));
	        doc.addField(MetaIndexField.REPLICATION_SEQUENCE, rs.getString("current_replication_sequence"));
	        doc.addNonEmptyField(MetaIndexField.LAST_CHANGE_SEQUENCE, rs.getString("max_seq_id"));
		} catch (SQLException e) {
			System.err.println("Unable to get replication information");
		}
        
        indexWriter.addDocument(doc.getLuceneDocument());
	}
	
	public void updateMetaInformation(IndexWriter indexWriter, ReplicationPacket lastPacket, Date indexingDate) throws IOException {
		
		// Remove the old one
		Term term = new Term(MetaIndexField.META.getName(), MetaIndexField.META_VALUE);
		TermQuery query = new TermQuery(term);
		indexWriter.deleteDocuments(query);
		
		// And the new one
    	MbDocument doc = new MbDocument();
    	doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
        doc.addField(MetaIndexField.LAST_UPDATED, NumericUtils.longToPrefixCoded(indexingDate.getTime()));
        doc.addField(MetaIndexField.SCHEMA_SEQUENCE, lastPacket.getSchemaSequence());
        doc.addField(MetaIndexField.REPLICATION_SEQUENCE, lastPacket.getReplicationSequence());
        Integer lastChangeId = lastPacket.getMaxChangeId();
        if (lastChangeId != null) {
        	doc.addField(MetaIndexField.LAST_CHANGE_SEQUENCE, lastChangeId);
        }
        indexWriter.addDocument(doc.getLuceneDocument());
	}
    
    public PreparedStatement addPreparedStatement(String identifier, String SQL) throws SQLException {
        PreparedStatement st = dbConnection.prepareStatement(SQL);
        preparedStatements.put(identifier, st);
        return st;
    }

    public PreparedStatement getPreparedStatement(String identifier) {
        return preparedStatements.get(identifier);
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    public void init(IndexWriter indexWriter) throws SQLException {
    }
    
    public void destroy() throws SQLException {
        for (PreparedStatement st : preparedStatements.values() ) {
            st.close();
        }
    }

    public abstract int getNoOfRows(int maxId) throws SQLException ;
    
    /**
     * Returns the max id
     *
     * @return
     */
    public abstract int getMaxId() throws SQLException;

    /**
     * Index data on a range ids defined by min and max
     * @param indexWriter
     * @param min
     * @param max
     * @return
     */
    public abstract void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException;

    public abstract IndexField getIdentifierField();
    
}

