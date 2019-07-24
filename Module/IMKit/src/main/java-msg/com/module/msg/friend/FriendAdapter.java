package com.module.msg.friend;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.avatar.AvatarUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.component.live.proto.Common.ESex;

import io.rong.imkit.R;

public class FriendAdapter extends DiffAdapter<FriendStatusModel, FriendAdapter.FriendViewHodler> {

    RecyclerOnItemClickListener<FriendStatusModel> mItemClickListener;

    public FriendAdapter(RecyclerOnItemClickListener<FriendStatusModel> itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FriendAdapter.FriendViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_holder, parent, false);
        FriendViewHodler viewHolder = new FriendViewHodler(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.FriendViewHodler holder, int position) {
        FriendStatusModel friendStatusModel = mDataList.get(position);
        holder.bindData(position, friendStatusModel);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class FriendViewHodler extends RecyclerView.ViewHolder {

        SimpleDraweeView mAvatarIv;
        ExTextView mNameTv;
        ExTextView mStatusTv;

        FriendStatusModel mFriendStatusModel;
        int position;

        public FriendViewHodler(View itemView) {
            super(itemView);

            mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
            mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
            mStatusTv = (ExTextView) itemView.findViewById(R.id.status_tv);

            itemView.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(v, position, mFriendStatusModel);
                    }
                }
            });
        }

        public void bindData(int position, FriendStatusModel mFriendStatusModel) {
            this.position = position;
            this.mFriendStatusModel = mFriendStatusModel;

            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(mFriendStatusModel.getAvatar())
                            .setBorderColorBySex(mFriendStatusModel.getSex() == ESex.SX_MALE.getValue())
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mNameTv.setText(mFriendStatusModel.getNickname());
            if (mFriendStatusModel.getStatus() == FriendStatusModel.EF_OnLine) {
                mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.greendot, 0, 0, 0);
            } else {
                mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.graydot, 0, 0, 0);
            }
            mStatusTv.setText(mFriendStatusModel.getStatusDesc());
        }
    }
}
