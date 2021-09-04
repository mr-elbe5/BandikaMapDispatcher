/*
 Bandika CMS - A Java based modular Content Management System
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
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class MapServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        StringTokenizer stk = new StringTokenizer(request.getRequestURI(), "/.");
        if (stk.countTokens() != 5){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Tile tile = new Tile();
        try {
            tile.type = TileType.valueOf(stk.nextToken());
        }
        catch (NoSuchElementException e) {
            Log.error("bad tile type", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try{
            tile.zoom = Integer.parseInt(stk.nextToken());
            tile.x = Integer.parseInt(stk.nextToken());
            tile.y = Integer.parseInt(stk.nextToken());
        }
        catch (NumberFormatException e) {
            Log.error("bad tile parameter format", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        tile.extension = stk.nextToken().toLowerCase();
        if (!tile.extension.equals("png")){
            Log.error("tile requests no png");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!sendLocalFile(tile, response)){
            if (!getRemoteFile(tile)){
                Log.error("could not get remote file");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!sendLocalFile(tile, response)){
                Log.error("could not send remote file as local");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    protected boolean sendLocalFile(Tile tile, HttpServletResponse response){
        String path = Configuration.getLocalPath() + tile.getTypedUri();
        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        try {
            response.setContentType("image/" + tile.extension);
            response.setHeader("Content-Disposition", "filename=\"" + file.getName() + '"');
            response.setHeader("Content-Length", Long.toString(file.length()));
            OutputStream out = response.getOutputStream();
            FileInputStream fin = new FileInputStream(file);
            byte[] bytes = new byte[4096];
            int len = 4096;
            while (len > 0) {
                len = fin.read(bytes, 0, 4096);
                if (len > 0) {
                    out.write(bytes, 0, len);
                }
            }
            out.flush();
            fin.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    protected boolean getRemoteFile(Tile tile) {
        try {
            String url = tile.getExternalUri();
            Log.info("requesting tile from " + url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(5))
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200){
                Log.error("remote server returned status " + response.statusCode());
                return false;
            }
            String path = Configuration.getLocalPath() + tile.getTypedUri();
            Log.info("received tile from " + url + " - saving as " + path);
            File file = new File(path);
            try {
                if (file.exists() && !file.delete()) {
                    Log.error("could not delete file " + path);
                    return false;
                }
                if (!assertDirectory(file.getParentFile())) {
                    Log.error("could not assert directories for " + path);
                    return false;
                }
                if (!file.createNewFile())
                    throw new IOException("file create error");
                FileOutputStream fout = new FileOutputStream(file);
                fout.write(response.body());
                fout.flush();
                fout.close();
            } catch (IOException e) {
                Log.error("could not write file " + path);
                return false;
            }
        }
        catch (IOException | InterruptedException e){
            Log.error("could not receive remote file", e);
            return false;
        }
        return true;
    }

    private boolean assertDirectory(File dir){
        if (dir.exists()){
            return true;
        }
        return dir.mkdirs();
    }

}
