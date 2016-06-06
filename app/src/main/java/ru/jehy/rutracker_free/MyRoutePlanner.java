package ru.jehy.rutracker_free;

import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.conn.routing.HttpRoute;
import cz.msebera.android.httpclient.conn.routing.HttpRoutePlanner;
import cz.msebera.android.httpclient.conn.routing.RouteInfo;
import cz.msebera.android.httpclient.protocol.HttpContext;

/**
 * Created by Bond on 2016-03-11.
 */
public class MyRoutePlanner implements HttpRoutePlanner {
    @Override
    public HttpRoute determineRoute(HttpHost target, HttpRequest request,
                                    HttpContext context) throws HttpException {
        return new HttpRoute(target, null
                , new HttpHost("proxy.googlezip.net", 443, "https")
                , true, RouteInfo.TunnelType.PLAIN, RouteInfo.LayerType.PLAIN); //Note: true
    }
}