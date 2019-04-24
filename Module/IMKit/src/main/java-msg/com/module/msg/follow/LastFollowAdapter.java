package com.module.msg.follow;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.live.proto.Common.ESex;

import io.rong.imkit.R;

public class LastFollowAdapter extends DiffAdapter<LastFollowModel, LastFollowAdapter.LastFollowViewHodler> {

    RecyclerOnItemClickListener<LastFollowModel> mItemClickListener;

    public LastFollowAdapter(RecyclerOnItemClickListener<LastFollowModel> listener) {
        this.mItemClickListener = listener;
    }

    @NonNull
    @Override
    public LastFollowViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.last_follow_item_holder, parent, false);
        LastFollowViewHodler viewHolder = new LastFollowViewHodler(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LastFollowViewHodler holder, int position) {
        LastFollowModel lastFollowModel = mDataList.get(position);
        holder.bindData(position, lastFollowModel);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class LastFollowViewHodler extends RecyclerView.ViewHolder {

        int postion;
        LastFollowModel lastFollowModel;

        RelativeLayout mContent;
        SimpleDraweeView mAvatarIv;
        ExTextView mNameTv;
        ExTextView mStatusDescTv;
        ImageView mSexIv;
        ExTextView mFollowTv;


        public LastFollowViewHodler(View itemView) {
            super(itemView);

            mContent = (RelativeLayout) itemView.findViewById(R.id.content);
            mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
            mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
            mStatusDescTv = (ExTextView) itemView.findViewById(R.id.status_desc_tv);
            mSexIv = (ImageView) itemView.findViewById(R.id.sex_iv);
            mFollowTv = (ExTextView) itemView.findViewById(R.id.follow_tv);

            mContent.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(mContent, postion, lastFollowModel);
                    }
                }
            });

            mFollowTv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(mFollowTv, postion, lastFollowModel);
                    }
                }
            });
        }

        public void bindData(int postion, LastFollowModel lastFollowModel) {
            this.postion = postion;
            this.lastFollowModel = lastFollowModel;

            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(lastFollowModel.getAvatar())
                            .setCircle(true)
                            .build());
            mNameTv.setText(lastFollowModel.getNickname());
            mStatusDescTv.setText(lastFollowModel.getStatusDesc());
            mSexIv.setBackgroundResource(lastFollowModel.getSex() == ESex.SX_MALE.getValue() ? R.drawable.sex_man_icon : R.drawable.sex_woman_icon);

            if (lastFollowModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                mFollowTv.setVisibility(View.GONE);
                return;
            } else {
                if (lastFollowModel.isIsFriend()) {
                    mFollowTv.setVisibility(View.VISIBLE);
                    mFollowTv.setText("已互关");
                    mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
                    mFollowTv.setBackground(null);
                } else if (lastFollowModel.isIsFollow()) {
                    mFollowTv.setVisibility(View.VISIBLE);
                    mFollowTv.setText("已关注");
                    mFollowTv.setTextColor(Color.parseColor("#CC7F00"));
                    mFollowTv.setBackground(null);
                } else {
                    mFollowTv.setVisibility(View.VISIBLE);
                    mFollowTv.setText("+关注");
                    mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
                    mFollowTv.setBackground(ContextCompat.getDrawable(U.app(), com.component.busilib.R.drawable.yellow_button_icon));
                }
            }
        }
    }
}
