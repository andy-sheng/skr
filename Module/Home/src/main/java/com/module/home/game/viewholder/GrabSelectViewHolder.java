package com.module.home.game.viewholder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.friends.SpecialModel;
import com.module.home.R;

public class GrabSelectViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "GrabSelectViewHolder";

    RelativeLayout mBackground;
    ExTextView mSpecialTv;
    ExTextView mIntroductionTv;
    ExImageView mSpecialIv;

    SpecialModel mSpecialModel;
    int mPosition;

    public GrabSelectViewHolder(View itemView, RecyclerOnItemClickListener mItemClickListener) {
        super(itemView);

        mBackground = (RelativeLayout) itemView.findViewById(R.id.background);
        mSpecialTv = (ExTextView) itemView.findViewById(R.id.special_tv);
        mIntroductionTv = (ExTextView) itemView.findViewById(R.id.introduction_tv);
        mSpecialIv = (ExImageView) itemView.findViewById(R.id.special_iv);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClicked(v, mPosition, mSpecialModel);
                }
            }
        });
    }

    public void bindData(SpecialModel specialModel, int postion) {
        this.mSpecialModel = specialModel;
        this.mPosition = postion;
        if (postion % 3 == 0) {
            mSpecialIv.setBackground(U.getDrawable(R.drawable.grab_special_1));
        } else if (postion % 3 == 1) {
            mSpecialIv.setBackground(U.getDrawable(R.drawable.grab_special_2));
        } else if (postion % 3 == 2) {
            mSpecialIv.setBackground(U.getDrawable(R.drawable.grab_special_3));
        }
        if (!TextUtils.isEmpty(specialModel.getBgColor())) {
            mBackground.setBackground(getShapeDrawable(Color.parseColor(specialModel.getBgColor())));
        } else {
            mBackground.setBackground(getShapeDrawable(Color.parseColor("#68ABD3")));
        }
        mSpecialTv.setText(this.mSpecialModel.getTagName());
        mIntroductionTv.setText(this.mSpecialModel.getIntroduction());
    }

    public Drawable getShapeDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(U.getDisplayUtils().dip2px(10));
        drawable.setColor(color);
        return drawable;
    }
}
