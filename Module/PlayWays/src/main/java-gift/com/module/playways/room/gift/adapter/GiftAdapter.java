package com.module.playways.room.gift.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.view.GiftView;
import com.zq.live.proto.Common.EGiftType;

public class GiftAdapter extends DiffAdapter<BaseGift, RecyclerView.ViewHolder> {

    GiftView.IGiftOpListener mIGiftOpListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gift_item_layout, parent, false);
        GiftItemHolder viewHolder = new GiftItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseGift model = mDataList.get(position);

        GiftItemHolder reportItemHolder = (GiftItemHolder) holder;
        reportItemHolder.bind(model);
    }

    public void setIGiftOpListener(GiftView.IGiftOpListener IGiftOpListener) {
        mIGiftOpListener = IGiftOpListener;
    }

    GiftUpdateListner mGiftUpdateListner = new GiftUpdateListner() {
        @Override
        public void updateGift(BaseGift baseGift) {
            update(baseGift);
        }
    };

    @Override
    public void update(BaseGift data) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).equals(data)) {
                mDataList.set(i, data);
                notifyItemChanged(i, 0);
                return;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class GiftItemHolder extends RecyclerView.ViewHolder {
        ExImageView mIvSelectedBg;
        BaseImageView mIvGiftIcon;
        ExTextView mIvGiftName;
        ImageView mIvCurrency;
        ExTextView mTvPrice;

        BaseGift mBaseGift;

        public GiftItemHolder(View itemView) {
            super(itemView);
            mTvPrice = (ExTextView) itemView.findViewById(R.id.tv_price);
            mIvSelectedBg = (ExImageView) itemView.findViewById(R.id.iv_selected_bg);
            mIvGiftIcon = (BaseImageView) itemView.findViewById(R.id.iv_gift_icon);
            mIvGiftName = (ExTextView) itemView.findViewById(R.id.iv_gift_name);
            mIvCurrency = (ImageView) itemView.findViewById(R.id.iv_currency);

            itemView.setOnClickListener(view -> {
                if (mIGiftOpListener != null && mIGiftOpListener.getCurSelectedGift() != mBaseGift) {
                    mIGiftOpListener.select(mBaseGift, mGiftUpdateListner);
                    mIvSelectedBg.setVisibility(View.VISIBLE);
                }
            });
        }

        public void bind(BaseGift model) {
            this.mBaseGift = model;
            mIvGiftName.setText(model.getGiftName());
            FrescoWorker.loadImage(mIvGiftIcon, ImageFactory.newPathImage(model.getGiftURL())
                    .setLoadingDrawable(U.getDrawable(R.drawable.skrer_logo))
                    .setFailureDrawable(U.getDrawable(R.drawable.skrer_logo))
                    .setWidth(U.getDisplayUtils().dip2px(45))
                    .setHeight(U.getDisplayUtils().dip2px(45))
                    .build());

            if (model.getGiftType() == EGiftType.EG_Coin.getValue()) {
                mIvCurrency.setBackground(U.getDrawable(R.drawable.ycdd_daojishi_jinbi));
            } else if (model.getGiftType() == EGiftType.EG_Zuan.getValue()) {
                mIvCurrency.setBackground(U.getDrawable(R.drawable.diamond_icon));
            }

            String price = String.valueOf(model.getRealPrice());
            if (price.endsWith(".0")) {
                price = price.replace(".0", "");
            }
            mTvPrice.setText(price);

            if (mBaseGift == mIGiftOpListener.getCurSelectedGift()) {
                mIvSelectedBg.setVisibility(View.VISIBLE);
            } else {
                mIvSelectedBg.setVisibility(View.GONE);
            }
        }
    }

    public interface GiftUpdateListner {
        void updateGift(BaseGift baseGift);
    }
}
