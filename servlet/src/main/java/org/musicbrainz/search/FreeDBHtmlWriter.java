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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.EnumMap;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.FieldMethodizer;

public class FreeDBHtmlWriter extends HtmlWriter {

	protected static final String TEMPLATE_RESOURCE_NAME = "freedb.html.vtl";
	
	public FreeDBHtmlWriter() throws Exception {
		super(TEMPLATE_RESOURCE_NAME);
	}

	@Override
	public void write(PrintWriter out, Results results, EnumMap<RequestParameter,String> extraInfoMap) throws IOException {
		
		VelocityContext context = new VelocityContext();
		context.put("offset", results.offset);
		context.put("totalHits", results.totalHits);
		context.put("results", results.results);
		
		context.put("Math", Math.class);
		// Make IndexField enum available in template context
		context.put("FreeDBIndexField", new FieldMethodizer( "org.musicbrainz.search.FreeDBIndexField" ));
		
		template.merge(context, out);
	}
	
}
