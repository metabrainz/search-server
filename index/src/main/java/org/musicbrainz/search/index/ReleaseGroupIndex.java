/* Copyright (c) 2009 Aurélien Mino
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

public class ReleaseGroupIndex extends DatabaseIndex {

    public ReleaseGroupIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public ReleaseGroupIndex() {
        super();
    }

    public String getName() {
        return "releasegroup";
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(ReleaseGroupIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return ReleaseGroupIndexField.ID;
	}
    
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM release_group");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM release_group WHERE id<=" + maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        addPreparedStatement("TAGS",
                 "SELECT release_group_tag.release_group, tag.name as tag, release_group_tag.count as count " +
                 " FROM release_group_tag " +
                 "  INNER JOIN tag ON tag=id " +
                 " WHERE release_group between ? AND ?");


        addPreparedStatement("RELEASES",
                "SELECT DISTINCT release_group, release.gid as gid, n0.name as name " +
                " FROM release " +
                "  LEFT JOIN release_name n0 ON release.name = n0.id " +
                " WHERE release_group BETWEEN ? AND ?");

        addPreparedStatement("ARTISTCREDITS",
                "SELECT rg.id as releaseGroupId, " +
                "  acn.position as pos, " +
                "  acn.join_phrase as joinphrase, " +
                "  a.gid as artistId,  " +
                "  a.comment as comment, " +
                "  an.name as artistName, " +
                "  an2.name as artistCreditName, " +
                "  an3.name as artistSortName " +
                " FROM release_group AS rg " +
                "  INNER JOIN artist_credit_name acn ON rg.artist_credit=acn.artist_credit " +
                "  INNER JOIN artist a ON a.id=acn.artist " +
                "  INNER JOIN artist_name an ON a.name=an.id " +
                "  INNER JOIN artist_name an2 ON acn.name=an2.id " +
                "  INNER JOIN artist_name an3 ON a.sort_name=an3.id " +
                " WHERE rg.id BETWEEN ? AND ?  " +
                " ORDER BY rg.id, acn.position ");          //Order by pos so come in expected order

        addPreparedStatement("RELEASEGROUPS",
                "SELECT rg.id, rg.gid, n0.name as name, lower(release_group_type.name) as type " +
                " FROM release_group AS rg " +
                "  LEFT JOIN release_name n0 ON rg.name = n0.id " +
                "  LEFT JOIN release_group_type ON rg.type = release_group_type.id " +
                " WHERE rg.id BETWEEN ? AND ?" +
                " ORDER BY rg.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {


        // Get Tags
        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs,"release_group");

        //Releases
        Map<Integer, List<ReleaseWrapper>> releases = new HashMap<Integer, List<ReleaseWrapper>>();
        st = getPreparedStatement("RELEASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int rgId = rs.getInt("release_group");
            List<ReleaseWrapper> list;
            if (!releases.containsKey(rgId)) {
                list = new LinkedList<ReleaseWrapper>();
                releases.put(rgId, list);
            } else {
                list = releases.get(rgId);
            }
            ReleaseWrapper rw = new ReleaseWrapper();
            rw.setReleaseId(rs.getString("gid"));
            rw.setReleaseName(rs.getString("name"));
            list.add(rw);
        }

        //Artist Credits
        st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        Map<Integer, ArtistCredit> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                 (rs,
                  "releaseGroupId",
                  "artistId",
                  "artistName",
                  "artistSortName",
                  "comment",
                  "joinphrase",
                  "artistCreditName");

        //ReleaseGroups
        st = getPreparedStatement("RELEASEGROUPS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, releases, artistCredits));
        }

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, List<ReleaseWrapper>> releases,
                                          Map<Integer, ArtistCredit> artistCredits) throws SQLException {
        MbDocument doc = new MbDocument();
        int id = rs.getInt("id");
        doc.addField(ReleaseGroupIndexField.ID, id);
        doc.addField(ReleaseGroupIndexField.RELEASEGROUP_ID, rs.getString("gid"));
        doc.addField(ReleaseGroupIndexField.RELEASEGROUP, rs.getString("name"));
        doc.addNonEmptyField(ReleaseGroupIndexField.TYPE, rs.getString("type"));

        //Add each release name within this release group
        if (releases.containsKey(id)) {
            for (ReleaseWrapper release : releases.get(id)) {
                doc.addFieldOrHyphen(ReleaseGroupIndexField.RELEASE, release.getReleaseName());
                doc.addFieldOrHyphen(ReleaseGroupIndexField.RELEASE_ID, release.getReleaseId());
            }
        }

        ArtistCredit ac = artistCredits.get(id);
        ArtistCreditHelper.buildIndexFieldsFromArtistCredit
               (doc,
                ac,
                ReleaseGroupIndexField.ARTIST,
                ReleaseGroupIndexField.ARTIST_NAMECREDIT,
                ReleaseGroupIndexField.ARTIST_ID,
                ReleaseGroupIndexField.ARTIST_NAME,
                ReleaseGroupIndexField.ARTIST_CREDIT);

         if (tags.containsKey(id)) {
            for (Tag tag : tags.get(id)) {
                doc.addField(LabelIndexField.TAG, tag.getName());
                doc.addField(LabelIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }

        return doc.getLuceneDocument();
    }

}


