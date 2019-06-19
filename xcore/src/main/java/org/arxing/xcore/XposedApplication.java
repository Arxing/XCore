package org.arxing.xcore;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class XposedApplication extends Application {
    static String PACKAGE_NAME;
    static SharedPreferences DEFAULT_PREF;

    @Override public void onCreate() {
        super.onCreate();
        PACKAGE_NAME = getPackageName();
        DEFAULT_PREF = getSharedPreferences(PACKAGE_NAME + "_preferences", Context.MODE_PRIVATE);
    }
}
