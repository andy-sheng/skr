package com.module.rankingmode.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;

public class RecordItemView extends RelativeLayout {

    SimpleDraweeView mSdvSingerIcon;
    ExImageView mIvRanking;
    ExTextView mTvSingerName;
    ExImageView mIvLightOne;
    ExImageView mIvLightTwo;
    ExImageView mIvLightThree;

    SimpleDraweeView mIvLightOneIcon;
    SimpleDraweeView mIvLightOneTwo;
    SimpleDraweeView mIvLightOneThree;

    public RecordItemView(Context context) {
        super(context);
        init();
    }

    public RecordItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.record_athletics_item_layout, this);
        mSdvSingerIcon = (SimpleDraweeView)findViewById(R.id.sdv_singer_icon);
        mIvRanking = (ExImageView) findViewById(R.id.iv_ranking);
        mTvSingerName = (ExTextView) findViewById(R.id.tv_singer_name);
        mIvLightOne = (ExImageView) findViewById(R.id.iv_light_one);
        mIvLightTwo = (ExImageView) findViewById(R.id.iv_light_two);
        mIvLightThree = (ExImageView) findViewById(R.id.iv_light_three);

        AvatarUtils.loadAvatarByUrl(mSdvSingerIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());


        mIvLightOneIcon = (SimpleDraweeView)findViewById(R.id.iv_light_one_icon);
        mIvLightOneTwo = (SimpleDraweeView)findViewById(R.id.iv_light_one_two);
        mIvLightOneThree = (SimpleDraweeView)findViewById(R.id.iv_light_one_three);

        AvatarUtils.loadAvatarByUrl(mIvLightOneIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvLightOneTwo,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvLightOneThree,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());
    }

    public void setData(){

    }
}
