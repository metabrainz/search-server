package org.musicbrainz.search.index;

import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Paul on 06/11/2014.
 */
public class AliasHelper
{
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
