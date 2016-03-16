package ru.jehy.rutracker_free;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by Bond on 2016-03-12.
 */

public class MyWebView extends WebView {
    public MyWebView(Context context) {
        super(context);
    }
    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);

    }

    @Override
    public void postUrl(String url, byte[] postData) {
        // fail, never works
        super.postUrl(url, postData);
    }
};