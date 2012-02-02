/* Copyright (c) 2009 Aurélien Mino
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.Index;
import org.musicbrainz.search.index.IndexField;

/** 
 * A wrapper around Lucene Document
 */
public class MbDocument {

    private Document doc;

    public MbDocument() {
        this.doc = new Document();
    }
    
    public MbDocument(Document doc) {
        this.doc = doc;
    }

    public Document getLuceneDocument() {
        return doc;
    }
   
    /* Methods used for indexing */

    /**
     * Add field
     *
     * @param field
     * @param value
     */
    public void addField(IndexField field, String value) {
        doc.add(new Field(field.getName(), value, field.getStore(), field.getIndex()));
    }

    public void addField(IndexField field, int value) {
        addField(field, Integer.toString(value));
    }
    
    
    /**
     * Add numeric field, handled specially so that ranges searches work properly
     * @param field
     * @param value
     */
    public void addNumericField(IndexField field, Integer value) {
        addField(field, NumericUtils.intToPrefixCoded(value));
    }
    
    /**
     * Add field if not empty
     *
     * @param field
     * @param value
     */
    public void addNonEmptyField(IndexField field, String value) {
        if (value != null && !value.isEmpty()) {
            addField(field, value);
        }
    }


    /**
     * Add field to document if not empty, otherwise add 'unknown' so can be search for
     * @param field
     * @param value
     */
    public void addFieldOrUnknown(IndexField field, String value) {
        if (value != null && !value.isEmpty()) {
                doc.add(new Field(field.getName(), value, field.getStore(), field.getIndex()));
        }
        else {
           doc.add(new Field(field.getName(), Index.UNKNOWN, field.getStore(), field.getIndex()));
        }

    }

    /**
     * Add field to document if not empty, otherwise add hyphen.
     *
     * This method is necessary when adding fields that make up a set within in a list so that
     * order is preserved.
     *
     * @param field
     * @param value
     */
    //TODO is this way we can achieve the same effect without needing to store these fields
    public void addFieldOrHyphen(IndexField field, String value) {
        if (value != null && !value.isEmpty()) {
                doc.add(new Field(field.getName(), value, field.getStore(), field.getIndex()));
        }
        else {
           doc.add(new Field(field.getName(), "-", field.getStore(), field.getIndex()));
        }

    }
    

    /* Methods used for searching */
    
    public String get(IndexField indexField) {
        return doc.get(indexField.getName());
    }
    
    /** This is required to retrieve numeric data that has been encoded so that it works correctly in
     * duration ranges
     *
     * @param indexField
     * @return
     */
    public String getAsText(IndexField indexField) {
        return String.valueOf(NumericUtils.prefixCodedToInt(doc.get(indexField.getName())));
    }

    public Integer getAsNumber(IndexField indexField) {
        return NumericUtils.prefixCodedToInt(doc.get(indexField.getName()));
    }

    public String[] getValues(IndexField indexField) {
        return doc.getValues(indexField.getName());
    }

    public Fieldable[] getFields(IndexField indexField) {
        return doc.getFieldables(indexField.getName());
    }
}
