package com.module.playways.voice.presenter;

import android.os.Handler;
import android.os.Message;

import com.changba.songstudio.audioeffect.AudioEffectStyleEnum;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.module.ModuleServiceManager;
import com.module.playways.BaseRoomData;
import com.module.playways.rank.msg.filter.PushMsgFilter;
import com.module.playways.rank.msg.manager.ChatRoomMsgManager;
import com.module.playways.voice.inter.IVoiceView;
import com.zq.live.proto.Room.RoomMsg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

public class VoiceCorePresenter extends RxLifeCyclePresenter {
    public String TAG = "VoiceCorePresenter";

    BaseRoomData mRoomData;

    IVoiceView mIVoiceView;

    boolean mDestroyed = false;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }
        }
    };

    PushMsgFilter mPushMsgFilter = new PushMsgFilter() {
        @Override
        public boolean doFilter(RoomMsg msg) {
            if (msg != null && msg.getRoomID() == mRoomData.getGameId()) {
                return true;
            }
            return false;
        }
    };

    public VoiceCorePresenter(@NotNull IVoiceView iVoiceView, @NotNull BaseRoomData roomData) {
        mIVoiceView = iVoiceView;
        mRoomData = roomData;
        TAG = "VoiceCorePresenter";
        if (mRoomData.getGameId() > 0) {
            Params params = Params.getFromPref();
            params.setScene(Params.Scene.voice);
            params.setStyleEnum(AudioEffectStyleEnum.ORIGINAL);
            params.setSelfUid((int) MyUserInfoManager.getInstance().getUid());
            EngineManager.getInstance().init("voiceroom", params);
            EngineManager.getInstance().joinRoom(mRoomData.getGameId() + "_chat", (int) UserAccountManager.getInstance().getUuidAsLong(), true);
            EngineManager.getInstance().muteLocalAudioStream(true);
        }
        if (mRoomData.getGameId() > 0) {
//            for (PlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
//                BasePushInfo basePushInfo = new BasePushInfo();
//                basePushInfo.setRoomID(mRoomData.getGameId());
//                basePushInfo.setSender(new UserInfo.Builder()
//                        .setUserID(playerInfoModel.getUserInfo().getUserId())
//                        .setAvatar(playerInfoModel.getUserInfo().getAvatar())
//                        .setNickName(playerInfoModel.getUserInfo().getNickname())
//                        .setSex(ESex.fromValue(playerInfoModel.getUserInfo().getSex()))
//                        .build());
//                String text = String.format("加入房间", playerInfoModel.getUserInfo().getNickname());
//                CommentMsgEvent msgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
//                EventBus.getDefault().post(msgEvent);
//            }
//
//            BasePushInfo basePushInfo = new BasePushInfo();
//            basePushInfo.setRoomID(mRoomData.getGameId());
//            basePushInfo.setSender(new UserInfo.Builder()
//                    .setUserID(1)
//                    .setAvatar("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/system_default.png")
//                    .setNickName("系统消息")
//                    .setSex(ESex.fromValue(0))
//                    .build());
//            String text = "撕哥一声吼：请文明参赛，发现坏蛋请用力举报！";
//            CommentMsgEvent msgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
//            EventBus.getDefault().post(msgEvent);
//            IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
//            if (msgService != null) {
//                msgService.syncHistoryFromChatRoom(String.valueOf(mRoomData.getGameId()), 10, true, null);
//            }
            ChatRoomMsgManager.getInstance().addFilter(mPushMsgFilter);
        }
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void destroy() {
        MyLog.d(TAG, "destroy begin");
        super.destroy();
        mDestroyed = true;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EngineManager.getInstance().destroy("voiceroom");
        mUiHanlder.removeCallbacksAndMessages(null);
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(String.valueOf(mRoomData.getGameId()));
        MyLog.d(TAG, "destroy over");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        switch (event.getType()) {
            case EngineEvent.TYPE_USER_LEAVE: {
                // 用户离开
                break;
            }
            case EngineEvent.TYPE_USER_MUTE_AUDIO: {
                //用户闭麦，开麦
                break;
            }
            case EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION: {
                // 有人在说话
                break;
            }
        }
    }
}
