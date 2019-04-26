package com.module.home.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.ex.ExLinearLayout;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.module.home.R;
import com.module.home.model.RechargeItemModel;

public class RechargeAdapter extends DiffAdapter<RechargeItemModel, RecyclerView.ViewHolder> {

    RechargeItemModel mSelectedItem;

    Drawable mNormalBg = new DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(8))
            .setSolidColor(Color.parseColor("#BEE9FF"))
            .build();

    Drawable mSelectedBg = U.getDrawable(R.drawable.chongzhizuanshi_xuanzhongzhuangtaibanzi);

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recharge_item_layout, parent, false);
        RechargeItemHolder viewHolder = new RechargeItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RechargeItemModel model = mDataList.get(position);

        RechargeItemHolder reportItemHolder = (RechargeItemHolder) holder;
        reportItemHolder.bind(model);
    }

    public RechargeItemModel getSelectedItem() {
        return mSelectedItem;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class RechargeItemHolder extends RecyclerView.ViewHolder {
        TextView mTvRechargeNum;
        TextView mTvCash;
        ViewGroup mInfoContainer;
        BaseImageView mIvBg;

        RechargeItemModel mRechargeItemModel;

        public RechargeItemHolder(View itemView) {
            super(itemView);
            mTvRechargeNum = (TextView) itemView.findViewById(R.id.tv_recharge_num);
            mTvCash = (TextView) itemView.findViewById(R.id.tv_cash);
            mInfoContainer = itemView.findViewById(R.id.info_container);
            mIvBg = (BaseImageView) itemView.findViewById(R.id.iv_bg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedItem != null) {
                        RechargeAdapter.this.update(mSelectedItem);
                    }
                    mSelectedItem = mRechargeItemModel;
                    RechargeAdapter.this.update(mSelectedItem);
                }
            });
        }

        public void bind(RechargeItemModel model) {
            this.mRechargeItemModel = model;
            mTvRechargeNum.setText(String.valueOf(model.getQuantity()));
            mTvCash.setText(model.getPrice() / 100000 + "");

            if (mSelectedItem == mRechargeItemModel) {
                mInfoContainer.setBackground(mSelectedBg);
            } else {
                mInfoContainer.setBackground(mNormalBg);
            }

            FrescoWorker.loadImage(mIvBg,
                    ImageFactory.newPathImage(TextUtils.isEmpty(model.getImgPath()) ? "" : model.getImgPath())
                            .setWidth(U.getDisplayUtils().dip2px(74))
                            .setHeight(U.getDisplayUtils().dip2px(66))
                            .build());
        }
    }
}
