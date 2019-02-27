package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.BaseRoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.room.RankRoomData;
import com.module.playways.rank.room.model.AudienceScoreModel;
import com.module.playways.rank.room.model.RankPlayerInfoModel;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.rank.R;
import com.zq.live.proto.Room.ELightType;
import com.zq.live.proto.Room.EWinType;

/**
 * 某个人的战绩
 */
public class RankResultView extends RelativeLayout {

    RelativeLayout mAvatarArea;
    SimpleDraweeView mAvatarIv;
    ExImageView mResultIv;
    ExTextView mNameTv;
    ExTextView mSongTv;

    RelativeLayout mResultArea;
    SimpleDraweeView mFirstAvatarIv;
    ImageView mFirstImgIv;
    ExTextView mFirstResultTv;
    SimpleDraweeView mSecondAvatarIv;
    ImageView mSecondImgIv;
    ExTextView mSecondResultTv;
    SimpleDraweeView mThirdAvatarIv;
    ImageView mThirdImgIv;
    ExTextView mThirdResultTv;

    RelativeLayout mScoreArea;
    BitmapTextView mPkScore;

    ExRelativeLayout mIsEscapeNorml;  //正常逃跑蒙层
    ExRelativeLayout mIsEscapeLast;   //最后一个的逃跑蒙层

    public RankResultView(Context context) {
        super(context);
        init();
    }

    public RankResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RankResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.rank_result_view_layout, this);

        mAvatarArea = (RelativeLayout) findViewById(R.id.avatar_area);
        mAvatarIv = (SimpleDraweeView) findViewById(R.id.avatar_iv);
        mResultIv = (ExImageView) findViewById(R.id.result_iv);
        mNameTv = (ExTextView) findViewById(R.id.name_tv);
        mSongTv = (ExTextView) findViewById(R.id.song_tv);
        mResultArea = (RelativeLayout) findViewById(R.id.result_area);
        mFirstAvatarIv = (SimpleDraweeView) findViewById(R.id.first_avatar_iv);
        mFirstImgIv = (ImageView) findViewById(R.id.first_img_iv);
        mFirstResultTv = (ExTextView) findViewById(R.id.first_result_tv);
        mSecondAvatarIv = (SimpleDraweeView) findViewById(R.id.second_avatar_iv);
        mSecondImgIv = (ImageView) findViewById(R.id.second_img_iv);
        mSecondResultTv = (ExTextView) findViewById(R.id.second_result_tv);
        mThirdAvatarIv = (SimpleDraweeView) findViewById(R.id.third_avatar_iv);
        mThirdImgIv = (ImageView) findViewById(R.id.third_img_iv);
        mThirdResultTv = (ExTextView) findViewById(R.id.third_result_tv);
        mScoreArea = (RelativeLayout) findViewById(R.id.score_area);
        mPkScore = (BitmapTextView) findViewById(R.id.pk_score);
        mIsEscapeNorml = (ExRelativeLayout) findViewById(R.id.is_escape_norml);
        mIsEscapeLast = (ExRelativeLayout) findViewById(R.id.is_escape_last);

    }

    /**
     * 绑定数据,表示是某个人的演唱结果
     *
     * @param roomData
     * @param useID
     * @param index    第几个
     */
    public void bindData(RankRoomData roomData, int useID, int index) {
        if (useID == 0) {
            return;
        }
        UserGameResultModel userGameResultModel = roomData.getRecordData().getUserGameResultModel(useID);
        RankPlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(roomData, userGameResultModel.getUserID());
        if (playerInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(playerInfoModel.getUserInfo().getAvatar())
                            .setCircle(true)
                            .setBorderColorBySex(playerInfoModel.getUserInfo().getIsMale())
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .build());
            mNameTv.setText(playerInfoModel.getUserInfo().getNickname());
            mSongTv.setText("《" + playerInfoModel.getSongList().get(0).getItemName() + "》");
            if (userGameResultModel.getWinType() == EWinType.Win.getValue()) {
                mResultIv.setBackground(getResources().getDrawable(R.drawable.ic_medal_win));
            } else if (userGameResultModel.getWinType() == EWinType.Draw.getValue()) {
                mResultIv.setBackground(getResources().getDrawable(R.drawable.ic_medal_draw));
            } else if (userGameResultModel.getWinType() == EWinType.Lose.getValue()) {
                mResultIv.setBackground(getResources().getDrawable(R.drawable.ic_medal_lose));
            }

            if (userGameResultModel.isIsEscape()) {
                if (index == userGameResultModel.getAudienceScores().size()) {
                    mIsEscapeNorml.setVisibility(GONE);
                    mIsEscapeLast.setVisibility(VISIBLE);
                } else {
                    mIsEscapeNorml.setVisibility(VISIBLE);
                    mIsEscapeLast.setVisibility(GONE);
                }
            }
        }

        if (userGameResultModel.getAudienceScores().size() == 3) {
            AudienceScoreModel audienceScoreModel1 = userGameResultModel.getAudienceScores().get(0);
            PlayerInfoModel playerInfoModel1 = RoomDataUtils.getPlayerInfoById(roomData, audienceScoreModel1.getUserID());
            if (playerInfoModel1 != null) {
                AvatarUtils.loadAvatarByUrl(mFirstAvatarIv,
                        AvatarUtils.newParamsBuilder(playerInfoModel1.getUserInfo().getAvatar())
                                .setCircle(true)
                                .setBorderColorBySex(playerInfoModel1.getUserInfo().getIsMale())
                                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                .build());
                if (audienceScoreModel1.getLightType() == ELightType.ELT_B_LIGHT.getValue()) {
                    mFirstImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_baodeng));
                    mFirstResultTv.setTextColor(Color.parseColor("#EDC100"));
                } else if (audienceScoreModel1.getLightType() == ELightType.ELT_M_LIGHT.getValue()) {
                    mFirstImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_mie));
                    mFirstResultTv.setTextColor(Color.parseColor("#9697A0"));
                } else if (audienceScoreModel1.getLightType() == ELightType.ELT_X_LIGHT.getValue()) {
                    mFirstImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_xiaolian));
                    mFirstResultTv.setTextColor(Color.parseColor("#9697A0"));
                }
                mFirstResultTv.setText("" + audienceScoreModel1.getScore());
            }

            AudienceScoreModel audienceScoreModel2 = userGameResultModel.getAudienceScores().get(1);
            PlayerInfoModel playerInfoModel2 = RoomDataUtils.getPlayerInfoById(roomData, audienceScoreModel2.getUserID());
            if (playerInfoModel2 != null) {
                AvatarUtils.loadAvatarByUrl(mSecondAvatarIv,
                        AvatarUtils.newParamsBuilder(playerInfoModel2.getUserInfo().getAvatar())
                                .setCircle(true)
                                .setBorderColorBySex(playerInfoModel2.getUserInfo().getIsMale())
                                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                .build());
                if (audienceScoreModel2.getLightType() == ELightType.ELT_B_LIGHT.getValue()) {
                    mSecondImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_baodeng));
                    mSecondResultTv.setTextColor(Color.parseColor("#EDC100"));
                } else if (audienceScoreModel2.getLightType() == ELightType.ELT_M_LIGHT.getValue()) {
                    mSecondImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_mie));
                    mSecondResultTv.setTextColor(Color.parseColor("#9697A0"));
                } else if (audienceScoreModel2.getLightType() == ELightType.ELT_X_LIGHT.getValue()) {
                    mSecondImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_xiaolian));
                    mSecondResultTv.setTextColor(Color.parseColor("#9697A0"));
                }
                mSecondResultTv.setText("" + audienceScoreModel2.getScore());
            }

            AudienceScoreModel audienceScoreModel3 = userGameResultModel.getAudienceScores().get(2);
            PlayerInfoModel playerInfoModel3 = RoomDataUtils.getPlayerInfoById(roomData, audienceScoreModel3.getUserID());
            if (playerInfoModel3 != null) {
                AvatarUtils.loadAvatarByUrl(mThirdAvatarIv,
                        AvatarUtils.newParamsBuilder(playerInfoModel3.getUserInfo().getAvatar())
                                .setCircle(true)
                                .setBorderColorBySex(playerInfoModel3.getUserInfo().getIsMale())
                                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                .build());
                if (audienceScoreModel3.getLightType() == ELightType.ELT_B_LIGHT.getValue()) {
                    mThirdImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_baodeng));
                    mThirdResultTv.setTextColor(Color.parseColor("#EDC100"));
                } else if (audienceScoreModel3.getLightType() == ELightType.ELT_M_LIGHT.getValue()) {
                    mThirdImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_mie));
                    mThirdResultTv.setTextColor(Color.parseColor("#9697A0"));
                } else if (audienceScoreModel3.getLightType() == ELightType.ELT_X_LIGHT.getValue()) {
                    mThirdImgIv.setBackground(getResources().getDrawable(R.drawable.zhanji_xiaolian));
                    mThirdResultTv.setTextColor(Color.parseColor("#9697A0"));
                }
                mThirdResultTv.setText("" + audienceScoreModel3.getScore());
            }
        }

        mPkScore.setText(userGameResultModel.getTotalScore() + "");
    }
}
