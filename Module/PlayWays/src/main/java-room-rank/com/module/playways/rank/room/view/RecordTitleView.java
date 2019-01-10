package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.prepare.GameModeType;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.scoremodel.ScoreDetailModel;
import com.module.playways.rank.room.utils.ScoreConfigUtils;
import com.module.rank.R;
import com.zq.level.view.NormalLevelView;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class RecordTitleView extends RelativeLayout {

    public final static String TAG = "RecordTitleView";

    RoomData mRoomData;

    RecordData mRecordData;

    ScoreDetailModel mScoreDetailModel;

    SimpleDraweeView mSdvOwnIcon;
    NormalLevelView mSdvOwnLevel;

    ExTextView mTvOwnerName;
    ExImageView mIvOwnRecord;
    ExTextView mTvOwnRecord;

    ExTextView mTvLightCount;
    ExImageView mIvLightCount;
    ExTextView mTvSongName;
    ExImageView mTvSongScore;

    RecordCircleView mRecordCircleView;

    ViewGroup mViewGroup; // 放动画的容器

    public RecordTitleView(Context context) {
        this(context, null);
    }

    public RecordTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(ViewGroup viewGroup, RecordData recordData, RoomData roomData) {
        this.mViewGroup = viewGroup;
        mRoomData = roomData;
        mRecordData = recordData;
        //娱乐模式
        if (mRoomData.getGameType() == GameModeType.GAME_MODE_FUNNY) {
            inflate(getContext(), R.layout.record_title_happy, this);
            mTvLightCount = (ExTextView) findViewById(R.id.tv_light_count);
            mIvLightCount = (ExImageView) findViewById(R.id.iv_light_count);
            mSdvOwnIcon = (SimpleDraweeView) findViewById(R.id.sdv_own_icon);
            mTvOwnerName = (ExTextView) findViewById(R.id.tv_owner_name);
            mTvOwnRecord = (ExTextView) findViewById(R.id.tv_own_record);

            AvatarUtils.loadAvatarByUrl(mSdvOwnIcon,
                    AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                            .setCircle(true)
                            .setGray(false)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.WHITE)
                            .build());

            mTvOwnerName.setText(MyUserInfoManager.getInstance().getNickName());
            mTvLightCount.setText("亮灯X" + recordData.getSelfVoteInfoModel().getVoter().size());
        } else {
            inflate(getContext(), R.layout.record_title_athletics, this);

            mSdvOwnLevel = (NormalLevelView) findViewById(R.id.sdv_own_level);
            mRecordCircleView = (RecordCircleView) findViewById(R.id.record_circle_view);

            animationGo(recordData.mScoreDetailModel);
        }

        mTvSongName = (ExTextView) findViewById(R.id.tv_song_name);
        mTvSongScore = (ExImageView) findViewById(R.id.tv_song_score);

        mIvOwnRecord = (ExImageView) findViewById(R.id.iv_own_record);

        mTvSongName.setText("《" + mRoomData.getSongModel().getItemName() + "》");

        mTvSongScore.setBackground(getResources().getDrawable(ScoreConfigUtils.getImageResoucesScore(mRecordData.mScoreDetailModel.getBattleRatingScore())));

        Observable.fromIterable(mRecordData.mVoteInfoModels)
                .filter(new Predicate<VoteInfoModel>() {
                    @Override
                    public boolean test(VoteInfoModel voteInfoModel) throws Exception {
                        return voteInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid();
                    }
                })
                .subscribe(new Consumer<VoteInfoModel>() {
                    @Override
                    public void accept(VoteInfoModel voteInfoModel) throws Exception {
                        Drawable drawable = null;
                        String str = "";
                        switch (voteInfoModel.getRank()) {
                            case 0:
                                drawable = getResources().getDrawable(R.drawable.ic_medal1_normal);
                                str = "冠军";
                                break;
                            case 2:
                                drawable = getResources().getDrawable(R.drawable.ic_medal2_normal);
                                str = "亚军";
                                break;
                            case 3:
                                drawable = getResources().getDrawable(R.drawable.ic_medal3_normal);
                                str = "季军";
                                break;
                        }
                        mIvOwnRecord.setBackground(drawable);

                        if (mTvOwnRecord != null) {
                            mTvOwnRecord.setText(str);
                        }
                    }
                }, throwable -> MyLog.e(throwable));
    }

    private void animationGo(ScoreDetailModel scoreDetailModel) {
        MyLog.d(TAG, "animationGo" + " scoreDetailModel=" + scoreDetailModel);
        if (scoreDetailModel == null) {
            return;
        }
        this.mScoreDetailModel = scoreDetailModel;

        // 展示之前的段位
        mSdvOwnLevel.bindData(scoreDetailModel.getRankScore().getLevelBefore(), scoreDetailModel.getSubRankScore().getLevelBefore()
                , scoreDetailModel.getTotalStarLimit().getLimitBefore(), scoreDetailModel.getRankStarScore().getScoreBefore());
        mSdvOwnLevel.postDelayed(new Runnable() {
            @Override
            public void run() {
                battleAnimationGo();
                scoreAnimationGo();
            }
        }, 1000);
    }

    private void scoreAnimationGo() {
        if (mScoreDetailModel.hasLevelChange()) {
            // TODO: 2019/1/10  段位变化
            if (mScoreDetailModel.getRankStarScore().getScoreBefore() == mScoreDetailModel.getTotalStarLimit().getLimitBefore()) {
                // 之前就满星， 播放段位改变的动画
                mSdvOwnLevel.levelChange(mViewGroup, mScoreDetailModel.getRankScore().getLevelBefore(), mScoreDetailModel.getSubRankScore().getLevelBefore(),
                        mScoreDetailModel.getRankScore().getLevelNow(), mScoreDetailModel.getSubRankScore().getLevelNow(), mScoreDetailModel.getTotalStarLimit().getLimitNow(), new NormalLevelView.SVGAListener() {
                            @Override
                            public void onFinish() {
                                // 播放段位改变后砸星星动画
                                if (mScoreDetailModel.getRankStarScore().getScoreNow() == 0) {
                                    // 没有星，判断是否有分数，变动播放分数动画(先不做)

                                } else {
                                    // 还有星星，播加星动画
                                    mSdvOwnLevel.starUp(mViewGroup, 0,
                                            mScoreDetailModel.getRankStarScore().getScoreNow() - 1, null);
                                }
                            }
                        });
            } else {
                // 播放星星砸满的动画
                mSdvOwnLevel.starUp(mViewGroup, mScoreDetailModel.getRankStarScore().getScoreBefore(),
                        mScoreDetailModel.getTotalStarLimit().getLimitBefore() - 1, new NormalLevelView.SVGAListener() {
                            @Override
                            public void onFinish() {
                                // 播放段位改变的动画
                                mSdvOwnLevel.levelChange(mViewGroup, mScoreDetailModel.getRankScore().getLevelBefore(), mScoreDetailModel.getSubRankScore().getLevelBefore(),
                                        mScoreDetailModel.getRankScore().getLevelNow(), mScoreDetailModel.getSubRankScore().getLevelNow(),
                                        mScoreDetailModel.getTotalStarLimit().getLimitNow(),
                                        new NormalLevelView.SVGAListener() {
                                            @Override
                                            public void onFinish() {
                                                // 判断当前星星状态
                                                if (mScoreDetailModel.getRankStarScore().getScoreNow() == 0) {
                                                    // 没有星，判断分数是否变化，播放分数动画(先不做)
                                                } else {
                                                    // 还有星星，播加星动画
                                                    mSdvOwnLevel.starUp(mViewGroup, 0,
                                                            mScoreDetailModel.getRankStarScore().getScoreNow() - 1, null);
                                                }
                                            }
                                        });
                            }
                        });
            }
        } else if (mScoreDetailModel.getStarChange() > 0) {
            // TODO: 2019/1/10 无段位变化，星星增加
            mSdvOwnLevel.starUp(mViewGroup, mScoreDetailModel.getRankStarScore().getScoreBefore(),
                    mScoreDetailModel.getRankStarScore().getScoreNow() - 1, null);
        } else if (mScoreDetailModel.getStarChange() < 0) {
            // TODO: 2019/1/10 无段位变化，星星减少
            mSdvOwnLevel.starLoss(mViewGroup, mScoreDetailModel.getRankStarScore().getScoreBefore(),
                    mScoreDetailModel.getRankStarScore().getScoreNow() - 1, null);
        }
    }

    private void battleAnimationGo() {
        if (mScoreDetailModel.hasBattleChange()) {
            mRecordCircleView.setData(0, mScoreDetailModel.getBattleTotalLimit().getLimitNow(),
                    mScoreDetailModel.getBattleRealScore().getScoreBefore(), mScoreDetailModel.getBattleRealScore().getScoreNow()
                    , mScoreDetailModel.getRankProtect().getLimitBefore());
        }
    }
}
