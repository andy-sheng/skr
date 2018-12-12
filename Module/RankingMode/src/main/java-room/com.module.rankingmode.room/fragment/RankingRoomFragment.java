package com.module.rankingmode.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.module.rankingmode.R;
import com.module.rankingmode.room.comment.CommentView;
import com.module.rankingmode.room.view.BottomContainerView;
import com.module.rankingmode.room.view.InputContainerView;
import com.module.rankingmode.room.view.TopContainerView;

public class RankingRoomFragment extends BaseFragment {

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    TopContainerView mTopContainerView;

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
    }

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
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
    protected boolean onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
            return true;
        }
        return super.onBackPressed();
    }
}
