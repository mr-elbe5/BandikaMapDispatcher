/*
 BandikaMapDispatcher
 Copyright (C) 2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.elbe5.mapdispatcher;

enum TileType{
    carto,
    topo
}
public class Tile {

    TileType type;
    int zoom = 0;
    int x = 0;
    int y = 0;
    String extension = "png";

    String getDirectory(){
        return zoom + "/" + x ;
    }

    String getUri(){
        return zoom + "/" + x + "/" + y + "." + extension;
    }

    String getTypedUri(){
        return type.name() + "/" + getUri();
    }
}
