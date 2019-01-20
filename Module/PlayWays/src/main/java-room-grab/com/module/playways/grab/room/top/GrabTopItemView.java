package com.module.playways.grab.room.top;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.SpecialModel;
import com.module.rank.R;

public class GrabTopItemView extends RelativeLayout {
    public static final int MODE_GRAB = 1;
    public static final int MODE_SING = 2;

    BaseImageView mAvatarIv;
    ExTextView mDescTv;
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

        mAvatarIv = (BaseImageView) this.findViewById(R.id.avatar_iv);
        mDescTv = (ExTextView) this.findViewById(R.id.desc_tv);
        mDescTv.setText("未抢");
    }

    public void tryAddParent(GrabTopRv grabTopRv) {
        if (this.getParent() == null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
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

    public void setGrap(boolean grap){
        if(grap){
            mDescTv.setText("抢唱");
        }else{
            mDescTv.setText("未抢");
        }
    }

    public void setLight(boolean on){
        if(on){
            mDescTv.setText("亮灯");
        }else{
            mDescTv.setText("灭灯");
        }
    }
}
