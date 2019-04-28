package com.module.playways.voice.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.utils.U;
import com.common.view.ex.ExLinearLayout;
import com.engine.EngineEvent;
import com.engine.UserStatus;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;


public class VoiceUserStatusContainerView extends RelativeLayout {

    ExLinearLayout mUserStatusContainer;
    RankRoomData mRoomData;
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
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        mUiHanlder.removeCallbacksAndMessages(null);
    }

    public void setRoomData(RankRoomData roomData) {
        mRoomData = roomData;
        bindData();
    }

    private void bindData() {
        for (RankPlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
            if (playerInfoModel.isAI()) {
                continue;
            }
            VoiceUserStatusView voiceUserStatusView = new VoiceUserStatusView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, U.getDisplayUtils().dip2px(150));
            lp.weight = 1;
            mUserStatusContainer.addView(voiceUserStatusView, lp);
            voiceUserStatusView.bindData(playerInfoModel);
            mViewMap.put(playerInfoModel.getUserInfo().getUserId(), voiceUserStatusView);
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
                        voiceUserStatusView.userSpeak(u.getVolume());
                    }
                }
                break;
            }
        }
    }
}
