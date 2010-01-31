package org.musicbrainz.search.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.*;

import java.io.StringReader;
import java.io.IOException;

import com.ibm.icu.text.Normalizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

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
        Tokenizer tokenizer0 = new WhitespaceTokenizer(new StringReader(sb.toString()));
        Tokenizer tokenizer1 = new WhitespaceTokenizer(new StringReader(sb.toString()));
        Tokenizer tokenizer2 = new WhitespaceTokenizer(new StringReader(sb.toString()));
        Tokenizer tokenizer3 = new WhitespaceTokenizer(new StringReader(sb.toString()));
        Tokenizer tokenizer4 = new WhitespaceTokenizer(new StringReader(sb.toString()));

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

        TermAttribute term  = (TermAttribute)tokenizer0.addAttribute(TermAttribute.class);
        TermAttribute term1 = (TermAttribute)result1.addAttribute(TermAttribute.class);
        TermAttribute term2 = (TermAttribute)result2.addAttribute(TermAttribute.class);
        TermAttribute term3 = (TermAttribute)result3.addAttribute(TermAttribute.class);
        TermAttribute term4 = (TermAttribute)result4.addAttribute(TermAttribute.class);

        while(tokenizer0.incrementToken())
        {
            result1.incrementToken();
            result2.incrementToken();
            result3.incrementToken();
            result4.incrementToken();


            if(!term1.term().equals(term.term()))
            {
                changedByAccent ++;
            }
            if(!term2.term().equals(term.term()))
                        {
                changedByASCII ++;
            }
            if(!term3.term().equals(term.term()))
                        {
                changedByNFKC ++;
            }
            if(!term4.term().equals(term.term()))
                        {
                changedByASCIIAndNFKC ++;
            }
            if(
                    (!term.term().equals(term2.term()))
                    &&
                    (term.term().equals(term4.term()))
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
