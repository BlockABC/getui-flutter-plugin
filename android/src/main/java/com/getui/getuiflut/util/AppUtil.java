package com.getui.getuiflut.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by denghaofa
 * on 2019-05-28 20:07
 */
public class AppUtil {
    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;

    /**
     * Init utils.
     * <p>Init it in the class of Application.</p>
     *
     * @param app application
     */
    public static void init(final Application app) {
        if (sApplication == null) {
            if (app == null) {
                sApplication = getApplicationByReflect();
            } else {
                sApplication = app;
            }
        } else {
            if (app != null && app.getClass() != sApplication.getClass()) {
                sApplication = app;
            }
        }
    }

    /**
     * Return the context of Application object.
     *
     * @return the context of Application object
     */
    public static Application getApp() {
        if (sApplication != null) return sApplication;
        Application app = getApplicationByReflect();
        init(app);
        return app;
    }

    private static Application getApplicationByReflect() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            return (Application) app;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }

    public static String appVerName() {
        Application application = getApp();
        PackageManager manager = application.getPackageManager();
        String name = "";
        try {
            PackageInfo info = manager.getPackageInfo(application.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static boolean isApkDebuggable() {
        try {
            ApplicationInfo info = getApp().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getPckName() {
        try {
            ApplicationInfo info = getApp().getApplicationInfo();
            return info.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            return "com.blockabc.cctip";
        }
    }

    public static String getMeta(String name) {
        try {
            ApplicationInfo info = getApp().getPackageManager().getApplicationInfo(getPckName(), PackageManager.GET_META_DATA);
            Bundle metaData = info.metaData;
            return metaData.getString(name, "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
