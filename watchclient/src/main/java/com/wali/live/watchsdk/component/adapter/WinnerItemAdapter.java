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
public class WinnerItemAdapter extends RecyclerView.Adapter<WinnerItemAdapter.WinnerHolder> {

    protected LayoutInflater mInflater;

    protected final List<WinnerItem> mItems = new ArrayList<>(0);

    protected long mBestId;

    public WinnerItemAdapter() {
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
        holder.bindView(mItems.get(position), mBestId);
    }

    public void setItemData(List<WinnerItem> items, long bestId) {
        mItems.clear();
        if (items != null) {
            mItems.addAll(items);
        }
        mBestId = bestId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class WinnerHolder extends RecyclerView.ViewHolder {
        private BaseImageView mWinnerAvatarIv;
        private TextView mDiamondNumTv;
        private TextView mLuckiestTv;
        private TextView mNameTv;

        protected final <T extends View> T $(@IdRes int resId) {
            return (T) itemView.findViewById(resId);
        }

        public WinnerHolder(View view) {
            super(view);
            mWinnerAvatarIv = $(R.id.winner_avatar_iv);
            mDiamondNumTv = $(R.id.diamond_num_tv);
            mLuckiestTv = $(R.id.luckest_tv);
            mNameTv = $(R.id.name_tv);
        }

        public void bindView(WinnerItem item, long bestId) {
            mNameTv.setText(item.mNickName);
            mDiamondNumTv.setText(item.mDiamondNum + "");
            AvatarUtils.loadAvatarByUidTs(mWinnerAvatarIv, item.mUserId, 0, true);
            mLuckiestTv.setVisibility(bestId != 0 && bestId == item.mUserId
                    ? View.VISIBLE : View.GONE);
        }
    }

    public static class WinnerItem {
        public long mUserId;
        public String mNickName;
        public int mDiamondNum;

        public WinnerItem(long userId, String nickName, int diamondNum) {
            mUserId = userId;
            mNickName = nickName;
            mDiamondNum = diamondNum;
        }
    }
}

