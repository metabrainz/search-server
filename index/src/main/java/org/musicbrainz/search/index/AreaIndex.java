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
import org.musicbrainz.mmd2.AreaList;
import org.musicbrainz.mmd2.DefAreaElementInner;
import org.musicbrainz.mmd2.Label;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.MbDocument;

import java.io.IOException;
import java.sql.*;


public class AreaIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "area";

    public AreaIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public AreaIndex() { }

    public String getName() {
        return AreaIndex.INDEX_NAME;
    }

	@Override
	public IndexField getIdentifierField() {
		return AreaIndexField.ID;
	}
	
    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(TagIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM area");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM area WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {


        addPreparedStatement("AREA",
                        "SELECT a.id, a.gid, a.name, a.sort_name, at.name as type " +
                        " FROM area a" +
                        "  LEFT JOIN area_type at ON a.type = at.id " +
                        " WHERE a.id BETWEEN ? AND ? " +
                        " ORDER BY a.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        PreparedStatement st = getPreparedStatement("AREA");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs) throws SQLException {
        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        //TODO DefAreaElementInner not defined as @Xmlrootelement so we have to wrap in list
        AreaList areaList = of.createAreaList();
        DefAreaElementInner area = of.createDefAreaElementInner();
        areaList.getArea().add(area);

        doc.addField(AreaIndexField.ID, rs.getString("id"));

        String guid = rs.getString("gid");
        doc.addField(AreaIndexField.AREA_ID, guid);
        area.setId(guid);

        String name = rs.getString("name");
        doc.addField(AreaIndexField.AREA, name);
        area.setName(name);

        String sortName = rs.getString("sort_name");
        doc.addField(AreaIndexField.SORTNAME,sortName);
        area.setSortName(sortName);

        String type = rs.getString("type");
        doc.addFieldOrUnknown(AreaIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            area.setType(type);
        }

        String store = MMDSerializer.serialize(areaList);
        doc.addField(AreaIndexField.AREA_STORE, store);
        return doc.getLuceneDocument();
    }

}