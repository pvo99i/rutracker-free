package ru.jehy.proxy_rutracker;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bond on 01-Dec-15.
 */
class MyWebViewClientOld extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
    @Override

    public WebResourceResponse shouldInterceptRequest (WebView view, String url) {
        // String jsonResponse = null;
        if (url.length() != 0) {
            try {
                /************** For getting response from HTTP URL start ***************/
                URL object = new URL(url);

                HttpURLConnection connection = (HttpURLConnection) object
                        .openConnection();
                // int timeOut = connection.getReadTimeout();
                connection.setReadTimeout(60 * 1000);
                connection.setConnectTimeout(60 * 1000);
                String authorization = "xyz:xyz$123";
                String encodedAuth = "";//"Basic "+ Base64.encode(authorization.getBytes());
                //connection.setRequestProperty("Authorization", encodedAuth);
                int responseCode = connection.getResponseCode();
                //String responseMsg = connection.getResponseMessage();

                if (responseCode == 200) {
                    InputStream inputStr = connection.getInputStream();
                    String encoding = connection.getContentEncoding() == null ? "UTF-8"
                            : connection.getContentEncoding();
                    return new WebResourceResponse(connection.getContentType(), encoding, inputStr);
                    //jsonResponse = IOUtils.toString(inputStr, encoding);
                    /************** For getting response from HTTP URL end ***************/

                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        return null;
    }

}