package com.module.playways.grab.room.top;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.module.rank.R;

public class GrabTopItemView extends RelativeLayout {
    public static final int MODE_GRAB = 1;
    public static final int MODE_SING = 2;

    ExRelativeLayout mAvatarContainer;
    BaseImageView mAvatarIv;
    public ExImageView mFlagIv;


    int mMode = MODE_GRAB;

    public GrabTopItemView(Context context) {
        super(context);
        init();
    }

    public GrabTopItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabTopItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_top_view_holder_layout, this);
        mAvatarContainer = (ExRelativeLayout) this.findViewById(R.id.avatar_container);
        mAvatarIv = (BaseImageView) this.findViewById(R.id.avatar_iv);
        mFlagIv = (ExImageView) this.findViewById(R.id.flag_iv);
    }

    public void tryAddParent(LinearLayout grabTopRv) {
        if (this.getParent() == null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            grabTopRv.addView(this, lp);
        }
    }

    public void bindData(UserInfoModel userInfoModel) {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                .setCircle(true)
                .setBorderColorBySex(userInfoModel.getSex() == 1)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .build()
        );
    }

    public void reset(){
        mFlagIv.setVisibility(GONE);
        mAvatarContainer.setBackground(null);
    }

    public void setGrap(boolean grap) {
        if (grap) {
            mFlagIv.setVisibility(VISIBLE);
            mFlagIv.setImageResource(R.drawable.xiangchang_flag);
        } else {
            mFlagIv.setVisibility(GONE);
        }
    }

    public void setLight(boolean on) {
        mFlagIv.setVisibility(VISIBLE);
        if (on) {
            mFlagIv.setImageResource(R.drawable.liangdeng);
        } else {
            mFlagIv.setImageResource(R.drawable.miedeng);
        }
    }

    public void setGetSingChance() {
        mAvatarContainer.setBackgroundResource(R.drawable.grab_winner_avatar_bg);
    }
}
