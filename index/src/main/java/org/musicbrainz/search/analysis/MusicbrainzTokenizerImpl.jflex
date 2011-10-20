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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

%%

%class MusicbrainzTokenizerImpl
%unicode
%integer
%function getNextToken
%pack
%char

%{

public static final int ALPHANUM                          = MusicbrainzTokenizer.ALPHANUM;
public static final int APOSTROPHE                        = MusicbrainzTokenizer.APOSTROPHE;
public static final int ACRONYM                           = MusicbrainzTokenizer.ACRONYM;
public static final int CONTROLANDPUNCTUATION             = MusicbrainzTokenizer.CONTROLANDPUNCTUATION;
public static final int ALPHANUMANDPUNCTUATION            = MusicbrainzTokenizer.ALPHANUMANDPUNCTUATION;
public static final int HOST                              = MusicbrainzTokenizer.HOST;
public static final int NUM                               = MusicbrainzTokenizer.NUM;
public static final int CJ                                = MusicbrainzTokenizer.CJ;
/**
 * @deprecated this solves a bug where HOSTs that end with '.' are identified
 *             as ACRONYMs. It is deprecated and will be removed in the next
 *             release.
 */
public static final int ACRONYM_DEP       = MusicbrainzTokenizer.ACRONYM_DEP;

public static final String [] TOKEN_TYPES = MusicbrainzTokenizer.TOKEN_TYPES;

public final int yychar()
{
    return yychar;
}

/**
 * Fills Lucene token with the current token text.
 */
final void getText(Token t) {
  t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

/**
 * Fills TermAttribute with the current token text.
 */
final void getText(TermAttribute t) {
  t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

%}


//Apparently LETTER doesnt contain Thai characters (this may no longer be true)
THAI       = [\u0E00-\u0E59]

// Basic word: a sequence of digits & letters (includes Thai to enable ThaiAnalyzer to function)
ALPHANUM   = ({LETTER}|{THAI}|[:digit:])+

//All Normal Chars we do Match and want to keep plus stuff we never want to match
LD = {ALPHANUM} | {CJ} | {WHITESPACE}

// Everything except LD, Special chars we eventually filter out unless the token is only made up of these characters
CONTROLANDPUNC     =  !(![^] | {LD})

//MUST CONTAIN Alphanumeric and Punctuation Characters
ALPHANUMANDPUNCTUATION   = ({ALPHANUM}|{CONTROLANDPUNC})*{CONTROLANDPUNC}{ALPHANUM}({ALPHANUM}|{CONTROLANDPUNC})* |
                           ({ALPHANUM}|{CONTROLANDPUNC})*{ALPHANUM}{CONTROLANDPUNC}({ALPHANUM}|{CONTROLANDPUNC})*

//Must contain only punctuation characters
CONTROLANDPUNCTUATION    =  ({CONTROLANDPUNC})+

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
APOSTROPHE =  {ALPHA} ("'" {ALPHA})+

// acronyms (with or without trailing dot): U.S.A., I.B.M, etc.
// use a post-filter to remove dots
ACRONYM    =  {LETTER} ("." {LETTER})+ (".")?

ACRONYM_DEP	= {ALPHANUM} "." ({ALPHANUM} ".")+

// hostname
HOST       =  {ALPHANUM} ((".") {ALPHANUM})+

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)

// punctuation
P	         = ("_"|"-"|"/"|"."|",")

// at least one digit
HAS_DIGIT  = ({LETTER}|[:digit:])* [:digit:] ({LETTER}|[:digit:])*

ALPHA      = ({LETTER})+

// From the JFlex manual: "the expression that matches everything of <a> not matched by <b> is !(!<a>|<b>)"
LETTER     = !(![:letter:]|{CJ})

// Chinese  (but NOT Korean or Japanese, which is included in [:letter:])
CJ         = [\u3100-\u312f\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff]




WHITESPACE = \r\n | [ \r\n\t\f]

%%


{ACRONYM}                                                      { return ACRONYM; }
{ACRONYM_DEP}                                                  { return ACRONYM_DEP; }
{HOST}                                                         { return HOST; }
{APOSTROPHE}                                                   { return APOSTROPHE; }
{NUM}                                                          { return NUM; }
{ALPHANUMANDPUNCTUATION}                                       { return ALPHANUMANDPUNCTUATION; }
{ALPHANUM}                                                     { return ALPHANUM; }
{CONTROLANDPUNCTUATION}                                        { return CONTROLANDPUNCTUATION; }
{CJ}                                                           { return CJ; }



/** Ignore the rest */
. | {WHITESPACE}                                               { /* ignore */ }
