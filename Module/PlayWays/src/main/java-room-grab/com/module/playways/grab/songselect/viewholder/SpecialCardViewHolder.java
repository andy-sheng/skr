package com.module.playways.grab.songselect.viewholder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.model.SpecialModel;
import com.module.rank.R;

public class SpecialCardViewHolder extends RecyclerView.ViewHolder {


    RelativeLayout mBackground;
    ExTextView mSpecialTv;
    ExTextView mIntroductionTv;
    ExImageView mSpecialIv;

    SpecialModel mSpecialModel;
    int mPosition;
    private RecyclerOnItemClickListener<SpecialModel> mItemClickListener;

    public SpecialCardViewHolder(View itemView) {
        super(itemView);

        mBackground = (RelativeLayout) itemView.findViewById(R.id.background);
        mSpecialTv = (ExTextView) itemView.findViewById(R.id.special_tv);
        mIntroductionTv = (ExTextView) itemView.findViewById(R.id.introduction_tv);
        mSpecialIv = (ExImageView) itemView.findViewById(R.id.special_iv);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public void setItemClickListener(RecyclerOnItemClickListener<SpecialModel> itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public RecyclerOnItemClickListener<SpecialModel> getItemClickListener() {
        return mItemClickListener;
    }

    public Drawable getShapeDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(U.getDisplayUtils().dip2px(10));
        drawable.setColor(color);
        return drawable;
    }
}
