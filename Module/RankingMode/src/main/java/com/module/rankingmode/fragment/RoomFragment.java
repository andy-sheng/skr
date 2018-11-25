package com.module.rankingmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.engine.AgoraEngineAdapter;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.rankingmode.R;

import java.util.List;

public class RoomFragment extends BaseFragment {
    public final static String TAG = "RoomFragment";

    RelativeLayout mSelfContainer;
    RelativeLayout mOtherContainer;
    ExTextView mJoinRoomBtn;

    String ROOM_ID = "chengsimin";

    @Override
    public int initView() {
        return R.layout.room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSelfContainer = (RelativeLayout) mRootView.findViewById(R.id.self_container);
        mOtherContainer = (RelativeLayout) mRootView.findViewById(R.id.other_container);
        mJoinRoomBtn = mRootView.findViewById(R.id.join_room_btn);
        mJoinRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (U.getPermissionUtils().checkCamera(getActivity())) {
                    joinRoom();
                } else {
                    U.getPermissionUtils().requestCamera(new PermissionUtils.RequestPermission() {
                        @Override
                        public void onRequestPermissionSuccess() {
                            joinRoom();
                        }

                        @Override
                        public void onRequestPermissionFailure(List<String> permissions) {

                        }

                        @Override
                        public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                        }
                    }, getActivity());
                }
            }
        });

        mRootView.findViewById(R.id.send_msg_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
                if (msgService != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("text","当前时间"+System.currentTimeMillis());
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
    }

    void joinRoom() {
        ModuleServiceManager.getInstance().getMsgService().joinChatRoom(ROOM_ID, new ICallback() {
            @Override
            public void onSucess(Object obj) {
                U.getToastUtil().showShort("加入房间成功");
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {

            }
        });
        AgoraEngineAdapter.getInstance().setCommunicationMode();
        AgoraEngineAdapter.getInstance().enableVideo();
        AgoraEngineAdapter.getInstance().setVideoEncoderConfiguration();
        AgoraEngineAdapter.getInstance().joinChannel(null, ROOM_ID, null, 0);
        AgoraEngineAdapter.getInstance().bindLocalVideoView(mOtherContainer);
        AgoraEngineAdapter.getInstance().bindRemoteVideo(mSelfContainer, 0);
    }

    @Override
    public void destroy() {
        super.destroy();
        AgoraEngineAdapter.getInstance().destroy();
        ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(ROOM_ID);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
