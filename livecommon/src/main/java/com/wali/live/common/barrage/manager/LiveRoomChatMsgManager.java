package com.wali.live.common.barrage.manager;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.push.collection.CommentCollection;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.repository.model.GroupMemType;
import com.mi.live.data.room.model.FansPrivilegeModel;
import com.mi.live.data.user.User;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.common.model.CommentModel;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.event.EventClass;
import com.wali.live.proto.LiveMessageProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.mi.live.data.push.model.BarrageMsg.INNER_GLOBAL_VFAN;


/**
 * @module com.wali.live.video.widget
 * <p/>
 * Created by MK on 16/2/25.
 * <p/>
 * 直播或者观看直播时，房间聊天的消息管理
 */
public class LiveRoomChatMsgManager {
    private static final String TAG = "LiveRoomChatMsgManager";

    /**
     * 游戏直播默认的消息屏蔽值
     */
    public static final boolean DEFAULT_FORBID_GIFT = false;
    public static final boolean DEFAULT_FORBID_SYS = false;
    public static final boolean DEFAULT_FORBID_CHAT = false;

    protected CommentCollection<CommentModel> chatMsgList; //房间聊天消息的列表
    protected MsgAcceptFilter msgTypeFilter; //消息过滤器
    protected HashSet<String> mLikeIdSet = new HashSet<>(); //房间部分弹幕去重列表
    List<Long> rankTops = new ArrayList<>();

    private boolean mHideLikeMsg = false;
    private boolean mHideGiftMsg = false;
    private boolean mHideShareMsg = false;
    private int initMaxSize = CommentCollection.DEFAULT_MAX_SIZE;

    /**
     * 以下两个参数，目前只在游戏直播有效
     * mHideSysMsg
     * [暂无]屏蔽的内容包括系统消息：RoomSystemMsgPresenter；
     * 观众消息：RoomViewerPresenter的Join 和 Leave消息
     */
    private boolean mHideSysMsg = false;
    private boolean mHideChatMsg = false;

    private volatile boolean mIsGameLiveMode = false;

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
        chatMsgList = new CommentCollection<CommentModel>(maxChatMsgSize); //房间聊天消息的列表
        msgTypeFilter = filter;
        initMaxSize = maxChatMsgSize;
    }

    // isInBound 是否是收到的
    public boolean addChatMsg(BarrageMsg msg, boolean isInBound) {
        if ((msgTypeFilter != null && !msgTypeFilter.isAcceptMsg(msg)) || !canAddToChatMsgManager(msg)) {
            return false;
        }
        CommentModel commentModel = CommentModel.loadFromBarrage(msg, mIsGameLiveMode);
//        chatMsgList.insert(commentModel);
        newInsert(commentModel);
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
                    CommentModel commentModel = CommentModel.loadFromBarrage(barrageMsg, mIsGameLiveMode);
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
                    replaceEnterAndLike(CommentModel.loadFromBarrage(barrageMsg, mIsGameLiveMode));
                }
            }
            EventBus.getDefault().post(new CommentRefreshEvent(getMsgList(), !isInBound, this.toString()));
        }
    }

    private long mLastEnterTime = -1;

    //新增进入房间离开房间l的消息刷屏限制
    @WorkerThread
    private void newInsert(CommentModel commentModel) {
        if (commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN
                || commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_LEAVE) {
            long time = System.currentTimeMillis();
            if (mLastEnterTime > 0 && time - mLastEnterTime < 3_000) {// 3秒内连续进入和离开房间的弹幕被聚合
                mLastEnterTime = time;
                chatMsgList.replaceTail(commentModel);
                return;
            }
            mLastEnterTime = time;
        } else {
            mLastEnterTime = -1;
        }

        //进入房间 点亮 二期逻辑
        replaceEnterAndLike(commentModel);

//        EventBus.getDefault().post(new EventClass.SmartBarrageEvent(commentModel));
    }

    private void replaceEnterAndLike(CommentModel commentModel) {
        MyLog.w(TAG, "replaceEnterAndLike");
        if (chatMsgList.getDatasource().size() > 0) {
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
        return chatMsgList.getDatasource();
    }

    public void clear() {
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
    public void sendBarrageMessageAsync(String msgBody, int msgType, String roomid, long anchorId, BarrageMsg.PkMessageExt pkExt, BarrageMsg.MsgExt ext, BarrageMsg.GlobalRoomMessageExt globalRoomMessageExt) {
        MyLog.d(TAG, "sendBarrageMessageAsync");
        if (MyUserInfoManager.getInstance().getLevel() == 0) {
            MyLog.d(TAG, "user level is zero, vipLevel:" + MyUserInfoManager.getInstance().getVipLevel());
            MyUserInfoManager.getInstance().syncSelfDetailInfo();//TODO 请求使用LiveSyncManager.getInstance().asyncOwnUserInfo();
        }
        if (!TextUtils.isEmpty(msgBody) && !TextUtils.isEmpty(roomid)) {
            String globalMes = SmileyParser.getInstance()
                    .convertString(msgBody, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();
            BarrageMsg msg = getBarrageMsg(msgType, roomid, anchorId, pkExt, ext, globalRoomMessageExt, globalMes, MyUserInfoManager.getInstance().getVipLevel(), MyUserInfoManager.getInstance().isVipFrozen());

            BarrageMessageManager.getInstance().sendBarrageMessageAsync(msg, true);
            //假装是个push过去
            BarrageMessageManager.getInstance().pretendPushBarrage(msg);
//            addChatMsg(msg, false);
        }
    }

    @NonNull
    public BarrageMsg getBarrageMsg(int msgType, String roomid, long anchorId, BarrageMsg.PkMessageExt pkExt,
                                    BarrageMsg.MsgExt ext, BarrageMsg.GlobalRoomMessageExt globalRoomMsgExt,
                                    String globalMes, int vipLevel, boolean isVipForzen) {
        BarrageMsg msg = new BarrageMsg();
        msg.setMsgType(msgType);
        msg.setSender(UserAccountManager.getInstance().getUuidAsLong());
        String nickname = MyUserInfoManager.getInstance().getNickname();
        if (nickname == null) {
            nickname = String.valueOf(UserAccountManager.getInstance().getUuidAsLong());
        }
        msg.setNobleLevel(MyUserInfoManager.getInstance().getNobleLevel());
        msg.setVipFrozen(isVipForzen);
        msg.setVipLevel(vipLevel);
        msg.setSenderName(nickname);
        msg.setSenderLevel(MyUserInfoManager.getInstance().getLevel());
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
            msg.setGlobalRoomMessageExt(globalRoomMsgExt);
        }

        msg.setRedName(MyUserInfoManager.getInstance().isRedName());
        msg.appendCommonInfo();
        return msg;
    }

    public void sendTextBarrageMessageAsync(String body, String liveId, long anchorId, BarrageMsg.PkMessageExt pkExt) {
        sendBarrageMessageAsync(body, BarrageMsgType.B_MSG_TYPE_TEXT, liveId, anchorId, pkExt, null, null);
    }


    //管理员飘屏或付费飘屏
    public void sendHuyaBarrageMessageAsync(String body,int msgType, String roomid,long anchorId, long huyaAnchorId, BarrageMsg.PkMessageExt pkExt, BarrageMsg.MsgExt ext, int roomType, BarrageMsg.GlobalRoomMessageExt globalRoomMsgExt, int source) {
        MyLog.d(TAG, "sendHuyaBarrageMessageAsync");


        LiveMessageProto.HuyaSendMessageReq.Builder huyaSendMessageReq = LiveMessageProto.HuyaSendMessageReq.newBuilder()
                .setMsgContent(body)
                .setFromUid(MyUserInfoManager.getInstance().getUuid())
                .setAnchorHuyaUid(huyaAnchorId)
                .setSendTimestamp(System.currentTimeMillis())
                .setZuid(anchorId)
                .setRoomId(roomid)
                .setRoomType(roomType)
                .setSource(source)
                .setMsgType(1);

        BarrageMessageManager.getInstance().sendHuYaBarrageMessage(huyaSendMessageReq.build());

        if (!TextUtils.isEmpty(body) && !TextUtils.isEmpty(roomid)) {
            String globalMes = SmileyParser.getInstance()
                    .convertString(body, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();

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
                msg.setGlobalRoomMessageExt(globalRoomMsgExt);
            }
            msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
            //假装是个push过去
            BarrageMessageManager.getInstance().pretendPushBarrage(msg);
//            addChatMsg(msg, false);
        }
    }

    public void sendFlyBarrageMessageAsync(String body, String liveId, long anchorId, int flyBarrageType,
                                           BarrageMsg.PkMessageExt pkExt, FansPrivilegeModel vfansPrivilegeModel) {
        BarrageMsg.GlobalRoomMessageExt globalRoomMessageExt = getGlobalRoomMessageExt(flyBarrageType);

        if (vfansPrivilegeModel != null && vfansPrivilegeModel.getMemType() != GroupMemType.GROUP_MEM_TYPE_NONE) {

            BarrageMsg.InnerGlobalRoomMessageExt ext = new BarrageMsg.InnerGlobalRoomMessageExt();
            ext.setType(INNER_GLOBAL_VFAN);
            BarrageMsg.VFansMemberBriefInfo vFansMemberBriefInfo = new BarrageMsg.VFansMemberBriefInfo();
            vFansMemberBriefInfo.setPetLevel(vfansPrivilegeModel.getPetLevel());
            vFansMemberBriefInfo.setVipExpire(System.currentTimeMillis() > vfansPrivilegeModel.getExpireTime() * 1000);
            vFansMemberBriefInfo.setMedalValue(vfansPrivilegeModel.getMedal());

            ext.setvFansMemberBriefInfo(vFansMemberBriefInfo);
            globalRoomMessageExt.getInnerGlobalRoomMessageExtList().add(ext);
        }

        BarrageMsg.GiftMsgExt msgExt = new BarrageMsg.GiftMsgExt();
        msgExt.msgBody = SmileyParser.getInstance().convertString(body, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();
        MyLog.w(TAG, "sendFlyBarrageMessageAsync 2");

        sendBarrageMessageAsync(body, BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE, liveId, anchorId, pkExt, msgExt, globalRoomMessageExt);
    }


    public void sendLikeBarrageMessageAsync(String liveId, long anchorId, int heartViewColorId, String heartViewBitmapPath) {
        BarrageMsg.LikeMsgExt msgExt = new BarrageMsg.LikeMsgExt();
        msgExt.id = heartViewColorId;
        msgExt.bitmapPath = heartViewBitmapPath;
        sendBarrageMessageAsync(GlobalData.app().getString(R.string.live_start_light), BarrageMsgType.B_MSG_TYPE_LIKE, liveId, anchorId, null, msgExt, null);
    }

    @NonNull
    public BarrageMsg.GlobalRoomMessageExt getGlobalRoomMessageExt(int flyBarrageType) {
        BarrageMsg.GlobalRoomMessageExt globalRoomMessageExt = new BarrageMsg.GlobalRoomMessageExt();
        BarrageMsg.InnerGlobalRoomMessageExt innerGlobalRoomMessageExt = new BarrageMsg.InnerGlobalRoomMessageExt();
        innerGlobalRoomMessageExt.setType(flyBarrageType);
        ArrayList<BarrageMsg.InnerGlobalRoomMessageExt> innerGlobalRoomMessageExtList = new ArrayList<>();
        innerGlobalRoomMessageExtList.add(innerGlobalRoomMessageExt);
        globalRoomMessageExt.setInnerGlobalRoomMessageExtList(innerGlobalRoomMessageExtList);
        return globalRoomMessageExt;
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
                    // vip隐身
                    if (hideEnabled(msg)) {
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

    private Boolean hideEnabled(BarrageMsg msg) {
        if (msg != null) {
            boolean vipHide = msg.getVipLevel() > 0 && !msg.isVipFrozen() && msg.isVipHide();
            boolean nobleHide = msg.getNobleLevel() >= User.NOBLE_LEVEL_FOURTH && msg.isVipHide();
            return (vipHide || nobleHide);
        }
        return false;
    }

    public void clearAllCache() {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                if (chatMsgList != null) {
                    chatMsgList.clear();
                }
                if (mLikeIdSet != null) {
                    mLikeIdSet.clear();
                }
                BarrageMessageManager.mSendingMsgCache.clear();
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).subscribe();
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

    public int getInitMaxSize() {
        return initMaxSize;
    }

    public void cleanMsgData() {
        chatMsgList.clear();
    }

}
