package com.module.home.ranked.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
import com.module.home.ranked.model.RankDataModel;
import com.zq.level.view.NormalLevelView2;
import com.zq.person.event.ShowPersonCenterEvent;

import org.greenrobot.eventbus.EventBus;

// 段位榜的viewHolder
public class RankedDuanViewHolder extends RecyclerView.ViewHolder {

    RankDataModel mRankDataModel;
    int position;

    TextView mSeqTv;
    SimpleDraweeView mAvatarIv;
    TextView mNameTv;
    TextView mDuanDesc;
    NormalLevelView2 mLevelView;

    public RankedDuanViewHolder(View itemView) {
        super(itemView);

        mSeqTv = itemView.findViewById(R.id.seq_tv);
        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mNameTv = itemView.findViewById(R.id.name_tv);
        mDuanDesc = itemView.findViewById(R.id.duan_desc);
        mLevelView = itemView.findViewById(R.id.level_view);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                EventBus.getDefault().post(new ShowPersonCenterEvent(mRankDataModel.getUserID()));
            }
        });
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
        mDuanDesc.setText(model.getLevelDesc());
        mLevelView.bindData(model.getMainRanking(), model.getSubRanking());
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
