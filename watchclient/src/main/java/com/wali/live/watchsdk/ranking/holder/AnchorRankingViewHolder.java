package com.wali.live.watchsdk.ranking.holder;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.lit.recycler.viewmodel.SimpleTextModel;

public class AnchorRankingViewHolder extends BaseHolder<SimpleTextModel> {
    public RelativeLayout rankingRootLayout;
    public BaseImageView avatarDv;
    public ImageView rankImg;
    public TextView nameTv;
    public TextView voteTv;
    public TextView rankNum;
    public View clickArea;
    public TextView followState;
    public ImageView imgBadge;
    public ImageView imgBadgeVip;
    public ImageView imgGenderIv;
    public TextView levelTv;
    public FrameLayout avatarBg;
    public View liveIcon;
    public RelativeLayout nameAreaRl;

    public AnchorRankingViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        rankingRootLayout = $(R.id.rankingRootLayout);
        avatarDv = $(R.id.rank_avatar);
        rankImg = $(R.id.rankImg);
        nameTv = $(R.id.txt_username);
        voteTv = $(R.id.voteTv);
        rankNum = $(R.id.rankNum);
        clickArea = $(R.id.btn_area);
        followState = $(R.id.tv_follow_state);
        imgBadge = $(R.id.img_badge);
        imgBadgeVip = $(R.id.img_badge_vip);
        imgGenderIv = $(R.id.img_gender);
        levelTv = $(R.id.level_tv);
        avatarBg = $(R.id.rank_avatar_bg);
        liveIcon = $(R.id.live_icon);
        nameAreaRl = $(R.id.name_gender_level);
    }

    @Override
    protected void bindView() {

    }
}