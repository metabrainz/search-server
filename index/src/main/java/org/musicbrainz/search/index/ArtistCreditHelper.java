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

import com.google.common.base.Strings;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


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
     *
     *
     * @param rs
     * @param entityKey
     * @param artistCreditId
     * @throws SQLException
     */
    public static Map<Integer, ArtistCreditWrapper> completeArtistCreditFromDbResults(ResultSet rs,
                                                                                      String entityKey,
                                                                                      String artistCreditId,
                                                                                      String artistId,
                                                                                      String artistName,
                                                                                      String artistSortName,
                                                                                      String comment,
                                                                                      String joinPhrase,
                                                                                      String artistCreditName) throws SQLException {
        Map<Integer, ArtistCreditWrapper> artistCredits = new HashMap<Integer, ArtistCreditWrapper>();
        ObjectFactory of = new ObjectFactory();
        ArtistCreditWrapper acw;
        ArtistCredit ac;
        while (rs.next()) {
            int entityId = rs.getInt(entityKey);
            if (!artistCredits.containsKey(entityId)) {
                acw = new ArtistCreditWrapper();
                acw.setArtistCreditId(rs.getInt(artistCreditId));
                ac = of.createArtistCredit();
                acw.setArtistCredit(ac);
                artistCredits.put(entityId, acw);
            } else {
                acw = artistCredits.get(entityId);
                ac  = acw.getArtistCredit();
            }
            NameCredit nc = of.createNameCredit();
            Artist artist = of.createArtist();
            artist.setId(rs.getString(artistId));
            artist.setName(rs.getString(artistName));
            artist.setSortName(rs.getString(artistSortName));

            String disamb = rs.getString(comment);
            if(disamb!=null && !disamb.isEmpty()) {
                artist.setDisambiguation(disamb);
            }

            nc.setArtist(artist);
            String jp=rs.getString(joinPhrase);
            if(jp!=null && !jp.isEmpty()) {
                nc.setJoinphrase(jp);
            }
            String nameCredit = rs.getString(artistCreditName);
            if (!nameCredit.equals(artist.getName())) {
                nc.setName(nameCredit);
            }
            ac.getNameCredit().add(nc);
        }
        return artistCredits;
    }

    /**
     * Add aliases for the artist credits from the database
     *
     * @param artistCredits
     * @param entityKey
     * @param rs
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static Map<Integer, ArtistCreditWrapper> updateArtistCreditWithAliases(
            Map<Integer, ArtistCreditWrapper> artistCredits,
            String entityKey,
            ResultSet rs)
            throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        int entityId = -1;
        int position = -1;
        NameCredit nc = null;
        while (rs.next()) {
            int currEntityId = rs.getInt(entityKey);
            int currPosition = rs.getInt("pos");
            if(currEntityId!=entityId || currPosition!=position)
            {
                ArtistCreditWrapper acw = artistCredits.get(currEntityId);
                nc = acw.getArtistCredit().getNameCredit().get(currPosition);
                entityId = currEntityId;
                position = currPosition;
            }

            if(nc.getArtist().getAliasList()==null)
            {
                nc.getArtist().setAliasList(of.createAliasList());
            }
            List<Alias> aliasList = nc.getArtist().getAliasList().getAlias();
            Alias alias = of.createAlias();
            alias.setContent(rs.getString("name"));
            alias.setSortName(rs.getString("sort_name"));
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
            aliasList.add(alias);
        }
        rs.close();
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
             buildIndexFieldsOnlyFromArtistCredit( doc,
                     ac,
                     artist,
                     artistNameCredit,
                     artistId,
                     artistName);

            //Display Field
            doc.addField(artistCredit, MMDSerializer.serialize(ac));
        }
    }

    public static void buildIndexFieldsOnlyFromArtistCredit(MbDocument doc,
                                                        ArtistCredit ac,
                                                        IndexField artist,
                                                        IndexField artistNameCredit,
                                                        IndexField artistId,
                                                        IndexField artistName)

    {
        Set<String> aliasWithLocales = new HashSet<String>();
        if (ac!=null) {

            //The full artist credit as it appears on the release
            doc.addField(artist, ArtistCreditHelper.buildFullArtistCreditName(ac));
            for(NameCredit nc:ac.getNameCredit()) {

                //Each individual name credit (uses artist if name credit is unchanged from artist name)
                if(nc.getName()!=null) {
                    doc.addField(artistNameCredit, nc.getName());
                }
                else {
                    doc.addField(artistNameCredit, nc.getArtist().getName());
                }

                //Each artist id and name on the release
                doc.addField(artistId, nc.getArtist().getId());
                doc.addField(artistName, nc.getArtist().getName());

                //Add all aliases
                if(nc.getArtist().getAliasList()!=null && nc.getArtist().getAliasList().getAlias().size()>0)
                {
                    for(Alias alias:nc.getArtist().getAliasList().getAlias())
                    {
                        aliasWithLocales.add(alias.getContent());
                    }
                }

                for(String next:aliasWithLocales)
                {
                    doc.addField(artistName, next);
                }
            }
        }
    }


}
