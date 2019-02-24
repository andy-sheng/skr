package com.module.playways.rank.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.module.playways.RoomData;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.playways.rank.room.view.RankResultView;
import com.module.rank.R;

/**
 * pk战绩页面
 */
public class RankResultFragment extends BaseFragment {

    ExRelativeLayout mResultArea;
    RankResultView mFirstResult;
    RankResultView mSecondResult;
    RankResultView mThirdResult;
    ExImageView mResultTop;
    ExImageView mResultExit;

    RoomData mRoomData;

    @Override
    public int initView() {
        return R.layout.rank_result_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mResultArea = (ExRelativeLayout) mRootView.findViewById(R.id.result_area);
        mFirstResult = (RankResultView) mRootView.findViewById(R.id.first_result);
        mSecondResult = (RankResultView) mRootView.findViewById(R.id.second_result);
        mThirdResult = (RankResultView) mRootView.findViewById(R.id.third_result);
        mResultTop = (ExImageView) mRootView.findViewById(R.id.result_top);
        mResultExit = (ExImageView) mRootView.findViewById(R.id.result_exit);

        mResultExit.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(RankResultFragment.this);
            }
        });
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 1) {
            mRoomData = (RoomData) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
