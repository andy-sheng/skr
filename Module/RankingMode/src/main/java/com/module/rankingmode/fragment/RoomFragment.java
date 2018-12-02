package com.module.rankingmode.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.common.view.ex.ExTextView;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.agora.effect.EffectModel;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.rankingmode.R;
import com.module.rankingmode.view.AudioControlPanelView;
import com.module.rankingmode.view.VideoControlPanelView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class RoomFragment extends BaseFragment {
    public final static String TAG = "RoomFragment";

    LinearLayout mOthersContainer;
    ExButton mModeSwitchBtn;
    SurfaceView mCameraSurfaceView;
    ExTextView mInfoTextView;

    String ROOM_ID = "chengsimin";
    boolean useChangbaEngine = false;
    Handler mUiHandler = new Handler();

    @Override
    public int initView() {
        return R.layout.room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mOthersContainer = mRootView.findViewById(R.id.others_container);
        mModeSwitchBtn = mRootView.findViewById(R.id.mode_switch_btn);
        mRootView.findViewById(R.id.send_msg_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
                if (msgService != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("text", "当前时间" + System.currentTimeMillis());
                    msgService.sendChatRoomMessage(ROOM_ID, CustomMsgType.MSG_TYPE_TEXT, jsonObject, new ICallback() {
                        @Override
                        public void onSucess(Object obj) {
                            U.getToastUtil().showShort("聊天室弹幕发送成功");
                        }

                        @Override
                        public void onFailed(Object obj, int errcode, String message) {

                        }
                    });
                }
            }
        });

        mInfoTextView = mRootView.findViewById(R.id.info_text);

        mRootView.findViewById(R.id.capture_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EngineManager.getInstance().startRecord();
            }
        });

        mRootView.findViewById(R.id.show_audio_control_panel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogPlus.newDialog(getContext())
                        .setExpanded(true, U.getDisplayUtils().getScreenHeight() / 2)
                        .setContentHolder(new ViewHolder(new AudioControlPanelView(getContext())))
                        .setGravity(Gravity.BOTTOM)
                        .setCancelable(true)
                        .create().show();

            }
        });
        mRootView.findViewById(R.id.show_video_control_panel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogPlus.newDialog(getContext())
                        .setExpanded(true, U.getDisplayUtils().getScreenHeight() / 2)
                        .setContentHolder(new ViewHolder(new VideoControlPanelView(getContext())))
                        .setGravity(Gravity.BOTTOM)
                        .setCancelable(true)
                        .create().show();

            }
        });

        mModeSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useChangbaEngine = !useChangbaEngine;
                joinEngineRoom();
            }
        });

        if (U.getPermissionUtils().checkCamera(getActivity())) {
            joinRongRoom();
        } else {
            U.getPermissionUtils().requestCamera(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    joinRongRoom();
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {

                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                }
            }, getActivity());
        }
        joinEngineRoom();
    }

    void joinRongRoom() {
        // 加入融云房间
        ModuleServiceManager.getInstance().getMsgService().joinChatRoom(ROOM_ID, new ICallback() {
            @Override
            public void onSucess(Object obj) {
                U.getToastUtil().showShort("加入弹幕房间成功");
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {

            }
        });
    }

    void joinEngineRoom() {
        if (useChangbaEngine) {
            mModeSwitchBtn.setText("使用唱吧引擎：已开启");
        } else {
            mModeSwitchBtn.setText("使用唱吧引擎：已关闭");
        }
        EngineManager.getInstance().init(Params.newBuilder(Params.CHANNEL_TYPE_LIVE_BROADCASTING)
                .setUseCbEngine(useChangbaEngine)
                .setEnableVideo(true)
                .setEnableAudio(true)
                .build());
        // 不再次调用 join Agora 的preview 不生效,因为init时已经离开房间了
        EngineManager.getInstance().joinRoom(ROOM_ID, 0, true);

        if (useChangbaEngine) {
            recreateCameraView();
            // 确保view已经真正add进去了
            EngineManager.getInstance().startPreview(mCameraSurfaceView);
        } else {
            recreateCameraView();
            EngineManager.getInstance().startPreview(mCameraSurfaceView);
        }
    }

    private void recreateCameraView() {
        RelativeLayout container = (RelativeLayout) mRootView;
        if (mCameraSurfaceView != null) {
            ((RelativeLayout) mRootView).removeView(mCameraSurfaceView);
        }
        mCameraSurfaceView = new SurfaceView(getContext());
        container.addView(mCameraSurfaceView, 0
                , new RelativeLayout.LayoutParams(360, 640));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void destroy() {
        super.destroy();
        mUiHandler.removeCallbacksAndMessages(null);
        ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(ROOM_ID);
        EngineManager.getInstance().destroy();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_USER_JOIN) {
            addInfo(event.getUserStatus().getUserId() + "加入引擎房间");
        } else if (event.getType() == EngineEvent.TYPE_USER_LEAVE) {
            addInfo(event.getUserStatus().getUserId() + "离开引擎房间");
            if (event.getUserStatus().hasBindView()) {
                mOthersContainer.removeView(event.getUserStatus().getView());
            }
        } else if (event.getType() == EngineEvent.TYPE_FIRST_VIDEO_DECODED) {
            addInfo(event.getUserStatus().getUserId() + "首帧decode");
            if (!event.getUserStatus().isSelf()
                    && !event.getUserStatus().hasBindView()) {
                TextureView textureView = new TextureView(getContext());
                mOthersContainer.addView(textureView, 360, 640);
                EngineManager.getInstance().bindRemoteView(event.getUserStatus().getUserId(), textureView);
            }
        }else if(event.getType() == EngineEvent.TYPE_ENGINE_DESTROY){
            addInfo("reset引擎");
            mOthersContainer.removeAllViews();
        }
    }

    void addInfo(String info) {
        String aa = mInfoTextView.getText().toString();
        mInfoTextView.setText(aa + "\n" + info);
    }
}
