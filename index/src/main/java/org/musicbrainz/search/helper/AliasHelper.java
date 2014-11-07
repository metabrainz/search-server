package org.musicbrainz.search.helper;

import com.google.common.base.Strings;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.IndexField;
import org.musicbrainz.search.index.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *  Shared for retrieving aliases for an entity, all entities use the same pattern
 */
public class AliasHelper
{
    public static String constructAliasQuery(String entityTableName)
    {
        return "SELECT e."+entityTableName+" as entityId, e.name as alias, e.sort_name as alias_sortname, e.primary_for_locale, e.locale, att.name as type," +
                "e.begin_date_year, e.begin_date_month, e.begin_date_day, e.end_date_year, e.end_date_month, e.end_date_day" +
                " FROM " + entityTableName + "_alias e" +
                "  LEFT JOIN " + entityTableName + "_alias_type att on (e.type=att.id)" +
                " WHERE " + entityTableName + " BETWEEN ? AND ?" +
                " ORDER BY " + entityTableName +", alias, alias_sortname";
    }

    public static Map<Integer, Set<Alias>> completeFromDbResults(int min, int max, PreparedStatement st) throws SQLException
    {
        ObjectFactory of = new ObjectFactory();
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int entityId = rs.getInt("entityId");
            Set<Alias> list;
            if (!aliases.containsKey(entityId)) {
                list = new LinkedHashSet<Alias>();
                aliases.put(entityId, list);
            } else {
                list = aliases.get(entityId);
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
        return aliases;
    }

    /**
     *
     * @param of
     * @param doc
     * @param aliases
     * @param entityId
     * @param aliasIndexField
     * @return
     */
    public static AliasList addAliasesToDocAndConstructAliasList(ObjectFactory of, MbDocument doc, Map<Integer, Set<Alias>> aliases, int entityId, IndexField aliasIndexField)
    {
        AliasList aliasList = of.createAliasList();
        for (Alias nextAlias : aliases.get(entityId))
        {
            doc.addField(aliasIndexField, nextAlias.getContent());
            if (!nextAlias.getSortName().equals(nextAlias.getContent()))
            {
                doc.addField(aliasIndexField, nextAlias.getSortName());
            }
            aliasList.getAlias().add(nextAlias);
        }
        return aliasList;
    }
}
