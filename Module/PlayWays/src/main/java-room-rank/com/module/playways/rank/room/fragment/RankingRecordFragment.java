package com.module.playways.rank.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.view.RecordItemView;
import com.module.playways.rank.room.view.RecordTitleView;
import com.module.rank.R;

import java.util.concurrent.TimeUnit;

public class RankingRecordFragment extends BaseFragment {
    ExImageView mIvTopImg;
    RecordItemView mRecordItemOne;
    RecordItemView mRecordItemTwo;
    RecordItemView mRecordItemThree;
    ExTextView mTvBack;
    ExTextView mTvAgain;

    RecordTitleView mRecordTitleView;

    RecordData mRecordData;

    RoomData mRoomData;
    @Override
    public int initView() {
        return R.layout.ranking_record_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvTopImg = (ExImageView) mRootView.findViewById(R.id.iv_top_img);
        mRecordItemOne = (RecordItemView) mRootView.findViewById(R.id.record_item_one);
        mRecordItemTwo = (RecordItemView) mRootView.findViewById(R.id.record_item_two);
        mRecordItemThree = (RecordItemView) mRootView.findViewById(R.id.record_item_three);
        mTvBack = (ExTextView) mRootView.findViewById(R.id.tv_back);
        mTvAgain = (ExTextView) mRootView.findViewById(R.id.tv_again);
        mRecordTitleView = (RecordTitleView)mRootView.findViewById(R.id.record_title_view);

        RxView.clicks(mTvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    getActivity().finish();
                });

        RxView.clicks(mTvAgain)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    getActivity().finish();
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKINGMODE)
                            .withInt("key_game_type", mRoomData.getGameType())
                            .withBoolean("selectSong", true)
                            .navigation();
                });

        try {
            mRecordTitleView.setData(mRecordData, mRoomData);
            mRecordItemOne.setData(mRoomData, mRecordData.mVoteInfoModels.get(0));
            mRecordItemTwo.setData(mRoomData, mRecordData.mVoteInfoModels.get(1));
            mRecordItemThree.setData(mRoomData, mRecordData.mVoteInfoModels.get(2));
        }catch (Exception e){
            MyLog.e(TAG, e);
        }
    }

    @Override
    protected boolean onBackPressed() {
        getActivity().finish();
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if(type == 0){
            mRecordData = (RecordData) data;
        }else if(type == 1){
            mRoomData = (RoomData) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

}
