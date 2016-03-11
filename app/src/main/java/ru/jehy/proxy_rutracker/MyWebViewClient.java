package ru.jehy.proxy_rutracker;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


class MyWebViewClient extends WebViewClient {

    String authCookie = null;

    public HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));


            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            DefaultHttpClient d = new DefaultHttpClient(ccm, params);
            d.setRoutePlanner(new MyRoutePlanner());
            return d;
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    public static String md5(final String s) {
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


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {//(WebView view, String url) {
        // String jsonResponse = null;
        String url = request.getUrl().toString();

        Log.d("WebView", "Request for url: " + url + " intercepted");



        /*if (url.contains("marketgid.com")
                || url.contains("adriver.ru")
                || url.contains("thisclick.network")
                || url.contains("static2.rutracker.org/brand")
                || url.contains("hghit.com")
                || url.contains("onedmp.com")
                ) {
            Log.d("WebView", "Not fetching banners");
            return null;
        }*/

        if (url.startsWith("https://")) {
            Log.d("WebView", "Not fetching url with HTTPS, it won't work on google proxy");
            return super.shouldInterceptRequest(view, request);
        }
        if (url.startsWith("http://google.com") || url.startsWith("http://www.google.com")) {
            Log.d("WebView", "Not trying to proxy google scripts");
            return super.shouldInterceptRequest(view, request);
        }
        if (url.length() != 0) {
            try {
                String[] header = authHeader();
                Log.d("WebView", header[0] + " : " + header[1]);

                HttpHost proxy = new HttpHost("proxy.googlezip.net", 443, "https");
                //dhc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

                HttpClient cli = getNewHttpClient();
                cli.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                HttpResponse response;
                Map<String, String> headers = request.getRequestHeaders();

                String l_username = request.getUrl().getQueryParameter("login_username");
                String l_password = request.getUrl().getQueryParameter("login_password");
                String login = request.getUrl().getQueryParameter("login");
                if (request.getMethod().equals("GET") && !url.contains("login.rutracker.org/forum/login.php")) {
                    HttpGet request1 = new HttpGet(url);

                    for (Map.Entry<String, String> entry : headers.entrySet())
                        request1.setHeader(entry.getKey(), entry.getValue());

                    request1.setHeader(header[0], header[1]);
                    request1.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    request1.setHeader("Accept-Encoding", "gzip");
                    request1.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
                    request1.setHeader("Accept-Encoding", "gzip");
                    request1.setHeader("Referer", "http://rutracker.org/forum/index.php");
                    if (authCookie != null && (url.startsWith("http://rutracker.org")||url.startsWith("http://login.rutracker.org"))) {
                        request1.setHeader("Cookie", authCookie);
                        Log.d("WebView", "cookie sent:" + authCookie);
                    }
                    Log.d("WebView", "cookie sent:" + authCookie);
                    response = cli.execute(request1);

                } else {
                    HttpPost request1 = new HttpPost(url);
                    if (login != null) {
                        List<NameValuePair> nvps = new ArrayList<>();
                        nvps.add(new BasicNameValuePair("login", login));
                        nvps.add(new BasicNameValuePair("login_username", l_username));
                        nvps.add(new BasicNameValuePair("login_password", l_password));
                        request1.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                    }
                    for (Map.Entry<String, String> entry : headers.entrySet())
                        request1.setHeader(entry.getKey(), entry.getValue());
                    request1.setHeader(header[0], header[1]);
                    request1.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    request1.setHeader("Accept-Encoding", "gzip");
                    request1.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
                    request1.setHeader("Accept-Encoding", "gzip");
                    request1.setHeader("Referer", "http://rutracker.org/forum/index.php");
                    if (authCookie != null && (url.startsWith("http://rutracker.org")||url.startsWith("http://login.rutracker.org"))) {
                        request1.setHeader("Cookie", authCookie);
                        Log.d("WebView", "cookie sent:" + authCookie);
                    }
                    response = cli.execute(request1);
                }
                //HttpResponse response = httpclient.execute(request1);
                //Log.d("WebView", "Response: " + response.toString());
                if (url.startsWith("http://login.rutracker.org/forum/login.php")) {

                    Header[] all = response.getAllHeaders();
                    for (Header header1 : all) {
                        Log.d("WebView", "LOGIN HEADER: " + header1.getName() + " : " + header1.getValue());

                    }
                    Header[] cookies = response.getHeaders("set-cookie");
                    if (cookies.length > 0) {
                        Header cookie = cookies[0];
                        String val = cookie.getValue();
                        val = val.substring(0, val.indexOf(";"));
                        Log.d("WebView", "=== Auth cookie: ===" + val);
                        authCookie = val;
                    }
                }
                int responseCode = response.getStatusLine().getStatusCode();
                String responseMessage = response.getStatusLine().getReasonPhrase();

                if (responseCode == 200) {
                    InputStream input = response.getEntity().getContent();
                    String encoding = null;
                    if (response.getEntity().getContentEncoding() != null)
                        encoding = response.getEntity().getContentEncoding().getValue();
                    Log.d("WebView", "data ok");
                    InputStream inputStr;

                    if (response.getEntity().getContentEncoding() != null && "gzip".equals(response.getEntity().getContentEncoding().getValue()))
                        inputStr = (new GZIPInputStream(input));
                    else
                        inputStr = input;
                    //inputStr.
                    //String encoding = "UTF-8";
                    Log.d("WebView", "connection encoding : " + encoding);
                    String mime = response.getEntity().getContentType().getValue();
                    Log.d("WebView", "mime full: " + mime);
                    if (mime.contains(";")) {
                        String[] arr = mime.split(";");
                        mime = arr[0];
                        arr = arr[1].split("=");
                        encoding = arr[1];
                        Log.d("WebView", "encoding from mime: " + encoding);
                    }
                    if (encoding == null || encoding.equals("gzip"))
                        encoding = "UTF-8";

                    Log.d("WebView", "clean mime: " + mime);
                    Log.d("WebView", "encoding final: " + encoding);
                    if (url.contains("rutracker.org"))
                        encoding = "windows-1251";//for rutracker only
                    if (mime.equals("text/html") && url.contains("rutracker.org")) {
                        encoding = "windows-1251";//for rutracker only
                        String data = convertStreamToString(inputStr, encoding);
                        data = data.replace("id=\"top-login-form\" method=\"post", "id=\"top-login-form\" method=\"get");
                        data = data.replace("<form id=\"login-form\" action=\"http://login.rutracker.org/forum/login.php\" method=\"post\">",
                                "<form id=\"login-form\" action=\"http://login.rutracker.org/forum/login.php\" method=\"get\">");
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
                total.append("\n" + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }
}