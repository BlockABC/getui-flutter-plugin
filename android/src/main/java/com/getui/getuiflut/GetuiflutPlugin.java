package com.getui.getuiflut;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getui.getuiflut.util.AppUtil;
import com.getui.getuiflut.util.LogUtil;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.Tag;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * GetuiflutPlugin
 */
public class GetuiflutPlugin implements FlutterPlugin, MethodCallHandler, NotifyManager.NotifyHandler, ActivityAware {
    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "getuiflut");
        channel.setMethodCallHandler(new GetuiflutPlugin(registrar, channel));
    }

    private static final String TAG = "GetuiflutPlugin";
    private static final int FLUTTER_CALL_BACK_CID = 1;
    private static final int FLUTTER_CALL_BACK_MSG = 2;

    enum MessageType {
        Default,
        onReceiveMessageData,
        onNotificationMessageArrived,
        onNotificationMessageClicked
    }

    enum StateType {
        Default,
        onReceiveClientId,
        onReceiveOnlineState
    }

    private Context context;
    MethodChannel channel;

    public static GetuiflutPlugin instance;

    public final Map<Integer, Result> callbackMap;

    private static Handler flutterHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLUTTER_CALL_BACK_CID:
                    if (msg.arg1 == StateType.onReceiveClientId.ordinal()) {
                        GetuiflutPlugin.instance.channel.invokeMethod("onReceiveClientId", msg.obj);
                        Log.d("flutterHandler", "onReceiveClientId >>> " + msg.obj);

                    } else if (msg.arg1 == StateType.onReceiveOnlineState.ordinal()) {
                        GetuiflutPlugin.instance.channel.invokeMethod("onReceiveOnlineState", msg.obj);
                        Log.d("flutterHandler", "onReceiveOnlineState >>> " + msg.obj);
                    } else {
                        Log.d(TAG, "default state type...");
                    }
                    break;
                case FLUTTER_CALL_BACK_MSG:
                    if (msg.arg1 == MessageType.onReceiveMessageData.ordinal()) {
                        GetuiflutPlugin.instance.channel.invokeMethod("onReceiveMessageData", msg.obj);
                        Log.d("flutterHandler", "onReceiveMessageData >>> " + msg.obj);

                    } else if (msg.arg1 == MessageType.onNotificationMessageArrived.ordinal()) {
                        GetuiflutPlugin.instance.channel.invokeMethod("onNotificationMessageArrived", msg.obj);
                        Log.d("flutterHandler", "onNotificationMessageArrived >>> " + msg.obj);

                    } else if (msg.arg1 == MessageType.onNotificationMessageClicked.ordinal()) {
                        GetuiflutPlugin.instance.channel.invokeMethod("onNotificationMessageClicked", msg.obj);
                        Log.d("flutterHandler", "onNotificationMessageClicked >>> " + msg.obj);
                    } else {
                        Log.d(TAG, "default Message type...");
                    }
                    break;

                default:
                    break;
            }

        }
    };

    public GetuiflutPlugin() {
        this.callbackMap = new HashMap<>();
        instance = this;
        NotifyManager.getInstance().registerNotifyHandler(this);
    }

    public GetuiflutPlugin(Registrar registrar, MethodChannel channel) {
        this();
        this.channel = channel;
        this.context = registrar.context();
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (null == channel) {
            return;
        }
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("initGetuiPush")) {
            initGtSdk();
        } else if (call.method.equals("getClientId")) {
            result.success(getClientId());
        } else if (call.method.equals("resume")) {
            resume();
        } else if (call.method.equals("showNotification")) {
            showNotification(call, result);
        } else if (call.method.equals("stopPush")) {
            stopPush();
        } else if (call.method.equals("bindAlias")) {
            Log.d(TAG, "bindAlias:" + call.argument("alias").toString());
            bindAlias(call.argument("alias").toString(), "");
        } else if (call.method.equals("unbindAlias")) {
            Log.d(TAG, "unbindAlias:" + call.argument("alias").toString());
            unbindAlias(call.argument("alias").toString(), "");
        } else if (call.method.equals("setTag")) {
            Log.d(TAG, "tags:" + (ArrayList<String>) call.argument("tags"));
            setTag((ArrayList<String>) call.argument("tags"));
        } else if (call.method.equals("onActivityCreate")) {
            Log.d(TAG, "do onActivityCreate");
            onActivityCreate();
        } else {
            result.notImplemented();
        }
    }

    private void showNotification(MethodCall call, Result result) {
        if (null == call.arguments) {
            return;
        }
        if (!(call.arguments instanceof Map)) {
            return;
        }
        Map<String, Object> map = (Map<String, Object>) call.arguments;
        String title = "title";
        if (null != map.get("title")) {
            title = map.get("title").toString();
        }
        String des = "des";
        if (null != map.get("des")) {
            des = map.get("des").toString();
        }
        String data = "data";
        if (null != map.get("data")) {
            data = map.get("data").toString();
        }
        PushMessageHandler.getInstance(AppUtil.getApp()).showNotification(title, des, data);
    }

    private void initGtSdk() {
        Log.d(TAG, "init getui sdk...test");
        PushManager.getInstance().initialize(context, FlutterPushService.class);
        PushManager.getInstance().registerPushIntentService(context, FlutterIntentService.class);
    }

    private void onActivityCreate() {
        try {
            Method method = PushManager.class.getDeclaredMethod("registerPushActivity", Context.class, Class.class);
            method.setAccessible(true);
            method.invoke(PushManager.getInstance(), context, GetuiPluginActivity.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String getClientId() {
        Log.d(TAG, "get client id");
        return PushManager.getInstance().getClientid(context);
    }

    private void resume() {
        Log.d(TAG, "resume push service");
        PushManager.getInstance().turnOnPush(context);
    }

    private void stopPush() {
        Log.d(TAG, "stop push service");
        PushManager.getInstance().turnOffPush(context);
    }

    /**
     * 绑定别名功能:后台可以根据别名进行推送
     *
     * @param alias 别名字符串
     * @param aSn   绑定序列码, Android中无效，仅在iOS有效
     */
    public void bindAlias(String alias, String aSn) {
        PushManager.getInstance().bindAlias(context, alias);
    }

    /**
     * 取消绑定别名功能
     *
     * @param alias 别名字符串
     * @param aSn   绑定序列码, Android中无效，仅在iOS有效
     */
    public void unbindAlias(String alias, String aSn) {
        PushManager.getInstance().unBindAlias(context, alias, false);
    }

    /**
     * 给用户打标签 , 后台可以根据标签进行推送
     *
     * @param tags 别名数组
     */
    public void setTag(List<String> tags) {
        if (tags == null || tags.size() == 0) {
            return;
        }

        Tag[] tagArray = new Tag[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = new Tag();
            tag.setName(tags.get(i));
            tagArray[i] = tag;
        }

        PushManager.getInstance().setTag(context, tagArray, "setTag");
    }

    static void transmitMessageReceive(String message, String func) {
        if (instance == null) {
            Log.d(TAG, "Getui flutter plugin doesn't exist");
            return;
        }
        int type;
        if (func.equals("onReceiveClientId")) {
            type = StateType.onReceiveClientId.ordinal();
        } else if (func.equals("onReceiveOnlineState")) {
            type = StateType.onReceiveOnlineState.ordinal();
        } else {
            type = StateType.Default.ordinal();
        }
        Message msg = Message.obtain();
        msg.what = FLUTTER_CALL_BACK_CID;
        msg.arg1 = type;
        msg.obj = message;
        flutterHandler.sendMessage(msg);
    }

    static void transmitMessageReceive(Map<String, Object> message, String func) {
        if (instance == null) {
            Log.d(TAG, "Getui flutter plugin doesn't exist");
            return;
        }
        int type;
        if (func.equals("onReceiveMessageData")) {
            type = MessageType.onReceiveMessageData.ordinal();
        } else if (func.equals("onNotificationMessageArrived")) {
            type = MessageType.onNotificationMessageArrived.ordinal();
        } else if (func.equals("onNotificationMessageClicked")) {
            type = MessageType.onNotificationMessageClicked.ordinal();
        } else {
            type = MessageType.Default.ordinal();
        }
        Message msg = Message.obtain();
        msg.what = FLUTTER_CALL_BACK_MSG;
        msg.arg1 = type;
        msg.obj = message;
        flutterHandler.sendMessage(msg);
    }

    @Override
    public boolean handleNotify(String json) {
        LogUtil.e("handleNotify", json);
        channel.invokeMethod("onNotificationClick", json);
        return false;
    }

    @Override
    public boolean handleMessage(String json) {
        return false;
    }

    @Override
    public boolean handleClientId(String clientId) {
        return false;
    }

    private boolean isResume;
    private final List<Activity> activityList = new ArrayList<>();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        context = binding.getApplicationContext();
        channel = new MethodChannel(binding.getBinaryMessenger(), "getuiflut");
        channel.setMethodCallHandler(this);
        PushMessageHandler.getInstance(AppUtil.getApp()).setPushHandler(new PushMessageHandler.PushHandler() {
            @Override
            public boolean canHandle() {
                return isResume;
            }

            @Override
            public Activity getCurrentActivity() {
                return currentActivity();
            }
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        NotifyManager.getInstance().unRegisterNotifyHandler(this);
        channel.setMethodCallHandler(null);
        channel = null;
        this.binding = null;
        removeCallback();
    }

    ActivityPluginBinding binding;

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.binding = binding;
        if (null == getActivity()) {
            MainHandler.getMainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addCallBack();
                }
            }, 100);
            return;
        }
        addCallBack();
    }

    Application.ActivityLifecycleCallbacks callbacks;
    Application application;

    private void addCallBack() {
        callbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                activityList.add(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                isResume = true;
                LogUtil.e("onActivityResumed", isResume);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                isResume = false;
                LogUtil.e("onActivityPaused", isResume);
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                activityList.remove(activity);
            }
        };
        application = getActivity().getApplication();
        application.registerActivityLifecycleCallbacks(callbacks);
    }

    void removeCallback() {
        if (null == application || null == callbacks) {
            return;
        }
        application.unregisterActivityLifecycleCallbacks(callbacks);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }

    Activity getActivity() {
        if (null == this.binding) {
            return null;
        }
        return this.binding.getActivity();
    }

    Activity currentActivity() {
        if (0 == activityList.size()) {
            return getActivity();
        }
        return activityList.get(activityList.size() - 1);
    }
}
