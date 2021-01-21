package com.getui.getuiflut;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by denghaofa
 * on 2019-06-13 12:17
 */
public class NotifyManager {
    private static NotifyManager instance;
    private final List<NotifyHandler> notifyHandlerList = new ArrayList<>();

    public static NotifyManager getInstance() {
        if (null == instance) {
            instance = new NotifyManager();
        }
        return instance;
    }

    private NotifyManager() {

    }

    //通知监听者
    public void notify(final String json) {
        MainHandler.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                for (NotifyHandler notifyHandler : notifyHandlerList) {
                    if (notifyHandler.handleNotify(json)) {
                        return;
                    }
                }
            }
        });
    }

    public void handleMessageData(final String data) {
        MainHandler.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                for (NotifyHandler notifyHandler : notifyHandlerList) {
                    if (notifyHandler.handleMessage(data)) {
                        return;
                    }
                }
            }
        });
    }

    public void handleClientId(final String clientId) {
        MainHandler.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                for (NotifyHandler notifyHandler : notifyHandlerList) {
                    if (notifyHandler.handleClientId(clientId)) {
                        return;
                    }
                }
            }
        });
    }

    //注册通知栏消息处理监听者
    public void registerNotifyHandler(NotifyHandler notifyHandler) {
        if (null != notifyHandler) {
            notifyHandlerList.add(notifyHandler);
        }
    }

    public void unRegisterNotifyHandler(NotifyHandler notifyHandler) {
        if (null != notifyHandler) {
            notifyHandlerList.remove(notifyHandler);
        }
    }

    /**
     * 通知栏消息点击处理，方法返回true表示拦截监听，不再传递给下一个监听者
     */
    public interface NotifyHandler {
        boolean handleNotify(String json);

        boolean handleMessage(String json);

        boolean handleClientId(String clientId);
    }
}
