//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.manager;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongIM.OnSendMessageListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.RongIMClient.SendImageMessageCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.message.ImageMessage;

public class SendImageManager {
    private static final String TAG = "SendImageManager";
    private ExecutorService executorService;
    private io.rong.imkit.manager.SendImageManager.UploadController uploadController;

    public static io.rong.imkit.manager.SendImageManager getInstance() {
        return io.rong.imkit.manager.SendImageManager.SingletonHolder.sInstance;
    }

    private SendImageManager() {
        this.executorService = this.getExecutorService();
        this.uploadController = new io.rong.imkit.manager.SendImageManager.UploadController();
    }

    public void sendImages(ConversationType conversationType, String targetId, List<Uri> imageList, boolean isFull) {
        RLog.d("SendImageManager", "sendImages " + imageList.size());
        Iterator var5 = imageList.iterator();

        while (var5.hasNext()) {
            Uri image = (Uri) var5.next();
            if (!TextUtils.isEmpty(image.getPath())) {
                File file = new File(image.getPath());
                if (file.exists()) {
                    ImageMessage content = ImageMessage.obtain(image, image, isFull);
                    OnSendMessageListener listener = RongContext.getInstance().getOnSendMessageListener();
                    if (listener != null) {
                        Message message = listener.onSend(Message.obtain(targetId, conversationType, content));
                        if (message != null) {
                            RongIMClient.getInstance().insertMessage(conversationType, targetId,null, message.getContent(), new ResultCallback<Message>() {
                                public void onSuccess(Message message) {
                                    message.setSentStatus(SentStatus.SENDING);
                                    RongIMClient.getInstance().setMessageSentStatus(message.getMessageId(), SentStatus.SENDING, (ResultCallback) null);
                                    RongContext.getInstance().getEventBus().post(message);
                                    io.rong.imkit.manager.SendImageManager.this.uploadController.execute(message);
                                }

                                public void onError(ErrorCode e) {
                                }
                            });
                        }
                    } else {
                        RongIMClient.getInstance().insertMessage(conversationType, targetId,null, content, new ResultCallback<Message>() {
                            public void onSuccess(Message message) {
                                message.setSentStatus(SentStatus.SENDING);
                                RongIMClient.getInstance().setMessageSentStatus(message.getMessageId(), SentStatus.SENDING, (ResultCallback) null);
                                RongContext.getInstance().getEventBus().post(message);
                                io.rong.imkit.manager.SendImageManager.this.uploadController.execute(message);
                            }

                            public void onError(ErrorCode e) {
                            }
                        });
                    }
                }
            }
        }

    }

    public void cancelSendingImages(ConversationType conversationType, String targetId) {
        RLog.d("SendImageManager", "cancelSendingImages");
        if (conversationType != null && targetId != null && this.uploadController != null) {
            this.uploadController.cancel(conversationType, targetId);
        }

    }

    public void cancelSendingImage(ConversationType conversationType, String targetId, int messageId) {
        RLog.d("SendImageManager", "cancelSendingImages");
        if (conversationType != null && targetId != null && this.uploadController != null && messageId > 0) {
            this.uploadController.cancel(conversationType, targetId, messageId);
        }

    }

    public void reset() {
        this.uploadController.reset();
    }

    private ExecutorService getExecutorService() {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(1, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue(), this.threadFactory("Rong SendMediaManager", false));
        }

        return this.executorService;
    }

    private ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            public Thread newThread(@Nullable Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

    private class UploadController implements Runnable {
        final List<Message> pendingMessages = new ArrayList();
        Message executingMessage;

        public UploadController() {
        }

        public void execute(Message message) {
            List var2 = this.pendingMessages;
            synchronized (this.pendingMessages) {
                this.pendingMessages.add(message);
                if (this.executingMessage == null) {
                    this.executingMessage = (Message) this.pendingMessages.remove(0);
                    io.rong.imkit.manager.SendImageManager.this.executorService.submit(this);
                }

            }
        }

        public void reset() {
            RLog.w("SendImageManager", "Rest Sending Images.");
            List var1 = this.pendingMessages;
            synchronized (this.pendingMessages) {
                Iterator var2 = this.pendingMessages.iterator();

                while (true) {
                    if (!var2.hasNext()) {
                        this.pendingMessages.clear();
                        break;
                    }

                    Message message = (Message) var2.next();
                    message.setSentStatus(SentStatus.FAILED);
                    RongContext.getInstance().getEventBus().post(message);
                }
            }

            if (this.executingMessage != null) {
                this.executingMessage.setSentStatus(SentStatus.FAILED);
                RongContext.getInstance().getEventBus().post(this.executingMessage);
                this.executingMessage = null;
            }

        }

        public void cancel(ConversationType conversationType, String targetId) {
            List var3 = this.pendingMessages;
            synchronized (this.pendingMessages) {
                int count = this.pendingMessages.size();

                for (int i = 0; i < count; ++i) {
                    Message msg = (Message) this.pendingMessages.get(i);
                    if (msg.getConversationType().equals(conversationType) && msg.getTargetId().equals(targetId)) {
                        this.pendingMessages.remove(msg);
                    }
                }

                if (this.pendingMessages.size() == 0) {
                    this.executingMessage = null;
                }

            }
        }

        public void cancel(ConversationType conversationType, String targetId, int messageId) {
            List var4 = this.pendingMessages;
            synchronized (this.pendingMessages) {
                int count = this.pendingMessages.size();

                for (int i = 0; i < count; ++i) {
                    Message msg = (Message) this.pendingMessages.get(i);
                    if (msg.getConversationType().equals(conversationType) && msg.getTargetId().equals(targetId) && msg.getMessageId() == messageId) {
                        this.pendingMessages.remove(msg);
                        break;
                    }
                }

                if (this.pendingMessages.size() == 0) {
                    this.executingMessage = null;
                }

            }
        }

        private void polling() {
            List var1 = this.pendingMessages;
            synchronized (this.pendingMessages) {
                RLog.d("SendImageManager", "polling " + this.pendingMessages.size());
                if (this.pendingMessages.size() > 0) {
                    this.executingMessage = (Message) this.pendingMessages.remove(0);
                    io.rong.imkit.manager.SendImageManager.this.executorService.submit(this);
                } else {
                    this.pendingMessages.clear();
                    this.executingMessage = null;
                }

            }
        }

        public void run() {
            RongIM.getInstance().sendImageMessage(this.executingMessage, (String) null, (String) null, false, new SendImageMessageCallback() {
                public void onAttached(Message message) {
                }

                public void onError(Message message, ErrorCode code) {
                    io.rong.imkit.manager.SendImageManager.UploadController.this.polling();
                }

                public void onSuccess(Message message) {
                    io.rong.imkit.manager.SendImageManager.UploadController.this.polling();
                }

                public void onProgress(Message message, int progress) {
                }
            });
        }
    }

    static class SingletonHolder {
        static io.rong.imkit.manager.SendImageManager sInstance = new io.rong.imkit.manager.SendImageManager();

        SingletonHolder() {
        }
    }
}
