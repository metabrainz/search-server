package org.musicbrainz.search.analysis;

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

import com.ibm.icu.text.*;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.ArrayUtil;

import java.io.IOException;

/**
 * A {@link TokenFilter} that transforms text with ICU.
 * <p>
 * ICU provides text-transformation functionality via its Transliteration API.
 * Although script conversion is its most common use, a transliterator can
 * actually perform a more general class of tasks. In fact, Transliterator
 * defines a very general API which specifies only that a segment of the input
 * text is replaced by new text. The particulars of this conversion are
 * determined entirely by subclasses of Transliterator.
 * </p>
 * <p>
 * Some useful transformations for search are built-in:
 * <ul>
 * <li>Conversion from Traditional to Simplified Chinese characters
 * <li>Conversion from Hiragana to Katakana
 * <li>Conversion from Fullwidth to Halfwidth forms.
 * <li>Script conversions, for example Serbian Cyrillic to Latin
 * </ul>
 * For more advanced cases, the following capabilities might also be of use:
 * <ul>
 * <li>Conversion of Thai text from glyphic order to logical order for internal
 * processing.
 * <li>Romanization of text, or conversion between the different Indic scripts.
 * <li>Creation of custom rules specific to your application's needs.
 * </ul>
 * </p>
 * <p>
 * Example usage: <blockquote>stream = new ICUTransformFilter(stream,
 * Transliterator.getInstance("Traditional-Simplified"));</blockquote>
 * </p>
 * <p>
 * Whether or not this filter respects equivalence or preserves normalization
 * forms depends entirely upon the ruleset being applied.
 * </p>
 * <p>
 * For good performance, it is helpful to declare a filter in any custom
 * transform you build. This allows the transform to efficiently skip over
 * unaffected text. It is also useful to consider if there are simpler
 * solutions. For example, if you want to standardize Fullwidth and Halfwidth
 * forms, use of {@link ICUNormalizationFilter} with compatibility decomposition
 * will erase width differences, with better performance.
 * </p>
 * For more details, see the <a
 * href="http://userguide.icu-project.org/transforms/general">ICU User
 * Guide</a>.
 */

public class ICUTransformFilter extends TokenFilter {
  // Transliterator to transform the text
  private final Transliterator transform;

  // Reusable position object
  private final Transliterator.Position position = new Transliterator.Position();

  // Wraps a termAttribute around the replaceable interface.
  private final ReplaceableTermAttribute replaceableAttribute = new ReplaceableTermAttribute();

  // new api term attribute, will be updated with transformed text.
  private TermAttribute termAtt;

  /**
   * Create a new ICUTransformFilter that transforms text on the given stream.
   * 
   * @param input {@link TokenStream} to filter.
   * @param transform Transliterator to transform the text.
   */
  public ICUTransformFilter(TokenStream input, Transliterator transform) {
    super(input);
    this.transform = transform;
    termAtt = (TermAttribute) addAttribute(TermAttribute.class);

    /*
     * A good UnicodeFilter is vital for performance. Unfortunately, sometimes
     * people omit filters in their rulesets. However, in the special case that
     * the transform is a RuleBasedTransliterator, this situation can be
     * corrected. It can only be applied to a pure RuleBasedTransliterator, and
     * it is only applied when there is no supplied filter.
     * 
     * For a great example of a ruleset like this, see the built-in
     * Simplified/Traditional ruleset from CLDR. This is a massive performance
     * optimization for that case!
     * 
     * If CompoundTransliterator and its children were exposed (its
     * package-private and children are inaccessible), then more cases could be
     * optimized.
     * 
     * Regardless of who wrote the rules, you can ALWAYS apply a filter own your
     * own:Transliterator.getInstance(
     * "[:Arabic:] UnfilteredTransformThatOnlyProcessesTheArabicBlock");
     * 
     * Just be careful to ensure you don't filter characters that should be
     * converted! This can be tricky if, for example, the transliterator
     * internally invokes ::NFKC().
     */

    if (transform.getFilter() == null
        && (transform instanceof RuleBasedTransliterator)) {
      final UnicodeSet sourceSet = transform.getSourceSet();
      if (sourceSet != null && !sourceSet.isEmpty())
        transform.setFilter(sourceSet);
    }
  }

  public boolean incrementToken() throws IOException {

    /*
     * Wrap the TermAttribute around the replaceable interface, clear the
     * positions, and transliterate. Finally, update the TermAttribute with the
     * [potentially different] length.
     */

    if (input.incrementToken()) {
      final int length = termAtt.termLength();
      replaceableAttribute.setText(termAtt);

      position.start = 0;
      position.limit = length;
      position.contextStart = 0;
      position.contextLimit = length;

      transform.filteredTransliterate(replaceableAttribute, position, false);
      termAtt.setTermLength(replaceableAttribute.length());
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Wrap a {@link TermAttribute} with the Replaceable API.
   * 
   * This allows for ICU transforms to run without unnecessary object creation.
   * 
   * This wrapper does not keep the TermAttribute's length up to date at all
   * times, when you are done you must finalize the replacement process by setting
   * the TermAttribute's length.
   */

  final class ReplaceableTermAttribute implements Replaceable {
    private char buffer[];

    private int length;

    private TermAttribute token;

    ReplaceableTermAttribute() {
    }

    void setText(final TermAttribute reusableToken) {
      this.token = reusableToken;
      this.buffer = reusableToken.termBuffer();
      this.length = reusableToken.termLength();
    }

    public int char32At(int pos) {
      return UTF16.charAt(buffer, 0, length, pos);
    }

    public char charAt(int pos) {
      return buffer[pos];
    }

    public void copy(int start, int limit, int dest) {
      char text[] = new char[limit - start];
      getChars(start, limit, text, 0);
      replace(dest, dest, text, 0, limit - start);
    }

    public void getChars(int srcStart, int srcLimit, char[] dst, int dstStart) {
      System.arraycopy(buffer, srcStart, dst, dstStart, srcLimit - srcStart);
    }

    public boolean hasMetaData() {
      return false;
    }

    public int length() {
      return length;
    }

    public void replace(int start, int limit, String text) {
      replace(start, limit, text.toCharArray(), 0, text.length());
    }

    public void replace(int start, int limit, char[] text, int charsStart,
        int charsLen) {
      final int replacementLength = limit - start;
      final int newLength = length - replacementLength + charsLen;
      // resize if necessary
      if (newLength > length)
        buffer = token.resizeTermBuffer(getNextSize(newLength));
      // if the substring being replaced is longer or shorter than the
      // replacement, need to shift things around
      if (replacementLength != charsLen && limit < length)
        System.arraycopy(buffer, limit, buffer, start + charsLen, length - limit);
      // insert the replacement text
      System.arraycopy(text, charsStart, buffer, start, charsLen);
      length = newLength;
    }
  }

  public static int getNextSize(int targetSize) {
    /* This over-allocates proportional to the list size, making room
     * for additional growth.  The over-allocation is mild, but is
     * enough to give linear-time amortized behavior over a long
     * sequence of appends() in the presence of a poorly-performing
     * system realloc().
     * The growth pattern is:  0, 4, 8, 16, 25, 35, 46, 58, 72, 88, ...
     */
    return (targetSize >> 3) + (targetSize < 9 ? 3 : 6) + targetSize;
  }

}
