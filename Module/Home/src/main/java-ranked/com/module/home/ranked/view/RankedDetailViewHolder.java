package com.module.home.ranked.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.utils.U;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
import com.module.home.ranked.model.RankDataModel;

import static com.module.home.ranked.model.RankDataModel.BLUE_ZUAN;
import static com.module.home.ranked.model.RankDataModel.MEILI;

// 打赏和魅力榜的viewHolder
public class RankedDetailViewHolder extends RecyclerView.ViewHolder {

    RankDataModel mRankDataModel;
    int position;

    ImageView mRewardBg;
    TextView mSeqTv;
    SimpleDraweeView mAvatarIv;
    TextView mNameTv;
    ImageView mRankedIconIv;
    TextView mRankedDescTv;

    public RankedDetailViewHolder(View itemView) {
        super(itemView);

        mRewardBg = itemView.findViewById(R.id.reward_bg);
        mSeqTv = itemView.findViewById(R.id.seq_tv);
        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mNameTv = itemView.findViewById(R.id.name_tv);
        mRankedIconIv = itemView.findViewById(R.id.ranked_icon_iv);
        mRankedDescTv = itemView.findViewById(R.id.ranked_desc_tv);
    }

    public void bindData(int position, RankDataModel model) {
        this.position = position;
        this.mRankDataModel = model;

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(model.getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.WHITE)
                .build());
        mNameTv.setText(UserInfoManager.getInstance().getRemarkName(model.getUserID(), model.getNickname()));
        mRankedDescTv.setText("" + mRankDataModel.getScore());

        if (model.getVType() == BLUE_ZUAN) {
            // 打赏榜
            mRankedIconIv.setImageResource(R.drawable.ranked_lanzuan_icon);
            if (mRankDataModel.getRankSeq() <= 3) {
                mRewardBg.setVisibility(View.VISIBLE);
                mSeqTv.setTextColor(Color.WHITE);
            } else {
                mRewardBg.setVisibility(View.GONE);
                mSeqTv.setTextColor(Color.parseColor("#333B7B"));
            }
            mSeqTv.setBackground(null);
            mSeqTv.setText("" + mRankDataModel.getRankSeq());
        } else if (model.getVType() == MEILI) {
            mRewardBg.setVisibility(View.GONE);
            // 人气榜
            mRankedIconIv.setImageResource(R.drawable.ranked_meili_icon);
            if (mRankDataModel.getRankSeq() <= 3) {
                mSeqTv.setText("");
                if (mRankDataModel.getRankSeq() == 1) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_1));
                } else if (mRankDataModel.getRankSeq() == 2) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_2));
                } else {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_3));
                }
            } else {
                mSeqTv.setBackground(null);
                mSeqTv.setText("" + mRankDataModel.getRankSeq());
            }
        }

    }
}
