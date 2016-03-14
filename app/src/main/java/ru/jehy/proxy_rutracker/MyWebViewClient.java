package ru.jehy.proxy_rutracker;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

        Uri url = request.getUrl();

        Log.d("WebView", "Request for url: " + url + " intercepted");

        if(url==null || url.getHost()==null)
        {
            Log.d("WebView", "No url or host provided, better let webview deal with it");
            return super.shouldInterceptRequest(view, request);
        }

        if (Utils.is_adv(url)) {
            Log.d("WebView", "Not fetching advertisment");
            return new WebResourceResponse("text/javascript", "UTF-8", null);
        }

        if (url.getScheme().equals("https")) {
            Log.d("WebView", "Not proxying url with HTTPS, it won't work on google proxy. Gonna fetch it directly.");
            return super.shouldInterceptRequest(view, request);
        }
        if (url.getHost().equals("google.com") || url.getHost().equals("www.google.com")) {
            Log.d("WebView", "Not trying to proxy google scripts");
            return super.shouldInterceptRequest(view, request);
        }
        // if (url.length() != 0) {
        try {
            String[] header = Utils.authHeader();
            Log.d("WebView", header[0] + " : " + header[1]);

            HttpHost proxy = new HttpHost("proxy.googlezip.net", 443, "https");

            HttpClient cli = getNewHttpClient();
            cli.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            HttpResponse response;
            Map<String, String> headers = request.getRequestHeaders();


            if (request.getMethod().equals("GET") && !Utils.is_login_form(url)) {
                HttpGet request1 = new HttpGet(url.toString());

                for (Map.Entry<String, String> entry : headers.entrySet())
                    request1.setHeader(entry.getKey(), entry.getValue());

                request1.setHeader(header[0], header[1]);
                request1.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                request1.setHeader("Accept-Encoding", "gzip");
                //request1.setHeader("Host", "195.82.146.214");
                request1.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
                request1.setHeader("Accept-Encoding", "gzip");
                request1.setHeader("Referer", "http://rutracker.org/forum/index.php");
                if (authCookie != null && (url.getHost().equals("rutracker.org") || url.getHost().equals("login.rutracker.org"))) {
                    request1.setHeader("Cookie", authCookie);
                    Log.d("WebView", "cookie sent:" + authCookie);
                }
                response = cli.execute(request1);

            } else {
                Log.d("WebView", "WebviewClient: it is a post\\login request!");

                HttpPost request1 = new HttpPost(url.toString());
                UrlEncodedFormEntity params = Utils.get2post(url);
                if (params != null)
                    request1.setEntity(params);

                for (Map.Entry<String, String> entry : headers.entrySet())
                    request1.setHeader(entry.getKey(), entry.getValue());
                request1.setHeader(header[0], header[1]);
                request1.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                request1.setHeader("Accept-Encoding", "gzip");
                request1.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");
                request1.setHeader("Accept-Encoding", "gzip");
                request1.setHeader("Referer", "http://rutracker.org/forum/index.php");
                if (authCookie != null && Utils.is_rutracker(url)) {
                    request1.setHeader("Cookie", authCookie);
                    Log.d("WebView", "cookie sent:" + authCookie);
                }
                response = cli.execute(request1);
            }

            if (Utils.is_login_form(url)) {

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
                if (Utils.is_rutracker(url))
                    encoding = "windows-1251";//for rutracker only, for mimes other then html
                if (mime.equals("text/html") && Utils.is_rutracker(url)) {
                    encoding = "windows-1251";//for rutracker only
                    String data = Utils.convertStreamToString(inputStr, encoding);
                    data = data.replace("method=\"post\"", "method=\"get\"");
                    /*data = data.replace("id=\"top-login-form\" method=\"post", "id=\"top-login-form\" method=\"get");
                    data = data.replace("<form id=\"login-form\" action=\"http://login.rutracker.org/forum/login.php\" method=\"post\">",
                            "<form id=\"login-form\" action=\"http://login.rutracker.org/forum/login.php\" method=\"get\">");*/
                    inputStr = new ByteArrayInputStream(data.getBytes(encoding));
                    Log.d("WebView", "data " + data);
                }
                return new WebResourceResponse(mime, encoding, inputStr);


            } else {
                Log.d("WebView", "Response code: " + responseCode);
                Log.d("WebView", "Response message: " + responseMessage);
                String msgText = "Failed to fetch data:<br>Message: " + responseMessage +
                        "<br>Code: " + responseCode + "<br>" +
                        "<a href=\"javascript:location.reload(true)\">Refresh this page</a>";
                ByteArrayInputStream msgStream = new ByteArrayInputStream(msgText.getBytes("UTF-8"));
                return new WebResourceResponse("text/html", "UTF-8", msgStream);


            }
        } catch (Exception e) {
            Log.d("WebView", "Error fetching URL " + url + ":");
            e.printStackTrace();

        }
        return null;
    }

}