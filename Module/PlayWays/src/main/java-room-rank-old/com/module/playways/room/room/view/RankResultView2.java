package com.module.playways.room.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.RoomDataUtils;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.room.model.AudienceScoreModel;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.room.room.model.UserGameResultModel;
import com.module.playways.R;
import com.zq.live.proto.Room.EWinType;

import java.io.File;

public class RankResultView2 extends FrameLayout {

    public final String TAG = "RankResultView2";

    RelativeLayout mResultArea;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mSongTv;
    ExImageView mResultIv;
    ExImageView mRankAiIv;
    BitmapTextView mAiScoreBtv;
    ExImageView mRankManIv;
    BitmapTextView mManScoreBtv;
    RelativeLayout mScoreArea;
    BitmapTextView mPkScore;
    ExImageView mPlaybackIv;
    ExTextView mIsEscapeArea;
    ImageView mIvEscape;

    boolean mIsPlay = false;  //标记是否播放

    public RankResultView2(Context context) {
        super(context);
        init();
    }

    public RankResultView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RankResultView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.rank_result_view2_layout, this);

        mResultArea = (RelativeLayout) this.findViewById(R.id.result_area);
        mAvatarIv = (SimpleDraweeView) this.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mSongTv = (ExTextView) this.findViewById(R.id.song_tv);
        mResultIv = (ExImageView) this.findViewById(R.id.result_iv);
        mRankAiIv = (ExImageView) this.findViewById(R.id.rank_ai_iv);
        mAiScoreBtv = (BitmapTextView) this.findViewById(R.id.ai_score_btv);
        mRankManIv = (ExImageView) this.findViewById(R.id.rank_man_iv);
        mManScoreBtv = (BitmapTextView) this.findViewById(R.id.man_score_btv);
        mScoreArea = (RelativeLayout) this.findViewById(R.id.score_area);
        mPkScore = (BitmapTextView) this.findViewById(R.id.pk_score);
        mPlaybackIv = (ExImageView) this.findViewById(R.id.playback_iv);
        mIsEscapeArea = (ExTextView) this.findViewById(R.id.is_escape_area);
        mIvEscape = (ImageView) this.findViewById(R.id.iv_escape);

        mPlaybackIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                MyLog.d(TAG, "clickValid mIsPlay=" + mIsPlay + " mListener=" + mListener);
                if (mIsPlay) {
                    // TODO: 2019/3/22 暂停播放
                    mIsPlay = false;
                    mPlaybackIv.setBackground(getResources().getDrawable(R.drawable.rank_play));
                    if (mListener != null) {
                        mListener.onPauseBtnClick();
                    }
                } else {
                    // TODO: 2019/3/22 开始播放
                    mIsPlay = true;
                    mPlaybackIv.setBackground(getResources().getDrawable(R.drawable.rank_stop));
                    if (mListener != null) {
                        mListener.onPlayBtnClick();
                    }
                }
            }
        });
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
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .build());
            if (MyUserInfoManager.getInstance().getUid() == playerInfoModel.getUserInfo().getUserId()
                    && new File(RoomDataUtils.getSaveAudioForAiFilePath()).exists()
                    && (roomData.getSingBeginTs() > System.currentTimeMillis() - 1000 * 3600)
            ) {
                mPlaybackIv.setVisibility(VISIBLE);
            } else {
                mPlaybackIv.setVisibility(GONE);
            }
            mNameTv.setText(playerInfoModel.getUserInfo().getNicknameRemark());
            mSongTv.setText("《" + playerInfoModel.getSongList().get(0).getItemName() + "》");
            if (userGameResultModel.getWinType() == EWinType.Win.getValue()) {
                mResultIv.setBackground(getResources().getDrawable(R.drawable.ic_medal_win));
            } else if (userGameResultModel.getWinType() == EWinType.Draw.getValue()) {
                mResultIv.setBackground(getResources().getDrawable(R.drawable.ic_medal_draw));
            } else if (userGameResultModel.getWinType() == EWinType.Lose.getValue()) {
                mResultIv.setBackground(getResources().getDrawable(R.drawable.ic_medal_lose));
            }

            if (userGameResultModel.isIsEscape()) {
                mIsEscapeArea.setVisibility(VISIBLE);
                mIvEscape.setVisibility(VISIBLE);
            }
        }

        if (userGameResultModel.getAudienceScores().size() == 3) {
            float manScore = 0;
            for (AudienceScoreModel audienceScoreModel : userGameResultModel.getAudienceScores()) {
                if (audienceScoreModel.getUserID() == UserAccountManager.SYSTEM_RANK_AI) {
                    // Ai机器人
                    mAiScoreBtv.setText(audienceScoreModel.getScore() + "");
                } else {
                    manScore = manScore + audienceScoreModel.getScore();
                }
            }

            if (manScore == 0) {
                mRankManIv.setBackground(getResources().getDrawable(R.drawable.paiwei_baodeng_m));
                mManScoreBtv.setText("0.0");
            } else {
                mRankManIv.setBackground(getResources().getDrawable(R.drawable.paiwei_baodeng_l));
                mManScoreBtv.setText(manScore + "");
            }
        }

        mPkScore.setText(userGameResultModel.getTotalScore() + "");
    }

    Listener mListener;

    public void setListener(Listener l) {
        this.mListener = l;
    }

    public void playComplete() {
        // TODO: 2019/3/22 暂停播放
        mIsPlay = false;
        mPlaybackIv.setBackground(getResources().getDrawable(R.drawable.rank_play));
    }

    public interface Listener {
        void onPlayBtnClick();

        void onPauseBtnClick();
    }
}
