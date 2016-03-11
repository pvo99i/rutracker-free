package ru.jehy.proxy_rutracker;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Bond on 01-Dec-15.
 */

class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String[] authHeader() {
        String[] result = new String[2];
        result[0] = "Chrome-Proxy";
        String authValue = "ac4500dd3b7579186c1b0620614fdb1f7d61f944";
        String timestamp = Long.toString(System.currentTimeMillis()).substring(0, 10);
        String[] chromeVersion = {"49", "0", "2623", "87"};

        String sid = (timestamp + authValue + timestamp);

        sid = md5(sid);
        result[1] = "ps=" + timestamp + "-" + Integer.toString((int) (Math.random() * 1000000000)) + "-" + Integer.toString((int) (Math.random() * 1000000000)) + "-" + Integer.toString((int) (Math.random() * 1000000000)) + ", sid=" + sid + ", b=" + chromeVersion[2] + ", p=" + chromeVersion[3] + ", c=win";
        return result;
    }


    @TargetApi(21)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {//(WebView view, String url) {
        // String jsonResponse = null;
        String url = request.getUrl().toString();

        Log.d("WebView", "Request for url: " + url + " intercepted");
        if (url.startsWith("https://")) {
            Log.d("WebView", "Not fetching url with HTTPS, it won't work on google proxy");
            return null;
        }
        if (url.length() != 0) {
            try {
                /************** For getting response from HTTP URL start ***************/
                //URL object = new URL(url);

                /*HttpURLConnection testConnection = (HttpURLConnection) new URL("http://check.googlezip.net/connect").openConnection();
                InputStream testInputStr = testConnection.getInputStream();
                String testData = convertStreamToString(testInputStr, "UTF-8");
                Log.d("WebView", "test data " + testData);*/

                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("compress.googlezip.net", 80));
                //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.googlezip.net", 443));

                //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("jehy.ru", 3128));

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(proxy);

                //HttpURLConnection connection = (HttpURLConnection) object
                //        .openConnection();
                // int timeOut = connection.getReadTimeout();
                //connection.addRequestProperty("Authorization", "SpdyProxy ps=\"1390372720-748089166-1671804897-22716992\", sid=\"CLOE3NWitssCFQLQcAodfEEKaw\"");
                String[] header = authHeader();
                Log.d("WebView", header[0] + " : " + header[1]);
                //connection.setDoInput(true);

                connection.setRequestProperty(header[0], header[1]);

                //connection.setRequestProperty("Chrome-Proxy","ps=1457625860-988620712-827289254-268507814, sid=a6d0f60c28064b6294917f32adec5a68, b=2623, p=87, c=win");
                //connection.setRequestProperty("Authorization","SpdyProxy ps=1457625860-988620712-827289254-268507814, sid=a6d0f60c28064b6294917f32adec5a68, b=2623, p=87, c=win");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
                connection.setReadTimeout(60 * 1000);
                connection.setConnectTimeout(60 * 1000);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept-Encoding", "gzip");
                if (connection.usingProxy())
                    Log.d("WebView", "connection using proxy");
                else
                    Log.d("WebView", "connection NOT using proxy");
                //connection.setRequestMethod("GET");
                //connection.connect();
                //String authorization = "xyz:xyz$123";
                //String encodedAuth = "";//"Basic "+ Base64.encode(authorization.getBytes());
                //connection.setRequestProperty("Authorization", encodedAuth);
                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();

                if (responseCode == 200) {
                    Log.d("WebView", "data ok");
                    InputStream inputStr;
                    if ("gzip".equals(connection.getContentEncoding()))
                        inputStr = (new GZIPInputStream(connection.getInputStream()));
                    else
                        inputStr = connection.getInputStream();
                    //inputStr.
                    String encoding = "UTF-8";
                    /*if(connection.getContentEncoding() == null)
                    {
                        Log.d("WebView", "no connection content encoding, leaving default UTF-8 ");
                    }
                    else {
                        encoding = connection.getContentEncoding();
                    }*/
                    Log.d("WebView", "connection encoding : " + connection.getContentEncoding());
                    String mime = connection.getContentType();
                    Log.d("WebView", "mime full: " + mime);
                    if (mime.contains(";")) {
                        String[] arr = mime.split(";");
                        mime = arr[0];
                        arr = arr[1].split("=");
                        encoding = arr[1];
                        Log.d("WebView", "encoding from mime: " + encoding);
                    }

                    Log.d("WebView", "clean mime: " + mime);
                    Log.d("WebView", "encoding final: " + encoding);
                    if (mime.equals("text/html")) {
                        if (url.contains("rutracker.org"))
                            encoding = "windows-1251";//for rutracker only
                        String data = convertStreamToString(inputStr, encoding);
                        inputStr = new ByteArrayInputStream(data.getBytes(encoding));
                        Log.d("WebView", "data " + data);
                    }
                    return new WebResourceResponse(mime, encoding, inputStr);
                    //jsonResponse = IOUtils.toString(inputStr, encoding);
                    /************** For getting response from HTTP URL end ***************/

                } else {
                    Log.d("WebView", "Response code: " + responseCode);
                    Log.d("WebView", "Response message: " + responseMessage);

                }
            } catch (Exception e) {
                Log.d("WebView", "Error fetching URL " + url + ":");
                e.printStackTrace();

            }
        }
        return null;
    }

    static String convertStreamToString(java.io.InputStream is, String encoding) {
        //java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        //return s.hasNext() ? s.next() : "";
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(is, encoding));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }
}