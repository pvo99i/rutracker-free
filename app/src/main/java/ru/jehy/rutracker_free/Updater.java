package ru.jehy.rutracker_free;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by jehy on 2016-03-16.
 */
class Updater extends AsyncTask<MainActivity, Void, Void> {

    private Exception exception;

    protected Void doInBackground(MainActivity... activity) {
        checkUpdates(activity[0]);
        return null;
    }

    Integer getLastAppVersion() {
        try {
            // Create a URL for the desired page
            URL url = new URL("https://raw.githubusercontent.com/jehy/rutracker-free/master/app/build.gradle");
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                int f = str.indexOf("releaseVersionCode");
                if (f != -1) {
                    str = str.substring(f + ("releaseVersionCode").length()).trim();
                    Log.d("Rutracker free", "Last release version: " + str);
                    return Integer.parseInt(str);
                }
            }
            in.close();
            Log.d("Rutracker free", "Failed to get last release version!");
        } catch (Exception e) {
            Log.d("Rutracker free", "Failed to get last release version:");
            e.printStackTrace();
        }
        return null;
    }

    void checkUpdates(final MainActivity activity) {
        final Integer lastAppVersion = getLastAppVersion();
        if (lastAppVersion == null)
            return;
        if (lastAppVersion <= BuildConfig.VERSION_CODE) {
            Log.d("Rutracker free", "App version is okay, skipping update");
            return;
        }
        String li = SettingsManager.get(activity, "LastIgnoredUpdateVersion");
        if (li != null) {
            Integer liInt = Integer.parseInt(li);
            if (liInt >= lastAppVersion)
                return;
        }

        activity.Update(lastAppVersion);
    }

}