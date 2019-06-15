package com.moudule.playways.beauty.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

public class BeautyHolder extends RecyclerView.ViewHolder {

    ExImageView mSelectBgIv;
    TextView mItemTv;

    BeautyControlPanelView.BeautyViewModel mBeautyViewModel;
    int position;

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

    public void bindData(BeautyControlPanelView.BeautyViewModel model, int position, boolean isSelected) {
        this.mBeautyViewModel = model;
        this.position = position;
        mItemTv.setText(model.getName());
        mItemTv.setBackground(model.getDrawable());
        setIsSelected(isSelected);
    }

    public void setIsSelected(boolean isSelected) {
        if (isSelected) {
            mSelectBgIv.setVisibility(View.VISIBLE);
        } else {
            mSelectBgIv.setVisibility(View.GONE);
        }
    }
}
