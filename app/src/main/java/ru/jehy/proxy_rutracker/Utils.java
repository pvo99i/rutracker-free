package ru.jehy.proxy_rutracker;

import android.util.Log;
import android.webkit.WebResourceResponse;

import org.apache.commons.lang3.StringUtils;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Bond on 2016-03-14.
 */
public class Utils {

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean is_adv(Uri url) {
        String[] adv_hosts = {"marketgid.com", "adriver.ru", "thisclick.network", "hghit.com",
                "onedmp.com", "acint.net", "yadro.ru", "tovarro.com", "marketgid.com", "rtb.com", "adx1.com",
                "directadvert.ru", "rambler.ru"};

        String[] adv_paths = {"brand", "iframe"};

        String host = url.getHost();
        for (String item : adv_hosts) {
            if (StringUtils.containsIgnoreCase(host, item)) {
                return true;
            }
        }
        if (StringUtils.containsIgnoreCase(url.getHost(), "rutracker.org")) {
            String path = url.getPath();
            for (String item : adv_paths) {
                {
                    if (StringUtils.containsIgnoreCase(path, item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String[] authHeader() {
        String[] result = new String[2];
        result[0] = "Chrome-Proxy";
        String authValue = "ac4500dd3b7579186c1b0620614fdb1f7d61f944";
        String timestamp = Long.toString(System.currentTimeMillis()).substring(0, 10);
        String[] chromeVersion = {"49", "0", "2623", "87"};

        String sid = (timestamp + authValue + timestamp);

        sid = Utils.md5(sid);
        result[1] = "ps=" + timestamp + "-" + Integer.toString((int) (Math.random() * 1000000000)) + "-" + Integer.toString((int) (Math.random() * 1000000000)) + "-" + Integer.toString((int) (Math.random() * 1000000000)) + ", sid=" + sid + ", b=" + chromeVersion[2] + ", p=" + chromeVersion[3] + ", c=win";
        return result;
    }


    static String convertStreamToString(java.io.InputStream is, String encoding) {
        //java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        //return s.hasNext() ? s.next() : "";
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(is, encoding));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append("\n" + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }
}
