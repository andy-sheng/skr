package com.module.rankingmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.engine.AgoraEngineAdapter;
import com.module.rankingmode.R;

import java.util.List;

public class RoomFragment extends BaseFragment {
    public final static String TAG = "RoomFragment";

    RelativeLayout mSelfContainer;
    RelativeLayout mOtherContainer;
    ExTextView mJoinRoomBtn;

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
    }

    void joinRoom() {
        AgoraEngineAdapter.getInstance().setCommunicationMode();
        AgoraEngineAdapter.getInstance().enableVideo();
        AgoraEngineAdapter.getInstance().setVideoEncoderConfiguration();
        AgoraEngineAdapter.getInstance().joinChannel(null, "csm", null, 0);
        AgoraEngineAdapter.getInstance().bindLocalVideoView(mOtherContainer);
        AgoraEngineAdapter.getInstance().bindRemoteVideo(mSelfContainer, 0);
    }

    @Override
    public void destroy() {
        super.destroy();
        AgoraEngineAdapter.getInstance().destroy();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
