package ru.jehy.rutracker_free;

import android.content.Context;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;


class MyWebViewClientOld extends WebViewClient {
    ProxyProcessor proxy = null;

    public MyWebViewClientOld(Context c) {
        proxy = new ProxyProcessor(c);
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }


    public WebResourceResponse shouldInterceptRequest(WebView view, String urlString) {
        WebResourceResponse w = proxy.shouldInterceptRequest(view, urlString);
        if (w == null)
            return super.shouldInterceptRequest(view, urlString);
        else
            return w;
    }

}