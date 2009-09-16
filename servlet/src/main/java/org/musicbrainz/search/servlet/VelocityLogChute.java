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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;


/** Send log messages from Velocity into standard logging
 *
 */
public class VelocityLogChute implements LogChute {


    /**
     * Default name for the JDK logger instance
     */
    private static final String DEFAULT_LOG_NAME = "org.musicbrainz.search.servlet.velocity";

    protected Logger logger = null;

    public void init(RuntimeServices rs) {

        logger = Logger.getLogger(DEFAULT_LOG_NAME);
        log(LogChute.INFO_ID, "Search Server Velocity using logger '" + DEFAULT_LOG_NAME);
    }

    private static Map<Integer, Level> logLevelMapping = new HashMap<Integer, Level>();

    static {
        logLevelMapping.put(LogChute.ERROR_ID, Level.SEVERE);
        logLevelMapping.put(LogChute.WARN_ID, Level.WARNING);
        logLevelMapping.put(LogChute.INFO_ID, Level.INFO);
        logLevelMapping.put(LogChute.DEBUG_ID, Level.FINE);
        logLevelMapping.put(LogChute.TRACE_ID, Level.FINER);
    }

    /**
     * Returns the java.util.logging.Level that matches
     * Velocity level.
     *
     * @param level
     * @return The current log level of the JDK Logger.
     */
    protected Level getlogLevel(int level) {
        Level logLevel = logLevelMapping.get(level);
        if (logLevel == null) {
            return Level.FINEST;
        }
        return logLevel;
    }

    /**
     * Logs messages
     *
     * @param level   severity level
     * @param message complete error message
     */
    public void log(int level, String message) {
        log(level, message, null);
    }

    /**
     * Send a log message from Velocity along with an exception or error
     *
     * @param level
     * @param message
     * @param t
     */
    public void log(int level, String message, Throwable t) {
        Level logLevel = getlogLevel(level);
        if (t == null) {
            logger.log(logLevel, message);
        } else {
            logger.log(logLevel, message, t);
        }
    }

    /**
     * is logging enabled at this log level
     */
    public boolean isLevelEnabled(int level) {
        return logger.isLoggable(getlogLevel(level));
    }

}
