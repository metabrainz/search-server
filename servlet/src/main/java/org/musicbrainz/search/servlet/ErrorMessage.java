
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
package org.musicbrainz.search.servlet;

import java.text.MessageFormat;

public enum ErrorMessage {

    SERVLET_INIT_FAILED ("Error during servlet initialization {0}"),
    NO_QUERY_PARAMETER ("No Query parameter supplied"),
    NO_TYPE_PARAMETER ("No Type parameter supplied"),
    NO_FORMAT_PARAMETER ("No Format parameter supplied"),
    UNKNOWN_RESOURCE_TYPE ("Unknown resource type {0}"),
    NO_HANDLER_FOR_TYPE_AND_FORMAT ("No handler for resource type {0} and format {1}"),
    INDEX_NOT_AVAILABLE_FOR_TYPE ("Index is currently not available for resource type {0}"),
    UNABLE_TO_PARSE_SEARCH ("Unable to parse search:{0}"),
    NO_MATCHES ("zero search hits"),    //Formatting as is because depended on by mb_server
    UNKNOWN_COUNT_TYPE ("Count parameter {0} not valid, should be a type "),
    UNABLE_TO_PARSE_SEARCH_SLASHES_ARE_REGEXP ("Unable to parse search, forward slash is used for regex unless escaped:{0}"),
    ;

    String msg;

    ErrorMessage(String msg)
    {
        this.msg = msg;
    }

    public String getMsg()
    {
        return msg;
    }

    /**
     *
     * @param args
     * @return the message after applying the arguments into the message placeholder
     */
    public String getMsg(Object ... args)
    {
        return MessageFormat.format(getMsg(),args);
    }

}
