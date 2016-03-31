package ru.jehy.rutracker_free;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by jehy on 2016-03-31.
 */
public class ProxyProcessor {

    private String authCookie = null;
    private Context MainContext;

    public ProxyProcessor(Context c) {
        MainContext = c;
        authCookie = CookieManager.get(MainContext);
    }


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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Uri url = request.getUrl();
        return this.process(url, request.getMethod(), view, request.getRequestHeaders());
    }

    public WebResourceResponse shouldInterceptRequest(WebView view, String urlString) {
        Uri url = Uri.parse(urlString);
        return this.process(url, "GET", view, null);
    }

    public WebResourceResponse process(Uri url, String method, WebView view, Map<String, String> headers) {

        Log.d("WebView", "Request for url: " + url + " intercepted");

        if (url.getHost() == null) {
            Log.d("WebView", "No url or host provided, better let webview deal with it");
            return null;
        }

        if (Utils.is_adv(url)) {
            Log.d("WebView", "Not fetching advertisment");
            return new WebResourceResponse("text/javascript", "UTF-8", null);
        }

        if (url.getScheme().equals("https")) {
            Log.d("WebView", "Not proxying url with HTTPS, it won't work on google proxy. Gonna fetch it directly.");
            return null;
        }
        if (url.getHost().equals("google.com") || url.getHost().equals("www.google.com")) {
            Log.d("WebView", "Not trying to proxy google scripts");
            return null;
        }
        if (url.getPath().equals("/custom.css")) {
            Log.d("WebView", "Adding custom css file...");

            // please try to test this
            //return new WebResourceResponse("text/css", "UTF-8", null);

            try {
                return new WebResourceResponse("text/css", "UTF-8", (MainContext).getAssets().open("rutracker.css"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            String[] header = Utils.authHeader();
            Log.d("WebView", header[0] + " : " + header[1]);

            HttpHost proxy = new HttpHost("proxy.googlezip.net", 443, "https");

            HttpClient cli = getNewHttpClient();
            cli.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            HttpResponse response;


            if (!url.toString().contains("convert_post=1") && !method.equals("post")) {
                HttpGet request1 = new HttpGet(url.toString());

                for (Map.Entry<String, String> entry : headers.entrySet())
                    request1.setHeader(entry.getKey(), entry.getValue());

                request1.setHeader(header[0], header[1]);
                request1.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                request1.setHeader("Accept-Encoding", "gzip, deflate, sdch");
                request1.setHeader("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
                if (authCookie != null && Utils.is_rutracker(url)) {
                    request1.setHeader("Cookie", authCookie);
                    Log.d("WebView", "cookie sent:" + authCookie);
                }
                request1.setHeader("Referer", "http://rutracker.org/forum/index.php");
                request1.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");

                try {
                    response = cli.execute(request1);
                } catch (Exception e) {
                    String msgText = "Что-то пошло не так:<br>Сообщение: " + e.getMessage() +
                            "<br>Код: " +
                            "Вы можете <a href=\"javascript:location.reload(true)\">Обновить страницу</a>" +
                            "или <a href=\"http://rutracker.org/forum/index.php\">вернуться на главную</a>";
                    ByteArrayInputStream msgStream = new ByteArrayInputStream(msgText.getBytes("UTF-8"));
                    return new WebResourceResponse("text/html", "UTF-8", msgStream);
                }

            } else {
                Log.d("WebView", "WebviewClient: it is a post request!");
                String urlWithStrippedGet = url.toString();
                int queryPart = urlWithStrippedGet.indexOf("?");
                if (queryPart != -1)
                    urlWithStrippedGet = urlWithStrippedGet.substring(0, queryPart);

                HttpPost request1 = new HttpPost(urlWithStrippedGet);
                UrlEncodedFormEntity params = Utils.get2post(url);
                if (params != null)
                    request1.setEntity(params);
                if (headers != null)
                    for (Map.Entry<String, String> entry : headers.entrySet())
                        request1.setHeader(entry.getKey(), entry.getValue());
                request1.setHeader(header[0], header[1]);
                request1.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                request1.setHeader("Accept-Encoding", "gzip, deflate, sdch");
                request1.setHeader("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
                if (authCookie != null && Utils.is_rutracker(url)) {
                    request1.setHeader("Cookie", authCookie);
                    Log.d("WebView", "cookie sent:" + authCookie);
                }
                request1.setHeader("Referer", "http://rutracker.org/forum/index.php");
                request1.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");

                response = cli.execute(request1);
            }

            if (Utils.is_login_form(url)) {

                Header[] all = response.getAllHeaders();
                for (Header header1 : all) {
                    Log.d("WebView", "LOGIN HEADER: " + header1.getName() + " : " + header1.getValue());

                }
                Header[] cookies = response.getHeaders("set-cookie");
                if (cookies.length > 0) {
                    String val = cookies[0].getValue();
                    val = val.substring(0, val.indexOf(";"));
                    authCookie = val.trim();
                    CookieManager.put(MainContext, authCookie);
                    Log.d("WebView", "=== Auth cookie: ==='" + val + "'");
                    Log.d("WebView", "redirecting to main page...");
                    //redirect does not work o_O
                    String msgText = "<script>window.location = \"http://rutracker.org/forum/index.php\"</script>";
                    ByteArrayInputStream msgStream = new ByteArrayInputStream(msgText.getBytes("UTF-8"));
                    return new WebResourceResponse("text/html", "UTF-8", msgStream);
                } else
                    Log.d("WebView", "No cookie received!!!");
            }
            int responseCode = response.getStatusLine().getStatusCode();
            String responseMessage = response.getStatusLine().getReasonPhrase();
            /*if (responseCode == 302) {
                Log.d("WebView", "It is redirect!");
                Header[] locHeaders = response.getHeaders("location");
                String newLocation = locHeaders[0].getValue();
                Log.d("WebView", "going to " + newLocation);
                return this.shouldInterceptRequest(view, newLocation);
            }*/

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
                if (Utils.is_rutracker(url) || url.toString().contains("static.t-ru.org"))
                    encoding = "windows-1251";//for rutracker only, for mimes other then html

                Log.d("WebView", "encoding final: " + encoding);

                if (mime.equals("text/html") && Utils.is_rutracker(url)) {
                    encoding = "windows-1251";//for rutracker only
                    String data = Utils.convertStreamToString(inputStr, encoding);
                    //data = data.replace("method=\"post\"", "method=\"get\"");
                    String replace = "<form(.*?)method=\"post\"(.*?)>";
                    String replacement = "<form$1method=\"get\"$2><input type=\"hidden\" name=\"convert_post\" value=1>";
                    data = data.replaceAll(replace, replacement);
                    data = data.replace("</head>", "<link rel=\"stylesheet\" href=\"/custom.css\" type=\"text/css\"></head>");
                    inputStr = new ByteArrayInputStream(data.getBytes(encoding));
                    Log.d("WebView", "data " + data);
                    String shareUrl = url.toString();
                    int pos = shareUrl.indexOf("&login_username");
                    if (pos != -1)
                        shareUrl = shareUrl.substring(0, pos);
                    String shareMsg = "Посмотри, что я нашёл на рутрекере при помощи приложения rutracker free: \n" + shareUrl;
                    int start = data.indexOf("href=\"magnet:");
                    if (start != -1) {
                        start += 13;
                        int end = data.indexOf("\"", start);
                        String link = data.substring(start, end);
                        shareMsg += "\n\nMagnet ссылка на скачивание:\nmagnet:" + link;
                    }
                    Intent mShareIntent = new Intent();
                    mShareIntent.setAction(Intent.ACTION_SEND);
                    mShareIntent.setType("text/plain");
                    mShareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
                    ((MainActivity) MainContext).setShareIntent(mShareIntent);

                }
                return new WebResourceResponse(mime, encoding, inputStr);


            } else {
                Log.d("WebView", "Response code: " + responseCode);
                Log.d("WebView", "Response message: " + responseMessage);
                String msgText = "Что-то пошло не так:<br>Сообщение: " + responseMessage +
                        "<br>Код: " + responseCode + "<br>" +
                        "Вы можете <a href=\"javascript:location.reload(true)\">Обновить страницу</a>" +
                        "или <a href=\"http://rutracker.org/forum/index.php\">вернуться на главную</a>";
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
