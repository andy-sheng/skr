package com.module.home.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.home.R;
import com.module.home.model.RechargeItemModel;

public class HalfRechargeAdapter extends RechargeAdapter {
    public HalfRechargeAdapter() {
        mNormalBg = new DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .setSolidColor(U.getColor(R.color.black_trans_20))
                .build();

        mSelectedBg = U.getDrawable(R.drawable.chongzhijiemian_dianjiuxuanzhongtai);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.half_recharge_item_layout, parent, false);
        HalfRechargeItemHolder viewHolder = new HalfRechargeItemHolder(view);
        return viewHolder;
    }

    public class HalfRechargeItemHolder extends RechargeItemHolder {
        public HalfRechargeItemHolder(View itemView) {
            super(itemView);
        }

        public void bind(RechargeItemModel model) {
            this.mRechargeItemModel = model;
            mTvRechargeNum.setText(String.valueOf(model.getQuantity()));
            mTvCash.setText(model.getPrice() / 100000 + "元");

            if (mSelectedItem == mRechargeItemModel) {
                mInfoContainer.setBackground(mSelectedBg);
            } else {
                mInfoContainer.setBackground(mNormalBg);
            }
        }
    }
}
