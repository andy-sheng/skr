package com.module.home.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;

public class UserInfoTileView2 extends RelativeLayout {

    RelativeLayout mContentArea;
    SimpleDraweeView mIvUserIcon;
    ImageView mLevelBg;
    ExTextView mUserLevelTv;
    ExTextView mNameTv;
    ExTextView mRankInfo;
    
    public UserInfoTileView2(Context context) {
        super(context);
        init();
    }

    public UserInfoTileView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UserInfoTileView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.user_info_title2_layout, this);

        mContentArea = (RelativeLayout)this.findViewById(R.id.content_area);
        mIvUserIcon = (SimpleDraweeView)this.findViewById(R.id.iv_user_icon);
        mLevelBg = (ImageView)this.findViewById(R.id.level_bg);
        mUserLevelTv = (ExTextView)this.findViewById(R.id.user_level_tv);
        mNameTv = (ExTextView)this.findViewById(R.id.name_tv);
        mRankInfo = (ExTextView)this.findViewById(R.id.rank_info);
    }
}
