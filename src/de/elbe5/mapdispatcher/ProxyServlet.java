/*
 Bandika MapDispatcher - a proxy and preloader for OSM map tiles
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.mapdispatcher;

import de.elbe5.application.Configuration;
import de.elbe5.base.log.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

public class ProxyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // uri should be like /out/z/x/y.png
        String uri = request.getRequestURI().substring(1);
        int lastSlash = uri.lastIndexOf("/");
        if (!uri.endsWith(".png")){
            Log.error("bad uri");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        StringTokenizer stk = new StringTokenizer(uri,"/",false);
        int numTokens = stk.countTokens();
        String fileName = "";
        StringBuilder remoteFileName = new StringBuilder();
        int zoom = 0;
        for (int i = numTokens-1; stk.hasMoreTokens(); i--){
            String s = stk.nextToken();
            switch (i) {
                case 2 -> {
                    try {
                        zoom = Integer.parseInt(s);
                    } catch (Exception e) {
                        Log.error("bad zoom");
                    }
                    remoteFileName.append(s);
                }
                case 1 -> {
                    remoteFileName.append("/");
                    remoteFileName.append(s);
                }
                case 0 -> {
                    fileName = s;
                    remoteFileName.append("/");
                    remoteFileName.append(s);
                }
            }
        }
        if (zoom <= Configuration.getMapServerMaxZoom()) {
            byte[] bytes = MapFile.getLocalFile(Configuration.getLocalPath() + uri);
            if (bytes == null) {
                bytes = MapFile.getRemoteFile(Configuration.getMapServerUri() + uri);
                if (bytes == null) {
                    Log.error("could not get remote file");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                if (!MapFile.saveLocalFile(Configuration.getLocalPath() + uri, bytes)) {
                    Log.error("could not save file");
                }
            }
            if (!sendFile(bytes, fileName, response)) {
                Log.error("could not send local file");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        else{
            byte[] bytes = MapFile.getRemoteFile(Configuration.getDetailServerUri() + remoteFileName);
            if (bytes == null) {
                Log.error("could not get remote file");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!sendFile(bytes, fileName, response)) {
                Log.error("could not send remote file");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    protected boolean sendFile(byte[] bytes, String name, HttpServletResponse response){
        try {
            response.setContentType("image/png");
            response.setHeader("Content-Disposition", "filename=\"" + name + '"');
            response.setHeader("Content-Length", Long.toString(bytes.length));
            OutputStream out = response.getOutputStream();
            out.write(bytes, 0, bytes.length);
            out.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
