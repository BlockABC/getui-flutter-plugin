package com.getui.getuiflut;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.getui.getuiflut.util.AppUtil;
import com.getui.getuiflut.util.StringUtil;


public class NotifyService extends Service {
    public static final String NOTIFY_DATA = "NOTIFY_DATA";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String data = intent.getStringExtra(NOTIFY_DATA);
            handleIntentData(data);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntentData(String data) {
        if (StringUtil.isEmpty(data)) {
            return;
        }
        Activity activity = PushMessageHandler.getInstance(AppUtil.getApp()).getCurrentActivity();
        if (null != activity) {
            Intent intent = new Intent(activity, SchemeActivity.class);
            activity.startActivity(intent);
        } else {
            newTask();
        }
        try {
            NotifyManager.getInstance().notify(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newTask() {
        try {
            Intent intent = new Intent();
            intent.setPackage(AppUtil.getPckName());
            intent.setAction("CCTip_NOTIFICATION_CLICK");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
