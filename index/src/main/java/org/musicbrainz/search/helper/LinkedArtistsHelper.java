package org.musicbrainz.search.helper;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import org.musicbrainz.mmd2.Artist;
import org.musicbrainz.mmd2.DefDirection;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.mmd2.Relation;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Use for artist relation information
 *
 */
public class LinkedArtistsHelper
{
    /**
     * Construct Artist Relation query for the given artist relation table
     *
     * @param artistRelationTableName
     * @return
     */
    public static String constructArtistRelationQuery(String artistRelationTableName, String entityTableName)
    {
        return " SELECT aw.id as awid, l.id as lid, w.id as wid, w.gid, a.gid as aid, a.name as artist_name, a.sort_name as artist_sortname," +
                " lt.name as link, lat.name as attribute" +
                " FROM " +
                artistRelationTableName +
                " aw " +
                " INNER JOIN artist a ON a.id    = aw.entity0" +
                " INNER JOIN " +
                entityTableName +
                "  w ON w.id     = aw.entity1" +
                " INNER JOIN link l ON aw.link = l.id " +
                " INNER JOIN link_type lt on l.link_type=lt.id" +
                " LEFT JOIN  link_attribute la on la.link=l.id" +
                " LEFT JOIN  link_attribute_type lat on la.attribute_type=lat.id" +
                " WHERE w.id BETWEEN ? AND ?  "  +
                " ORDER BY aw.id";
    }
    /**
     * Load Artist Relations using prepared statement
     *
     * @param min
     * @param max
     * @return
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public static ArrayListMultimap<Integer, Relation> loadArtistRelations(int min, int max, PreparedStatement st) throws SQLException, IOException
    {
        ObjectFactory of = new ObjectFactory();
        ArrayListMultimap<Integer, Relation> artists = ArrayListMultimap.create();
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        int lastLinkId=-1;
        Relation lastRelation = null;
        while (rs.next()) {
            int linkId = rs.getInt("awid");

            //If have another attribute for the same relation
            if(linkId==lastLinkId) {
                Relation.AttributeList.Attribute attribute = of.createRelationAttributeListAttribute();
                attribute.setContent(rs.getString("attribute"));
                Relation.AttributeList attributeList=lastRelation.getAttributeList();
                attributeList.getAttribute().add(attribute);
            }
            //New relation (may or may not be new work but doesn't matter)
            else {
                int workId = rs.getInt("wid");

                Relation relation = of.createRelation();

                Artist artist = of.createArtist();
                artist.setId(rs.getString("aid"));
                artist.setName(rs.getString("artist_name"));
                artist.setSortName(rs.getString("artist_sortname"));

                relation.setArtist(artist);
                relation.setType(rs.getString("link"));
                relation.setDirection(DefDirection.BACKWARD);

                //Each relation may contain attributes if it does needs attribute list
                String attributeValue = rs.getString("attribute");
                if(!Strings.isNullOrEmpty(attributeValue))
                {
                    Relation.AttributeList attributeList = of.createRelationAttributeList();
                    relation.setAttributeList(attributeList);
                    Relation.AttributeList.Attribute attribute = new ObjectFactory().createRelationAttributeListAttribute();
                    attribute.setContent(attributeValue);
                    attributeList.getAttribute().add(attribute);
                }
                //Add relation
                artists.put(workId, relation);

                lastRelation=relation;
                lastLinkId=linkId;
            }
        }
        rs.close();
        return artists;
    }
}
