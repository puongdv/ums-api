package vn.ghn.utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author cuongtc
 */
public class HttpUtil {

    private static final Logger LOGGER = Logger.getLogger(HttpUtil.class);
    private static final Gson GSON = new Gson();

    private static final String charset = "UTF-8";

    public static String sendRequest(String requestUrl, Map<String, String> param, String httpMethod, String contentType) {
        try {
            String strParam = "";
            if(param != null && !param.isEmpty()){
                int i = 0;
                for (Map.Entry<String, String> entrySet : param.entrySet()) {
                    
                    if(i>0) strParam+="&";
                    
                    strParam+= entrySet.getKey() + "=" + entrySet.getValue();
                    i++;
                }
                if(!"POST".equals(httpMethod)){
                    requestUrl = requestUrl + "?" + param;
                }
            }
            
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            if("POST".equals(httpMethod)){
                connection.setDoInput(true);
                connection.setDoOutput(true);
                try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                    if(!strParam.isEmpty()){                    
                        writer.write(strParam);
                    }
                }
            }
            
            connection.setRequestMethod(httpMethod);
            connection.setRequestProperty("Accept", contentType);
            connection.setRequestProperty("Content-Type", String.format("%s; charset=%s", contentType, charset));                           
            StringBuilder jsonString;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                jsonString = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
            }
            connection.disconnect();
            return jsonString.toString();
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
            return null;
        }
    }

    public static <T> T sendRequest(Type type, String requestUrl, String strRequest, String httpMethod, String contentType) {
        try {
            if (httpMethod == null) {
                httpMethod = MethodContant.GET;
            }

            if (contentType == null) {
                contentType = ContentTypeContant.APPLICATION_JSON;
            }

            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if("POST".equals(httpMethod)){
                connection.setDoInput(true);
                connection.setDoOutput(true);  
                try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), charset)) {
                    if (!strRequest.isEmpty()) {
                        writer.write(strRequest);
                    }
                }
            }
            
            connection.setRequestMethod(httpMethod);
            connection.setRequestProperty("Accept", contentType);
            connection.setRequestProperty("Content-Type", String.format("%s; charset=%s", contentType, charset));
            
            StringBuilder jsonString;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                jsonString = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
            }
            connection.disconnect();
            return GSON.fromJson(jsonString.toString(), type);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
            return null;
        }
    }
    public static <T> T send(Type type, String url, String data, String method, String contentType, Map<String, String> headers) throws MalformedURLException, IOException{
        try {
            OutputStream os = null;
            HttpURLConnection httpcon;

            httpcon = (HttpURLConnection) ((new URL(url).openConnection()));

            if(!"GET".equals(method)) httpcon.setDoOutput(true);

            httpcon.setRequestProperty("Content-Type", contentType);
            httpcon.setRequestProperty("Accept", contentType);
            if(headers != null && !headers.isEmpty()){
                headers.entrySet().stream().forEach((entrySet) -> {
                    httpcon.setRequestProperty(entrySet.getKey(), entrySet.getValue());
                });
            }
            httpcon.setRequestMethod(method);
            httpcon.connect();
            if(!"GET".equals(method) && data != null){
                byte[] outputBytes = data.getBytes(charset);
                os = httpcon.getOutputStream();
                os.write(outputBytes);
            }
            StringBuilder jsonString;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(httpcon.getInputStream()))) {
                jsonString = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
            }
            httpcon.disconnect();        
            if(os != null) os.close();
            return GSON.fromJson(jsonString.toString(), type);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }        
    }
    
    public static Map<String, List<String>> makeRequestResultHeader(String requestUrl, String contentType, Map<String, String> param, String method) {
        try {
            String strParam = "";
            if (param != null && !param.isEmpty()) {
                int i = 0;
                for (Map.Entry<String, String> entrySet : param.entrySet()) {

                    if (i > 0) {
                        strParam += "&";
                    }

                    strParam += entrySet.getKey() + "=" + entrySet.getValue();
                    i++;
                }
            }
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", contentType);
            connection.setRequestProperty("Content-Type", String.format("%s; charset=%s", contentType, charset));
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                if (!strParam.isEmpty()) {
                    writer.write(strParam);
                }
            }
            StringBuilder jsonString;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                jsonString = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
            }
            connection.disconnect();
            return connection.getHeaderFields();
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
            return null;
        }
    }

    public static Map<String, List<String>> sendPostRequestResultHeader(String requestUrl, String contentType, Map<String, String> param, Map<String, String> headers) {
        try {
            String strParam = "";
            if (param != null && !param.isEmpty()) {
                int i = 0;
                for (Map.Entry<String, String> entrySet : param.entrySet()) {

                    if (i > 0) {
                        strParam += "&";
                    }

                    strParam += entrySet.getKey() + "=" + entrySet.getValue();
                    i++;
                }
            }
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", contentType);
            connection.setRequestProperty("Content-Type", String.format("%s; charset=%s", contentType, charset));

            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entrySet : headers.entrySet()) {
                    connection.setRequestProperty(entrySet.getKey(), entrySet.getValue());
                }
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                if (!strParam.isEmpty()) {
                    writer.write(strParam);
                }
            }
            StringBuilder jsonString;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                jsonString = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
            }
            connection.disconnect();
            return connection.getHeaderFields();
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
            return null;
        }
    }
    
    public static String sendUrlencoded(String apiUrl, String urlParameters) throws MalformedURLException, IOException{        
        byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int postDataLength = postData.length;        
        URL url = new URL( apiUrl );
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
        conn.setUseCaches(false);
        try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
           wr.write( postData );
        }
        
        StringBuilder jsonString;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            jsonString = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
        }
        conn.disconnect();
        return jsonString.toString();
    }
}
