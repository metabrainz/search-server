package org.musicbrainz.search.analysis;

import com.ibm.icu.text.Normalizer;
import junit.framework.TestCase;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.musicbrainz.search.LuceneVersion;

import java.io.IOException;
import java.io.StringReader;

/**
 * Compare FIlters
 */
public class CompareNormalizationFiltersTest extends TestCase {

    public void testFilters() throws IOException
    {
        StringBuffer sb = new StringBuffer();


        for(char i=0;i<65535;i++)
        {
            Character c = new Character(i);
            if(!Character.isWhitespace(c)) {
                sb.append(new Character(i).toString() + ' ');
            }
        }
        //System.out.println(sb.toString());
        Tokenizer tokenizer0 = new WhitespaceTokenizer(LuceneVersion.LUCENE_VERSION,new StringReader(sb.toString()));
        Tokenizer tokenizer1 = new WhitespaceTokenizer(LuceneVersion.LUCENE_VERSION,new StringReader(sb.toString()));
        Tokenizer tokenizer2 = new WhitespaceTokenizer(LuceneVersion.LUCENE_VERSION,new StringReader(sb.toString()));
        Tokenizer tokenizer3 = new WhitespaceTokenizer(LuceneVersion.LUCENE_VERSION,new StringReader(sb.toString()));
        Tokenizer tokenizer4 = new WhitespaceTokenizer(LuceneVersion.LUCENE_VERSION,new StringReader(sb.toString()));

        TokenStream result1 = new AccentFilter(tokenizer1);
        TokenStream result2 = new ASCIIFoldingFilter(tokenizer2);
        TokenStream result3 = new ICUNormalizationFilter(tokenizer3, Normalizer.NFKD);
        TokenStream result4 = new ASCIIFoldingFilter(new ICUNormalizationFilter(tokenizer4, Normalizer.NFKD));
        Token t0;
        Token t;
        Token t2;
        Token t3;
        Token t4;

        
        int changedByAccent =0;
        int changedByASCII  =0;
        int changedByNFKC    =0;
        int changedByASCIIAndNFKC    =0;

        System.out.println("Chars that are changed by one filter different to other filter");
        System.out.println("input:existingfilter:newfilter");

        CharTermAttribute term  = (CharTermAttribute)tokenizer0.addAttribute(CharTermAttribute.class);
        CharTermAttribute term1 = (CharTermAttribute)result1.addAttribute(CharTermAttribute.class);
        CharTermAttribute term2 = (CharTermAttribute)result2.addAttribute(CharTermAttribute.class);
        CharTermAttribute term3 = (CharTermAttribute)result3.addAttribute(CharTermAttribute.class);
        CharTermAttribute term4 = (CharTermAttribute)result4.addAttribute(CharTermAttribute.class);

        while(tokenizer0.incrementToken())
        {
            result1.incrementToken();
            result2.incrementToken();
            result3.incrementToken();
            result4.incrementToken();


            if(!new String(term1.buffer(),0,term1.length()).equals(new String(term.buffer(), 0, term.length())))
            {
                changedByAccent ++;
            }
            if(!new String(term2.buffer(),0,term2.length()).equals(new String(term.buffer(), 0, term.length())))
                        {
                changedByASCII ++;
            }
            if(!new String(term3.buffer(),0,term3.length()).equals(new String(term.buffer(), 0, term.length())))
                        {
                changedByNFKC ++;
            }
            if(!new String(term4.buffer(),0,term4.length()).equals(new String(term.buffer(), 0, term.length())))
                        {
                changedByASCIIAndNFKC ++;
            }
            if(
                    (!new String(term.buffer(),0,term.length()).equals(new String(term2.buffer(), 0, term2.length())))
                    &&
                    (new String(term.buffer(),0,term.length()).equals(new String(term4.buffer(), 0, term4.length())))
                    )


            {
                //printAsHexAndValue(t0.term());
                //printAsHexAndValue(t.term());
                //printAsHexAndValue(t2.term());
                //printAsHexAndValue(t3.term());
                //System.out.println();
                
            }
        }
        System.out.println("Accent      Filter changed "+ changedByAccent + " chars");
        System.out.println("ASCII       Filter changed "+ changedByASCII + " chars");
        System.out.println("ICU         Filter changed "+ changedByNFKC   + " chars");
        System.out.println("ASCIIICU    Filter changed "+ changedByASCIIAndNFKC   + " chars");


    }

    private void printAsHexAndValue(String term)
    {
        System.out.print("0x" + Integer.toHexString(Character.valueOf(term.charAt(0)))+ " " + term + ":");
    }
}
