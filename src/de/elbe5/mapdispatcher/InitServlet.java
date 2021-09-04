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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class InitServlet extends MapServlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        System.out.println("initializing BandikaMapDispatcher Application...");
        Log.initLog("BandikaMapDispatcher");
        ServletContext context=servletConfig.getServletContext();
        Configuration.setConfigs(context);
        Log.log("External carto server is " + Configuration.getCartoMapServerUri());
        Log.log("External topo server is " + Configuration.getTopoMapServerUri());
        Log.log("BandikaMapDispatcher initialized");
    }

}
