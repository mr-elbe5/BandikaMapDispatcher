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
import de.elbe5.request.RequestData;
import org.json.simple.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Preloader {

    static final String STATE_IDLE = "IDLE";
    static final String STATE_RUNNING = "RUNNING";

    PreloadThread preloadThread = null;

    int zoom = 0;
    int minX = 0;
    int maxX = 0;
    int minY = 0;
    int maxY = 0;

    long allTiles = 0;
    long presentTiles = 0;
    long fetchedTiles = 0;
    long errors = 0;

    String state = STATE_IDLE;

    void startPreload(RequestData rdata, HttpServletResponse response){
        String token = rdata.getString("token");
        if (token==null || !token.equals(Configuration.getToken())){
            Log.error("bad token");
            rdata.put("token","wrong token");
            showPreload(rdata, response);
            return;
        }
        if (preloadThread != null && state.equals(STATE_RUNNING)){
            preloadThread.interrupt();
            preloadThread = null;
            state = STATE_IDLE;
        }
        zoom = rdata.getInt("zoom", 0);
        int maxSide = (int) Math.pow(2.0, zoom) - 1;
        minX = Math.max(0, rdata.getInt("minX", 0));
        maxX = Math.min(rdata.getInt("maxX", -1), maxSide);
        if (maxX == -1){
            maxX = maxSide;
        }
        minY = Math.max(0, rdata.getInt("minY", 0));
        maxY = Math.min(rdata.getInt("maxY", -1), maxSide);
        if (maxY == -1){
            maxY = maxSide;
        }
        long fx = maxX - minX + 1;
        long fy = maxY - minY + 1;
        allTiles = fx*fy;
        presentTiles = 0;
        fetchedTiles = 0;
        errors = 0;
        rdata.put("minX", Integer.toString(minX));
        rdata.put("maxX", Integer.toString(maxX));
        rdata.put("minY", Integer.toString(minY));
        rdata.put("maxY", Integer.toString(maxY));
        preloadThread = new PreloadThread();
        rdata.put("started", Boolean.toString(true));
        rdata.put("allTiles", Long.toString(allTiles));
        preloadThread.start();
        showPreload(rdata, response);
    }

    void stopPreload(HttpServletResponse response) {
        if (preloadThread != null && state.equals(STATE_RUNNING)){
            preloadThread.signalStop();
        }
        getState(response);
    }

    void preloadStopped(){
        state = STATE_IDLE;
        preloadThread = null;
        Log.info("preloader stopped");
    }

    @SuppressWarnings("unchecked")
    void getState(HttpServletResponse response){
        JSONObject obj = new JSONObject();
        obj.put("state", state);
        obj.put("present", Long.toString(presentTiles));
        obj.put("fetched", Long.toString(fetchedTiles));
        obj.put("errors", Long.toString(errors));
        String json = obj.toJSONString();
        sendJson(json, response);
    }

    void sendJson(String json, HttpServletResponse response){
        try {
            ServletOutputStream out = response.getOutputStream();
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Expires", "Tues, 01 Jan 1980 00:00:00 GMT");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setContentType("application/json");
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            response.setHeader("Content-Length", Integer.toString(bytes.length));
            out.write(bytes);
            out.flush();
        } catch (IOException ioe) {
            Log.error("json response error", ioe);
        }
    }

    void showPreload(RequestData rdata, HttpServletResponse response){
        RequestDispatcher rd = rdata.getRequest().getServletContext().getRequestDispatcher("/WEB-INF/preloadForm.jsp");
        try {
            rd.forward(rdata.getRequest(), response);
        } catch (ServletException | IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    class PreloadThread extends Thread{

        private boolean shouldStop = false;

        void signalStop(){
            shouldStop = true;
        }

        @Override
        public void run() {
            Log.info("preloader starting");
            state = STATE_RUNNING;
            shouldStop = false;
            for (int x = minX; x<=maxX; x++){
                for (int y = minY; y <= maxY; y++){
                    String uri = zoom + "/" + x + "/" + y + ".png";
                    if (MapFile.fileExists(Configuration.getLocalPath() + uri)){
                        presentTiles++;
                    }
                    else{
                        byte[] bytes = MapFile.getRemoteFile(Configuration.getMapServerUri() + uri);
                        if (bytes == null){
                            Log.error("could not get remote file");
                            errors++;
                        }
                        if (!MapFile.saveLocalFile(Configuration.getLocalPath() + uri, bytes)){
                            Log.error("could not save file");
                            errors++;
                        }
                        fetchedTiles++;
                    }
                    if (shouldStop){
                        Log.info("interrupting preloader");
                        break;
                    }
                }
                if (shouldStop){
                    break;
                }
            }
            preloadStopped();
        }
    }

}
