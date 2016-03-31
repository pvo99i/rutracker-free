package ru.jehy.rutracker_free;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;


class MyWebViewClient extends WebViewClient {

    ProxyProcessor proxy = null;

    public MyWebViewClient(Context c) {
        proxy = new ProxyProcessor(c);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        WebResourceResponse w = proxy.shouldInterceptRequest(view, request);
        if (w == null)
            return super.shouldInterceptRequest(view, request);
        else
            return w;
    }

}