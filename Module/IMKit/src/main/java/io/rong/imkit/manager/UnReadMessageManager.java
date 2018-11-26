//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imkit.model.Event.ConnectEvent;
import io.rong.imkit.model.Event.ConversationRemoveEvent;
import io.rong.imkit.model.Event.ConversationUnreadEvent;
import io.rong.imkit.model.Event.MessageLeftEvent;
import io.rong.imkit.model.Event.OnReceiveMessageEvent;
import io.rong.imkit.model.Event.RemoteMessageRecallEvent;
import io.rong.imkit.model.Event.SyncReadStatusEvent;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;

public class UnReadMessageManager {
    private static final String TAG = "UnReadMessageManager";
    private List<io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo> mMultiConversationUnreadInfos;
    private int left;

    private UnReadMessageManager() {
        this.mMultiConversationUnreadInfos = new ArrayList();
        EventBus.getDefault().register(this);
    }

    public static io.rong.imkit.manager.UnReadMessageManager getInstance() {
        return io.rong.imkit.manager.UnReadMessageManager.SingletonHolder.sInstance;
    }

    public void onEventMainThread(OnReceiveMessageEvent event) {
        RLog.d("UnReadMessageManager", "OnReceiveMessageEvent " + event.getLeft());
        this.left = event.getLeft();
        this.syncUnreadCount(event.getMessage(), event.getLeft());
    }

    public void onEventMainThread(MessageLeftEvent event) {
        RLog.d("UnReadMessageManager", "MessageLeftEvent " + event.left);
        this.left = event.left;
        this.syncUnreadCount((Message) null, event.left);
    }

    public void onEventMainThread(ConnectEvent event) {
        this.syncUnreadCount((Message) null, 0);
    }

    private void syncUnreadCount(Message message, int left) {
        Iterator var3 = this.mMultiConversationUnreadInfos.iterator();

        while (var3.hasNext()) {
            final io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo msgInfo = (io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo) var3.next();
            if (left == 0) {
                RongIMClient.getInstance().getUnreadCount(msgInfo.conversationTypes, new ResultCallback<Integer>() {
                    public void onSuccess(Integer integer) {
                        RLog.d("UnReadMessageManager", "get result: " + integer);
                        msgInfo.count = integer;
                        msgInfo.observer.onCountChanged(integer);
                    }

                    public void onError(ErrorCode e) {
                    }
                });
            }
        }

    }

    public void onEventMainThread(ConversationRemoveEvent removeEvent) {
        ConversationType conversationType = removeEvent.getType();
        Iterator var3 = this.mMultiConversationUnreadInfos.iterator();

        while (true) {
            while (var3.hasNext()) {
                final io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo msgInfo = (io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo) var3.next();
                ConversationType[] var5 = msgInfo.conversationTypes;
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    ConversationType ct = var5[var7];
                    if (ct.equals(conversationType)) {
                        RongIMClient.getInstance().getUnreadCount(msgInfo.conversationTypes, new ResultCallback<Integer>() {
                            public void onSuccess(Integer integer) {
                                msgInfo.count = integer;
                                msgInfo.observer.onCountChanged(integer);
                            }

                            public void onError(ErrorCode e) {
                            }
                        });
                        break;
                    }
                }
            }

            return;
        }
    }

    public void onEventMainThread(Message message) {
        this.syncUnreadCount(message, 0);
    }

    public void onEventMainThread(ConversationUnreadEvent unreadEvent) {
        ConversationType conversationType = unreadEvent.getType();
        Iterator var3 = this.mMultiConversationUnreadInfos.iterator();

        while (true) {
            while (var3.hasNext()) {
                final io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo msgInfo = (io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo) var3.next();
                ConversationType[] var5 = msgInfo.conversationTypes;
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    ConversationType ct = var5[var7];
                    if (ct.equals(conversationType)) {
                        RongIMClient.getInstance().getUnreadCount(msgInfo.conversationTypes, new ResultCallback<Integer>() {
                            public void onSuccess(Integer integer) {
                                msgInfo.count = integer;
                                msgInfo.observer.onCountChanged(integer);
                            }

                            public void onError(ErrorCode e) {
                            }
                        });
                        break;
                    }
                }
            }

            return;
        }
    }

    public void addObserver(ConversationType[] conversationTypes, IUnReadMessageObserver observer) {
        List var3 = this.mMultiConversationUnreadInfos;
        synchronized (this.mMultiConversationUnreadInfos) {
            final io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo msgInfo = new io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo();
            msgInfo.conversationTypes = conversationTypes;
            msgInfo.observer = observer;
            this.mMultiConversationUnreadInfos.add(msgInfo);
            RongIMClient.getInstance().getUnreadCount(conversationTypes, new ResultCallback<Integer>() {
                public void onSuccess(Integer integer) {
                    msgInfo.count = integer;
                    msgInfo.observer.onCountChanged(integer);
                }

                public void onError(ErrorCode e) {
                }
            });
        }
    }

    public void removeObserver(IUnReadMessageObserver observer) {
        List var2 = this.mMultiConversationUnreadInfos;
        synchronized (this.mMultiConversationUnreadInfos) {
            io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo result = null;
            Iterator var4 = this.mMultiConversationUnreadInfos.iterator();

            while (var4.hasNext()) {
                io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo msgInfo = (io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo) var4.next();
                if (msgInfo.observer == observer) {
                    result = msgInfo;
                    break;
                }
            }

            if (result != null) {
                this.mMultiConversationUnreadInfos.remove(result);
            }

        }
    }

    public void clearObserver() {
        List var1 = this.mMultiConversationUnreadInfos;
        synchronized (this.mMultiConversationUnreadInfos) {
            this.mMultiConversationUnreadInfos.clear();
        }
    }

    public void onMessageReceivedStatusChanged() {
        this.syncUnreadCount((Message) null, 0);
    }

    public void onEventMainThread(SyncReadStatusEvent event) {
        RLog.d("UnReadMessageManager", "SyncReadStatusEvent " + this.left);
        if (this.left == 0) {
            ConversationType conversationType = event.getConversationType();
            Iterator var3 = this.mMultiConversationUnreadInfos.iterator();

            while (true) {
                while (var3.hasNext()) {
                    final io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo msgInfo = (io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo) var3.next();
                    ConversationType[] var5 = msgInfo.conversationTypes;
                    int var6 = var5.length;

                    for (int var7 = 0; var7 < var6; ++var7) {
                        ConversationType ct = var5[var7];
                        if (ct.equals(conversationType)) {
                            RongIMClient.getInstance().getUnreadCount(msgInfo.conversationTypes, new ResultCallback<Integer>() {
                                public void onSuccess(Integer integer) {
                                    msgInfo.count = integer;
                                    msgInfo.observer.onCountChanged(integer);
                                }

                                public void onError(ErrorCode e) {
                                }
                            });
                            break;
                        }
                    }
                }

                return;
            }
        }
    }

    public void onEventMainThread(RemoteMessageRecallEvent event) {
        RLog.d("UnReadMessageManager", "SyncReadStatusEvent " + this.left);
        if (this.left == 0) {
            ConversationType conversationType = event.getConversationType();
            Iterator var3 = this.mMultiConversationUnreadInfos.iterator();

            while (true) {
                while (var3.hasNext()) {
                    final io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo msgInfo = (io.rong.imkit.manager.UnReadMessageManager.MultiConversationUnreadMsgInfo) var3.next();
                    ConversationType[] var5 = msgInfo.conversationTypes;
                    int var6 = var5.length;

                    for (int var7 = 0; var7 < var6; ++var7) {
                        ConversationType ct = var5[var7];
                        if (ct.equals(conversationType)) {
                            RongIMClient.getInstance().getUnreadCount(msgInfo.conversationTypes, new ResultCallback<Integer>() {
                                public void onSuccess(Integer integer) {
                                    msgInfo.count = integer;
                                    msgInfo.observer.onCountChanged(integer);
                                }

                                public void onError(ErrorCode e) {
                                }
                            });
                            break;
                        }
                    }
                }

                return;
            }
        }
    }

    private class MultiConversationUnreadMsgInfo {
        ConversationType[] conversationTypes;
        int count;
        IUnReadMessageObserver observer;

        private MultiConversationUnreadMsgInfo() {
        }
    }

    private static class SingletonHolder {
        static io.rong.imkit.manager.UnReadMessageManager sInstance = new io.rong.imkit.manager.UnReadMessageManager();

        private SingletonHolder() {
        }
    }
}
