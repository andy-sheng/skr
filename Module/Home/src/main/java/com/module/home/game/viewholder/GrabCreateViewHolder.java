package com.module.home.game.viewholder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.shadow.ShadowConfig;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.home.R;

public class GrabCreateViewHolder extends RecyclerView.ViewHolder {

    ExRelativeLayout mBackground;
    ExTextView mSpecialTv;
    ExTextView mIntroductionTv;
    ExImageView mSpecialIv;

    int position;

    public GrabCreateViewHolder(View itemView, RecyclerOnItemClickListener mItemClickListener) {
        super(itemView);


        mBackground = (ExRelativeLayout) itemView.findViewById(R.id.background);
        mSpecialTv = (ExTextView) itemView.findViewById(R.id.special_tv);
        mIntroductionTv = (ExTextView) itemView.findViewById(R.id.introduction_tv);
        mSpecialIv = (ExImageView) itemView.findViewById(R.id.special_iv);

        int corner = U.getDisplayUtils().dip2px(10);
        mBackground.setShadowConfig(ShadowConfig.obtain()
                .color(Color.parseColor("#80A42DF3"))
                .leftTopCorner(corner)
                .rightTopCorner(corner)
                .leftBottomCorner(corner)
                .rightBottomCorner(corner)
                .xOffset(U.getDisplayUtils().dip2px(1))
                .yOffset(U.getDisplayUtils().dip2px(4))
                .radius(U.getDisplayUtils().dip2px(4))
        );

        itemView.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClicked(view, position, null);
                }
            }
        });
    }

    public void bindData(int position) {
        this.position = position;
    }
}
