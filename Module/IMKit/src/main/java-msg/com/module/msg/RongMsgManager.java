package com.module.msg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.common.ICallback;
import com.module.msg.model.CustomChatRoomMsg;

import java.util.HashMap;
import java.util.HashSet;

import io.rong.imkit.RongIM;
import io.rong.test.token.RCTokenManager;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public class RongMsgManager {

    public final static String TAG = "RongMsgAdapter";

    private static class RongMsgAdapterHolder {
        private static final RongMsgManager INSTANCE = new RongMsgManager();
    }

    private RongMsgManager() {
        String id = U.getMD5Utils().MD5_32(U.getDeviceUtils().getDeviceID());
        RCTokenManager.getInstance().getToken(id, id, "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543140813627&di=8fb590ba624746b929d3354c4aeb6ae9&imgtype=0&src=http%3A%2F%2Fimage.biaobaiju.com%2Fuploads%2F20180918%2F15%2F1537256494-ZnSKMzEoBI.jpeg");
    }

    public static final RongMsgManager getInstance() {
        return RongMsgAdapterHolder.INSTANCE;
    }


    private RongIMClient.OperationCallback mOperationCallback = new RongIMClient.OperationCallback() {
        @Override
        public void onSuccess() {
            if (mJoinroomCallback != null) {
                mJoinroomCallback.onSucess(null);
            }
        }

        @Override
        public void onError(RongIMClient.ErrorCode errorCode) {
            if (mJoinroomCallback != null) {
                mJoinroomCallback.onFailed(null, errorCode.getValue(), errorCode.getMessage());
            }
        }
    };

    private ICallback mJoinroomCallback;

    /**
     * 消息类型-->消息处理器的映射
     */
    private HashMap<Integer, HashSet<IPushMsgProcess>> mProcessorMap = new HashMap<>();

    public void init() {
        RongIM.registerMessageType(CustomChatRoomMsg.class);
        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {

            /**
             * 收到消息的处理。
             *
             * @param message 收到的消息实体。
             * @param left    剩余未拉取消息数目。
             * @return 收到消息是否处理完成，true 表示自己处理铃声和后台通知，false 走融云默认处理方式。
             */
            @Override
            public boolean onReceived(Message message, int left) {
                MyLog.d(TAG, "onReceived" + " message=" + message + " left=" + left);
                if (message.getContent() instanceof CustomChatRoomMsg) {
                    // 是自定义消息
                    CustomChatRoomMsg customChatRoomMsg = (CustomChatRoomMsg) message.getContent();
                    HashSet<IPushMsgProcess> processors = mProcessorMap.get(customChatRoomMsg.getMessageType());
                    if (processors != null) {
                        // todo json形式（不用可删除）
                        JSONObject jsonObject = JSON.parseObject(customChatRoomMsg.getContentJsonStr());
                        for (IPushMsgProcess processor : processors) {
                            processor.process(customChatRoomMsg.getMessageType(), jsonObject);
                        }

                        // todo pb形式
                        byte[] data = customChatRoomMsg.getData();
                        for (IPushMsgProcess process : processors){
                            process.process(customChatRoomMsg.getMessageType(), data);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public synchronized void addMsgProcessor(IPushMsgProcess processor) {
        MyLog.d(TAG, "addMsgProcessor" + " processor=" + processor);
        for (int type : processor.acceptType()) {
            HashSet<IPushMsgProcess> processorSet = mProcessorMap.get(type);
            if (processorSet == null) {
                processorSet = new HashSet<>();
                mProcessorMap.put(type, processorSet);
            }
            processorSet.add(processor);
        }
    }

    public void joinChatRoom(String roomId, ICallback callback) {
        mJoinroomCallback = callback;
        /**
         * 不拉之前的消息
         */
        RongIM.getInstance().joinChatRoom(roomId, 0, mOperationCallback);
    }


    public void leaveChatRoom(String roomId) {
        mJoinroomCallback = null;
        RongIM.getInstance().quitChatRoom(roomId, mOperationCallback);
    }

    public void sendChatRoomMessage(String roomId, int messageType, JSONObject contentJson, ICallback callback) {
        CustomChatRoomMsg customChatRoomMsg = new CustomChatRoomMsg();
        customChatRoomMsg.setMessageType(messageType);
        customChatRoomMsg.setContentJsonStr(contentJson.toJSONString());
        Message msg = Message.obtain(roomId, Conversation.ConversationType.CHATROOM, customChatRoomMsg);
        RongIM.getInstance().sendMessage(msg, "pushContent", "pushData", new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                if (callback != null) {
                    callback.onSucess(message);
                }
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                MyLog.d(TAG, "send msg onError errorCode=" + errorCode);
                if (callback != null) {
                    callback.onFailed(message, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

    public void sendChatRoomMessage(String roomId, int messageType, byte[] data, ICallback callback){
        CustomChatRoomMsg customChatRoomMsg = new CustomChatRoomMsg();
        customChatRoomMsg.setMessageType(messageType);
        customChatRoomMsg.setData(data);
        Message msg = Message.obtain(roomId, Conversation.ConversationType.CHATROOM, customChatRoomMsg);
        RongIM.getInstance().sendMessage(msg, "pushContent", "pushData", new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                if (callback != null) {
                    callback.onSucess(message);
                }
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                MyLog.d(TAG, "send msg onError errorCode=" + errorCode);
                if (callback != null) {
                    callback.onFailed(message, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

}
