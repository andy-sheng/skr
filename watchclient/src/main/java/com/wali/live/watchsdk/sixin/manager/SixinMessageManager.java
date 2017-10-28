package com.wali.live.watchsdk.sixin.manager;

import android.os.Message;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.wali.live.dao.Conversation;
import com.wali.live.dao.SixinMessage;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.statistics.StatisticUtils;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;
import com.wali.live.watchsdk.sixin.data.SixinMessageCloudStore;
import com.wali.live.watchsdk.sixin.data.SixinMessageLocalStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @module 私信消息push接入
 */
public class SixinMessageManager implements MiLinkPacketDispatcher.PacketDataHandler {

    private static final String TAG = "SixinMessageManager";
    private static final int TIME_OUT = 30 * 1000;
    private static final int MESSAGE_SIXIN_MESSAGE_INSERT_DB = 0;
    private static final int MESSAGE_SIXIN_HANDLE_ACK = 1;
    private static final int MESSAGE_CONVERSATION_CHANGE_FOUCES_STATUS_BY_ME = 3;
    private static final int MESSAGE_SIXIN_UPDATE_SEND_FAILED_STATUS = 4;
    private static final int MESSAGE_CONVERSATION_CHANGE_FOUCES_STATUS_NOT_BY_ME = 5;
    private static final int MESSAGE_GROUP_UPDATE_UNREAD_COUNT = 6; // 群消息的未读数更新
    private static final int MESSAGE_GROUP_DELETE_ALL_MSG = 7; // 删除所有的群消息
    private static final int MESSAGE_GROUP_UPDATE_LAST_SEQ = 8; // 更新群最后的seq
    private static final int MESSAGE_GROUP_HANDLE_MESSAGE_ACK = 9; //处理群消息的ack

    public static final int MESSAGE_BARRAGE_MSG_TIME_OUT_CHECK = 2;//友好互助下

    private static SixinMessageManager sInstance = new SixinMessageManager();

    public static SixinMessageCloudStore mSixinMessageCloudStore = new SixinMessageCloudStore();
    public static CustomHandlerThread mCustomHandlerThread = new CustomHandlerThread(TAG) {
        //暂时用来　私信消息入库
        @Override
        protected void processMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SIXIN_MESSAGE_INSERT_DB: {
                    int needNotify = msg.arg1;
                    boolean needsNotifyDBInsertEvent = needNotify == 1;
                    List<SixinMessage> sixinMessages = (List<SixinMessage>) msg.obj;
                    SixinMessageLocalStore.insertSixinMessages(sixinMessages, needsNotifyDBInsertEvent);
                    for (SixinMessage sixinMessage : sixinMessages) {
                        if (!sixinMessage.getIsInbound() && MiLinkClientAdapter.getsInstance().isMiLinkLogined()) {
//                            SendingMessageCache.put(sixinMessage.getId(), System.currentTimeMillis());
                        }
                    }
                }
                break;
            }
        }
    };

    public static SixinMessageManager getInstance() {
        return sInstance;
    }

    private SixinMessageManager() {
    }

    @Override
    public boolean processPacketData(PacketData data) {
        if (data == null) {
            return false;
        }
        String command = data.getCommand();
        MyLog.d(TAG, "command=" + command);
        switch (command) {
            case MiLinkCommand.COMMAND_PUSH_BARRAGE:
                break;
            case MiLinkCommand.COMMAND_PUSH_CHAT_MSG:
                processPushChatMessage(data);
                break;
            case MiLinkCommand.COMMAND_PUSH_READ_MSG:
                break;
            case MiLinkCommand.COMMAND_SYNC_CHAT_MSG:
                processSyncUnreadResponse(data);
                break;
            case MiLinkCommand.COMMAND_SEND_CHAT_MSG://处理发出的chatMsg ack
                processSendChatMessageResponse(data);
                break;
            case MiLinkCommand.COMMAND_SEND_READ_MSG://处理发出的readMsg ack
                break;
            case MiLinkCommand.COMMAND_NOTIFY_CHAT_MSG:
                processNotifyMessage(data);
                break;
            case MiLinkCommand.COMMAND_NET_WORK_PRODE:
                break;
        }
        return false;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                MiLinkCommand.COMMAND_PUSH_BARRAGE,
                MiLinkCommand.COMMAND_SEND_CHAT_MSG,
                MiLinkCommand.COMMAND_SEND_READ_MSG,
                MiLinkCommand.COMMAND_SYNC_CHAT_MSG,
                MiLinkCommand.COMMAND_PUSH_READ_MSG,
                MiLinkCommand.COMMAND_PUSH_CHAT_MSG,
                MiLinkCommand.COMMAND_NOTIFY_CHAT_MSG,
                MiLinkCommand.COMMAND_NET_WORK_PRODE,
        };
    }

    /**
     * 处理单聊消息的response
     *
     * @param packetData
     */
    public void processSendChatMessageResponse(PacketData packetData) {
        if (packetData != null) {
            try {
                LiveMessageProto.ChatMessageResponse chatMessageResponse = LiveMessageProto.ChatMessageResponse.parseFrom(packetData.getData());
                if (chatMessageResponse != null) {
                    if (chatMessageResponse.getRet() == MiLinkConstant.ERROR_CODE_SUCCESS) {
//                        ToastUtils.showToast(GlobalData.app(), "发送成功,cid:" + chatMessageResponse.getCid() + " seq:" + chatMessageResponse.getMsgSeq());
                        Message message = Message.obtain();
                        message.what = MESSAGE_SIXIN_HANDLE_ACK;
                        message.obj = chatMessageResponse;
                        mCustomHandlerThread.sendMessage(message);
                    } else {
                        MyLog.w(TAG + " retcode: " + chatMessageResponse.getRet() + " errormsg:  " + chatMessageResponse.getErrorMsg() + " " + chatMessageResponse.getMsgSeq()
                                + " cid: " + chatMessageResponse.getCid());
                        if (chatMessageResponse.getRet() == MiLinkConstant.NOT_FOLLOW_EACH_OTHER) {
                            ToastUtils.showToast(GlobalData.app(), R.string.msg_send_failed_not_follow_each_other);
                        } else if (chatMessageResponse.getRet() == MiLinkConstant.ERROR_CODE_MSG_TOO_LARGE) {
                            ToastUtils.showToast(GlobalData.app(), R.string.msg_send_failed_not_text_too_larger);
                        } else if (chatMessageResponse.getRet() == MiLinkConstant.BLOCKD) {
                            ToastUtils.showToast(GlobalData.app(), R.string.msg_send_failed_blocked);
                        } else if (chatMessageResponse.getRet() == MiLinkConstant.ILLEGAL_MSG) {
                            ToastUtils.showToast(GlobalData.app(), R.string.msg_send_failed_illege_text);
                        } else if (chatMessageResponse.getRet() == MiLinkConstant.DUPLICATED_HI_MSG) {
                            ToastUtils.showToast(GlobalData.app(), R.string.sixin_say_hello_result_code_failed_duplicated);
                        }

                        StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_IM_SEND_FAIL_INFO, StatisticUtils.SUCCESS, "retcode:" + chatMessageResponse.getRet());

                        Message message = Message.obtain();
                        message.what = MESSAGE_SIXIN_UPDATE_SEND_FAILED_STATUS;
                        message.obj = chatMessageResponse.getCid();
                        mCustomHandlerThread.sendMessage(message);

                    }
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    /**
     * 处理聊天历史记录同步的数据包
     * 目前只支持999号客服
     *
     * @param packetData
     * @return
     */
    public void processSyncChatHistoryResponse(PacketData packetData) {
        if (packetData != null) {
        }
    }

    /**
     * 处理同步单聊消息的response
     *
     * @param packetData
     * @return
     */
    public boolean processSyncUnreadResponse(PacketData packetData) {
        boolean isSyncToEnd = true;
        if (packetData != null) {
            try {
                LiveMessageProto.SyncUnreadResponse syncUnreadResponse = LiveMessageProto.SyncUnreadResponse.parseFrom(packetData.getData());
                if (syncUnreadResponse != null) {
                    List<LiveMessageProto.Unread> unreadList = syncUnreadResponse.getUnreadList();
                    if (unreadList != null && unreadList.size() > 0) {
//                        ToastUtils.showToast(GlobalData.app(), "sync到数据拉");
                        List<SixinMessage> sixinMessages = new ArrayList<>();
                        for (LiveMessageProto.Unread unread : unreadList) {
                            List<LiveMessageProto.Message> messageList = unread.getMsgList();
                            if (messageList != null && messageList.size() > 0) {
                                for (LiveMessageProto.Message message : messageList) {
                                    if (message.getFromUser() != 0) {
                                        SixinMessage sixinMessage = new SixinMessage(message);
                                        sixinMessages.add(sixinMessage);
                                    } else {
                                        //TODO 待逻辑,系统关注私信暂时不处理 , 系统消息是私信房间关注的话，则需要处理成一条消息
                                    }
                                    MyLog.w(TAG + message.getFromUser() + " " + message.getFromUserNickName()
                                            + " " + message.getMsgStatus());
                                }
                                //read ack 是服务器要收到就回的
                                LiveMessageProto.Message lastMessage = messageList.get(messageList.size() - 1);
                                SixinMessage lastSixinMessage = new SixinMessage(lastMessage);
                                mSixinMessageCloudStore.sendReadAck(lastSixinMessage.getTarget(), lastSixinMessage.getSenderMsgId(), lastSixinMessage.getMsgSeq(), lastMessage.getMsgStatus());
                            }
                        }
                        if (sixinMessages.size() > 0) {
                            insertMessageToDB(sixinMessages);
                        }
                        isSyncToEnd = false;
                    }
                    MLPreferenceUtils.setSettingString(GlobalData.app(), MLPreferenceUtils.PREF_KEY_SIXIN_SYNC_PAGE_ID, syncUnreadResponse.getPageId());
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return isSyncToEnd;
    }

    /**
     * 处理收到的单聊消息
     *
     * @param packetData
     */
    public void processPushChatMessage(PacketData packetData) {
        if (packetData != null) {
            try {
                LiveMessageProto.PushMessage pushMessage = LiveMessageProto.PushMessage.parseFrom(packetData.getData());
                if (pushMessage != null) {
                    List<LiveMessageProto.Message> messageList = pushMessage.getMessageList();
                    if (messageList != null && messageList.size() > 0) {
                        List<SixinMessage> sixinMessages = new ArrayList<>();
                        for (LiveMessageProto.Message message : messageList) {
//                            ToastUtils.showToast(GlobalData.app(), "收到消息:" + message.getMsgBody());
                            SixinMessage sixinMessage = new SixinMessage(message);
                            sixinMessages.add(sixinMessage);
                            //由于服务器需要客户端收到消息就发read,所以没办法
                            mSixinMessageCloudStore.sendReadAck(sixinMessage.getTarget(), sixinMessage.getSenderMsgId(), sixinMessage.getMsgSeq(), sixinMessage.getMsgStatus());
                        }
                        insertMessageToDB(sixinMessages);
                    }
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    public static void insertMessageToDB(List<SixinMessage> sixinMessage) {
        Message message = Message.obtain();
        message.what = MESSAGE_SIXIN_MESSAGE_INSERT_DB;
        message.obj = sixinMessage;
        message.arg1 = 1;
        mCustomHandlerThread.sendMessage(message);
    }

    /**
     * 把私信消息插入到数据库　是否用通知
     *
     * @param sixinMessage
     * @param needsNotifyDBUpdate
     */
    private void insertMessageToDB(List<SixinMessage> sixinMessage, boolean needsNotifyDBUpdate) {
        Message message = Message.obtain();
        message.what = MESSAGE_SIXIN_MESSAGE_INSERT_DB;
        message.obj = sixinMessage;
        message.arg1 = needsNotifyDBUpdate ? 1 : 0;
        mCustomHandlerThread.sendMessage(message);
    }

    /**
     * 处理有单聊消息的通知
     *
     * @param packetData
     */
    public void processNotifyMessage(PacketData packetData) {
        if (packetData != null) {
            try {
                LiveMessageProto.ChatNotifyMessage chatNotifyMessage = LiveMessageProto.ChatNotifyMessage.parseFrom(packetData.getData());
                mSixinMessageCloudStore.sendSyncUnreadMessage(chatNotifyMessage.getFollowType());
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(SixinMessageLocalStore.SixinMessageBulkInsertEvent event) {
        if (event != null) {
            List<SixinMessage> sixinMessages = event.sixinMessages;
            if (sixinMessages != null && sixinMessages.size() > 0) {
                for (SixinMessage sixinMessage : sixinMessages) {
                    ConversationLocalStore.insertOrUpdateConversationByMessage(sixinMessage, event.hasNewMessage);
//                    notifySixinMessage(sixinMessage);
                }
            }
            if (event.hasNewMessage) {
                long unreadCount = ConversationLocalStore.getAllConversationUnReadCount();
                EventBus.getDefault().post(new ConversationLocalStore.NotifyUnreadCountChangeEvent(unreadCount));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(SixinMessageLocalStore.SixinMessageUpdateEvent event) {
        if (event != null) {
            SixinMessage sixinMessage = event.sixinMessage;
            if (sixinMessage != null) {
                Conversation conversation = ConversationLocalStore.getConversationByTarget(sixinMessage.getTarget(), sixinMessage.getTargetType());
                if (conversation != null && (conversation.getMsgId().equals(sixinMessage.getId()) || sixinMessage.getMsgTyppe() == SixinMessage.S_MSG_TYPE_DRAFT)) {
                    ConversationLocalStore.updateConversationBySixinMessage(conversation, sixinMessage);
                    ConversationLocalStore.updateConversation(conversation);
                }
            }
        }
    }
}
