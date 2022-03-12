/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.mapdispatcher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class MapServlet extends HttpServlet {

    protected boolean sendLocalFile(byte[] bytes, String name, HttpServletResponse response){
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

    private boolean assertDirectory(File dir){
        if (dir.exists()){
            return true;
        }
        return dir.mkdirs();
    }

}
