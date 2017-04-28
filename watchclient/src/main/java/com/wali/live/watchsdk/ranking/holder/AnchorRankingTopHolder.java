package com.wali.live.watchsdk.ranking.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.lit.recycler.viewmodel.SimpleTextModel;

public class AnchorRankingTopHolder extends BaseHolder<SimpleTextModel> {
    public static final int CARD_NUM = 3;

    public BaseImageView imgFirst;
    public BaseImageView imgSecond;
    public BaseImageView imgThird;

    public LinearLayout[] infoContents;

    public TextView[] txtNames;
    public ImageView[] imgGenders;
    public TextView[] txtLevels;
    public TextView[] txtVotes;
    public TextView[] txtFollowStates;
    public RelativeLayout[] rlytRoots;
    public RelativeLayout[] rlytClickAreas;
    public View[] iconAreas;


    public AnchorRankingTopHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        infoContents = new LinearLayout[CARD_NUM];
        txtNames = new TextView[CARD_NUM];
        imgGenders = new ImageView[CARD_NUM];
        txtLevels = new TextView[CARD_NUM];
        txtVotes = new TextView[CARD_NUM];
        txtFollowStates = new TextView[CARD_NUM];
        rlytRoots = new RelativeLayout[CARD_NUM];
        rlytClickAreas = new RelativeLayout[CARD_NUM];
        iconAreas = new View[CARD_NUM];

        imgFirst = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgFirst);
        imgSecond = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgSecond);
        imgThird = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgThird);

        infoContents[0] = (LinearLayout) itemView.findViewById(R.id.single_card_bottom_info_1);
        infoContents[1] = (LinearLayout) itemView.findViewById(R.id.single_card_bottom_info_2);
        infoContents[2] = (LinearLayout) itemView.findViewById(R.id.single_card_bottom_info_3);

        rlytRoots[0] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytFirstRoot));
        rlytRoots[1] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytSecondRoot));
        rlytRoots[2] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytThirdRoot));

        for (int i = 0; i < CARD_NUM; i++) {
            txtNames[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtUsername));
            imgGenders[i] = (ImageView) (infoContents[i].findViewById(R.id.current_rank_imgGender));
            txtLevels[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtLevel));
            txtVotes[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtVote));
            txtFollowStates[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtFollowState));
            rlytClickAreas[i] = (RelativeLayout) (infoContents[i].findViewById(R.id.current_rank_rlytClickArea));
            iconAreas[i] = (infoContents[i].findViewById(R.id.live_icon));
        }
    }

    @Override
    protected void bindView() {

    }
}