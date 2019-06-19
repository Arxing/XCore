package org.arxing.xcore;

import com.annimon.stream.Stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Xposed {
    private static ClassLoader defaultClassLoader;

    /* helper methods */

    public static Class findClass(String cls) {
        return XposedHelpers.findClass(cls, defaultClassLoader);
    }

    public static void setDefaultClassLoader(ClassLoader classLoader) {
        defaultClassLoader = classLoader;
    }

    public static int getMethodDepth(String method) {
        return XposedHelpers.getMethodDepth(method);
    }

    public static Class[] classesAsArray(Class... classes) {
        return XposedHelpers.getClassesAsArray(classes);
    }

    public static Class[] argTypesAsArray(Object... args) {
        return XposedHelpers.getParameterTypes(args);
    }

    /* instance new */

    public static Object newInstance(Class cls, Object... args) {
        return XposedHelpers.newInstance(cls, args);
    }

    public static Object newInstance(Class cls, Class[] argTypes, Object... args) {
        return XposedHelpers.newInstance(cls, argTypes, args);
    }

    public static Object newInstance(String cls, Object... args) {
        return XposedHelpers.newInstance(findClass(cls), args);
    }

    public static Object newInstance(String cls, Class[] argTypes, Object... args) {
        return XposedHelpers.newInstance(findClass(cls), argTypes, args);
    }

    /* find method */

    public static Constructor findConstructor(Class cls, Class... paramTypes) {
        return XposedHelpers.findConstructorBestMatch(cls, paramTypes);
    }

    public static Constructor findConstructor(String cls, Class... paramTypes) {
        return findConstructor(findClass(cls), paramTypes);
    }

    public static Method findMethod(Class cls, String method, Class... paramTypes) {
        return XposedHelpers.findMethodBestMatch(cls, method, paramTypes);
    }

    public static Method findMethod(String cls, String method, Class... paramTypes) {
        return findMethod(findClass(cls), method, paramTypes);
    }

    /* method hooks */

    private static void hookMethod(Class cls,
                                   String method,
                                   MethodHooker before,
                                   MethodHooker after,
                                   MethodReplacer replacer,
                                   Object... paramTypes) {
        List<Object> tmp = new ArrayList<>();
        if (paramTypes.length > 0)
            tmp.addAll(Stream.of(paramTypes).toList());
        if (before != null)
            tmp.add(new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    before.hook(param);
                }
            });
        if (after != null)
            tmp.add(new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    after.hook(param);
                }
            });
        if (replacer != null)
            tmp.add(new XC_MethodReplacement() {
                @Override protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return replacer.replace(param);
                }
            });
        Object[] paramTypesAndCallback = tmp.toArray(new Object[0]);

        if (paramTypes.length > 0) {
            XposedHelpers.findAndHookMethod(cls, method, paramTypesAndCallback);
        } else {
            XposedBridge.hookAllMethods(cls, method, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (before != null)
                        before.hook(param);
                }

                @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (after != null)
                        after.hook(param);
                }
            });
        }
    }

    private static void hookMethod(String cls,
                                   ClassLoader classLoader,
                                   String method,
                                   MethodHooker before,
                                   MethodHooker after,
                                   MethodReplacer replacer,
                                   Object... paramTypes) {
        hookMethod(XposedHelpers.findClass(cls, classLoader), method, before, after, replacer, paramTypes);
    }

    private static void hookMethod(String cls,
                                   String method,
                                   MethodHooker before,
                                   MethodHooker after,
                                   MethodReplacer replacer,
                                   Object... paramTypes) {
        hookMethod(cls, defaultClassLoader, method, before, after, replacer, paramTypes);
    }

    private static void hookAllMethods(Class cls, String method, MethodHooker before, MethodHooker after) {
        hookMethod(cls, method, before, after, null);
    }

    public static void hookAllMethodsBefore(Class cls, String method, MethodHooker hooker) {
        hookAllMethods(cls, method, hooker, null);
    }

    public static void hookAllMethodsAfter(Class cls, String method, MethodHooker hooker) {
        hookAllMethods(cls, method, null, hooker);
    }

    private static void hookAllMethods(String cls, String method, MethodHooker before, MethodHooker after) {
        hookAllMethods(XposedHelpers.findClass(cls, defaultClassLoader), method, before, after);
    }

    public static void hookAllMethodsBefore(String cls, String method, MethodHooker hooker) {
        hookAllMethods(cls, method, hooker, null);
    }

    public static void hookAllMethodsAfter(String cls, String method, MethodHooker hooker) {
        hookAllMethods(cls, method, null, hooker);
    }

    public static void hookMethodBefore(Class cls, String method, MethodHooker hooker, Object... paramTypes) {
        hookMethod(cls, method, hooker, null, null, paramTypes);
    }

    public static void hookMethodBefore(String cls, String method, MethodHooker hooker, Object... paramTypes) {
        hookMethod(cls, method, hooker, null, null, paramTypes);
    }

    public static void hookMethodAfter(Class cls, String method, MethodHooker hooker, Object... paramTypes) {
        hookMethod(cls, method, null, hooker, null, paramTypes);
    }

    public static void hookMethodAfter(String cls, String method, MethodHooker hooker, Object... paramTypes) {
        hookMethod(cls, method, null, hooker, null, paramTypes);
    }

    public static void hookMethodReplace(Class cls, String method, MethodReplacer hooker, Object... paramTypes) {
        hookMethod(cls, method, null, null, hooker, paramTypes);
    }

    public static void hookMethodReplace(String cls, String method, MethodReplacer hooker, Object... paramTypes) {
        hookMethod(cls, method, null, null, hooker, paramTypes);
    }

    /* constructor */

    private static void hookConstructor(Class cls, MethodHooker before, MethodHooker after, MethodReplacer replacer, Object... paramTypes) {
        List<Object> tmp = new ArrayList<>();
        if (paramTypes.length > 0)
            tmp.addAll(Stream.of(paramTypes).toList());
        if (before != null)
            tmp.add(new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    before.hook(param);
                }
            });
        if (after != null)
            tmp.add(new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    after.hook(param);
                }
            });
        if (replacer != null)
            tmp.add(new XC_MethodReplacement() {
                @Override protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return replacer.replace(param);
                }
            });
        Object[] paramTypesAndCallback = tmp.toArray(new Object[0]);

        if (paramTypes.length > 0) {
            XposedHelpers.findAndHookConstructor(cls, paramTypesAndCallback);
        } else {
            XposedBridge.hookAllConstructors(cls, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (before != null)
                        before.hook(param);
                }

                @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (after != null)
                        after.hook(param);
                }
            });
        }
    }

    private static void hookConstructor(String cls,
                                        MethodHooker before,
                                        MethodHooker after,
                                        MethodReplacer replacer,
                                        Object... paramTypes) {
        hookConstructor(XposedHelpers.findClass(cls, defaultClassLoader), before, after, replacer, paramTypes);
    }

    public static void hookConstructorBefore(Class cls, MethodHooker hooker, Object... paramTypes) {
        hookConstructor(cls, hooker, null, null, paramTypes);
    }

    public static void hookConstructorBefore(String cls, MethodHooker hooker, Object... paramTypes) {
        hookConstructor(cls, hooker, null, null, paramTypes);
    }

    public static void hookConstructorAfter(Class cls, MethodHooker hooker, Object... paramTypes) {
        hookConstructor(cls, null, hooker, null, paramTypes);
    }

    public static void hookConstructorAfter(String cls, MethodHooker hooker, Object... paramTypes) {
        hookConstructor(cls, null, hooker, null, paramTypes);
    }

    public static void hookConstructorReplace(Class cls, MethodReplacer replacer, Object... paramTypes) {
        hookConstructor(cls, null, null, replacer, paramTypes);
    }

    public static void hookConstructorReplace(String cls, MethodReplacer replacer, Object... paramTypes) {
        hookConstructor(cls, null, null, replacer, paramTypes);
    }

    /* interface */

    public interface MethodHooker {
        void hook(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    public interface MethodReplacer {
        Object replace(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    /* call methods */

    public static Object callStaticMethod(String cls, String method, Object... args) {
        return XposedHelpers.callStaticMethod(findClass(cls), method, args);
    }

    public static Object callStaticMethod(String cls, String method, Class[] argTypes, Object... args) {
        return XposedHelpers.callStaticMethod(findClass(cls), method, argTypes, args);
    }

    public static Object callMethod(Object instance, String method, Object... args) {
        return XposedHelpers.callMethod(instance, method, args);
    }

    public static Object callMethod(Object instance, String method, Class[] argTypes, Object... args) {
        return XposedHelpers.callMethod(instance, method, argTypes, args);
    }

    /* get fields */

    /* + instance */

    public static String getStringField(Object instance, String name) {
        return (String) XposedHelpers.getObjectField(instance, name);
    }

    public static Object getObjectField(Object instance, String name) {
        return XposedHelpers.getObjectField(instance, name);
    }

    public static int getIntField(Object instance, String name) {
        return XposedHelpers.getIntField(instance, name);
    }

    public static long getLongField(Object instance, String name) {
        return XposedHelpers.getLongField(instance, name);
    }

    public static byte getByteField(Object instance, String name) {
        return XposedHelpers.getByteField(instance, name);
    }

    public static double getDoubleField(Object instance, String name) {
        return XposedHelpers.getDoubleField(instance, name);
    }

    public static float getFloatField(Object instance, String name) {
        return XposedHelpers.getFloatField(instance, name);
    }

    public static boolean getBooleanField(Object instance, String name) {
        return XposedHelpers.getBooleanField(instance, name);
    }

    /* + static */

    /* + + use class */

    public static String getStaticStringField(Class cls, String name) {
        return (String) XposedHelpers.getStaticObjectField(cls, name);
    }

    public static Object getStaticObjectField(Class cls, String name) {
        return XposedHelpers.getStaticObjectField(cls, name);
    }

    public static int getStaticIntField(Class cls, String name) {
        return XposedHelpers.getStaticIntField(cls, name);
    }

    public static long getStaticLongField(Class cls, String name) {
        return XposedHelpers.getStaticLongField(cls, name);
    }

    public static byte getStaticByteField(Class cls, String name) {
        return XposedHelpers.getStaticByteField(cls, name);
    }

    public static double getStaticDoubleField(Class cls, String name) {
        return XposedHelpers.getStaticDoubleField(cls, name);
    }

    public static float getStaticFloatField(Class cls, String name) {
        return XposedHelpers.getStaticFloatField(cls, name);
    }

    public static boolean getStaticBooleanField(Class cls, String name) {
        return XposedHelpers.getStaticBooleanField(cls, name);
    }

    /* + + use String class */

    public static String getStaticStringField(String cls, String name) {
        return (String) XposedHelpers.getStaticObjectField(findClass(cls), name);
    }

    public static Object getStaticObjectField(String cls, String name) {
        return XposedHelpers.getStaticObjectField(findClass(cls), name);
    }

    public static int getStaticIntField(String cls, String name) {
        return XposedHelpers.getStaticIntField(findClass(cls), name);
    }

    public static long getStaticLongField(String cls, String name) {
        return XposedHelpers.getStaticLongField(findClass(cls), name);
    }

    public static byte getStaticByteField(String cls, String name) {
        return XposedHelpers.getStaticByteField(findClass(cls), name);
    }

    public static double getStaticDoubleField(String cls, String name) {
        return XposedHelpers.getStaticDoubleField(findClass(cls), name);
    }

    public static float getStaticFloatField(String cls, String name) {
        return XposedHelpers.getStaticFloatField(findClass(cls), name);
    }

    public static boolean getStaticBooleanField(String cls, String name) {
        return XposedHelpers.getStaticBooleanField(findClass(cls), name);
    }

    /* set fields */

    /* + instance */

    public static void setStringField(Object instance, String name, String value) {
        XposedHelpers.setObjectField(instance, name, value);
    }

    public static void setObjectField(Object instance, String name, Object value) {
        XposedHelpers.setObjectField(instance, name, value);
    }

    public static void setIntField(Object instance, String name, int value) {
        XposedHelpers.setIntField(instance, name, value);
    }

    public static void setLongField(Object instance, String name, long value) {
        XposedHelpers.setLongField(instance, name, value);
    }

    public static void setByteField(Object instance, String name, byte value) {
        XposedHelpers.setByteField(instance, name, value);
    }

    public static void setDoubleField(Object instance, String name, double value) {
        XposedHelpers.setDoubleField(instance, name, value);
    }

    public static void setFloatField(Object instance, String name, float value) {
        XposedHelpers.setFloatField(instance, name, value);
    }

    public static void setBooleanField(Object instance, String name, boolean value) {
        XposedHelpers.setBooleanField(instance, name, value);
    }

    /* + static */

    /* + + use class */

    public static void setStaticStringField(Class cls, String name, String value) {
        XposedHelpers.setStaticObjectField(cls, name, value);
    }

    public static void setStaticObjectField(Class cls, String name, Object value) {
        XposedHelpers.setStaticObjectField(cls, name, value);
    }

    public static void setStaticIntField(Class cls, String name, int value) {
        XposedHelpers.setStaticIntField(cls, name, value);
    }

    public static void setStaticLongField(Class cls, String name, long value) {
        XposedHelpers.setStaticLongField(cls, name, value);
    }

    public static void setStaticByteField(Class cls, String name, byte value) {
        XposedHelpers.setStaticByteField(cls, name, value);
    }

    public static void setStaticDoubleField(Class cls, String name, double value) {
        XposedHelpers.setStaticDoubleField(cls, name, value);
    }

    public static void setStaticFloatField(Class cls, String name, float value) {
        XposedHelpers.setStaticFloatField(cls, name, value);
    }

    public static void setStaticBooleanField(Class cls, String name, boolean value) {
        XposedHelpers.setStaticBooleanField(cls, name, value);
    }

    /* + + use String class */

    public static void setStaticStringField(String cls, String name, String value) {
        XposedHelpers.setStaticObjectField(findClass(cls), name, value);
    }

    public static void setStaticObjectField(String cls, String name, Object value) {
        XposedHelpers.setStaticObjectField(findClass(cls), name, value);
    }

    public static void setStaticIntField(String cls, String name, int value) {
        XposedHelpers.setStaticIntField(findClass(cls), name, value);
    }

    public static void setStaticLongField(String cls, String name, long value) {
        XposedHelpers.setStaticLongField(findClass(cls), name, value);
    }

    public static void setStaticByteField(String cls, String name, byte value) {
        XposedHelpers.setStaticByteField(findClass(cls), name, value);
    }

    public static void setStaticDoubleField(String cls, String name, double value) {
        XposedHelpers.setStaticDoubleField(findClass(cls), name, value);
    }

    public static void setStaticFloatField(String cls, String name, float value) {
        XposedHelpers.setStaticFloatField(findClass(cls), name, value);
    }

    public static void setStaticBooleanField(String cls, String name, boolean value) {
        XposedHelpers.setStaticBooleanField(findClass(cls), name, value);
    }

    /* enum */

    public static Object getEnumConstant(Class cls, int index) {
        return cls.getEnumConstants()[index];
    }

    public static Object getEnumConstant(String cls, int index) {
        return getEnumConstant(findClass(cls), index);
    }

    public static Object getEnumConstant(Class cls, String name) {
        return Enum.valueOf(cls, name);
    }

    public static Object getEnumConstant(String cls, String name) {
        return getEnumConstant(findClass(cls), name);
    }
}
