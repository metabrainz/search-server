package org.musicbrainz.search.servlet;
/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Paul Taylor

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

import junit.framework.TestCase;

public class ErrorMessageTest extends TestCase {


    public void testWriteErrorMessageNoArgs() throws Exception {
        assertEquals("No Query parameter supplied", ErrorMessage.NO_QUERY_PARAMETER.getMsg());
    }

    public void testWriteErrorMessageWithArgs() throws Exception {
        assertEquals("No handler for resource type cdstub and format xml", ErrorMessage.NO_HANDLER_FOR_TYPE_AND_FORMAT.getMsg("cdstub", "xml"));
    }


}
