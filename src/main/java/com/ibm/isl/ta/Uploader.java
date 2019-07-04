package com.ibm.isl.ta;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import org.json.JSONArray;
import org.json.JSONObject;

public class Uploader {

    /**
     * Upload Profile Analysis to Transformation Advisor
     *
     * @param workspace
     * @param profileName
     * @param url
     * @param analysisFile
     * @return HTTP Response body
     * @throws IOException
     */
    public String post(String workspace, String profileName, String url, File analysisFile) throws IOException {
        URL urlObj = null;
        String response = "";
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Get the Connection according to the connection type provided in the uploadEndPoint.json
        if (urlObj != null) {
            if (connectionIsHttps(url)) {
                URLConnection con = getHttpsConnection(url);
                response = handleConnection(con, workspace, profileName, analysisFile, response);
            } else {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                response = handleConnection(con, workspace, profileName, analysisFile, response);
            }


        } else {
            System.out.println("Unable to create the specified URL");
        }
        return response;
    }

    private static Boolean connectionIsHttps(String urlString) {
        if (urlString.regionMatches(0, "https", 0, 5)) {
            return true;
        } else {
            return false;
        }
    }


    private static String handleConnection(URLConnection con, String workspace, String profileName, File analysisFile, String response) throws IOException {

        con.setRequestProperty("Content-Type", "application/octet-stream");
        con.setRequestProperty("workspace", workspace);
        con.setRequestProperty("profileName", profileName);
        con.setDoOutput(true);

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(analysisFile);
            final byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                con.getOutputStream().write(buffer, 0, bytesRead);
            }
            con.getOutputStream().flush();
        } catch (SSLException ssle) {
            //Odds are here that there is a protocol issue and your java is too old for the TLS1.2
            //i. Oracle Java 1.6.0_181-b10 or newer
            //ii. IBM Java 6.0.16.40 or newer
            //iii. Manual upload if upgrading Java is not an option
        	System.out.println("Your version of Java does not support TLS 1.2");
        	System.out.println("It is recommended that you manually upload the zip result files via the Transformation Advisor UI");
        	System.out.println("Alternatively you can rerun this command using the --java-home param and point to the JRE provided in this Data Collector");
        	System.out.println("Alternatively you can upgrade to Oracle Java 1.6.0_181-b10 or newer");
        	System.out.println("Alternatively you can upgrade to IBM Java 6.0.16.40 or newer");
        }
        catch (Exception e) {
        	System.out.println("Problem connecting with server");
        	e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            	System.out.println("Problem reading the generated zip file.");
            	e.printStackTrace();
            }
            try {
                con.getOutputStream().close();
            } catch (IOException e) {
            	System.out.println("Problem sending the outputStream to the liberty server.");
            	e.printStackTrace();
            }
        }

        HttpURLConnection httpConn = (HttpURLConnection) con;
        BufferedReader replyReader = null;
        try {
            if (httpConn.getResponseCode() >= 400) {

                replyReader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));

                String line, responseString = null;
                while ((line = replyReader.readLine()) != null) {
                    responseString = response.concat(line);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                if (responseString != null) {
                    if (objectMapper.readTree(responseString).get("response").size() > 1) {
                        response = objectMapper.readTree(responseString).get("response").get("error").asText();
                    } else { // handle the case for the old TA response.
                        response = objectMapper.readTree(responseString).get("response").asText();
                    }
                }

            } else {
                replyReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));

                String line, responseString = null;
                while ((line = replyReader.readLine()) != null) {
                    responseString = response.concat(line);
                }

                try {
                    JSONObject respJson = new JSONObject(responseString);
                    if (respJson != null) {
                        response = (String) respJson.get("responseMsg");
                    } else {
                        response = responseString;
                    }
                } catch (Exception e) {
                	System.out.println("Failed to parse response: " + responseString);
                    response = responseString;
                }
            }
            replyReader.close();
        } catch (IOException e) {
        	System.out.println("Problem with the server connection.");
        	e.printStackTrace();
        }

        System.out.println(response);
        return response;
    }


    private static URLConnection getHttpsConnection(String url) throws IOException {

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }

        }};

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        if (null != sc) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }


        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setHostnameVerifier(allHostsValid);
        return conn;

    }

}
