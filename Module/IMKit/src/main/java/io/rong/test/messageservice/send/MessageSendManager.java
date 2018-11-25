package io.rong.test.messageservice.send;

import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rong.test.RongIMAPIManager;

public class MessageSendManager {

    private static class MessageSendManagerHolder {
        private static final MessageSendManager INSTANCE = new MessageSendManager();
    }

    public static final MessageSendManager getInstance() {
        return MessageSendManager.MessageSendManagerHolder.INSTANCE;

    }

    /**
     * 发送单聊消息
     * @param fromUserId
     * @param toUserId
     * @param objectName
     * @param content
     * @param pushContent
     * @param pushData
     * @param count
     * @param verifyBlacklist
     * @param isPersisted
     * @param isCounted
     * @param isIncludeSender
     * @param contentAvailable
     */
    public void singleMsgSend(String fromUserId,
                              String toUserId, String objectName,
                              String content,
                              String pushContent,
                              String pushData,
                              String count, String verifyBlacklist,
                              String isPersisted,
                              String isCounted,
                              String isIncludeSender,
                              String contentAvailable) {

        MessageSendServerApi messageSendServerApi = RongIMAPIManager.getInstance().createService(MessageSendServerApi.class);
        messageSendServerApi.singleMsgSend(fromUserId, toUserId, objectName, content, pushContent, pushData, count,
                verifyBlacklist, isPersisted, isCounted, isIncludeSender, contentAvailable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
