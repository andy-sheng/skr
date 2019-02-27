package com.module.playways.voice.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.view.ex.ExLinearLayout;
import com.engine.EngineEvent;
import com.engine.UserStatus;
import com.module.playways.BaseRoomData;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.room.model.RankRoundInfoModel;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;


public class VoiceUserStatusContainerView extends RelativeLayout {

    ExLinearLayout mUserStatusContainer;
    BaseRoomData<RankRoundInfoModel> mRoomData;
    HashMap<Integer, VoiceUserStatusView> mViewMap = new HashMap<>();
    Handler mUiHanlder = new Handler();

    public VoiceUserStatusContainerView(Context context) {
        super(context);
        init();
    }

    public VoiceUserStatusContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceUserStatusContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.voice_user_status_view_container, this);
        mUserStatusContainer = (ExLinearLayout) this.findViewById(R.id.user_status_container);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        mUiHanlder.removeCallbacksAndMessages(null);
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
        bindData();
    }

    private void bindData() {
        for (PlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
            if (playerInfoModel.isAI()) {
                continue;
            }
            VoiceUserStatusView voiceUserStatusView = new VoiceUserStatusView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            mUserStatusContainer.addView(voiceUserStatusView, lp);
            voiceUserStatusView.bindData(playerInfoModel.getUserInfo());
            mViewMap.put(playerInfoModel.getUserInfo().getUserId(), voiceUserStatusView);
            if (playerInfoModel.isSkrer()) {
                // 是机器人
                mUiHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        voiceUserStatusView.userOffline();
                    }
                }, (long) (Math.random() * 3000) + 1000);
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        switch (event.getType()) {
            case EngineEvent.TYPE_USER_LEAVE: {
                // 用户离开
                UserStatus userStatus = event.getUserStatus();
                int userId = userStatus.getUserId();
                VoiceUserStatusView voiceUserStatusView = mViewMap.get(userId);
                if (voiceUserStatusView != null) {
                    voiceUserStatusView.userOffline();
                }
                break;
            }
            case EngineEvent.TYPE_USER_MUTE_AUDIO: {
                //用户闭麦，开麦
                UserStatus userStatus = event.getUserStatus();
                int userId = userStatus.getUserId();
                VoiceUserStatusView voiceUserStatusView = mViewMap.get(userId);
                if (voiceUserStatusView != null) {
                    voiceUserStatusView.userMute(userStatus.isAudioMute());
                }
                break;
            }
            case EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION: {
                // 有人在说话
                List<EngineEvent.UserVolumeInfo> l = event.getObj();
                for (EngineEvent.UserVolumeInfo u : l) {
                    int uid = u.getUid();
                    if (uid == 0) {
                        uid = (int) UserAccountManager.getInstance().getUuidAsLong();
                    }
                    VoiceUserStatusView voiceUserStatusView = mViewMap.get(uid);
                    if (voiceUserStatusView != null) {
                        voiceUserStatusView.userSpeak();
                    }
                }
                break;
            }
        }
    }
}
