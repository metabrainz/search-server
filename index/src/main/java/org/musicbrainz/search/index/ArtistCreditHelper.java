/*
 * MusicBrainz Search Server
 * Copyright (C) 2010  Paul Taylor

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

import org.musicbrainz.mmd2.Artist;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.mmd2.NameCredit;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.MbDocument;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Utility class for manipulating Artist Credits
 */
public class ArtistCreditHelper {

    /**
     * Build the full artist credit name as it (probably) appears on the actual release
     *
     * @param artistCredit
     * @return
     */
    public static String buildFullArtistCreditName(ArtistCredit artistCredit) {
        StringBuffer sb = new StringBuffer();
        for (NameCredit nameCredit : artistCredit.getNameCredit()) {
            if (nameCredit.getName() != null) {
                sb.append(nameCredit.getName());
            } else {
                sb.append(nameCredit.getArtist().getName());
            }
            if (nameCredit.getJoinphrase() != null) {
                sb.append(' ' + nameCredit.getJoinphrase() + ' ');
            }
        }
        return sb.toString();
    }

    /**
     * Convert the serialize artist credit back into ArtistCredit object
     *
     * @param artistCreditSerilized
     * @return
     */
    public static ArtistCredit unserialize(String artistCreditSerilized) {
        return (ArtistCredit) MMDSerializer.unserialize(artistCreditSerilized, ArtistCredit.class);
    }

    /**
     * Complete Artist Credits for Database results
     *
     * @param rs
     * @param entityKey
     * @return
     * @throws SQLException
     */
    public static Map<Integer, ArtistCredit> completeArtistCreditFromDbResults(ResultSet rs,
                                                                              String entityKey,
                                                                              String artistId,
                                                                              String artistName,
                                                                              String artistSortName,
                                                                              String comment,
                                                                              String joinPhrase,
                                                                              String artistCreditName) throws SQLException {
        Map<Integer, ArtistCredit> artistCredits = new HashMap<Integer, ArtistCredit>();
        ObjectFactory of = new ObjectFactory();
        ArtistCredit ac;
        while (rs.next()) {
            int entityId = rs.getInt(entityKey);
            if (!artistCredits.containsKey(entityId)) {
                ac = of.createArtistCredit();
                artistCredits.put(entityId, ac);
            } else {
                ac = artistCredits.get(entityId);
            }
            NameCredit nc = of.createNameCredit();
            Artist artist = of.createArtist();
            artist.setId(rs.getString(artistId));
            artist.setName(rs.getString(artistName));
            artist.setSortName(rs.getString(artistSortName));
            artist.setDisambiguation(rs.getString(comment));
            nc.setArtist(artist);
            nc.setJoinphrase(rs.getString(joinPhrase));


            String nameCredit = rs.getString(artistCreditName);
            if (!nameCredit.equals(artist.getName())) {
                nc.setName(nameCredit);
            }
            ac.getNameCredit().add(nc);
        }
        return artistCredits;
    }

    /**
     * Populate all artist credit fields from Lucene index fields from ArtistCredit instance
     *
     * @param doc
     * @param ac
     * @param artist
     * @param artistNameCredit
     * @param artistId
     * @param artistName
     * @param artistCredit
     */
    public static void buildIndexFieldsFromArtistCredit(MbDocument doc,
                                                        ArtistCredit ac,
                                                        IndexField artist,
                                                        IndexField artistNameCredit,
                                                        IndexField artistId,
                                                        IndexField artistName,
                                                        IndexField artistCredit) 

    {
         if (ac!=null) {

            //Search Fields
            doc.addField(artist, ArtistCreditHelper.buildFullArtistCreditName(ac));
            for(NameCredit nc:ac.getNameCredit()) {
                if(nc.getName()!=null) {
                    doc.addField(artist, nc.getName());
                    doc.addField(artistNameCredit, nc.getName());
                }
                else {
                    doc.addField(artistNameCredit, nc.getArtist().getName());
                }
                doc.addField(artistId, nc.getArtist().getId());
                doc.addField(artistName, nc.getArtist().getName());
            }

            //Display Field
            doc.addField(artistCredit, MMDSerializer.serialize(ac));

        }
    }
}
