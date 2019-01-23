package com.module.playways.rank.room.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rank.R;
import com.zq.level.view.NormalLevelView;
import com.zq.person.fragment.OtherPersonFragment;

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
        NormalLevelView mLevelView;

        RankInfoModel mRankInfoModel;

        public RankInfoItemHolder(View itemView) {
            super(itemView);
            mTvRank = (ExTextView) itemView.findViewById(R.id.tv_rank);
            mSdvIcon = (SimpleDraweeView) itemView.findViewById(R.id.sdv_icon);
            mTvName = (ExTextView) itemView.findViewById(R.id.tv_name);
            mTvSegment = (ExTextView) itemView.findViewById(R.id.tv_segment);
            mLevelView = (NormalLevelView) itemView.findViewById(R.id.level_view);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRankInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                        return;
                    }

                    UserInfoModel userInfoModel = new UserInfoModel();
                    userInfoModel.setUserId(mRankInfoModel.getUserID());
                    userInfoModel.setNickname(mRankInfoModel.getNickname());
                    userInfoModel.setAvatar(mRankInfoModel.getAvatar());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_MODEL, userInfoModel);
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((FragmentActivity) itemView.getContext(), OtherPersonFragment.class)
                            .setBundle(bundle)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build());
                }
            });
        }

        public void bind(RankInfoModel rankInfoModel) {
            mRankInfoModel = rankInfoModel;
            mTvRank.setText(rankInfoModel.getRankSeq() + "");
            mTvName.setText(rankInfoModel.getNickname());
            mTvSegment.setText(rankInfoModel.getLevelDesc());
            mLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking(), rankInfoModel.getMaxStar(), rankInfoModel.getStarCnt());
            AvatarUtils.loadAvatarByUrl(mSdvIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.parseColor("#85EAFF"))
                            .build());
        }
    }
}
