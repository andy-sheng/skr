package com.module.rankingmode.room.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;
import com.module.rankingmode.room.view.RecordItemView;

public class RankingRecordFragment extends BaseFragment {
    ExImageView mIvTopImg;
    SimpleDraweeView mSdvOwnIcon;
    ExTextView mTvOwnerName;
    ExImageView mIvOwnRecord;
    ExTextView mTvOwnRecord;
    ExTextView mTvLightCount;
    RecordItemView mRecordItemOne;
    RecordItemView mRecordItemTwo;
    RecordItemView mRecordItemThree;
    ExTextView mTvBack;
    ExTextView mTvAgain;

    @Override
    public int initView() {
        return R.layout.ranking_record_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvTopImg = (ExImageView) mRootView.findViewById(R.id.iv_top_img);
        mSdvOwnIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_own_icon);
        mTvOwnerName = (ExTextView) mRootView.findViewById(R.id.tv_owner_name);
        mIvOwnRecord = (ExImageView) mRootView.findViewById(R.id.iv_own_record);
        mTvOwnRecord = (ExTextView) mRootView.findViewById(R.id.tv_own_record);
        mTvLightCount = (ExTextView) mRootView.findViewById(R.id.tv_light_count);
        mRecordItemOne = (RecordItemView) mRootView.findViewById(R.id.record_item_one);
        mRecordItemTwo = (RecordItemView) mRootView.findViewById(R.id.record_item_two);
        mRecordItemThree = (RecordItemView) mRootView.findViewById(R.id.record_item_three);
        mTvBack = (ExTextView) mRootView.findViewById(R.id.tv_back);
        mTvAgain = (ExTextView) mRootView.findViewById(R.id.tv_again);

        AvatarUtils.loadAvatarByUrl(mSdvOwnIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
