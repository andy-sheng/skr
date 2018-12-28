package com.module.playways.rank.room.fragment;

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
import com.module.rank.R;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.view.RecordItemView;

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

    RecordData mRecordData;

    RoomData mRoomData;
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

        return;
//        Observable.fromIterable(mRecordData.mVoteInfoModels)
//                .filter(new Predicate<VoteInfoModel>() {
//                    @Override
//                    public boolean test(VoteInfoModel voteInfoModel) throws Exception {
//                        return voteInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid();
//                    }
//                })
//                .subscribe(new Consumer<VoteInfoModel>() {
//            @Override
//            public void accept(VoteInfoModel voteInfoModel) throws Exception {
//                Drawable drawable = null;
//                String str = "";
//                switch (voteInfoModel.getRank()){
//                    case 1:
//                        drawable = getResources().getDrawable(R.drawable.ic_medal1_normal);
//                        str = "冠军";
//                        break;
//                    case 2:
//                        drawable = getResources().getDrawable(R.drawable.ic_medal2_normal);
//                        str = "亚军";
//                        break;
//                    case 3:
//                        drawable = getResources().getDrawable(R.drawable.ic_medal3_normal);
//                        str = "季军";
//                        break;
//                }
//                mIvOwnRecord.setBackground(drawable);
//
//                if(mTvOwnRecord != null){
//                    mTvOwnRecord.setText(str);
//                }
//            }
//        });
//
//        if(mTvOwnerName != null){
//            mTvOwnerName.setText(MyUserInfoManager.getInstance().getNickName());
//        }
//
//        try {
//            mRecordItemOne.setData(mRoomData, mRecordData.mVoteInfoModels.get(0));
//            mRecordItemOne.setData(mRoomData, mRecordData.mVoteInfoModels.get(1));
//            mRecordItemOne.setData(mRoomData, mRecordData.mVoteInfoModels.get(2));
//        }catch (Exception e){
//            MyLog.e(TAG, e);
//        }
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
