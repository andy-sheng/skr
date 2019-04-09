package com.module.playways.voice.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.component.busilib.manager.BgMusicManager;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.rank.room.RankRoomData;
import com.module.playways.rank.room.comment.listener.CommentItemListener;
import com.module.playways.rank.room.comment.CommentView;
import com.module.playways.rank.room.fragment.RankResultFragment;
import com.module.playways.rank.room.view.InputContainerView;
import com.module.playways.voice.inter.IVoiceView;
import com.module.playways.voice.presenter.VoiceCorePresenter;
import com.module.playways.voice.view.VoiceBottomContainerView;
import com.module.playways.voice.view.VoiceRightOpView;
import com.module.playways.voice.view.VoiceTopContainerView;
import com.module.playways.voice.view.VoiceUserStatusContainerView;
import com.module.rank.R;
import com.zq.dialog.PersonInfoDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VoiceRoomFragment extends BaseFragment implements IVoiceView {

    public final static String TAG = "GrabRoomFragment";

    RankRoomData mRoomData;

    RelativeLayout mRankingContainer;

    InputContainerView mInputContainerView;

    VoiceBottomContainerView mBottomContainerView;

    CommentView mCommentView;

    VoiceTopContainerView mTopContainerView;

    VoiceUserStatusContainerView mUserStatusContainerView;

    VoiceRightOpView mVoiceRightOpView;

    ExImageView mGameResultIv;

    VoiceCorePresenter mCorePresenter;

    PersonInfoDialog mPersonInfoDialog;

    boolean mIsGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }
        }
    };

    @Override
    public int initView() {
        return R.layout.voice_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 请保证从下面的view往上面的view开始初始化
        mRankingContainer = mRootView.findViewById(R.id.ranking_container);
        mRankingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputContainerView.hideSoftInput();
            }
        });
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();
        initUserStatusView();
        initOpView();
        initResultView();

        mCorePresenter = new VoiceCorePresenter(this, mRoomData);
        addPresent(mCorePresenter);

        BgMusicManager.getInstance().setRoom(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
        mBottomContainerView = (VoiceBottomContainerView) mRootView.findViewById(R.id.bottom_container_view);
        mBottomContainerView.setListener(new VoiceBottomContainerView.Listener() {
            @Override
            public void showInputBtnClick() {
                if (mPersonInfoDialog != null && mPersonInfoDialog.isShowing()) {
                    mPersonInfoDialog.dismiss();
                }
                mInputContainerView.showSoftInput();
            }
        });
        mBottomContainerView.setRoomData(mRoomData);
    }

    private void initCommentView() {
        mCommentView = mRootView.findViewById(R.id.comment_view);
        mCommentView.setListener(new CommentItemListener() {
            @Override
            public void clickAvatar(int userId) {
                showPersonInfoView(userId);
            }

            @Override
            public void clickAgreeKick(int userId, boolean isAgree) {

            }
        });
        mCommentView.setRoomData(mRoomData);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentView.getLayoutParams();
        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(430 + 60);
    }


    private void initTopView() {
        mTopContainerView = mRootView.findViewById(R.id.top_container_view);

        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());
        RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mTopContainerView.getLayoutParams();
        topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
//        mTopContainerView.setListener(new GrabTopContainerView.Listener() {
//            @Override
//            public void closeBtnClick() {
//                quitGame();
//            }
//
//            @Override
//            public void onVoiceChange(boolean voiceOpen) {
//                mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen, true);
//            }
//        });
    }

    private void initUserStatusView() {
        mUserStatusContainerView = mRootView.findViewById(R.id.user_status_container_view);
        mUserStatusContainerView.setRoomData(mRoomData);
    }

    private void initOpView() {
        mVoiceRightOpView = mRootView.findViewById(R.id.voice_right_op_view);
    }

    private void initResultView() {
        mGameResultIv = (ExImageView) mRootView.findViewById(R.id.game_result_iv);
        mGameResultIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), RankResultFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(1, mRoomData)
                        .build());
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowPersonCardEvent event) {
        if (event.getUid() != MyUserInfoManager.getInstance().getUid()) {
            showPersonInfoView(event.getUid());
        }
    }

    private void showPersonInfoView(int userID) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            return;
        }
        if (userID == 0) {
            return;
        }
        mInputContainerView.hideSoftInput();

        mPersonInfoDialog = new PersonInfoDialog(getActivity(), userID, true, false);
        mPersonInfoDialog.show();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (RankRoomData) data;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        MyLog.d(TAG, "destroy");
        if (mPersonInfoDialog != null && mPersonInfoDialog.isShowing()) {
            mPersonInfoDialog.dismiss();
            mPersonInfoDialog = null;
        }
        mUiHanlder.removeCallbacksAndMessages(null);

        mIsGameEndAniamtionShow = false;
        U.getSoundUtils().release(TAG);
        BgMusicManager.getInstance().setRoom(false);
    }

    @Override
    protected boolean onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
            return true;
        }
        return false;
    }


}
