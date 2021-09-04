/*
 BandikaMapDispatcher
 Copyright (C) 2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.application;

import javax.servlet.ServletContext;

public class Configuration {

    static String localPath = "";
    static String defaultLocalMaxZoom = "";
    static String defaultMapServerUri ="";
    static String topoLocalMaxZoom ="";
    static String topoMapServerUri ="";

    public static String getDefaultLocalMaxZoom() {
        return defaultLocalMaxZoom;
    }

    public static String getLocalPath() {
        return localPath;
    }

    public static String getDefaultMapServerUri() {
        return defaultMapServerUri;
    }

    public static String getTopoLocalMaxZoom() {
        return topoLocalMaxZoom;
    }

    public static String getTopoMapServerUri() {
        return topoMapServerUri;
    }

    // read from config file

    private static String getSafeInitParameter(ServletContext servletContext, String key){
        String s=servletContext.getInitParameter(key);
        return s==null ? "" : s;
    }

    public static void setConfigs(ServletContext servletContext) {
        localPath = getSafeInitParameter(servletContext,"localPath");
        defaultLocalMaxZoom = getSafeInitParameter(servletContext,"defaultLocalMaxZoom");
        defaultMapServerUri = getSafeInitParameter(servletContext,"defaultMapServerUri");
        topoLocalMaxZoom = getSafeInitParameter(servletContext,"topoLocalMaxZoom");
        topoMapServerUri = getSafeInitParameter(servletContext,"topoMapServerUri");
    }

}
