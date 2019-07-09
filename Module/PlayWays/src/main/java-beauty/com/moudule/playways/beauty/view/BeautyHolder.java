package com.moudule.playways.beauty.view;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

import static com.moudule.playways.beauty.view.BeautyControlPanelView.TYPE_STICKER;

public class BeautyHolder extends RecyclerView.ViewHolder {

    ExImageView mSelectBgIv;
    TextView mItemTv;

    BeautyControlPanelView.BeautyViewModel mBeautyViewModel;
    int position;
    int mType;

    public BeautyHolder(View itemView, RecyclerOnItemClickListener<BeautyControlPanelView.BeautyViewModel> listener) {
        super(itemView);
        itemView.setTag(this);

        mSelectBgIv = (ExImageView) itemView.findViewById(R.id.select_bg_iv);
        mItemTv = (TextView) itemView.findViewById(R.id.item_tv);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.onItemClicked(itemView, position, mBeautyViewModel);
                }
            }
        });
    }

    public void bindData(BeautyControlPanelView.BeautyViewModel model, int position, boolean isSelected, int type) {
        this.mBeautyViewModel = model;
        this.position = position;
        this.mType = type;
        setIsSelected(isSelected);
    }

    public void setIsSelected(boolean isSelected) {
        if (mType == TYPE_STICKER) {
            mItemTv.setText("");
            mItemTv.setBackground(U.getDrawable(mBeautyViewModel.getResId()));
            if (isSelected) {
                mSelectBgIv.setVisibility(View.VISIBLE);
            } else {
                mSelectBgIv.setVisibility(View.GONE);
            }
        } else {
            mItemTv.setText(mBeautyViewModel.getType().getName());
            mSelectBgIv.setVisibility(View.GONE);
            if (isSelected) {
                mItemTv.setTextColor(Color.WHITE);
                mItemTv.setBackground(getSelectedDrawable(mBeautyViewModel.getColor()));
            } else {
                mItemTv.setTextColor(Color.parseColor(mBeautyViewModel.getColor()));
                mItemTv.setBackground(getUnSelectedDrawable(mBeautyViewModel.getColor()));
            }
        }
    }

    Drawable getSelectedDrawable(String color) {
        return new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor(color))
                .setCornersRadius(U.getDisplayUtils().dip2px(20f))
                .build();
    }

    Drawable getUnSelectedDrawable(String color) {
        return new DrawableCreator.Builder()
                .setSolidColor(U.getColor(R.color.transparent))
                .setStrokeColor(Color.parseColor(color))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f))
                .setCornersRadius(U.getDisplayUtils().dip2px(20f))
                .build();
    }


}
