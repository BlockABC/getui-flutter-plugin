package com.getui.getuiflut;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.getui.getuiflut.util.JsonUtil;
import com.getui.getuiflut.util.LogUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by denghaofa
 * on 2019-06-13 10:53
 */
public class PushMessageHandler {
    private Application application;
    private int notificationId = 0;
    private final String CHANNEL_TRANSACTION = "transaction";
    private final Map<String, String> handledMsgMap = new HashMap<>();
    private PushHandler pushHandler;

    private boolean onResume = true;

    private static volatile PushMessageHandler instance;

    public static PushMessageHandler getInstance(Application application) {
        if (null == instance) {
            synchronized (PushMessageHandler.class) {
                if (null == instance) {
                    instance = new PushMessageHandler(application);
                }
            }
        }
        return instance;
    }

    private PushMessageHandler(Application application) {
        this.application = application;
        //创建通知通道
        //这部分代码可以写在任何位置，只需要保证在通知弹出之前调用就可以了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
             * 渠道ID可以随便定义，只要保证全局唯一性就可以。
             * 渠道名称是给用户看的，需要能够表达清楚这个渠道的用途。
             * 重要等级的不同则会决定通知的不同行为，当然这里只是初始状态下的重要等级，用户可以随时手动更改某个渠道的重要等级，App是无法干预的。
             * */
            String channelId = CHANNEL_TRANSACTION;
            String channelName = "交易提醒";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "other";
            channelName = "其他消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }
    }

    public void handleMessage(String json) {
        if (null == application || null == json) {
            return;
        }
        sendNotification(json);
    }

    public void showNotification(String title, String des, String data) {
        //创建点击通知时发送的广播
        final Intent intent = new Intent(application, NotifyService.class);
        intent.putExtra(NotifyService.NOTIFY_DATA, data);
        PendingIntent pi = PendingIntent.getService(application, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification notification = new NotificationCompat.Builder(application, CHANNEL_TRANSACTION)  //注意了这里需要一个channelId
                .setContentTitle(title)
                .setContentText(des)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(application.getResources(), R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setShowWhen(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        MainHandler.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                notificationId++;
                if (notificationId > 6666) {
                    notificationId = 0;
                }
                intent.setData(Uri.parse("push://push/" + notificationId));
                final NotificationManager manager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(notificationId, notification);
            }
        });
    }

    private void sendNotification(String json) {
        Object jsonObject = JsonUtil.parseJson(json);
        if (jsonObject instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) jsonObject;
            final NotificationManager manager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
            String title = map.get("title").toString();
            String text = title;
            if (null != map.get("body")) {
                text = map.get("body").toString();
            }
            //创建点击通知时发送的广播
            final Intent intent = new Intent(application, NotifyService.class);
            intent.putExtra(NotifyService.NOTIFY_DATA, json);
            PendingIntent pi = PendingIntent.getService(application, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            final Notification notification = new NotificationCompat.Builder(application, CHANNEL_TRANSACTION)  //注意了这里需要一个channelId
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(application.getResources(), R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setShowWhen(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();
            MainHandler.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    notificationId++;
                    if (notificationId > 6666) {
                        notificationId = 0;
                    }
                    intent.setData(Uri.parse("push://push/" + notificationId));
                    manager.notify(notificationId, notification);
                }
            });
        }

    }

    /**
     * 创建通知通道
     *
     * @param channelId
     * @param channelName
     * @param importance
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        //设置震动响铃等，高版本仅在此处设置有效，notification中设置无效
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400});//设置震动：静止0ms，震动1000ms，静止1000毫秒，震动1000毫秒，需要声明权限
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);//设置是否在锁屏中显示
        channel.setSound((Uri.fromFile(new File("/system/media/audio/ringtones/Luna.ogg"))), null);

        /*
        for fragment:  (NotificationManager) getActivity().getSystemService
        for activity:  (NotificationManager) getSystemService
         */
        NotificationManager notificationManager = (NotificationManager) application.getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 判断msgId对应的推送是否已经处理过了
     *
     * @param msgId
     * @return
     */
    private boolean isMsgHandled(String msgId) {
        LogUtil.e("isMsgHandled", msgId);
        //缓存中存在，处理过
        synchronized (handledMsgMap) {
            if (handledMsgMap.containsKey(msgId)) {
                return true;
            }
            handledMsgMap.put(msgId, "1");
        }
        return false;
    }

    public void setPushHandler(PushHandler pushHandler) {
        this.pushHandler = pushHandler;
    }

    public interface PushHandler {
        boolean canHandle();

        Activity getCurrentActivity();
    }

    public Activity getCurrentActivity() {
        if (null != pushHandler) {
            return pushHandler.getCurrentActivity();
        }
        return null;
    }
}
