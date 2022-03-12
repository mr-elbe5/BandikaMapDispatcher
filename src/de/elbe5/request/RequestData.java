/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.request;

import de.elbe5.base.data.BinaryFile;
import de.elbe5.base.data.KeyValueMap;
import de.elbe5.base.log.Log;

import javax.servlet.http.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestData extends KeyValueMap {

    public static RequestData getRequestData(HttpServletRequest request) {
        return (RequestData) request.getAttribute(RequestKeys.KEY_REQUESTDATA);
    }

    private final Map<String, Cookie> cookies = new HashMap<>();

    protected HttpServletRequest request;

    private int id = 0;

    private final String method;

    public RequestData(String method, HttpServletRequest request) {
        this.request = request;
        this.method = method;
    }

    public void init(){
        request.setAttribute(RequestKeys.KEY_REQUESTDATA, this);
        readRequestParams();
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPostback() {
        return method.equals("POST");
    }

    /*********** message *********/

    public boolean hasMessage() {
        return containsKey(RequestKeys.KEY_MESSAGE);
    }

    public void setMessage(String msg, String type) {
        put(RequestKeys.KEY_MESSAGE, msg);
        put(RequestKeys.KEY_MESSAGETYPE, type);
    }

    /************** request attributes *****************/

    public void readRequestParams() {
        if (isPostback()) {
            String type = request.getContentType();
            if (type != null && type.toLowerCase().startsWith("multipart/form-data")) {
                getMultiPartParams();
            } else if (type != null && type.equalsIgnoreCase("application/octet-stream")) {
                getSinglePartParams();
                getByteStream();
            }
        }
        getSinglePartParams();
    }

    private void getByteStream(){
        try {
            InputStream in = request.getInputStream();
            BinaryFile file=new BinaryFile();
            file.setBytesFromStream(in);
            file.setFileSize(file.getBytes().length);
            file.setFileName(request.getHeader("fileName"));
            file.setContentType(request.getHeader("contentType"));
            put("file", file);
        }
        catch (IOException ioe){
            Log.error("input stream error", ioe);
        }
    }

    private void getSinglePartParams() {
        Enumeration<?> enm = request.getParameterNames();
        while (enm.hasMoreElements()) {
            String key = (String) enm.nextElement();
            String[] strings = request.getParameterValues(key);
            put(key, strings);
        }
    }

    private void getMultiPartParams() {
        Map<String, List<String>> params = new HashMap<>();
        Map<String, List<BinaryFile>> fileParams = new HashMap<>();
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                String name = part.getName();
                String fileName = getFileName(part);
                if (fileName != null) {
                    if (fileName.isEmpty())
                        continue;
                    BinaryFile file = getMultiPartFile(part, fileName);
                    if (file != null) {
                        List<BinaryFile> values;
                        if (fileParams.containsKey(name))
                            values = fileParams.get(name);
                        else {
                            values = new ArrayList<>();
                            fileParams.put(name, values);
                        }
                        values.add(file);
                    }
                } else {
                    String param = getMultiPartParameter(part);
                    if (param != null) {
                        List<String> values;
                        if (params.containsKey(name))
                            values = params.get(name);
                        else {
                            values = new ArrayList<>();
                            params.put(name, values);
                        }
                        values.add(param);
                    }
                }
            }
        } catch (Exception e) {
            Log.error("error while parsing multipart params", e);
        }
        for (String key : params.keySet()) {
            List<String> list = params.get(key);
            if (list.size() == 1) {
                put(key, list.get(0));
            } else {
                String[] strings = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    strings[i] = list.get(i);
                }
                put(key, strings);
            }
        }
        for (String key : fileParams.keySet()) {
            List<BinaryFile> list = fileParams.get(key);
            if (list.size() == 1) {
                put(key, list.get(0));
            } else {
                BinaryFile[] files = new BinaryFile[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    files[i] = list.get(i);
                }
                put(key, files);
            }
        }
    }

    private String getMultiPartParameter(Part part) {
        try {
            byte[] bytes = new byte[(int) part.getSize()];
            int read = part.getInputStream().read(bytes);
            if (read > 0) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            Log.error("could not extract parameter from multipart", e);
        }
        return null;
    }

    private BinaryFile getMultiPartFile(Part part, String fileName) {
        try {
            BinaryFile file = new BinaryFile();
            file.setFileName(fileName);
            file.setContentType(part.getContentType());
            file.setFileSize((int) part.getSize());
            InputStream in = part.getInputStream();
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(file.getFileSize());
            byte[] buffer = new byte[8096];
            int len;
            while ((len = in.read(buffer, 0, 8096)) != -1) {
                out.write(buffer, 0, len);
            }
            file.setBytes(out.toByteArray());
            return file;
        } catch (Exception e) {
            Log.error("could not extract file from multipart", e);
            return null;
        }
    }

    private String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    /************** request attributes ***************/

    public void setRequestObject(String key, Object obj){
        request.setAttribute(key, obj);
    }

    public Object getRequestObject(String key){
        return request.getAttribute(key);
    }

    public <T> T getRequestObject(String key, Class<T> cls) {
        try {
            return cls.cast(request.getAttribute(key));
        }
        catch (NullPointerException | ClassCastException e){
            return null;
        }
    }

    public void removeRequestObject(String key){
        request.removeAttribute(key);
    }


}








