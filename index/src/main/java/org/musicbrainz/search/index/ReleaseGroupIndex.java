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
import org.apache.lucene.search.similarities.Similarity;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.ReleaseGroupSimilarity;
import org.musicbrainz.search.helper.*;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReleaseGroupIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "releasegroup";

    public ReleaseGroupIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public ReleaseGroupIndex() {
        super();
    }

    public String getName() {
        return ReleaseGroupIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(ReleaseGroupIndexField.class);
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
    public Similarity getSimilarity()
    {
        return new ReleaseGroupSimilarity();
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        addPreparedStatement("TAGS", TagHelper.constructTagQuery("release_group_tag", "release_group"));

        addPreparedStatement("RELEASES",
                "SELECT DISTINCT release_group, release.gid as gid, release.name, rs.name as status " +
                " FROM release " +
                "  LEFT JOIN release_status rs ON release.status = rs.id " +
                " WHERE release_group BETWEEN ? AND ?");

        addPreparedStatement("ARTISTCREDITS",
                "SELECT r.id as releaseGroupId, " +
                "  a.artist_credit, " +
                "  a.pos, " +
                "  a.joinphrase, " +
                "  a.artistId,  " +
                "  a.comment, " +
                "  a.artistName, " +
                "  a.artistCreditName, " +
                "  a.artistSortName " +
                " FROM release_group AS r " +
                "  INNER JOIN tmp_artistcredit a ON r.artist_credit=a.artist_credit " +
                " WHERE r.id BETWEEN ? AND ?  " +
                " ORDER BY r.id, a.pos");

        addPreparedStatement("ARTISTCREDITALIASES",
                "SELECT r.id as releaseGroupId," +
                " a.artist_credit, " +
                " a.pos, " +
                " aa.name," +
                " aa.sort_name," +
                " aa.primary_for_locale," +
                " aa.locale," +
                " aa.begin_date_year," +
                " aa.begin_date_month," +
                " aa.begin_date_day," +
                " aa.end_date_year," +
                " aa.end_date_month," +
                " aa.end_date_day," +
                " att.name as type" +
                " FROM release_group AS r " +
                "  INNER JOIN tmp_artistcredit a ON r.artist_credit=a.artist_credit " +
                "  INNER JOIN artist_alias aa ON a.id=aa.artist" +
                "  LEFT  JOIN artist_alias_type att on (aa.type=att.id)" +
                " WHERE r.id BETWEEN ? AND ?  " +
                " AND a.artistId!='" + ArtistIndex.VARIOUS_ARTIST_MBID +"'" +
                " AND a.artistId!='" + ArtistIndex.UNKNOWN_ARTIST_MBID  +"'" +
                " ORDER BY r.id, a.pos, aa.name");

        addPreparedStatement("SECONDARYTYPES",
                "SELECT rg.name as type, rgj.release_group as release_group " +
                " FROM release_group_secondary_type_join rgj " +
                " INNER JOIN release_group_secondary_type rg" +
                " ON rgj.secondary_type = rg.id " +
                " WHERE rgj.release_group BETWEEN ? AND ?");

        addPreparedStatement("RELEASEGROUPS",
                "SELECT rg.id, rg.gid, rg.name as name, release_group_primary_type.name as type, rg.comment " +
                " FROM release_group AS rg " +
                "  LEFT JOIN release_group_primary_type ON rg.type = release_group_primary_type.id " +
                " WHERE rg.id BETWEEN ? AND ?" +
                " ORDER BY rg.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer,List<Tag>> tags                             = TagHelper.loadTags(min, max, getPreparedStatement("TAGS"), "release_group");
        Map<Integer, List<ReleaseWrapper>> releases             = loadReleases(min, max);
        Map<Integer, ArtistCreditWrapper> artistCredits         = updateArtistCreditWithAliases(loadArtistCredits(min, max),min, max);
        Map<Integer, List<String>> secondaryTypes               = loadSecondaryTypes(min, max);
        //ReleaseGroups
        PreparedStatement st = getPreparedStatement("RELEASEGROUPS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, secondaryTypes, tags, releases, artistCredits));
        }
        rs.close();

    }

    /**
     * Load Releases
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<ReleaseWrapper>> loadReleases(int min, int max) throws SQLException, IOException {

        //Releases
        Map<Integer, List<ReleaseWrapper>> releases = new HashMap<Integer, List<ReleaseWrapper>>();
        PreparedStatement st = getPreparedStatement("RELEASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
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
            rw.setStatus(rs.getString("status"));
            list.add(rw);
        }
        rs.close();
        return releases;
    }

    /**
     * Load Artist Credits
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, ArtistCreditWrapper> loadArtistCredits(int min, int max) throws SQLException, IOException {

        //Artist Credits
        PreparedStatement st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, ArtistCreditWrapper> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults(rs, "releaseGroupId", "artist_Credit", "artistId", "artistName", "artistSortName", "comment", "joinphrase", "artistCreditName");
        rs.close();
        return artistCredits;
    }

    private Map<Integer, ArtistCreditWrapper> updateArtistCreditWithAliases(
            Map<Integer, ArtistCreditWrapper> artistCredits,
            int min,
            int max)
            throws SQLException, IOException {

        //Artist Credit Aliases
        PreparedStatement st = getPreparedStatement("ARTISTCREDITALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        return ArtistCreditHelper.updateArtistCreditWithAliases(artistCredits,"releaseGroupId", rs);
    }

    /**
     * Load work iswcs
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<String>> loadSecondaryTypes(int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> secondaryTypes = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("SECONDARYTYPES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int rgId = rs.getInt("release_group");

            List<String> list;
            if (!secondaryTypes.containsKey(rgId)) {
                list = new LinkedList<String>();
                secondaryTypes.put(rgId, list);
            } else {
                list = secondaryTypes.get(rgId);
            }
            list.add(rs.getString("type"));
        }
        rs.close();
        return secondaryTypes;
    }


    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<String>> secondaryTypes,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, List<ReleaseWrapper>> releases,
                                          Map<Integer, ArtistCreditWrapper> artistCredits) throws SQLException {
        MbDocument doc = new MbDocument();
        int id = rs.getInt("id");
        doc.addField(ReleaseGroupIndexField.ID, id);
        doc.addField(ReleaseGroupIndexField.RELEASEGROUP_ID, rs.getString("gid"));
        String name = rs.getString("name");
        doc.addField(ReleaseGroupIndexField.RELEASEGROUP, name);
        doc.addField(ReleaseGroupIndexField.RELEASEGROUP_ACCENT, name);

        String primaryType = rs.getString("type");
        doc.addFieldOrUnknown(ReleaseGroupIndexField.PRIMARY_TYPE, primaryType);

        if (secondaryTypes.containsKey(id)) {
            for (String secondaryType : secondaryTypes.get(id)) {
                doc.addField(ReleaseGroupIndexField.SECONDARY_TYPE, secondaryType);
            }
        }
        String type = ReleaseGroupHelper.calculateOldTypeFromPrimaryType(primaryType, secondaryTypes.get(id));
        doc.addFieldOrUnknown(ReleaseGroupIndexField.TYPE, type);

        doc.addFieldOrNoValue(ReleaseGroupIndexField.COMMENT, rs.getString("comment"));

        //Add each release name within this release group
        if (releases.containsKey(id)) {
            for (ReleaseWrapper release : releases.get(id)) {
                doc.addFieldOrNoValue(ReleaseGroupIndexField.RELEASE, release.getReleaseName());
                doc.addFieldOrNoValue(ReleaseGroupIndexField.RELEASE_ID, release.getReleaseId());
                doc.addFieldOrNoValue(ReleaseGroupIndexField.RELEASESTATUS, release.getStatus());
            }
            doc.addNumericField(ReleaseGroupIndexField.NUM_RELEASES, releases.get(id).size());
        }
        else {
            //No releases in releasegroup
            doc.addNumericField(ReleaseGroupIndexField.NUM_RELEASES, 0);
        }


        ArtistCreditWrapper ac = artistCredits.get(id);
        if(ac!=null) {
            ArtistCreditHelper.buildIndexFieldsFromArtistCredit
               (doc,
                ac.getArtistCredit(),
                ReleaseGroupIndexField.ARTIST,
                ReleaseGroupIndexField.ARTIST_NAMECREDIT,
                ReleaseGroupIndexField.ARTIST_ID,
                ReleaseGroupIndexField.ARTIST_NAME,
                ReleaseGroupIndexField.ARTIST_CREDIT);
         }
         else {
            System.out.println("\nNo artist credit found for releasegroup:"+rs.getString("gid"));
         }

         if (tags.containsKey(id)) {
            for (Tag tag : tags.get(id)) {
                doc.addField(ReleaseGroupIndexField.TAG, tag.getName());
                doc.addField(ReleaseGroupIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }

        return doc.getLuceneDocument();
    }

}


