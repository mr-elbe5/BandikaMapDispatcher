/*
 Bandika MapDispatcher - a proxy and preloader for OSM map tiles
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.mapdispatcher;

import de.elbe5.base.log.Log;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFile  {

    static boolean fileExists(String path){
        File file = new File(path);
        return file.exists();
    }

    static byte[] getLocalFile(String path){
        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            return null;
        }
        try {
            int len = (int)file.length();
            FileInputStream fin = new FileInputStream(file);
            byte[] bytes = new byte[len];
            if (fin.read(bytes, 0, len) != len){
                bytes = null;
            }
            fin.close();
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }

    static byte[] getRemoteFile(String url) {
        byte[] bytes;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(5))
                    .setHeader("User-Agent", "Mozilla/5.0 Firefox/92.0")
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30))
                    .executor(executor)
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200){
                Log.error("remote server returned status " + response.statusCode());
                return null;
            }
            bytes = response.body();
        }
        catch (IOException | InterruptedException e){
            Log.error("could not receive remote file", e);
            return null;
        }
        finally{
            executor.shutdownNow();
        }
        return bytes;
    }

    static boolean saveLocalFile(String path, byte[] bytes) {
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
            fout.write(bytes);
            fout.flush();
            fout.close();
        }
        catch (IOException e){
            Log.error("could not save file", e);
            return false;
        }
        return true;
    }

    static private boolean assertDirectory(File dir){
        if (dir.exists()){
            return true;
        }
        return dir.mkdirs();
    }

}
