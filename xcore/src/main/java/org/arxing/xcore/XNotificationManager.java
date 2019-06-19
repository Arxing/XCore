package org.arxing.xcore;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.robv.android.xposed.XposedHelpers;

public class XNotificationManager {
    private Context context;
    private Object notificationManager;
    private String defaultChannelId;
    private int defaultIconId;

    public XNotificationManager(Context context) {
        this.context = context;
        this.defaultIconId = getIconId();
        notificationManager = XposedHelpers.callMethod(context, "getSystemService", Context.NOTIFICATION_SERVICE);
    }

    public void setDefaultChannelId(String defaultChannelId) {
        this.defaultChannelId = defaultChannelId;
    }

    public void showNotification(String title,
                                 String content) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor builderConstructor = Xposed.findConstructor("android.app.Notification.Builder", Context.class, String.class);
        Object builder = builderConstructor.newInstance(context, defaultChannelId);
        Xposed.callMethod(builder, "setContentTitle", title);
        Xposed.callMethod(builder, "setContentText", content);
        Xposed.callMethod(builder, "setSmallIcon", defaultIconId);
        Object notification = Xposed.callMethod(builder, "build");
        Xposed.callMethod(notificationManager, "notify", (title + content).hashCode(), notification);
    }

    private int getIconId() {
        Object resource = Xposed.callMethod(context, "getResources");
        return (int) Xposed.callMethod(resource, "getIdentifier", "appicon", "drawable", context.getPackageName());
    }
}
