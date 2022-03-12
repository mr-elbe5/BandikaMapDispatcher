/*
 Bandika MapDispatcher - a proxy and preloader for OSM map tiles
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.application;

import de.elbe5.base.log.Log;

import javax.servlet.ServletContext;

public class Configuration {

    static String localPath = "";
    static String mapServerUri ="";
    static int remoteTimeoutSecs = 30;
    static String token = "";

    public static String getLocalPath() {
        return localPath;
    }

    public static String getMapServerUri() {
        return mapServerUri;
    }

    public static int getRemoteTimeoutSecs() {
        return remoteTimeoutSecs;
    }

    public static String getToken() {
        return token;
    }

    // read from config file

    private static String getSafeInitParameter(ServletContext servletContext, String key){
        String s=servletContext.getInitParameter(key);
        return s==null ? "" : s;
    }

    public static void setConfigs(ServletContext servletContext) {
        localPath = getSafeInitParameter(servletContext,"localPath");
        if (!localPath.endsWith("/")){
            localPath = localPath + "/";
        }
        mapServerUri = getSafeInitParameter(servletContext,"mapServerUri");
        if (!mapServerUri.endsWith("/")){
            mapServerUri = mapServerUri + "/";
        }
        try {
            remoteTimeoutSecs = Integer.parseInt(getSafeInitParameter(servletContext, "remoteTimeoutSecs"));
        }
        catch (NumberFormatException e){
            Log.error("bad firmat for remoteTimeoutSecs");
        }
        if (remoteTimeoutSecs == 0){
            remoteTimeoutSecs = 30;
        }
        token = getSafeInitParameter(servletContext,"token");
    }

}
