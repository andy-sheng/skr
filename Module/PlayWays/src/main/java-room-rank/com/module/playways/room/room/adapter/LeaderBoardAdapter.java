package com.module.playways.room.room.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.RankInfoModel;

import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.RouterConstants;
import com.module.playways.R;
import com.zq.level.view.NormalLevelView2;
import com.zq.live.proto.Common.ESex;

public class LeaderBoardAdapter extends DiffAdapter<RankInfoModel, RecyclerView.ViewHolder> {

    public static final int VIEW_HOLDER_TYPE_TEXT = 1;

    public LeaderBoardAdapter() {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (VIEW_HOLDER_TYPE_TEXT == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_item_layout, parent, false);
            RankInfoItemHolder viewHolder = new RankInfoItemHolder(view);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RankInfoModel model = mDataList.get(position);
        if (holder instanceof RankInfoItemHolder) {
            RankInfoItemHolder rankInfoItemHolder = (RankInfoItemHolder) holder;
            rankInfoItemHolder.bind(model);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {

        return VIEW_HOLDER_TYPE_TEXT;
    }

    public static class RankInfoItemHolder extends RecyclerView.ViewHolder {
        ExTextView mTvRank;
        SimpleDraweeView mSdvIcon;
        ExTextView mTvName;
        ExTextView mTvSegment;
        NormalLevelView2 mLevelView;

        RankInfoModel mRankInfoModel;

        public RankInfoItemHolder(View itemView) {
            super(itemView);
            mTvRank = (ExTextView) itemView.findViewById(R.id.tv_rank);
            mSdvIcon = (SimpleDraweeView) itemView.findViewById(R.id.sdv_icon);
            mTvName = (ExTextView) itemView.findViewById(R.id.tv_name);
            mTvSegment = (ExTextView) itemView.findViewById(R.id.tv_segment);
            mLevelView = (NormalLevelView2) itemView.findViewById(R.id.level_view);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("bundle_user_id", mRankInfoModel.getUserID());
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation();
                }
            });
        }

        public void bind(RankInfoModel rankInfoModel) {
            mRankInfoModel = rankInfoModel;
            mTvRank.setText(rankInfoModel.getRankSeq() + "");
            mTvName.setText(UserInfoManager.getInstance().getRemarkName(rankInfoModel.getUserID(),rankInfoModel.getNickname()));
            mTvSegment.setText(rankInfoModel.getLevelDesc());
            mLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking());
            AvatarUtils.loadAvatarByUrl(mSdvIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColorBySex(rankInfoModel.getSex() == ESex.SX_MALE.getValue())
                            .build());
        }
    }
}
