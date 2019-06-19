package org.arxing.xcore;

import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.arxing.utils.PrinterUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class XLogger {
    private final static String TAG = "Xposed";

    public static void log(String format, Object... objs) {
        Log.i(TAG, String.format(format, objs));
    }

    public static void log(Throwable t) {
        XposedBridge.log(t);
    }

    public static void trace() {
        trace("");
    }

    public static void logMethod(XC_MethodHook.MethodHookParam param) {
        if (param != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            String cls = param.method.getDeclaringClass().getName();
            String method = param.method.getName();
            sb.append("class= ").append(cls).append(", ").append("method= ").append(method).append("\n");
            sb.append("params:").append("\n").append(Stream.of(param.args).indexed().map(pair -> {
                int index = pair.getFirst();
                Object paramVal = pair.getSecond();
                String paramCls = paramVal == null ? "null" : paramVal.getClass().getName();
                return String.format("===> [%d] %s=%s", index, paramCls, PrinterUtils.parseParamVal(paramVal));
            }).collect(Collectors.joining("\n"))).append("\n");
            sb.append("thisObject= ")
              .append(PrinterUtils.parseParamVal(param.thisObject))
              .append("\n")
              .append("result= ")
              .append(PrinterUtils.parseParamVal(param.getResult()));
            log("%s", sb.toString());
        }
    }

    private static void trace(String title) {
        trace(title, null);
    }

    public static void trace(String title, XC_MethodHook.MethodHookParam param) {
        trace(title, param, true);
    }

    public static void trace(String title, XC_MethodHook.MethodHookParam param, boolean stackTracing) {
        trace(title, param, stackTracing, true);
    }

    public static void trace(String title, XC_MethodHook.MethodHookParam param, boolean stackTracing, boolean methodTracing) {
        highlight("%s", title);
        if (methodTracing)
            logMethod(param);
        if (stackTracing) {
            String trace = Stream.of(new Throwable().getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
            log("\n%s", trace);
        }
    }

    public static void highlight(String format, Object... objects) {
        log("=================================================================================================================> %s",
            String.format(format, objects));
    }
}
