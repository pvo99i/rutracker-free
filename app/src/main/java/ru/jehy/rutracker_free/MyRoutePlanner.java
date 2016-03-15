package ru.jehy.rutracker_free;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.protocol.HttpContext;

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