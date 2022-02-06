package com.sscommu.pokeumcho;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class SimpleHttpJSON {

    /** 보안 코드 */
    private static final String SECURE_CODE = "";

    private static String SCHEME = "https://";
    private static String HOST = "www.sscommu.com";
    private static String PATH_PREFIX = "/public/Android/";

    private String mPath;
    private Map<String, String> mQuery;

    /** Constructor */

    public SimpleHttpJSON(String path) {
        mPath = path;
    }

    public SimpleHttpJSON(String path, Map<String, String> query) {
        mPath = path;
        mQuery = query;

        mQuery.put("secure", SECURE_CODE);
    }


    public String getURL(boolean includeQuery) throws Exception {
        StringBuilder url = new StringBuilder();

        url.append(SCHEME)
                .append(HOST)
                .append(PATH_PREFIX)
                .append(mPath);

        if (includeQuery && mQuery != null) {
            url.append("?")
                    .append(getEncodedQuery());
        }

        return url.toString();
    }

    private String getEncodedQuery() throws Exception {
        StringBuilder query = new StringBuilder();

        for (String key : mQuery.keySet()) {
            query.append(
                    URLEncoder.encode(key, "UTF-8") +
                            "=" +
                            URLEncoder.encode(mQuery.get(key), "UTF-8")
                            + "&");
        }
        // 마지막 "&" 제거
        query.setLength(query.length() - 1);

        return query.toString();
    }


    public String sendGet() throws Exception {
        URL url = new URL(getURL(true));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            return getResponse(connection);
        } else {    // Bad Response Code
            return "";
        }
    }

    public String sendPost() throws Exception {
        URL url = new URL(getURL(false));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept-Charset","UTF-8");
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(getEncodedQuery().getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            return getResponse(connection);
        } else {    // Bad Response Code
            return "";
        }
    }


    private String getResponse(HttpURLConnection connection) {
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()))) {
            String inputLine = null;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString().trim();
        } catch (IOException exc) {
            // Handle exceptions
        }
        return "";
    }
}
