package com.module.playways.rank.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.RoomData;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.playways.rank.room.view.RankResultView;
import com.module.rank.R;
import com.zq.live.proto.Room.EWinType;

import java.util.concurrent.TimeUnit;

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
    ExImageView mShareIv;

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
        mShareIv = (ExImageView)mRootView.findViewById(R.id.share_iv);

        mResultExit.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(RankResultFragment.this);
            }
        });

        if (mRoomData.getRecordData().getSelfWinType() == EWinType.Win.getValue()) {
            mResultTop.setBackground(getResources().getDrawable(R.drawable.zhanji_top_win));
        } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Draw.getValue()) {
            mResultTop.setBackground(getResources().getDrawable(R.drawable.zhanji_top_draw));
        } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Lose.getValue()) {
            mResultTop.setBackground(getResources().getDrawable(R.drawable.zhanji_top_loss));
        }

        mFirstResult.bindData(mRoomData, mRoomData.getRecordData().getUserIdByRank(1), 1);
        mSecondResult.bindData(mRoomData, mRoomData.getRecordData().getUserIdByRank(2), 2);
        mThirdResult.bindData(mRoomData, mRoomData.getRecordData().getUserIdByRank(3), 3);

        RxView.clicks(mShareIv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    SharePanel sharePanel = new SharePanel(getActivity());
                    sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png");
                    sharePanel.show(ShareType.IMAGE_RUL);

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
