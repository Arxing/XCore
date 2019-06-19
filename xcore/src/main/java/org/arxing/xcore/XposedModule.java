package org.arxing.xcore;

import android.content.pm.ApplicationInfo;

import de.robv.android.xposed.XSharedPreferences;

public abstract class XposedModule {
    protected ClassLoader loader;
    protected ApplicationInfo appInfo;
    protected XSharedPreferences pref;
    private boolean initialized;

    public void init(ClassLoader loader, ApplicationInfo appInfo, XSharedPreferences pref) {
        this.loader = loader;
        this.appInfo = appInfo;
        this.pref = pref;
    }

    public abstract String pkg();

    public String process() {
        return null;
    }

    public abstract void handleLoadPackage();

    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    protected boolean isInitialized() {
        return initialized;
    }
}
