/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Aurélien Mino

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.mmd2.Tag;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class WorkIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "work";

    public WorkIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public WorkIndex() { }

    public String getName() {
        return WorkIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(WorkIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return WorkIndexField.ID;
	}
	
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM work");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM work WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        addPreparedStatement("TAGS",
                       "SELECT work_tag.work, tag.name as tag, work_tag.count as count " +
                       " FROM work_tag " +
                       "  INNER JOIN tag ON tag=id " +
                       " WHERE work between ? AND ?");

        addPreparedStatement("ARTISTCREDITS",
                "SELECT w.id as wid, " +
                "  a.pos, " +
                "  a.joinphrase, " +
                "  a.artistId,  " +
                "  a.comment, " +
                "  a.artistName, " +
                "  a.artistCreditName, " +
                "  a.artistSortName " +
                " FROM work AS w " +
                "  INNER JOIN tmp_artistcredit a ON w.artist_credit=a.artist_credit " +
                " WHERE w.id BETWEEN ? AND ?  " +
                " ORDER BY w.id, a.pos");

        addPreparedStatement("ALIASES",
                "SELECT work_alias.work as work, n.name as alias " +
                " FROM work_alias " +
                "  JOIN work_name n ON (work_alias.name = n.id) " +
                " WHERE work BETWEEN ? AND ?");

        addPreparedStatement("WORKS",
                        "SELECT w.id as wid, w.gid, wn.name as name, lower(wt.name) as type, iswc " +
                        " FROM work AS w " +
                        "  LEFT JOIN work_name wn ON w.name = wn.id " +
                        "  LEFT JOIN work_type wt ON w.type = wt.id " +
                        " WHERE w.id BETWEEN ? AND ? " +
                        " ORDER BY w.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        // Get Tags
        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs,"work");
        rs.close();

        //Artist Credits
        st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        Map<Integer, ArtistCredit> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                     (rs,
                      "wid",
                      "artistId",
                      "artistName",
                      "artistSortName",
                      "comment",
                      "joinphrase",
                      "artistCreditName");
        rs.close();

        // Get works aliases
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int workId = rs.getInt("work");

            List<String> list;
            if (!aliases.containsKey(workId)) {
                list = new LinkedList<String>();
                aliases.put(workId, list);
            } else {
                list = aliases.get(workId);
            }
            list.add(rs.getString("alias"));
        }
        rs.close();

        //Works
        st = getPreparedStatement("WORKS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, artistCredits, aliases));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, ArtistCredit> artistCredits,
                                          Map<Integer, List<String>> aliases) throws SQLException {
        MbDocument doc = new MbDocument();
        int id = rs.getInt("wid");
        doc.addField(WorkIndexField.ID, id);
        doc.addField(WorkIndexField.WORK_ID, rs.getString("gid"));
        doc.addField(WorkIndexField.WORK, rs.getString("name"));
        doc.addNonEmptyField(WorkIndexField.TYPE, rs.getString("type"));
        doc.addNonEmptyField(WorkIndexField.ISWC, rs.getString("iswc"));

        ArtistCredit ac = artistCredits.get(id);
        ArtistCreditHelper.buildIndexFieldsFromArtistCredit
               (doc,
                ac,
                WorkIndexField.ARTIST,
                WorkIndexField.ARTIST_NAMECREDIT,
                WorkIndexField.ARTIST_ID,
                WorkIndexField.ARTIST_NAME,
                WorkIndexField.ARTIST_CREDIT);

        if (aliases.containsKey(id)) {
            for (String alias : aliases.get(id)) {
                doc.addField(WorkIndexField.ALIAS, alias);
            }
        }

        if (tags.containsKey(id)) {
            for (Tag tag : tags.get(id)) {
                doc.addField(WorkIndexField.TAG, tag.getName());
                doc.addField(WorkIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }

        return doc.getLuceneDocument();
    }

}