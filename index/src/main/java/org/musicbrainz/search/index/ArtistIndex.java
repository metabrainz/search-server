/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Lukas Lalinsky

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

import com.google.common.base.Strings;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.similarities.Similarity;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.sql.*;
import java.util.*;

public class ArtistIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "artist";

    //Special purpose Artists
    public  static final String DELETED_ARTIST_MBID = "c06aa285-520e-40c0-b776-83d2c9e8a6d1";
    public  static final String VARIOUS_ARTIST_MBID = "89ad4ac3-39f7-470e-963a-56509c546377";
    public  static final String UNKNOWN_ARTIST_MBID = "125ec42a-7229-4250-afc5-e057484327fe";

    private static final String PERSON = "Person";

    private CharsetEncoder latinEncoder;

    private void initDecoders() {
        latinEncoder = Charset.forName("ISO-8859-1").newEncoder();
        latinEncoder.onMalformedInput(CodingErrorAction.REPORT);
        latinEncoder.onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    public ArtistIndex(Connection dbConnection) throws SQLException {
        super(dbConnection);
        initDecoders();
    }

    public ArtistIndex() {
        initDecoders();
    }

    public String getName() {
        return ArtistIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return super.getAnalyzer(ArtistIndexField.class);
    }

    @Override
    public IndexField getIdentifierField() {
        return ArtistIndexField.ID;
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM artist");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        PreparedStatement st = dbConnection.prepareStatement(
                "SELECT count(*) FROM artist WHERE id <= ? AND gid <> ?::uuid");
        st.setInt(1, maxId);
        st.setString(2, DELETED_ARTIST_MBID);
        ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public Similarity getSimilarity() {
        return new MusicbrainzSimilarity();
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {


        addPreparedStatement("TAGS",
                "SELECT artist_tag.artist, tag.name as tag, artist_tag.count as count " +
                        " FROM artist_tag " +
                        "  INNER JOIN tag ON tag=id " +
                        " WHERE artist between ? AND ?");

        addPreparedStatement("ALIASES",
                "SELECT a.artist as artist, a.name as alias, a.sort_name as alias_sortname, a.primary_for_locale, a.locale, att.name as type," +
                        "a.begin_date_year, a.begin_date_month, a.begin_date_day, a.end_date_year, a.end_date_month, a.end_date_day" +
                        " FROM artist_alias a" +
                        "  LEFT JOIN artist_alias_type att on (a.type=att.id)" +
                        " WHERE artist BETWEEN ? AND ?" +
                        " ORDER BY artist, alias, alias_sortname");

        addPreparedStatement("ARTISTCREDITS",
                        "SELECT artist as artist, name as artistcredit " +
                        " FROM artist_credit_name " +
                        " WHERE artist BETWEEN ? AND ? ");


        addPreparedStatement("ARTISTS",
                "SELECT a.id, a.gid as gid, a.name, a.sort_name, " +
                        "  artist_type.name as type, a.begin_date_year, a.begin_date_month, a.begin_date_day, " +
                        "  a.end_date_year, a.end_date_month, a.end_date_day,a.ended, " +
                        "  a.comment, lower(i.code) as country, lower(gender.name) as gender," +
                        "  a1.gid as area_gid, a1.name as area_name, a1.sort_name as area_sortname, " +
                        "  a2.gid as beginarea_gid, a2.name as beginarea_name, a2.sort_name as beginarea_sortname, " +
                        "  a3.gid as endarea_gid, a3.name as endarea_name, a3.sort_name as endarea_sortname" +
                        " FROM artist a " +
                        "  LEFT JOIN artist_type ON a.type = artist_type.id " +
                        "  LEFT JOIN iso_3166_1 i on a.area=i.area" +
                        "  LEFT JOIN gender ON a.gender=gender.id " +
                        "  LEFT JOIN area a1 on a.area = a1.id" +
                        "  LEFT JOIN area a2 on a.begin_area = a2.id" +
                        "  LEFT JOIN area a3 on a.end_area = a3.id" +
                " WHERE a.id BETWEEN ? AND ?");

        addPreparedStatement("IPICODES",
                "SELECT ipi, artist " +
                        " FROM artist_ipi  " +
                        " WHERE artist between ? AND ?");

    }

    private Map<Integer, List<String>> loadIpiCodes(int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> ipiCodes = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("IPICODES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("artist");

            List<String> list;
            if (!ipiCodes.containsKey(artistId)) {
                list = new LinkedList<String>();
                ipiCodes.put(artistId, list);
            } else {
                list = ipiCodes.get(artistId);
            }
            list.add(rs.getString("ipi"));
        }
        rs.close();
        return ipiCodes;
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        // Get Tags
        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs, "artist");
        rs.close();

        // IPI Codes
        Map<Integer, List<String>> ipiCodes = loadIpiCodes(min, max);

        //Aliases
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("artist");
            Set<Alias> list;
            if (!aliases.containsKey(artistId)) {
                list = new LinkedHashSet<Alias>();
                aliases.put(artistId, list);
            } else {
                list = aliases.get(artistId);
            }
            Alias alias = of.createAlias();
            alias.setContent(rs.getString("alias"));
            alias.setSortName(rs.getString("alias_sortname"));
            boolean isPrimary = rs.getBoolean("primary_for_locale");
            if(isPrimary) {
                alias.setPrimary("primary");
            }
            String locale = rs.getString("locale");
            if(locale!=null) {
                alias.setLocale(locale);
            }
            String type = rs.getString("type");
            if(type!=null) {
                alias.setType(type);
            }

            String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
            if(!Strings.isNullOrEmpty(begin))  {
                alias.setBeginDate(begin);
            }

            String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
            if(!Strings.isNullOrEmpty(end))  {
                alias.setEndDate(end);
            }
            list.add(alias);
        }
        rs.close();

        //Artist Credits)
        Map<Integer, Set<String>> artistCredits = new HashMap<Integer, Set<String>>();
        st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("artist");
            Set<String> list;
            if (!artistCredits.containsKey(artistId)) {
                list = new HashSet<String>();
                artistCredits.put(artistId, list);
            } else {
                list = artistCredits.get(artistId);
            }
            list.add(rs.getString("artistcredit"));
        }
        rs.close();

        st = getPreparedStatement("ARTISTS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            if (rs.getString("gid").equals(DELETED_ARTIST_MBID)) {
                continue;
            }
            indexWriter.addDocument(documentFromResultSet(rs, tags, ipiCodes, aliases, artistCredits));
        }
        rs.close();
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<Tag>> tags,
                                          Map<Integer, List<String>> ipiCodes,
                                          Map<Integer, Set<Alias>> aliases,
                                          Map<Integer, Set<String>> artistCredits) throws SQLException {

        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        Artist artist = of.createArtist();

        int artistId = rs.getInt("id");
        String artistGuid = rs.getString("gid");
        doc.addField(ArtistIndexField.ID, artistId);
        doc.addField(ArtistIndexField.ARTIST_ID, artistGuid);
        artist.setId(artistGuid);

        String artistName = rs.getString("name");
        doc.addField(ArtistIndexField.ARTIST, artistName);
        artist.setName(artistName);

        //Accented artist
        doc.addField(ArtistIndexField.ARTIST_ACCENT, artistName);

        String sortName = rs.getString("sort_name");
        doc.addField(ArtistIndexField.SORTNAME, sortName);
        artist.setSortName(sortName);

        String type = rs.getString("type");
        doc.addFieldOrUnknown(ArtistIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            artist.setType(type);
        }

        boolean ended = rs.getBoolean("ended");
        doc.addFieldOrUnknown(ArtistIndexField.ENDED, Boolean.toString(ended));

        String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
        doc.addNonEmptyField(ArtistIndexField.BEGIN, begin);

        String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
                doc.addNonEmptyField(ArtistIndexField.END, end);

        LifeSpan lifespan = of.createLifeSpan();
        artist.setLifeSpan(lifespan);
        if(!Strings.isNullOrEmpty(begin)) {
            lifespan.setBegin(begin);
        }
        if(!Strings.isNullOrEmpty(end)) {
            lifespan.setEnd(end);
        }
        lifespan.setEnded(Boolean.toString(ended));

        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(ArtistIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
            artist.setDisambiguation(comment);
        }

        String country = rs.getString("country");
        doc.addFieldOrUnknown(ArtistIndexField.COUNTRY, country);
        if (!Strings.isNullOrEmpty(country)) {
            artist.setCountry(country.toUpperCase(Locale.US));
        }

        String areaId = rs.getString("area_gid");
        if(areaId!=null) {
            DefAreaElementInner area = of.createDefAreaElementInner();
            area.setId(areaId);
            String areaName = rs.getString("area_name");
            doc.addFieldOrNoValue(ArtistIndexField.AREA, areaName);
            area.setName(areaName);
            String areaSortName = rs.getString("area_sortname");
            area.setSortName(areaSortName);
            artist.setArea(area);
        }
        else {
            doc.addField(ArtistIndexField.AREA, Index.NO_VALUE);
        }

        String beginAreaId = rs.getString("beginarea_gid");
        if(beginAreaId!=null) {
            DefAreaElementInner area = of.createDefAreaElementInner();
            area.setId(beginAreaId);
            String areaName = rs.getString("beginarea_name");
            doc.addFieldOrNoValue(ArtistIndexField.BEGIN_AREA, areaName);
            area.setName(areaName);
            String areaSortName = rs.getString("beginarea_sortname");
            area.setSortName(areaSortName);
            artist.setBeginArea(area);
        }
        else {
            doc.addField(ArtistIndexField.BEGIN_AREA, Index.NO_VALUE);
        }


        String endAreaId = rs.getString("endarea_gid");
        if(endAreaId!=null) {
            DefAreaElementInner area = of.createDefAreaElementInner();
            area.setId(endAreaId);
            String areaName = rs.getString("endarea_name");
            doc.addFieldOrNoValue(ArtistIndexField.END_AREA, areaName);
            area.setName(areaName);
            String areaSortName = rs.getString("endarea_sortname");
            area.setSortName(areaSortName);
            artist.setEndArea(area);
        }
        else {
            doc.addField(ArtistIndexField.END_AREA, Index.NO_VALUE);
        }

        String gender = rs.getString("gender");
        if (gender != null) {
            doc.addField(ArtistIndexField.GENDER, gender);
            artist.setGender(gender);
        } else {
            if ((type != null) && (type.equalsIgnoreCase(ArtistType.PERSON.getName()))) {
                doc.addField(ArtistIndexField.GENDER, Index.UNKNOWN);
            }
        }

        if (aliases.containsKey(artistId)) {
            AliasList aliasList = of.createAliasList();
            for (Alias nextAlias : aliases.get(artistId)) {
                doc.addField(ArtistIndexField.ALIAS, nextAlias.getContent());
                if(!Strings.isNullOrEmpty(nextAlias.getSortName())) {
                    if(!nextAlias.getSortName().equals(nextAlias.getContent())) {
                        doc.addField(ArtistIndexField.ALIAS, nextAlias.getSortName());
                    }
                }
                aliasList.getAlias().add(nextAlias);
            }
            artist.setAliasList(aliasList);
        }

        //Artist Credits are added for search only
        if (artistCredits.containsKey(artistId)) {
            for (String artistCredit : artistCredits.get(artistId)) {

                //Add alias even if same as artist
                doc.addField(ArtistIndexField.ALIAS, artistCredit);
            }
        }

        addArtistInitialized(type, artistName, sortName, doc);

        if (tags.containsKey(artistId)) {
            TagList tagList = of.createTagList();
            for (Tag nextTag : tags.get(artistId)) {
                Tag tag = of.createTag();
                doc.addField(ArtistIndexField.TAG, nextTag.getName());
                tag.setName(nextTag.getName());
                tag.setCount(new BigInteger(nextTag.getCount().toString()));
                tagList.getTag().add(tag);
            }
            artist.setTagList(tagList);
        }

        if (ipiCodes.containsKey(artistId)) {
            IpiList ipiList = of.createIpiList();
            for (String ipiCode : ipiCodes.get(artistId)) {
                doc.addField(ArtistIndexField.IPI, ipiCode);
                ipiList.getIpi().add(ipiCode);
            }
            artist.setIpiList(ipiList);
        }


        ArtistBoostDoc.boost(artistGuid, doc);

        String store = MMDSerializer.serialize(artist);
        doc.addField(ArtistIndexField.ARTIST_STORE, store);

        return doc.getLuceneDocument();
    }

    /**
     * Add artist with first name as an initial and then last name as an alias if the artist if of type Person,
     * can be latin encoded and has a sortname containing a comma indicating probably a real name rather than a
     * performance name
     *
     * This is only used for searching, not returned in the list of aliases as output because it is not actually
     * a real alias, just one we have added to help searching
     *
     * @param type
     * @param artistName
     * @param sortName
     * @param doc
     */

    private void addArtistInitialized(String type, String artistName, String sortName, MbDocument doc) {
        if (type == null || !type.equals(PERSON)) {
            return;
        }

        try {
            latinEncoder.encode(CharBuffer.wrap(artistName));
        } catch (CharacterCodingException cdd) {
            return;
        }

        if (sortName.contains(",")) {
            String[] names = artistName.split(" ");
            if (names.length >= 2) {
                doc.addField(ArtistIndexField.ALIAS, names[0].substring(0, 1) + ' ' + names[names.length - 1]);
            }
        }
    }
}
