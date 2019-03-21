package com.module.playways.grab.room.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.image.fresco.BaseImageView;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

public class InviteFirendAdapter extends DiffAdapter<SongModel, RecyclerView.ViewHolder> {

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_friend_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SongModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        BaseImageView mIvFriendIcon;
        ExTextView mIvFriendName;
        ExTextView mTvState;
        ExTextView mTvInvite;

        SongModel mWalletRecordModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mIvFriendIcon = (BaseImageView) itemView.findViewById(R.id.iv_friend_icon);
            mIvFriendName = (ExTextView) itemView.findViewById(R.id.iv_friend_name);
            mTvState = (ExTextView) itemView.findViewById(R.id.tv_state);
            mTvInvite = (ExTextView) itemView.findViewById(R.id.tv_invite);
        }

        public void bind(SongModel model) {
            this.mWalletRecordModel = model;


            /**
             * WFS_UNKNOWN = 0; //未知
             *     WFS_AUDIT = 1;//审核中
             *     WFS_SUCCESS =2; //提现成功
             *     WFS_FAIL =3; //提现失败
             */
//            if (1 == state) {
//                mTvState.setTextColor(Color.parseColor("#EF5E85"));
//            } else if (2 == state) {
//                mTvState.setTextColor(Color.parseColor("#B7BED5"));
//            } else if (3 == state) {
//                mTvState.setTextColor(Color.parseColor("#EF5E85"));
//            }
        }
    }
}