package ru.jehy.proxy_rutracker;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RunWebView();
    }

    public void RunWebView() {
        WebView myWebView = (WebView) findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= 21)
            myWebView.setWebViewClient(new MyWebViewClient());
        else
            myWebView.setWebViewClient(new MyWebViewClientOld());

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);

        //String url = "http://rutracker.org";

        String url = "http://myip.ru/";

        //request_header_add Proxy-Authorization "SpdyProxy ps=\"1390372720-748089166-1671804897-22716992\", sid=\"95b3da26c6bfc85b64b4768b7e683000\""

        Log.d("RunWebView", "Opening: " + url);
        //myWebView.loadUrl(url,extraHeaders);
        myWebView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    WebView myWebView = (WebView) findViewById(R.id.webView);
                    if (myWebView.canGoBack()) {
                        myWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
}

