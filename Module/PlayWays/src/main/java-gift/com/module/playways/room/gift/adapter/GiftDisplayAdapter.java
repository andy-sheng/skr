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
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.view.GiftDisplayView;
import com.zq.live.proto.Common.EGiftType;

public class GiftDisplayAdapter extends DiffAdapter<BaseGift, RecyclerView.ViewHolder> {
    public static final int UNKNOWN = 0;

    public static final int NORMAL = 1;

    public static final int FREE_COUNT_DOWN = 2;

    private boolean mIsCountDownCircleShow = true;

    GiftDisplayView.IGiftOpListener mIGiftOpListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GiftItemHolder viewHolder = null;
        if (viewType == NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gift_item_layout, parent, false);
            viewHolder = new GiftItemHolder(view);
        } else if (viewType == FREE_COUNT_DOWN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.free_cd_gift_item_layout, parent, false);
            viewHolder = new FreeCountGiftDownItemHolder(view);
        }

        return viewHolder;
    }

    public void setCountDownCircleShow(boolean isCountDownCircleShow) {
        mIsCountDownCircleShow = isCountDownCircleShow;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseGift model = mDataList.get(position);

        GiftItemHolder reportItemHolder = (GiftItemHolder) holder;
        reportItemHolder.bind(model);
    }

    @Override
    public int getItemViewType(int position) {
        BaseGift baseGift = mDataList.get(position);
        if (baseGift.getGiftType() == EGiftType.EG_Coin.getValue()) {
            return NORMAL;
        } else if (baseGift.getGiftType() == EGiftType.EG_Zuan.getValue()) {
            return NORMAL;
        } else if (baseGift.getGiftType() == EGiftType.EG_SYS_Handsel.getValue()) {
            return NORMAL;
        }

        return UNKNOWN;
    }

    public void setIGiftOpListener(GiftDisplayView.IGiftOpListener IGiftOpListener) {
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

    private class FreeCountGiftDownItemHolder extends GiftItemHolder {
        CircleCountDownView mCircleCountDownView;

        public FreeCountGiftDownItemHolder(View itemView) {
            super(itemView);
            mCircleCountDownView = itemView.findViewById(R.id.circle_count_down_view);
        }

        @Override
        public void bind(BaseGift model) {
            super.bind(model);
            if (mIsCountDownCircleShow) {
                mCircleCountDownView.setVisibility(View.VISIBLE);
                long ts = mIGiftOpListener.getCountDownTs();

                mCircleCountDownView.go((int) (ts * 100 / 61000), (int) (61000 - ts));
                MyLog.d(TAG, "bind" + " ts is =" + ts + ", (int) (61000 - ts) is " + (int) (61000 - ts));
            } else {
                mCircleCountDownView.cancelAnim();
                mCircleCountDownView.setVisibility(View.GONE);
            }
        }
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
            if (model.getGiftType() == EGiftType.EG_SYS_Handsel.getValue()) {
                mIvGiftName.setText(model.getGiftName() + "x" + model.getBalance());
            } else {
                mIvGiftName.setText(model.getGiftName());
            }

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
            } else if (model.getGiftType() == EGiftType.EG_SYS_Handsel.getValue()) {
                mIvCurrency.setBackground(null);
            }

            if (model.getRealPrice() == 0) {
                mTvPrice.setText("免费");
            } else {
                String price = String.valueOf(model.getRealPrice());
                if (price.endsWith(".0")) {
                    price = price.replace(".0", "");
                }
                mTvPrice.setText(price);
            }

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
