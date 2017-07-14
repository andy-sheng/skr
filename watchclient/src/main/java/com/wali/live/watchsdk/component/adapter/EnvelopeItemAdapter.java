package com.wali.live.watchsdk.component.adapter;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wmj on 17-7-14.
 *
 * @module 红包
 */
public class EnvelopeItemAdapter extends RecyclerView.Adapter<EnvelopeItemAdapter.WinnerHolder> {
    protected LayoutInflater mInflater;
    protected final List<WinnerItem> mItems = new ArrayList<>(0);

    public EnvelopeItemAdapter() {
    }

    @Override
    public WinnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mInflater.inflate(R.layout.red_envelope_winner_item, null);
        return new WinnerHolder(view);
    }

    @Override
    public void onBindViewHolder(WinnerHolder holder, int position) {
        holder.bindView(mItems.get(position));
    }

    public void setItemData(List<WinnerItem> items) {
        mItems.clear();
        if (items != null) {
            mItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected static class WinnerHolder extends RecyclerView.ViewHolder {
        private BaseImageView mWinnerAvatarIv;
        private TextView mDiamondNumTv;
        private TextView mLuckestTv;
        private TextView mNameTv;

        protected final <T extends View> T $(@IdRes int resId) {
            return (T) itemView.findViewById(resId);
        }

        public WinnerHolder(View view) {
            super(view);
            mWinnerAvatarIv = $(R.id.winner_avatar_iv);
            mDiamondNumTv = $(R.id.diamond_num_tv);
            mLuckestTv = $(R.id.luckest_tv);
            mNameTv = $(R.id.name_tv);
        }

        public void bindView(WinnerItem item) {
            mNameTv.setText(item.mName);
            mDiamondNumTv.setText(item.mDiamondNum + "");
            AvatarUtils.loadAvatarByUidTs(mWinnerAvatarIv, item.mUserId, 0, true);
            if (item.mIsLuckest) {
                mLuckestTv.setVisibility(View.VISIBLE);
            } else {
                mLuckestTv.setVisibility(View.GONE);
            }
        }
    }

    public static class WinnerItem {
        public String mName;
        public int mDiamondNum;
        public Long mUserId;
        public boolean mIsLuckest;

        public WinnerItem(String name, int diamondNum, Long userId, boolean isLuckest) {
            mName = name;
            mDiamondNum = diamondNum;
            mUserId = userId;
            mIsLuckest = isLuckest;
        }
    }
}

