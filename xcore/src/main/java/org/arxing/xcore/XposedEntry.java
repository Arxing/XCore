package org.arxing.xcore;

import android.content.pm.ApplicationInfo;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class XposedEntry implements IXposedHookLoadPackage {
    public ClassLoader loader;
    public ApplicationInfo appInfo;
    public String processName;
    public String pkg;
    private List<XposedModule> modules = new ArrayList<>();

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
//        XLogger.log("handleLoadPackage, pkg=%s, process=%s, first=%b", param.packageName, param.processName, param.isFirstApplication);
        param.isFirstApplication = false;
        this.loader = param.classLoader;
        this.appInfo = param.appInfo;
        this.processName = param.processName;
        this.pkg = param.packageName;
        if (modules.isEmpty()) {
            if (moduleDefines() != null) {
                for (XposedModule module : moduleDefines()) {
                    module.init(loader, appInfo, new XSharedPreferences(XposedApplication.PACKAGE_NAME));
                    modules.add(module);
                    if (pkg.equals(module.pkg()) && (module.process() == null || module.process().equals(processName))) {
                        module.handleLoadPackage();
                    }
                }
            }
        }
    }

    public abstract XposedModule[] moduleDefines();
}
