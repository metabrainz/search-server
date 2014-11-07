/* Copyright (c) 2013 Paul Taylor
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

import com.google.common.base.Strings;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.helper.AliasHelper;
import org.musicbrainz.search.helper.TagHelper;
import org.postgresql.geometric.PGpoint;

import java.io.IOException;
import java.sql.*;
import java.util.*;


public class PlaceIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "place";

    public static boolean isUsingH2Db = false;

    public PlaceIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public PlaceIndex() { }

    public String getName() {
        return PlaceIndex.INDEX_NAME;
    }

	@Override
	public IndexField getIdentifierField() {
		return PlaceIndexField.ID;
	}
	
    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(PlaceIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM place");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM place WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {


        addPreparedStatement("PLACE",
                        "SELECT p.coordinates, p.id, p.gid, p.name, p.address, pt.name as type, " +
                        "  p.begin_date_year, p.begin_date_month, p.begin_date_day, " +
                        "  p.end_date_year, p.end_date_month, p.end_date_day, p.ended, p.comment, " +
                        "  a1.gid as area_gid, a1.name as area_name " +
                        " FROM place p" +
                        "  LEFT JOIN place_type pt ON p.type = pt.id " +
                        "  LEFT JOIN area a1 on p.area = a1.id" +
                        " WHERE p.id BETWEEN ? AND ? " +
                        " ORDER BY p.id");

        addPreparedStatement("ALIASES", AliasHelper.constructAliasQuery("place"));
        addPreparedStatement("TAGS", TagHelper.constructTagQuery("place_tag", "place"));


    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        // Get place aliases
        Map<Integer, Set<Alias>> aliases = AliasHelper.completeFromDbResults(min, max, getPreparedStatement("ALIASES"));
        Map<Integer,List<Tag>> tags = TagHelper.loadTags(min, max, getPreparedStatement("TAGS"), "place");

        PreparedStatement st = getPreparedStatement("PLACE");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, aliases));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, Set<Alias>> aliases) throws SQLException {

        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        Place     place = of.createPlace();

        int placeId = rs.getInt("id");
        doc.addField(PlaceIndexField.ID, placeId);

        String guid = rs.getString("gid");
        doc.addField(PlaceIndexField.PLACE_ID, guid);
        place.setId(guid);

        String name = rs.getString("name");
        doc.addField(PlaceIndexField.PLACE, name);
        place.setName(name);

        String comment = rs.getString("comment");
        if (!Strings.isNullOrEmpty(comment)) {
            doc.addField(PlaceIndexField.COMMENT, comment);
            place.setDisambiguation(comment);
        }

        String type = rs.getString("type");
        doc.addFieldOrUnknown(PlaceIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            place.setType(type);
        }


        float latitude = 0.0f;
        float longitude= 0.0f;
        boolean isCoordinates = false;
        if(!isUsingH2Db)
        {
            PGpoint pgPoint = (PGpoint)rs.getObject("coordinates");
            if(pgPoint!=null)
            {
                latitude=(float)pgPoint.x;
                longitude=(float)pgPoint.y;
                isCoordinates=true;
            }
        }
        else
        {
            Object[] coords = (Object[])rs.getObject("coordinates");
            if(coords!=null)
            {
                latitude=Float.valueOf(coords[0].toString());
                longitude=Float.valueOf(coords[1].toString());
                isCoordinates=true;
            }
        }

        if(isCoordinates)
        {
            Coordinates coordinates = of.createCoordinates();
            coordinates.setLatitude(String.valueOf(latitude));
            coordinates.setLongitude(String.valueOf(longitude));
            place.setCoordinates(coordinates);
            doc.addNumericField(PlaceIndexField.LAT,latitude);
            doc.addNumericField(PlaceIndexField.LONG,longitude);
        }

        String address = rs.getString("address");
        if (!Strings.isNullOrEmpty(address)) {
            doc.addField(PlaceIndexField.ADDRESS, address);
            place.setAddress(address);
        }
        String areaId = rs.getString("area_gid");
        if(areaId!=null) {
            DefAreaElementInner area = of.createDefAreaElementInner();
            area.setId(areaId);
            String areaName = rs.getString("area_name");
            area.setName(areaName);
            doc.addFieldOrNoValue(ArtistIndexField.AREA, areaName);
            area.setSortName(areaName);
            place.setArea(area);
        }
        else {
            doc.addField(ArtistIndexField.AREA, Index.NO_VALUE);
        }


        boolean ended = rs.getBoolean("ended");
        doc.addFieldOrUnknown(ArtistIndexField.ENDED, Boolean.toString(ended));

        String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
        doc.addNonEmptyField(ArtistIndexField.BEGIN, begin);

        String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
        doc.addNonEmptyField(ArtistIndexField.END, end);

        LifeSpan lifespan = of.createLifeSpan();
        place.setLifeSpan(lifespan);
        if(!Strings.isNullOrEmpty(begin)) {
            lifespan.setBegin(begin);
        }
        if(!Strings.isNullOrEmpty(end)) {
            lifespan.setEnd(end);
        }
        lifespan.setEnded(Boolean.toString(ended));

        if (aliases.containsKey(placeId))
        {
            place.setAliasList(AliasHelper.addAliasesToDocAndConstructAliasList(of, doc, aliases, placeId, SeriesIndexField.ALIAS));
        }

        if (tags.containsKey(placeId))
        {
            place.setTagList(TagHelper.addTagsToDocAndConstructTagList(of, doc, tags, placeId, PlaceIndexField.TAG ));
        }

        String store = MMDSerializer.serialize(place);
        doc.addField(PlaceIndexField.PLACE_STORE, store);
        return doc.getLuceneDocument();
    }

}