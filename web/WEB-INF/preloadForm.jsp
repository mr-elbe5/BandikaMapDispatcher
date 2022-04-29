<%--
Bandika MapDispatcher - a proxy and preloader for OSM map tiles
Copyright (C) 2009-2021 Michael Roennau

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    boolean started = rdata.getBoolean("started");
    String allTiles = rdata.getString("allTiles");
    int zoom = rdata.getInt("zoom", 0);
    int minX = rdata.getInt("minX", 0);
    int maxX = rdata.getInt("maxX", -1);
    int minY = rdata.getInt("minY", 0);
    int maxY = rdata.getInt("maxY", -1);
    String token = rdata.getString("token");
    String message = rdata.getString("message");
%>
<html>
<head>
    <title>Preload Tiles</title>
    <link rel="stylesheet" href="/static-content/bootstrap.css"/>
    <script type="text/javascript" src="/static-content/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="/static-content/bootstrap.bundle.min.js"></script>
</head>
<body>
    <div class="container" style="margin-top: 2rem;">
        <h1>Preload Tiles</h1>
        <div>

        </div>
        <form action="preload" method="post">
            <input type = "hidden" name = "action" value="start" />
            <div class = "mb-3">
                <label for="zoom">Zoom</label>
                <input class="form-control" type="text" id="zoom" name="zoom" value="<%=zoom%>"/>
            </div>
            <div class = "mb-3">
                <label for="minX">Min X</label>
                <input class="form-control" type="text" id="minX" name="minX" value="<%=minX%>"/>
            </div>
            <div class = "mb-3">
                <label for="maxX">Max X</label>
                <input class="form-control" type="text" id="maxX" name="maxX" value="<%=maxX%>"/>
            </div>
            <div class = "mb-3">
                <label for="minY">Min Y</label>
                <input class="form-control" type="text" id="minY" name="minY" value="<%=minY%>"/>
            </div>
            <div class = "mb-3">
                <label for="maxY">Max Y</label>
                <input class="form-control" type="text" id="maxY" name="maxY" value="<%=maxY%>"/>
            </div>
            <div class = "mb-3">
                <label for="token">Token</label>
                <input class="form-control" type="text" id="token" name="token" value="<%=token%>"/>
            </div>
            <div class = "mb-3">
                <label for="message">Message</label>
                <input class="form-control" type="text" id="message" value="<%=message%>" disabled/>
            </div>
            <button type="submit" class="btn btn-primary" id="startButton">Start</button>
        </form>
        <form action="preload" method="post">
            <button type="submit" class="btn btn-primary" id="stopButton" onclick="stopPreload(); return false;">Stop</button>
        </form>
        <div id="results">
            <div class="font-weight-bold text-center">All Tiles: <%=allTiles%></div>
            <hr/>
            <table class="table" style="text-align: left;">
                <thead>
                    <tr>
                        <th scope="col">Status</th>
                        <th scope="col">Present Tiles</th>
                        <th scope="col">Fetched Tiles</th>
                        <th scope="col">Errors</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td id="state">not started</td>
                        <td id="present">0</td>
                        <td id="fetched">0</td>
                        <td id="errors">0</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <script type="application/javascript">

        var handle = null;
        var startButton = $('#startButton');
        var stopButton = $('#stopButton');

        function startPreload(){
            handle = window.setInterval(getState, 1000);
            startButton.prop('disabled', true);
            stopButton.prop('disabled', false);
        }

        function stopPreload(){
            if (handle != null){
                window.clearInterval(handle);
                handle = null;
            }
            startButton.prop('disabled', false);
            stopButton.prop('disabled', true);
            $.ajax({
                url: 'preload', type: 'POST', data: 'action=stop', dataType: 'json'
            }).success(function (data) {
                $('#state').html("STOPPED");
                $('#present').html(data.present);
                $('#fetched').html(data.fetched);
                $('#errors').html(data.errors);
            }).error(function (err){
                console.log(err);
            });
        }

        function getState() {
            $.ajax({
                url: 'preload', type: 'POST', data: 'action=getState', dataType: 'json'
            }).success(function (data) {
                $('#state').html(data.state);
                $('#present').html(data.present);
                $('#fetched').html(data.fetched);
                $('#errors').html(data.errors);
                if (data.state === 'IDLE'){
                    stopPreload();
                }
            }).error(function (err){
                console.log(err);
            });
        }

        startButton.prop('disabled', false);
        stopButton.prop('disabled', true);
        <% if (started){%>
        startPreload();
        <%}%>
    </script>
</body>
</html>
