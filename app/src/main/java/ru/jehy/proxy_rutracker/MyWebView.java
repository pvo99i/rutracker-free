package ru.jehy.proxy_rutracker;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by Bond on 2016-03-12.
 */

public class MyWebView extends WebView {
    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public MyWebView(Context context) {
        super(context);
    }
    @Override
    public void loadUrl(String url) {
        System.out.println("+++++WebView loadUrl:" + url);
        super.loadUrl(url);

    }

    @Override
    public void postUrl(String url, byte[] postData) {
        System.out.println("+++++++WebView postUrl:" + url);
        super.postUrl(url, postData);
    }
};