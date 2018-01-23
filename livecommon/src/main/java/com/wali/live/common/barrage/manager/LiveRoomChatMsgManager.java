package com.wali.live.common.barrage.manager;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.push.collection.InsertSortLinkedList;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.push.model.GlobalRoomMsgExt;
import com.mi.live.data.room.model.FansPrivilegeModel;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.common.model.CommentModel;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.event.EventClass;
import com.wali.live.proto.VFansCommonProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @module com.wali.live.video.widget
 * <p/>
 * Created by MK on 16/2/25.
 * <p/>
 * 直播或者观看直播时，房间聊天的消息管理
 */
public class LiveRoomChatMsgManager {
    private static final String TAG = "LiveRoomChatMsgManager";
    public static final int DEFAULT_MAX_SIZE = 300;

    /**
     * 游戏直播默认的消息屏蔽值
     */
    public static final boolean DEFAULT_FORBID_GIFT = false;
    public static final boolean DEFAULT_FORBID_SYS = false;
    public static final boolean DEFAULT_FORBID_CHAT = false;

    protected InsertSortLinkedList<CommentModel> setTopMsgList; //需要置顶的消息列表
    protected InsertSortLinkedList<CommentModel> chatMsgList; //房间聊天消息的列表
    protected MsgAcceptFilter msgTypeFilter; //消息过滤器
    protected HashSet<String> mLikeIdSet = new HashSet<>(); //房间部分弹幕去重列表
    List<Long> rankTops = new ArrayList<>();

    private boolean mHideLikeMsg = false;
    private boolean mHideGiftMsg = false;
    private boolean mHideShareMsg = false;
    private boolean mHideCommentMsg = false;

    /**
     * 以下两个参数，目前只在游戏直播有效
     * mHideSysMsg
     * [暂无]屏蔽的内容包括系统消息：RoomSystemMsgPresenter；
     * 观众消息：RoomViewerPresenter的Join 和 Leave消息
     */
    private boolean mHideSysMsg = false;
    private boolean mHideChatMsg = false;

    private boolean mIsGameLiveMode = false;

    public void setHideLikeMsg(boolean hideLikeMsg) {
        mHideLikeMsg = hideLikeMsg;
    }

    public void setHideGiftMsg(boolean hildeGiftMsg) {
        mHideGiftMsg = hildeGiftMsg;
    }

    public void setHideSysMsg(boolean hideSysMsg) {
        mHideSysMsg = hideSysMsg;
    }

    public void setHideChatMsg(boolean hideChatMsg) {
        mHideChatMsg = hideChatMsg;
    }

    public void setHideCommentMsg(boolean hideCommentMsg) {
        mHideCommentMsg = hideCommentMsg;
    }

    public void setHideShareMsg(boolean hideShareMsg) {
        mHideShareMsg = hideShareMsg;
    }

    public void setIsGameLiveMode(boolean isGameLiveMode) {
        mIsGameLiveMode = isGameLiveMode;

        if (mIsGameLiveMode) {
            mHideGiftMsg = DEFAULT_FORBID_GIFT;
            mHideSysMsg = DEFAULT_FORBID_SYS;
            mHideChatMsg = DEFAULT_FORBID_CHAT;
        }
    }

    public boolean isHideGiftMsg() {
        return mHideGiftMsg;
    }

    public boolean isHideSysMsg() {
        return mHideSysMsg;
    }

    public boolean isHideChatMsg() {
        return mHideChatMsg;
    }

    public LiveRoomChatMsgManager(int maxChatMsgSize) {
        this(maxChatMsgSize, null);
    }

    public LiveRoomChatMsgManager(int maxChatMsgSize, MsgAcceptFilter filter) {
        maxChatMsgSize = Integer.MAX_VALUE;
        setTopMsgList = new InsertSortLinkedList<CommentModel>(maxChatMsgSize); //需要置顶的消息列表
        chatMsgList = new InsertSortLinkedList<CommentModel>(maxChatMsgSize); //房间聊天消息的列表
        msgTypeFilter = filter;
    }

    // isInBound 是否是收到的
    public boolean addChatMsg(BarrageMsg msg, boolean isInBound) {
        if ((msgTypeFilter != null && !msgTypeFilter.isAcceptMsg(msg)) || !canAddToChatMsgManager(msg)) {
            return false;
        }
        CommentModel commentModel = CommentModel.loadFromBarrage(msg);
//        chatMsgList.insert(commentModel);
        replaceEnterAndLike(commentModel);
        EventBus.getDefault().post(new CommentRefreshEvent(getMsgList(), !isInBound, this.toString()));
        if (mIsGameLiveMode) {
            EventBus.getDefault().post(new EventClass.RefreshGameLiveCommentEvent(commentModel, this.toString()));
        }
        return true;
    }

    public void bulkAddChatMsg(List<BarrageMsg> msgs, boolean isInBound) {
        if (msgs != null && msgs.size() > 0) {
            if (mIsGameLiveMode) {
                List<CommentModel> commentModels = new ArrayList();
                for (BarrageMsg barrageMsg : msgs) {
                    if ((msgTypeFilter != null && !msgTypeFilter.isAcceptMsg(barrageMsg)) || !canAddToChatMsgManager(barrageMsg)) {
                        continue;
                    }
                    CommentModel commentModel = CommentModel.loadFromBarrage(barrageMsg);
                    chatMsgList.insert(commentModel);
                    commentModels.add(commentModel);
                }
                EventBus.getDefault().post(new EventClass.RefreshGameLiveCommentEvent(commentModels, this.toString()));
            } else {
                for (BarrageMsg barrageMsg : msgs) {
                    if ((msgTypeFilter != null && !msgTypeFilter.isAcceptMsg(barrageMsg)) || !canAddToChatMsgManager(barrageMsg)) {
                        continue;
                    }
//                    chatMsgList.insert(CommentModel.loadFromBarrage(barrageMsg));
                    replaceEnterAndLike(CommentModel.loadFromBarrage(barrageMsg));
                }
            }
            EventBus.getDefault().post(new CommentRefreshEvent(getMsgList(), !isInBound, this.toString()));
        }
    }


    private void replaceEnterAndLike(CommentModel commentModel) {
        MyLog.w(TAG, "replaceEnterAndLike");
        if (chatMsgList.size() > 0) {
            CommentModel lastComment = chatMsgList.getLastRough();
            if (lastComment.getMsgType() != BarrageMsgType.B_MSG_TYPE_JOIN &&
                    lastComment.getMsgType() != BarrageMsgType.B_MSG_TYPE_LIKE) {
                chatMsgList.insert(commentModel);
            } else if (lastComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN &&
                    (lastComment.getVipLevel() > 1 && !lastComment.isVipFrozen())) {
                chatMsgList.insert(commentModel);
            } else {
                chatMsgList.replaceTail(commentModel);
            }
        } else {
            chatMsgList.insert(commentModel);
        }
    }

    public void refresh() {
        EventBus.getDefault().post(new CommentRefreshEvent(getMsgList(), true, this.toString()));
    }

    public ArrayList<CommentModel> getMsgList() {
        ArrayList<CommentModel> list = new ArrayList<>(setTopMsgList.size() + chatMsgList.size());
        list.addAll(setTopMsgList.toArrayList());
        list.addAll(chatMsgList.toArrayList());
        return list;
    }

    public void clear() {
        if (setTopMsgList != null) {
            setTopMsgList.clear();
        }
        if (chatMsgList != null) {
            chatMsgList.clear();
        }
        if (mLikeIdSet != null) {
            mLikeIdSet.clear();
        }
        if (rankTops != null) {
            rankTops.clear();
        }
        EventBus.getDefault().post(new CommentRefreshEvent(new ArrayList<CommentModel>(), false, this.toString()));
    }

    public interface MsgAcceptFilter {
        boolean isAcceptMsg(BarrageMsg msg);
    }

    /**
     * @param msgBody
     * @param roomid
     * @param anchorId
     * @param pkExt
     */
    public void sendBarrageMessageAsync(String msgBody, int msgType, String roomid, long anchorId,
                                        BarrageMsg.PkMessageExt pkExt, BarrageMsg.MsgExt ext,
                                        GlobalRoomMsgExt globalRoomMsgExt) {
        if (MyUserInfoManager.getInstance().getUser().getLevel() == 0) {
            MyUserInfoManager.getInstance().syncSelfDetailInfo();
        }
        if (!TextUtils.isEmpty(msgBody) && !TextUtils.isEmpty(roomid)) {
            String globalMes = SmileyParser.getInstance()
                    .convertString(msgBody, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();
            BarrageMsg msg = new BarrageMsg();
            msg.setMsgType(msgType);
            msg.setSender(UserAccountManager.getInstance().getUuidAsLong());
            String nickname = MyUserInfoManager.getInstance().getUser().getNickname();
            if (nickname == null) {
                nickname = String.valueOf(UserAccountManager.getInstance().getUuidAsLong());
            }
            msg.setSenderName(nickname);
            msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
            msg.setRoomId(roomid);
            msg.setBody(globalMes);
            msg.setAnchorId(anchorId);
            msg.setSentTime(getLastBarrageMsgSentTime());
            if (MyUserInfoManager.getInstance().getUser() != null) {
                msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
            }
            if (pkExt != null) {
                msg.setRoomType(BarrageMsg.ROOM_TYPE_PK);
                msg.setOpponentAnchorId(pkExt.zuid);
                msg.setOpponentRoomId(pkExt.roomId);
            }
            if (ext != null) {
                msg.setMsgExt(ext);
            }
            if (globalRoomMsgExt != null) {
                msg.setGlobalRoomMsgExt(globalRoomMsgExt);
            }
            msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
            BarrageMessageManager.getInstance().sendBarrageMessageAsync(msg, true);
            //假装是个push过去
            BarrageMessageManager.getInstance().pretendPushBarrage(msg);
//            addChatMsg(msg, false);
        }
    }

    public void sendTextBarrageMessageAsync(String body, String liveId, long anchorId, BarrageMsg.PkMessageExt pkExt) {
        sendBarrageMessageAsync(body, BarrageMsgType.B_MSG_TYPE_TEXT, liveId, anchorId, pkExt, null, null);
    }


    public void sendFlyBarrageMessageAsync(String body, String liveId, long anchorId, int flyBarrageType,
                                           BarrageMsg.PkMessageExt pkExt, FansPrivilegeModel fansPrivilegeModel) {
        BarrageMsg.GiftMsgExt msgExt = new BarrageMsg.GiftMsgExt();
        msgExt.msgBody = SmileyParser.getInstance().convertString(body, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();

        GlobalRoomMsgExt globalRoomMsgExt = new GlobalRoomMsgExt();
        GlobalRoomMsgExt.BaseRoomMessageExt flyTypeExt = new GlobalRoomMsgExt.BaseRoomMessageExt();
        flyTypeExt.setType(flyBarrageType);
        globalRoomMsgExt.addMsgExt(flyTypeExt);

        if (fansPrivilegeModel != null && fansPrivilegeModel.getMemType() != VFansCommonProto.GroupMemType.NONE.getNumber()) {
            GlobalRoomMsgExt.FansMemberMsgExt fansMemberMsgExt = new GlobalRoomMsgExt.FansMemberMsgExt();
            fansMemberMsgExt.setMedalValue(fansPrivilegeModel.getMedal());
            fansMemberMsgExt.setVipExpire(System.currentTimeMillis() > fansPrivilegeModel.getExpireTime() * 1000);
            fansMemberMsgExt.setPetLevel(fansPrivilegeModel.getPetLevel());
            globalRoomMsgExt.addMsgExt(fansMemberMsgExt);
        }

        sendBarrageMessageAsync(body, BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE, liveId, anchorId, pkExt, msgExt, globalRoomMsgExt);
    }


    public void sendLikeBarrageMessageAsync(String liveId, long anchorId, int heartViewColorId, String heartViewBitmapPath) {
        BarrageMsg.LikeMsgExt msgExt = new BarrageMsg.LikeMsgExt();
        msgExt.id = heartViewColorId;
        msgExt.bitmapPath = heartViewBitmapPath;
        sendBarrageMessageAsync(GlobalData.app().getString(R.string.live_start_light), BarrageMsgType.B_MSG_TYPE_LIKE, liveId, anchorId, null, msgExt, null);
    }


    //只有自己能看到的提示消息
    public void sendLocalSystemMsg(String name, String message, String liveId, long anchorId) {
        sendLocalSystemMsg(name, message, liveId, anchorId, BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG);
    }


    //只有自己能看到的提示消息
    public void sendLocalSystemMsg(String name, String message, String liveId, long anchorId, int type) {
        if (!TextUtils.isEmpty(liveId)) {
            BarrageMsg msg = new BarrageMsg();
            msg.setMsgType(type);
            msg.setSender(0);
            msg.setSenderName(name);
            msg.setSenderLevel(0);
            msg.setRoomId(liveId);
            msg.setBody(message);
            msg.setAnchorId(anchorId);
            msg.setSentTime(getLastBarrageMsgSentTime());
            if (MyUserInfoManager.getInstance().getUser() != null) {
                msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
            }
            msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
            BarrageMessageManager.getInstance().sendBarrageMessageAsync(msg, false);
            addChatMsg(msg, false);
        }
    }

    public void sendLocalFocusBarrageMsg(String message, String liveId, long anchorId) {
        if (!TextUtils.isEmpty(liveId)) {
            BarrageMsg msg = new BarrageMsg();
            msg.setMsgType(BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR);
            msg.setSender(MyUserInfoManager.getInstance().getUser().getUid());
            msg.setSenderName(MyUserInfoManager.getInstance().getUser().getNickname());
            msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
            msg.setRoomId(liveId);
            msg.setBody(message);
            msg.setAnchorId(anchorId);
            msg.setSentTime(getLastBarrageMsgSentTime());
            msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
            if (MyUserInfoManager.getInstance().getUser() != null) {
                msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
            }
            addChatMsg(msg, false);
        }
    }


    public void sendShareBarrageMessageAsync(String liveId, long anchorId) {
        if (!TextUtils.isEmpty(liveId)) {
            BarrageMsg msg = new BarrageMsg();
            msg.setMsgType(BarrageMsgType.B_MSG_TYPE_SHARE);
            msg.setSender(UserAccountManager.getInstance().getUuidAsLong());
            msg.setSenderName(MyUserInfoManager.getInstance().getUser().getNickname());
            msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
            msg.setRoomId(liveId);
            msg.setBody(GlobalData.app().getString(R.string.barrage_share_body));
            msg.setAnchorId(anchorId);
            msg.setSentTime(getLastBarrageMsgSentTime());
            msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
            if (MyUserInfoManager.getInstance().getUser() != null) {
                msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
            }
            BarrageMessageManager.getInstance().sendBarrageMessageAsync(msg, true);
            addChatMsg(msg, false);
        }
    }


    public BarrageMsg sendLevelUpgradeAnimMessageAsync(String liveId, long anchorId) {
        if (!TextUtils.isEmpty(liveId)) {
            BarrageMsg msg = new BarrageMsg();
            msg.setMsgType(BarrageMsgType.B_MSG_TYPE_ANIM);
            msg.setSender(UserAccountManager.getInstance().getUuidAsLong());
            msg.setSenderName(MyUserInfoManager.getInstance().getUser().getNickname());
            msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
            msg.setRoomId(liveId);
            String name = TextUtils.isEmpty(msg.getSenderName()) ? String.valueOf(msg.getSender()) : msg.getSenderName();
            msg.setBody(String.format(GlobalData.app().getResources().getString(R.string.brrage_level_upgrade), name, msg.getSenderLevel()));
            msg.setAnchorId(anchorId);
            msg.setSentTime(getLastBarrageMsgSentTime());
            if (MyUserInfoManager.getInstance().getUser() != null) {
                msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
            }
            msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
            BarrageMsg.AnimMsgExt animMsgExt = new BarrageMsg.AnimMsgExt(1, "", BarrageMsg.AnimMsgExt.LEVEL_UPGREAD_ANIMATION_TYPE);
            msg.setMsgExt(animMsgExt);
            BarrageMessageManager.getInstance().sendBarrageMessageAsync(msg, true);
            return msg;
        }
        return null;
    }

    public void setRankTop(List<Long> rankTops) {
        this.rankTops = rankTops;
    }

    /**
     * 判断一条消息是否可以添加到弹幕显示区域
     */
    public boolean canAddToChatMsgManager(BarrageMsg msg) {
        boolean canAdd = true;
        if (null != msg) {
            switch (msg.getMsgType()) {
                case BarrageMsgType.B_MSG_TYPE_JOIN: {
                    if (msg.getVipLevel() > 0 && !msg.isVipFrozen() && msg.isVipHide()) {
                        // vip隐身,不显示入场消息
                        return false;
                    }
                    try {
                        String variables = MLPreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PREF_KEY_CONVERGED, PreferenceKeys.CONVERGED_DEFAULT_VALUE);
                        String[] variable = variables.split("_");
                        if (msg.getSenderLevel() >= Integer.parseInt(variable[0])
                                || rankTops.contains(msg.getSender())
                                || msg.getSender() == MyUserInfoManager.getInstance().getUuid()
                                || msg.getVipLevel() >= 3) {
                            canAdd = true;
                        } else {
                            canAdd = !checkMoreThanMaxCountByTime(msg.getSentTime(), Integer.parseInt(variable[2]) * 1000, Integer.parseInt(variable[3]));
                        }
                    } catch (Exception e) {
                        MyLog.e(e);
                    }
                    break;
                }
                case BarrageMsgType.B_MSG_TYPE_LIKE: {
                    if (mHideLikeMsg) {
                        return false; //根据 mHideLikeMsg 的值决定是否显示like type的消息。
                    }
                    String object = msg.getSender() + "_" + BarrageMsgType.B_MSG_TYPE_LIKE;
                    canAdd = !mLikeIdSet.contains(object); //先看之前是否点亮过

                    if (canAdd && msg.getSender() != MyUserInfoManager.getInstance().getUser().getUid()) { //　如果之前没有点亮，再做一层判断
                        try {
                            String variables = MLPreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PREF_KEY_CONVERGED, PreferenceKeys.CONVERGED_DEFAULT_VALUE);
                            String[] variable = variables.split("_");
                            if (msg.getSenderLevel() >= Integer.parseInt(variable[0]) || rankTops.contains(msg.getSender())) {
                                canAdd = true;
                            } else
                                canAdd = !checkMoreThanMaxCountByTime(msg.getSentTime(), Integer.parseInt(variable[2]) * 1000, Integer.parseInt(variable[3]));
                        } catch (Exception e) {
                            MyLog.e(e);
                        }
                    }

                    if (canAdd) {
                        mLikeIdSet.add(object);
                    }
                    break;
                }
                case BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR: {
                    String object = msg.getSender() + "_" + BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR;
                    canAdd = !mLikeIdSet.contains(object);
                    if (canAdd) {
                        mLikeIdSet.add(object);
                    }
                    break;
                }
                case BarrageMsgType.B_MSG_TYPE_ADD_SHOP: {
                    canAdd = true;
                    break;
                }
                case BarrageMsgType.B_MSG_TYPE_SHARE: {
                    if (mHideShareMsg) {
                        canAdd = false;
                    }
                    break;
                }
                case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT:
                    if (mHideGiftMsg || mHideLikeMsg) {
                        canAdd = false;
                    }
                    break;
                default:
                    //消息体为空的消息不产生弹幕
                    canAdd = !TextUtils.isEmpty(msg.getBody());
                    break;
            }
        }
        return canAdd;
    }

    public void clearAllCache() {
        if (setTopMsgList != null) {
            setTopMsgList.clear();
        }
        if (chatMsgList != null) {
            chatMsgList.clear();
        }
        if (mLikeIdSet != null) {
            mLikeIdSet.clear();
        }
        BarrageMessageManager.mSendingMsgCache.clear();
    }

    // TODO
    // 写的太糙了 要优化
    public boolean checkMoreThanMaxCountByTime(long timestamp, int timeInterval, int maxCount) {
        int result = 0;
        List<CommentModel> barrageMsgs = getMsgList();
        if (barrageMsgs != null && barrageMsgs.size() > 0) {
            for (int i = barrageMsgs.size() - 1; i >= 0; i--) {
                if (timestamp - barrageMsgs.get(i).getSentTime() <= timeInterval) {
                    result++;
                    if (result >= maxCount) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return result >= maxCount;
    }

    public long getLastBarrageMsgSentTime() {
        long time = 0;
        CommentModel last = chatMsgList.getLastRough();
        if (last != null) {
            time = last.getSentTime();
        }
        return time;
    }

    public void updateMaxSize(int maxSize) {
        chatMsgList.updateMaxSize(maxSize);
    }

    public void cleanMsgData() {
        chatMsgList.clear();
        setTopMsgList.clear();

    }

}
