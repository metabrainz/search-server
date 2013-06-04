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
import java.sql.*;
import java.util.*;

public class LabelIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "label";

    private static final String DELETED_LABEL_MBID = "f43e252d-9ebf-4e8e-bba8-36d080756cc1";

    public LabelIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public LabelIndex() {
    }


    public String getName() {
        return LabelIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(LabelIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return LabelIndexField.ID;
	}
    
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM label");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
    	PreparedStatement st = dbConnection.prepareStatement(
    		"SELECT count(*) FROM label WHERE id <= ? AND gid <> ?::uuid");
    	st.setInt(1, maxId);
    	st.setString(2, DELETED_LABEL_MBID);
    	ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public Similarity getSimilarity()
    {
        return new MusicbrainzSimilarity();
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        addPreparedStatement("TAGS",
                "SELECT label_tag.label, tag.name as tag, label_tag.count as count " +
                " FROM label_tag " +
                "  INNER JOIN tag ON tag=id " +
                " WHERE label between ? AND ?");

        addPreparedStatement("ALIASES",
                "SELECT a.label as label, n.name as alias, sn.name as alias_sortname, a.primary_for_locale, a.locale, att.name as type," +
                        "a.begin_date_year, a.begin_date_month, a.begin_date_day, a.end_date_year, a.end_date_month, a.end_date_day" +
                        " FROM label_alias a" +
                        "  JOIN label_name n ON (a.name = n.id) " +
                        "  JOIN label_name sn ON (a.sort_name = sn.id) " +
                        "  LEFT JOIN label_alias_type att on (a.type=att.id)" +
                        " WHERE label BETWEEN ? AND ?" +
                        " ORDER BY label, alias, alias_sortname");


        addPreparedStatement("LABELS",
                "SELECT label.id, gid, n0.name as name, n1.name as sort_name, " +
                "  label_type.name as type, begin_date_year, begin_date_month, begin_date_day, " +
                "  end_date_year, end_date_month, end_date_day, ended," +
                "  comment, label_code, lower(i.code) as country " +
                " FROM label " +
                "  LEFT JOIN label_name n0 ON label.name = n0.id " +
                "  LEFT JOIN label_name n1 ON label.sort_name = n1.id " +
                "  LEFT JOIN label_type ON label.type = label_type.id " +
                "  LEFT JOIN iso_3166_1 i on label.area=i.area" +
                " WHERE label.id BETWEEN ? AND ?");

        addPreparedStatement("IPICODES",
                "SELECT ipi, label " +
                        " FROM label_ipi  " +
                        " WHERE label between ? AND ?");

    }

    private  Map<Integer, List<String>> loadIpiCodes(int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> ipiCodes = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("IPICODES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("label");

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
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs,"label");
        rs.close();

        // IPI Codes
        Map<Integer, List<String>> ipiCodes = loadIpiCodes(min,max);


        // Get labels aliases
        //Aliases
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("label");
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

        // Get labels
        st = getPreparedStatement("LABELS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();

        while (rs.next()) {
            if(rs.getString("gid").equals(DELETED_LABEL_MBID))
            {
                continue;
            }
            indexWriter.addDocument(documentFromResultSet(rs, tags, ipiCodes, aliases));
        }
        rs.close();
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, List<String>> ipiCodes,
                                          Map<Integer, Set<Alias>> aliases) throws SQLException {

        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        Label label = of.createLabel();

        int labelId = rs.getInt("id");
        doc.addField(LabelIndexField.ID, labelId);

        String labelGuid = rs.getString("gid");
        doc.addField(LabelIndexField.LABEL_ID, labelGuid);
        label.setId(labelGuid);

        String name=rs.getString("name");
        doc.addField(LabelIndexField.LABEL,name );
        label.setName(name);

        //Accented artist
        doc.addField(LabelIndexField.LABEL_ACCENT, name );

        String sortName = rs.getString("sort_name");
        doc.addField(LabelIndexField.SORTNAME, sortName);
        label.setSortName(sortName);


        String type = rs.getString("type");
        doc.addFieldOrUnknown(LabelIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            label.setType(type);
        }

        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(LabelIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
            label.setDisambiguation(comment);
        }

        String country = rs.getString("country");
        doc.addFieldOrUnknown(LabelIndexField.COUNTRY, country);
        if (!Strings.isNullOrEmpty(country)) {
            label.setCountry(country.toUpperCase(Locale.US));
        }

        boolean ended = rs.getBoolean("ended");
        doc.addFieldOrUnknown(LabelIndexField.ENDED, Boolean.toString(ended));

        String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
        doc.addNonEmptyField(LabelIndexField.BEGIN, begin);

        String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
        doc.addNonEmptyField(LabelIndexField.END, end);

        LifeSpan lifespan = of.createLifeSpan();
        label.setLifeSpan(lifespan);
        if(!Strings.isNullOrEmpty(begin)) {
            lifespan.setBegin(begin);
        }
        if(!Strings.isNullOrEmpty(end)) {
            lifespan.setEnd(end);
        }
        lifespan.setEnded(Boolean.toString(ended));


        int labelcode = rs.getInt("label_code");
        if (labelcode > 0) {
            doc.addField(LabelIndexField.CODE, labelcode);
            label.setLabelCode(BigInteger.valueOf(labelcode));
        }
        else {
            doc.addField(LabelIndexField.CODE,Index.NO_VALUE);
        }

        if (aliases.containsKey(labelId)) {
            AliasList aliasList = of.createAliasList();
            for (Alias nextAlias : aliases.get(labelId)) {
                doc.addField(LabelIndexField.ALIAS, nextAlias.getContent());
                if(!nextAlias.getSortName().equals(nextAlias.getContent())) {
                    doc.addField(LabelIndexField.ALIAS, nextAlias.getSortName());
                }
                aliasList.getAlias().add(nextAlias);
            }
            label.setAliasList(aliasList);
        }


        if (tags.containsKey(labelId)) {
            TagList tagList = of.createTagList();
            for (Tag nextTag : tags.get(labelId)) {
                Tag tag = of.createTag();
                doc.addField(LabelIndexField.TAG, nextTag.getName());
                tag.setName(nextTag.getName());
                tag.setCount(new BigInteger(nextTag.getCount().toString()));
                tagList.getTag().add(tag);
            }
           label.setTagList(tagList);
        }

        if (ipiCodes.containsKey(labelId)) {
            IpiList ipiList = of.createIpiList();
            for (String ipiCode : ipiCodes.get(labelId)) {
                doc.addField(LabelIndexField.IPI, ipiCode);
                ipiList.getIpi().add(ipiCode);
            }
            label.setIpiList(ipiList);
        }

        LabelBoostDoc.boost(labelGuid, doc);

        String store = MMDSerializer.serialize(label);
        doc.addField(LabelIndexField.LABEL_STORE, store);

        return doc.getLuceneDocument();
    }

}
