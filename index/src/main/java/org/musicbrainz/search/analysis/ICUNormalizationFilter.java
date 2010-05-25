package  org.musicbrainz.search.analysis;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.ibm.icu.text.Normalizer;
import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.ArrayUtil;

import java.io.IOException;

/**
 * A {@link TokenFilter} that performs Unicode text normalization.
 * <p>
 * Normalization standardizes different forms of the same character in Unicode.
 * For any search application, it is essential to apply normalization to at
 * least ensure canonical equivalence. For example, a Vietnamese input method on
 * one operating system might represent the character ằ as one codepoint: LATIN
 * SMALL LETTER A WITH BREVE AND GRAVE, whereas the input method on another
 * operating system might represent the same character as two: LATIN SMALL
 * LETTER A WITH BREVE followed by COMBINING GRAVE ACCENT. Unless text is
 * normalized to a standard form, queries and documents from these different
 * systems will not be interpreted as the same character!
 * </p>
 * <p>
 * There are four modes that text can be normalized to:
 * <ul>
 * <li>NFD: Canonical Decomposition
 * <li>NFC: Canonical Decomposition, followed by Canonical Composition
 * <li>NFKD: Compatibility Decomposition
 * <li>NFKC: Compatibility Decomposition, followed by Canonical Composition
 * </ul>
 * </p>
 * <p>
 * For most search tasks, it makes sense to normalize to NFC or NFKC, as the
 * composed form will generally be shorter than the decomposed form. The
 * decomposed forms can still be useful for some tasks, for example providing
 * decomposed Korean text to a downstream {@link TokenFilter} would allow that
 * filter to work with individual Jamo instead of composed Hangul syllables.
 * </p>
 * <p>
 * For a typical search application, the way in which the text will be
 * standardized is the most important, and the two types of standardization are
 * described below.
 * </p>
 * <ul>
 * <li>Under normalization forms NFC and NFD, characters that are canonical
 * equivalents will be standardized.
 * <p>
 * Canonical equivalence is where there are multiple ways to encode the same
 * character or sequence of characters in Unicode. These differences display the
 * same to a user, but are different to the computer.
 * </p>
 * <p>
 * For example, é can be encoded in Unicode in at least two different ways:
 * <ul>
 * <li>U+00E9 [LATIN SMALL LETTER E WITH ACUTE]
 * <li>U+0065 U+0301 [LATIN SMALL LETTER E] [COMBINING ACUTE ACCENT]
 * </ul>
 * </p>
 * <br>
 * <li>Under normalization forms NFKC and NFKD, characters that are canonical or
 * compatibility equivalents will be standardized.
 * <p>
 * Compatibility equivalence is a weaker form of equivalence than canonical
 * equivalence. Similar to canonical equivalents, compatibility equivalents are
 * different ways to represent the same character. The difference is that unlike
 * canonical equivalents, compatibility equivalents may have different visual
 * appearance or format.
 * </p>
 * <p>
 * For example, the letter Ａ appears different than A, because of its width. The
 * below two forms are not canonical equivalents, but are compatibility
 * equivalents:
 * <ul>
 * <li>U+FF21 [FULLWIDTH LATIN CAPITAL LETTER A]
 * <li>U+0041 [LATIN CAPITAL LETTER A]
 * </ul>
 * </p>
 * <br>
 * </ul>
 * <p>
 * Normalization is computationally expensive and can both reorder characters
 * and change the length of text. In practice, typically the majority of text is
 * already normalized. This filter first performs a quick-check, and performs
 * normalization only when this quick-check fails or is uncertain.
 * </p>
 * <p>
 * When designing an analysis pipeline, it is important to minimize the number
 * of times you invoke ICUNormalizationFilter. At the same time, it is equally
 * important that the analysis process behaves in such a way that all equivalent
 * text is treated the same. The naïve solution to this problem is to invoke
 * ICUNormalizationFilter both before and after every {@link TokenFilter} in the
 * pipeline. This ensures that all equivalent text is treated the same and
 * remains normalized, but is very inefficient. <br>
 * Instead the two simple rules below can be followed to minimize the number of
 * invocations:
 * </p>
 * <p>
 * <b>If a {@link TokenFilter} does not <i>respect</i> the equivalence defined
 * for the normalization form, ICUNormalizationFilter must be called before that
 * {@link TokenFilter}.</b> <br>
 * This way, text is provided to that TokenFilter in a form that it understands,
 * and will be processed correctly. For example, the {@link ASCIIFoldingFilter}
 * does not respect canonical equivalence: it only folds precomposed
 * character+accent combinations to an accent-free form. Because of this, the
 * two forms of é listed in the example above will be treated differently; only
 * one will have its accent mark removed! By invoking ICUNormalizationFilter
 * with NFC first, you can ensure that both are treated the same; both will have
 * their accent marks removed.
 * </p>
 * <p>
 * <b>If a {@link TokenFilter} does not <i>preserve</i> the normalization form,
 * ICUNormalizationFilter must be called at some point after that
 * {@link TokenFilter} before indexing.</b> <br>
 * When a {@link TokenFilter} modifies text, it might cause text to become
 * denormalized. There are a number of ways this can happen, even concatenation
 * of two normalized chunks of text can produce a denormalized result. For
 * example, although the {@link ICUCaseFoldingFilter} respects both canonical
 * and compatibility equivalence, it does not preserve normalization forms. By
 * the first rule above, because it respects canonical equivalence, the
 * ICUNormalizationFilter need not be invoked before it for normalization form
 * NFC. But, because it does does not preserve normalization form NFC, the
 * ICUNormalizationFilter must be invoked before indexing, or before any
 * downstream TokenFilter that does not respect canonical equivalence, whichever
 * comes first.
 * </p>
 * <p>
 * It is generally more difficult to preserve normalization forms than it is to
 * respect equivalence. Respecting equivalence is usually a simple matter of
 * adding additional mappings. When designing an analysis pipeline, it is
 * recommended that every {@link TokenFilter} respect equivalence, and at the
 * end of the pipeline ICUNormalizationFilter can be invoked a single time.
 * </p>
 * For more details, see UAX #15: <a
 * href="http://www.unicode.org/reports/tr15/">Unicode Normalization Forms</a>
 */

public final class ICUNormalizationFilter extends TokenFilter {
  // the mode this normalizer uses
  private final Normalizer.Mode mode;

  // normalization output buffer, will be resized if needed.
  private char buffer[] = new char[4096];

  // new api term attribute, will be updated with normalized text if necessary.
  private TermAttribute termAtt;

  /**
   * Create an ICUNormalizationFilter that normalizes text to the specified
   * mode.
   * 
   * @param input {@link TokenStream} to filter
   * @param mode Normalization mode to apply
   */
  public ICUNormalizationFilter(TokenStream input, Normalizer.Mode mode) {
    super(input);
    this.mode = mode;
    termAtt = (TermAttribute) addAttribute(TermAttribute.class);
  }

  public boolean incrementToken() throws IOException {

    /*
     * First do a quick-check (this will be the significant majority of text).
     * If the text is already normalized, simply return it. Otherwise, normalize
     * the text.
     */

    if (input.incrementToken()) {
      final char src[] = termAtt.termBuffer();
      final int length = termAtt.termLength();

      /*
       * This quick-check returns three possible values: YES, NO, or MAYBE. When
       * it returns YES, the text is already normalized. When it returns NO, the
       * text is definitely not normalized. When it returns MAYBE, the only way
       * to determine if the text is normalized is to actually normalize it. See
       * http://www.unicode.org/unicode/reports/tr15/tr15-23.html#Annex8
       */

      if (Normalizer.quickCheck(src, 0, length, mode, 0) == Normalizer.YES)
        return true;

      /*
       * There are known maximum expansions for the different forms that could
       * remove the loop/exception handling below. These may change in new
       * versions of the Unicode standard, and are sometimes large. The loop is
       * for simplicity and ease of maintenance; with a large default buffer
       * size it should rarely execute more than once.
       * 
       * From http://unicode.org/reports/tr36/tr36-6.html#Buffer_Overflows: The
       * very large factors in the case of NFKC/D are due to some extremely rare
       * characters. Thus algorithms can use much smaller expansion factors for
       * the typical cases as long as they have a fallback process that accounts
       * for the possibility of these characters in data.
       * 
       * For example, under normalization forms NFKC or NFKD, ﷺ (FDFA, ARABIC
       * LIGATURE SALLALLAHOU ALAYHE WASALLAM) will be expanded to صلى الله عليه
       * وسلم
       */

      do {
        try {

          /*
           * This method is documented in the public API to throw
           * IndexOutOfBoundsException if there is not enough space. Its an
           * unfortunate mechanism, it would be a lot nicer if instead it
           * behaved like the ArabicShaping API, whereas instead it would return
           * the necessary length, possibly more than the buffer supplied. This
           * would simplify things, instead a call with a 0-length output buffer
           * would return the necessary length.
           */

          final int newLength = Normalizer.normalize(src, 0, length, buffer, 0,
              buffer.length, mode, 0);
          termAtt.setTermBuffer(buffer, 0, newLength);
          return true;
        } catch (IndexOutOfBoundsException e) {
          // technically, ICU encodes the necessary size as a String in the
          // exception, but don't depend on that...
          buffer = new char[ArrayUtil.getNextSize(buffer.length << 1)];
        }
      } while (true);
    } else {
      return false;
    }
  }
}
