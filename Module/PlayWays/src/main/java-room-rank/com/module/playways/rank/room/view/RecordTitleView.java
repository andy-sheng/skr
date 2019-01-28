package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.RoomData;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.module.playways.rank.room.model.score.ScoreStateModel;
import com.module.playways.rank.room.utils.ScoreAnimationHelp;
import com.module.playways.rank.room.utils.ScoreConfigUtils;
import com.module.rank.R;
import com.zq.level.view.NormalLevelView;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class RecordTitleView extends RelativeLayout {

    public final static String TAG = "RecordTitleView";

    RoomData mRoomData;

    ScoreResultModel scoreResultModel;

    RecordData mRecordData;

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

            animationGo(recordData.mScoreResultModel);
        }

        mTvSongName = (ExTextView) findViewById(R.id.tv_song_name);
        mTvSongScore = (ExImageView) findViewById(R.id.tv_song_score);

        mIvOwnRecord = (ExImageView) findViewById(R.id.iv_own_record);

        if (mRoomData.getSongModel() != null) {
            mTvSongName.setText("《" + mRoomData.getSongModel().getItemName() + "》");
        }

        if (mRecordData.mScoreResultModel != null) {
            int resId = ScoreConfigUtils.getImageResoucesScore(mRecordData.mScoreResultModel.getSss());
            if (resId != 0) {
                mTvSongScore.setBackground(getResources().getDrawable(resId));
            }
        }

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
                            case 1:
                                drawable = getResources().getDrawable(R.drawable.diyiming_huizhang);
                                str = "冠军";
                                break;
                            case 2:
                                drawable = getResources().getDrawable(R.drawable.dierming_huizhang);
                                str = "亚军";
                                break;
                            case 3:
                                drawable = getResources().getDrawable(R.drawable.disanming_huizhang);
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

    private void animationGo(ScoreResultModel scoreResultModel) {
        MyLog.d(TAG, "animationGo" + " ScoreResultModel = " + scoreResultModel);
        if (scoreResultModel == null || scoreResultModel.getStates() == null || scoreResultModel.getStates().size() <= 0) {
            return;
        }

        this.scoreResultModel = scoreResultModel;
        ScoreStateModel before = scoreResultModel.getStates().get(0);
        if (before != null) {
            mSdvOwnLevel.bindData(before.getMainRanking(), before.getSubRanking(), before.getMaxStar(), before.getCurrStar());
            if (before.getMaxBattleIndex() == 0) {
                // TODO: 2019/1/22 满级
                mRecordCircleView.fullLevel();
            } else {
                mRecordCircleView.setData(0, before.getMaxBattleIndex(), before.getCurrBattleIndex(), before.getCurrBattleIndex(), before.getProtectBattleIndex(), null);
            }

            mSdvOwnLevel.postDelayed(new Runnable() {
                @Override
                public void run() {
                    step1();
                }
            }, 1000);
        }
    }

    private void step1() {
        ScoreAnimationHelp.starChangeAnimation(mSdvOwnLevel, mViewGroup,
                scoreResultModel.getStates().get(0), scoreResultModel.getStates().get(1),
                new ScoreAnimationHelp.AnimationListener() {
                    @Override
                    public void onFinish() {
                        step2();
                    }
                });
    }

    private void step2() {
        ScoreAnimationHelp.battleChangeAnimation(mRecordCircleView, scoreResultModel,
                scoreResultModel.getStates().get(0), scoreResultModel.getStates().get(2),
                new ScoreAnimationHelp.AnimationListener() {
                    @Override
                    public void onFinish() {
                        step3();
                    }
                });
    }

    private void step3() {
        ScoreAnimationHelp.starChangeAnimation(mSdvOwnLevel, mViewGroup,
                scoreResultModel.getStates().get(1), scoreResultModel.getStates().get(2),
                new ScoreAnimationHelp.AnimationListener() {
                    @Override
                    public void onFinish() {
                    }
                });
    }


}
