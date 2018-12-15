package com.module.rankingmode.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.OnLineInfoModel;
import com.module.rankingmode.room.comment.CommentView;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.presenter.RankingCorePresenter;
import com.module.rankingmode.room.view.BottomContainerView;
import com.module.rankingmode.room.view.IGameRuleView;
import com.module.rankingmode.room.view.InputContainerView;
import com.module.rankingmode.room.view.TopContainerView;

import java.util.List;

public class RankingRoomFragment extends BaseFragment implements IGameRuleView {

    RoomData mRoomData;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    TopContainerView mTopContainerView;

    RankingCorePresenter presenter;

    ExTextView mTestTv;

    @Override
    public int initView() {
        return R.layout.ranking_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 请保证从下面的view往上面的view开始初始化
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();
        mTestTv = mRootView.findViewById(R.id.test_tv);
        presenter = new RankingCorePresenter(this, mRoomData);
        addPresent(presenter);
    }

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
        mBottomContainerView = (BottomContainerView) mRootView.findViewById(R.id.bottom_container_view);
        mBottomContainerView.setListener(new BottomContainerView.Listener() {
            @Override
            public void showInputBtnClick() {
                mInputContainerView.showSoftInput();
            }
        });
    }

    private void initCommentView() {
        mCommentView = mRootView.findViewById(R.id.comment_view);
    }

    private void initTopView() {
        mTopContainerView = mRootView.findViewById(R.id.top_container_view);
        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());
        RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mTopContainerView.getLayoutParams();
        topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        mTopContainerView.setListener(new TopContainerView.Listener() {
            @Override
            public void closeBtnClick() {
                getActivity().finish();
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mRoomData = (RoomData) data;
        }
    }

    @Override
    protected boolean onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void startSelfCountdown(Runnable countDownOver) {
        HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(3)
                .start(new HandlerTaskTimer.ObserverW() {
                           @Override
                           public void onNext(Integer integer) {
                               addText("你的演唱要开始了，倒计时" + (4 - integer));
                           }

                           @Override
                           public void onComplete() {
                               super.onComplete();
                               countDownOver.run();
                           }
                       }
                );
    }

    @Override
    public void startRivalCountdown(int uid) {
        addText("用户" + uid + "的演唱开始了");
    }

    @Override
    public void userExit() {

    }

    @Override
    public void gameFinish() {
        addText("游戏结束了");
    }

    @Override
    public void updateUserState(List<OnLineInfoModel> jsonOnLineInfoList) {
        if(jsonOnLineInfoList==null){
            return;
        }
        for (OnLineInfoModel onLineInfoModel : jsonOnLineInfoList) {
            if (!onLineInfoModel.isIsOnline()) {
                addText("用户" + onLineInfoModel.getUserID() + "处于离线状态");
            }
        }
    }

    @Override
    public void playLyric(int songId) {
        addText("开始播放歌词 songId=" + songId);
    }

    void addText(String te) {
        String a = mTestTv.getText() + "\n" + te;
        mTestTv.setText(a);
    }
}
