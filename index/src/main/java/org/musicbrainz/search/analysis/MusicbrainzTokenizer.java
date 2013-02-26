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


import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;

/** A grammar-based tokenizer constructed with JFlex
 *
 * <p> This should be a good tokenizer for most European-language documents:
 *
 * <ul>
 *   <li>Splits words at punctuation characters, removing punctuation. However, a
 *     dot that's not followed by whitespace is considered part of a token.
 *   <li>Splits words at hyphens, unless there's a number in the token, in which case
 *     the whole token is interpreted as a product number and is not split.
 *   <li>Recognizes email addresses and internet hostnames as one token.
 * </ul>
 *
 * <p>Many applications have specific tokenizer needs.  If this tokenizer does
 * not suit your application, please consider copying this source code
 * directory to your project and maintaining your own grammar-based tokenizer.
 *
 * <a name="version"/>
 * <p>You must specify the required {@link Version}
 * compatibility when creating StandardAnalyzer:
 * <ul>
 *   <li> As of 2.4, Tokens incorrectly identified as acronyms
 *        are corrected (see <a href="https://issues.apache.org/jira/browse/LUCENE-1068">LUCENE-1608</a>
 * </ul>
 */

public final class MusicbrainzTokenizer extends Tokenizer {
  /** A private instance of the JFlex-constructed scanner */
  private final MusicbrainzTokenizerImpl scanner;

  public static final int ALPHANUM                          = 0;
  public static final int APOSTROPHE                        = 1;
  public static final int ACRONYM                           = 2;
  public static final int CONTROLANDPUNCTUATION             = 3;
  public static final int ALPHANUMANDPUNCTUATION            = 4;
  public static final int HOST                              = 5;
  public static final int NUM                               = 6;
  public static final int CJ                                = 7;

  /** String token types that correspond to token type int constants */
  public static final String [] TOKEN_TYPES = new String [] {
    "<ALPHANUM>",
    "<APOSTROPHE>",
    "<ACRONYM>",
    "<CONTROLANDPUNCTUATION>",
    "<ALPHANUMANDPUNCTUATION>",
    "<HOST>",
    "<NUM>",
    "<CJ>",
  };

  private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

  /** Set the max allowed token length.  Any token longer
   *  than this is skipped. */
  public void setMaxTokenLength(int length) {
    this.maxTokenLength = length;
  }

  /** @see #setMaxTokenLength */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

  /**
   * Creates a new instance of the {@link org.apache.lucene.analysis.standard.StandardTokenizer}.  Attaches
   * the <code>input</code> to the newly created JFlex scanner.
   *
   * @param input The input reader
   *
   * See http://issues.apache.org/jira/browse/LUCENE-1068
   */
  public MusicbrainzTokenizer(Version matchVersion, Reader input) {
    super(input);
    this.scanner = new MusicbrainzTokenizerImpl(input);
    init(input, matchVersion);
  }

  /**
   * Creates a new MusicbrainzTokenizer with a given {@link AttributeSource}.
   */
  public MusicbrainzTokenizer(Version matchVersion, AttributeSource source, Reader input) {
    super(source, input);
    this.scanner = new MusicbrainzTokenizerImpl(input);
    init(input, matchVersion);
  }

  /**
   * Creates a new MusicbrainzTokenizer with a given {@link org.apache.lucene.util.AttributeSource.AttributeFactory}
   */
  public MusicbrainzTokenizer(Version matchVersion, AttributeFactory factory, Reader input) {
    super(factory, input);
    this.scanner = new MusicbrainzTokenizerImpl(input);
    init(input, matchVersion);
  }

  private void init(Reader input, Version matchVersion) {
    termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
  }

  // this tokenizer generates three attributes:
  // offset, positionIncrement and type
  private CharTermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private PositionIncrementAttribute posIncrAtt;
  private TypeAttribute typeAtt;

  /*
   * (non-Javadoc)
   *
   * @see org.apache.lucene.analysis.TokenStream#next()
   */
  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    int posIncr = 1;

    while(true) {
      int tokenType = scanner.getNextToken();

      if (tokenType == MusicbrainzTokenizerImpl.YYEOF) {
        return false;
      }

      if (scanner.yylength() <= maxTokenLength) {
        posIncrAtt.setPositionIncrement(posIncr);
        scanner.getText(termAtt);
        final int start = scanner.yychar();
        offsetAtt.setOffset(correctOffset(start), correctOffset(start+termAtt.length()));
        typeAtt.setType(MusicbrainzTokenizerImpl.TOKEN_TYPES[tokenType]);
        return true;
      } else
        // When we skip a too-long term, we still increment the
        // position increment
        posIncr++;
    }
  }


  @Override
  public final void end() {
    // set final offset
    int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
    offsetAtt.setOffset(finalOffset, finalOffset);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.lucene.analysis.TokenStream#reset()
   */
  @Override
  public void reset() throws IOException {
    super.reset();
    scanner.yyreset(input);
  }
}
