/*
 Bandika MapDispatcher - a proxy and preloader for OSM map tiles
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.base.log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    ZoneId timeZone = TimeZone.getDefault().toZoneId();

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(record.getLoggerName());
        String srcClass = record.getSourceClassName();
        String srcMethod = record.getSourceMethodName();
        if (record.getLevel() == Level.INFO) {
            sb.append(" INFO    ");
        } else if (record.getLevel() == Level.WARNING) {
            sb.append(" WARNING ");
        } else if (record.getLevel() == Level.SEVERE) {
            sb.append(" ERROR   ");
        }
        sb.append(LocalDateTime.ofInstant(Instant.ofEpochMilli(record.getMillis()), timeZone).toString());
        if (srcClass.length() > 0) {
            sb.append('\n');
            sb.append(srcClass);
            if (srcMethod.length() > 0) {
                sb.append('.');
                sb.append(srcMethod);
            }
        }
        sb.append(" - ");
        sb.append(record.getMessage());
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored") Throwable t = record.getThrown();
        if (t != null) {
            sb.append("\n> Exception: ");
            sb.append(t.getMessage());
            sb.append(" at:");
            for (StackTraceElement ste : t.getStackTrace()) {
                sb.append(String.format("\n> %s line %d", ste.getClassName(), ste.getLineNumber()));
            }
        }
        sb.append('\n');
        return sb.toString();
    }
}