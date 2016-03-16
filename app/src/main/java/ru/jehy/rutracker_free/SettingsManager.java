package ru.jehy.rutracker_free;

/**
 * Created by Bond on 2016-03-14.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Bond on 01-Dec-15.
 */
public class SettingsManager {
    static String get(Context mContext, String key) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        String data = settings.getString(key, null);
        if (data == null)
            Log.d("SettingsManager", "No settings " + key + " is stored! ");
        else
            Log.d("SettingsManager", "Got settings " + key + " equal to " + data);
        return data;
    }

    @SuppressLint("CommitPrefEdits")
    static void put(Context mContext, String key, String value) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        Log.d("SettingsManager", "Saved setting " + key + " equal to " + value);
        editor.commit();
    }
}

