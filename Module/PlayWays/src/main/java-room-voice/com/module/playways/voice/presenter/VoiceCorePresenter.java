package com.module.playways.voice.presenter;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.UserStatus;
import com.module.ModuleServiceManager;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.manager.ChatRoomMsgManager;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.room.RankRoomServerApi;
import com.module.playways.room.room.comment.model.CommentModel;
import com.module.playways.room.room.comment.model.CommentTextModel;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.voice.inter.IVoiceView;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Room.RoomMsg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class VoiceCorePresenter extends RxLifeCyclePresenter {
    public String TAG = "VoiceCorePresenter";

    RankRoomData mRoomData;

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

    public VoiceCorePresenter(@NotNull IVoiceView iVoiceView, @NotNull RankRoomData roomData) {
        mIVoiceView = iVoiceView;
        mRoomData = roomData;
        TAG = "VoiceCorePresenter";
        if (mRoomData.getGameId() > 0) {
            Params params = Params.getFromPref();
            params.setScene(Params.Scene.voice);
            params.setStyleEnum(Params.AudioEffect.none);
            params.setSelfUid((int) MyUserInfoManager.getInstance().getUid());
            EngineManager.getInstance().init("voiceroom", params);
            EngineManager.getInstance().joinRoom(mRoomData.getGameId() + "_chat", (int) UserAccountManager.getInstance().getUuidAsLong(), true,null);
            EngineManager.getInstance().muteLocalAudioStream(true);
        }
        if (mRoomData.getGameId() > 0) {
            for (RankPlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
                if (playerInfoModel.isSkrer() && playerInfoModel.isOnline()) {
                    // 是机器人
                    mUiHanlder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playerInfoModel.setOnline(false);
                            EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_USER_LEAVE);
                            UserStatus userStatus = new UserStatus(playerInfoModel.getUserID());
                            engineEvent.setUserStatus(userStatus);
                            EventBus.getDefault().post(engineEvent);
                        }
                    }, (long) (Math.random() * 3000) + 1000);
                }
            }

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

    /**
     * 退出游戏
     */
    public void exitGame() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(ApiManager.getInstance().createService(RankRoomServerApi.class).exitGame(body), null);
    }

    @Override
    public void destroy() {
        MyLog.d(TAG, "destroy begin");
        super.destroy();
        mDestroyed = true;
        EventBus.getDefault().unregister(this);
        EngineManager.getInstance().destroy("voiceroom");
        mUiHanlder.removeCallbacksAndMessages(null);
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        exitGame();
        ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(String.valueOf(mRoomData.getGameId()));
        MyLog.d(TAG, "destroy over");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        switch (event.getType()) {
            case EngineEvent.TYPE_USER_LEAVE: {
                // 用户离开
                UserStatus userStatus = event.getUserStatus();
                int userId = userStatus.getUserId();
                pretentLeaveMsg(userId);
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

    public void pretentLeaveMsg(int userId) {
        UserInfoModel userInfo = mRoomData.getUserInfo(userId);
        if (userInfo != null) {
            CommentTextModel commentModel = new CommentTextModel();
            commentModel.setUserId(userInfo.getUserId());
            commentModel.setAvatar(userInfo.getAvatar());
            commentModel.setUserName(userInfo.getNickname());
            commentModel.setAvatarColor(Color.WHITE);
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(userInfo.getNickname() + " ").setForegroundColor(CommentModel.TEXT_GRAY)
                    .append("离开了语音房").setForegroundColor(CommentModel.TEXT_GRAY)
                    .create();
            commentModel.setStringBuilder(stringBuilder);
            EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
        }
    }
}
