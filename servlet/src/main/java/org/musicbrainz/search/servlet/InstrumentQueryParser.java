package org.musicbrainz.search.servlet;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.servlet.mmd1.LabelType;

public class InstrumentQueryParser extends MultiFieldQueryParser
{

    public InstrumentQueryParser(String[] strings, org.apache.lucene.analysis.Analyzer analyzer)
    {
        super(LuceneVersion.LUCENE_VERSION, strings, analyzer);
    }

    @Override
    protected Query newTermQuery(Term term)
    {

        if ((term.field().equals(LabelIndexField.TYPE.getName())))
        {
            try
            {
                int typeId = Integer.parseInt(term.text());
                if (typeId >= LabelType.getMinSearchId() && typeId <= LabelType.getMaxSearchId())
                {
                    TermQuery tq = new TermQuery(new Term(term.field(), LabelType.getBySearchId(typeId).getName()));
                    return tq;
                } else
                {
                    return super.newTermQuery(term);
                }
            }
            catch (NumberFormatException nfe)
            {
                return super.newTermQuery(term);

            }
        }
        else
        {
            return super.newTermQuery(term);

        }
    }
}
