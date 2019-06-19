package org.arxing.xcore;

import android.content.Context;
import android.content.SharedPreferences;

public class XSharedPreferencesUtils {

    public static SharedPreferences getAppSharedPref() {
        return XposedApplication.DEFAULT_PREF;
    }

    public static SharedPreferences getAppSharedPref(Context context) {
        String pkg = context.getPackageName();
        String prefName = pkg + "_preferences";
        return context.getSharedPreferences(prefName, Context.MODE_MULTI_PROCESS);
    }
}
