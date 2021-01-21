package com.getui.getuiflut;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by denghaofa
 * on 2019-05-13 18:01
 */
public class MainHandler extends Handler {
    private static MainHandler mainHandler;

    private MainHandler(Looper looper) {
        super(looper);
    }

    public static MainHandler getMainHandler() {
        if (null == mainHandler) {
            synchronized (MainHandler.class) {
                if (null == mainHandler) {
                    mainHandler = new MainHandler(Looper.getMainLooper());
                }
            }
        }
        return mainHandler;
    }
}
