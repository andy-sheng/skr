package com.module.msg;

import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.module.common.ICallback;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public class RongMsgAdapter {

    public final static String TAG = "RongMsgAdapter";

    private static class RongMsgAdapterHolder {
        private static final RongMsgAdapter INSTANCE = new RongMsgAdapter();
    }

    private RongMsgAdapter() {

    }

    public static final RongMsgAdapter getInstance() {
        return RongMsgAdapterHolder.INSTANCE;
    }


    private RongIMClient.OperationCallback mOperationCallback = new RongIMClient.OperationCallback() {
        @Override
        public void onSuccess() {
            if (mJoinroomCallback != null) {
                mJoinroomCallback.onSucess();
            }
        }

        @Override
        public void onError(RongIMClient.ErrorCode errorCode) {
            if (mJoinroomCallback != null) {
                mJoinroomCallback.onFailed(errorCode.getValue(), errorCode.getMessage());
            }
        }
    };

    private ICallback mJoinroomCallback;


    public void init() {
        RongIM.registerMessageType(CustomChatRoomMsg.class);
        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int i) {
                MyLog.d(TAG,"onReceived" + " message=" + message + " i=" + i);

                return false;
            }
        });
    }

    public void joinChatRoom(String roomId, ICallback callback) {
        mJoinroomCallback = callback;
        RongIM.getInstance().joinChatRoom(roomId, 10, mOperationCallback);
    }


    public void leaveChatRoom(String roomId) {
        mJoinroomCallback = null;
        RongIM.getInstance().quitChatRoom(roomId, mOperationCallback);
    }

    public void sendChatRoomMessage(String roomId, JSONObject jsonObject){
        CustomChatRoomMsg customChatRoomMsg = new CustomChatRoomMsg();
        Message msg = Message.obtain(roomId, Conversation.ConversationType.CHATROOM, customChatRoomMsg);
        RongIM.getInstance().sendMessage(msg, "pushContent", "pushData", new IRongCallback.ISendMessageCallback(){
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

            }
        });
    }
}
