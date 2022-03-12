/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.mapdispatcher;

import de.elbe5.request.RequestData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PreloadServlet extends MapServlet {

    static Preloader preloader = new Preloader();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        RequestData rdata = new RequestData("GET", request);
        doRequest(rdata, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        RequestData rdata = new RequestData("POST", request);
        doRequest(rdata, response);
    }

    protected void doRequest(RequestData rdata, HttpServletResponse response) {
        rdata.init();
        String action = rdata.getString("action");
        switch (action) {
            case "start":
                preloader.startPreload(rdata, response);
                break;
            case "stop":
                preloader.stopPreload(rdata, response);
                break;
            case "getState":
                preloader.getState(response);
                break;
            default:
                preloader.showPreload(rdata, response);
                break;
        }
    }
}
