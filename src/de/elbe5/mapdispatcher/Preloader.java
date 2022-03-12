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

    int z = 0;
    int minX = 0;
    int maxX = 0;
    int minY = 0;
    int maxY = 0;

    int allTiles = 0;
    int presentTiles = 0;
    int fetchedTiles = 0;
    int errors = 0;

    String state = STATE_IDLE;

    void initValues(int zoom, int minX, int maxX, int minY, int maxY){
        z = zoom;
        this.minX = minX;
        this.maxX = maxX;
        if (this.maxX == -1){
            this.maxX = (int) Math.pow(2.0, z) - 1;
        }
        this.minY = minY;
        this.maxY = maxY;
        if (this.maxY == -1){
            this.maxY = (int) Math.pow(2.0, this.z) - 1;
        }
        allTiles = (this.maxX - this.minX + 1) * (this.maxY - this.minY + 1);
        presentTiles = 0;
        fetchedTiles = 0;
        errors = 0;
    }

    void startPreload(RequestData rdata, HttpServletResponse response){
        String token = rdata.getString("token");
        if (token==null || !token.equals(Configuration.getToken())){
            Log.error("bad token");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (preloadThread != null && state.equals(STATE_RUNNING)){
            preloadThread.interrupt();
            preloadThread = null;
            state = STATE_IDLE;
        }
        initValues(rdata.getInt("zoom", 0), rdata.getInt("minX", 0), rdata.getInt("maxX", -1), rdata.getInt("minY", 0), rdata.getInt("maxY", -1));
        preloadThread = new PreloadThread();
        rdata.put("started", Boolean.toString(true));
        rdata.put("allTiles", Integer.toString(allTiles));
        preloadThread.start();
        showPreload(rdata, response);
    }

    void stopPreload(RequestData rdata, HttpServletResponse response) {
        if (preloadThread != null && state.equals(STATE_RUNNING)){
            preloadThread.interrupt();
            preloadThread = null;
            state = STATE_IDLE;
        }
        JSONObject obj = new JSONObject();
        obj.put("state", state);
        String json = obj.toJSONString();
        sendJson(json, response);
    }

    void getState(HttpServletResponse response){
        Log.info("returning state");
        JSONObject obj = new JSONObject();
        obj.put("state", state);
        obj.put("present", presentTiles);
        obj.put("fetched", fetchedTiles);
        obj.put("errors", errors);
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
            //Log.info("json has been sent");
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

        @Override
        public void run() {
            Log.info("preloader starting");
            state = STATE_RUNNING;
            for (int x = minX; x<=maxX; x++){
                for (int y = minY; y <= maxY; y++){
                    String uri = z + "/" + x + "/" + y + ".png";
                    Log.info("assert " + uri);
                    if (MapFile.fileExists(Configuration.getLocalPath() + uri)){
                        Log.info("file exists");
                        presentTiles++;
                    }
                    else{
                        Log.info("new file");
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
                    if (state.equals(STATE_IDLE)){
                        preloadThread.interrupt();
                        Log.info("preloader interrupted");
                        preloadThread = null;
                        return;
                    }
                }
            }
            state = STATE_IDLE;
            preloadThread = null;
            Log.info("preloader stopped");
        }
    }

}