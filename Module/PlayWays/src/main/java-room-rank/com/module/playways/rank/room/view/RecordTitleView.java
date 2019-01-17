package com.module.playways.rank.room.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.RoomData;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.scoremodel.RankLevelModel;
import com.module.playways.rank.room.scoremodel.ScoreDetailModel;
import com.module.playways.rank.room.scoremodel.TotalLimit;
import com.module.playways.rank.room.scoremodel.UserScoreModel;
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

        if (mRoomData.getSongModel() != null) {
            mTvSongName.setText("《" + mRoomData.getSongModel().getItemName() + "》");
        }

        if (mRecordData.mScoreDetailModel != null) {
            int resId = ScoreConfigUtils.getImageResoucesScore(mRecordData.mScoreDetailModel.getBattleRatingScore());
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

    private void animationGo(ScoreDetailModel scoreDetailModel) {
        MyLog.d(TAG, "animationGo" + " scoreDetailModel=" + scoreDetailModel);
        if (scoreDetailModel == null) {
            return;
        }
        this.mScoreDetailModel = scoreDetailModel;

        // 展示之前的段位
        RankLevelModel rankLevelModel = scoreDetailModel.getRankScore();
        RankLevelModel subLevelModel = scoreDetailModel.getSubRankScore();
        TotalLimit totalLimit = scoreDetailModel.getTotalStarLimit();
        UserScoreModel userScoreModel = scoreDetailModel.getRankStarScore();
        if (rankLevelModel != null && totalLimit != null && userScoreModel != null) {
            mSdvOwnLevel.bindData(rankLevelModel.getLevelBefore(), subLevelModel.getLevelBefore()
                    , totalLimit.getLimitBefore(), userScoreModel.getScoreBefore());
            mSdvOwnLevel.postDelayed(new Runnable() {
                @Override
                public void run() {
                    battleAnimationGo();
                    scoreAnimationGo();
                }
            }, 1000);
        }
    }

    private void scoreAnimationGo() {
        // 星星和段位的动画，可以分为3段动画，星星动画，段位动画和星星动画3部分
        starAnimationFirst(new AnimationListener() {
            @Override
            public void onFinish() {
                levelAnimation(new AnimationListener() {
                    @Override
                    public void onFinish() {
                        starAnimationEnd(null);
                    }
                });
            }
        });
    }

    public interface AnimationListener {
        void onFinish();
    }

    // 段位的第一段动画，星星
    private void starAnimationFirst(AnimationListener listener) {
        if (listener == null) {
            return;
        }
        if (mScoreDetailModel.getLevelChange() == 0) {
            // 无段位变化，则无第一段动画
            listener.onFinish();
        } else if (mScoreDetailModel.getLevelChange() > 0) {
            // 升段
            if (mScoreDetailModel.getTotalStarLimit().getLimitBefore() == 0 || mScoreDetailModel.getTotalStarLimit().getLimitBefore() > 6) {
                // 星星超过限制
                listener.onFinish();
            } else if (mScoreDetailModel.getRankStarScore().getScoreBefore() == mScoreDetailModel.getTotalStarLimit().getLimitBefore()) {
                // 满星星, 则无第一段动画
                listener.onFinish();
            } else {
                // 星星砸满
                mSdvOwnLevel.starUp(mViewGroup, mScoreDetailModel.getRankStarScore().getScoreBefore(),
                        mScoreDetailModel.getTotalStarLimit().getLimitBefore() - 1, new NormalLevelView.SVGAListener() {
                            @Override
                            public void onFinish() {
                                // 第一段动画播放完成
                                listener.onFinish();
                            }
                        });
            }
        } else if (mScoreDetailModel.getLevelChange() < 0) {
            // 降段
            if (mScoreDetailModel.getTotalStarLimit().getLimitBefore() == 0 || mScoreDetailModel.getTotalStarLimit().getLimitBefore() > 6) {
                // 星星超过限制
                listener.onFinish();
            } else if (mScoreDetailModel.getRankStarScore().getScoreBefore() == 0) {
                // 之前无星，则无第一段动画
                listener.onFinish();
            } else {
                // 星星掉到0
                mSdvOwnLevel.starLoss(mViewGroup, mScoreDetailModel.getRankStarScore().getScoreBefore() - 1,
                        0, new NormalLevelView.SVGAListener() {
                            @Override
                            public void onFinish() {
                                // 第一段动画播放完成
                                listener.onFinish();
                            }
                        });
            }
        }
    }

    // 段位的第二段动画，段位
    private void levelAnimation(AnimationListener listener) {
        if (listener == null) {
            return;
        }
        if (mScoreDetailModel.getLevelChange() == 0) {
            // 无段位变化，则无第二段动画
            listener.onFinish();
        } else {
            // 有段位变化
            mSdvOwnLevel.levelChange(mViewGroup, mScoreDetailModel.getRankScore().getLevelBefore(), mScoreDetailModel.getSubRankScore().getLevelBefore(),
                    mScoreDetailModel.getRankScore().getLevelNow(), mScoreDetailModel.getSubRankScore().getLevelNow(),
                    mScoreDetailModel.getTotalStarLimit().getLimitNow(), new NormalLevelView.SVGAListener() {
                        @Override
                        public void onFinish() {
                            listener.onFinish();
                        }
                    });
        }
    }

    // 段位的第三段动画，星星
    private void starAnimationEnd(AnimationListener listener) {
        if (mScoreDetailModel.getStarChange() == 0) {
            // 无星星变化，则无第三段动画
            if (listener != null) {
                listener.onFinish();
            }
        } else if (mScoreDetailModel.getStarChange() > 0) {
            // 星星增加
            if (mScoreDetailModel.getTotalStarLimit().getLimitNow() == 0 || mScoreDetailModel.getTotalStarLimit().getLimitNow() > 6) {
                // 星星限制已经取消,直接显示几颗星即可
                mSdvOwnLevel.bindStarData(mScoreDetailModel.getTotalStarLimit().getLimitNow(), mScoreDetailModel.getRankStarScore().getScoreNow());
            } else if (mScoreDetailModel.getStarChange() >= mScoreDetailModel.getRankStarScore().getScoreNow()) {
                // 增幅超过现在有的星星数,第三段从0涨到现在
                mSdvOwnLevel.starUp(mViewGroup, 0, mScoreDetailModel.getRankStarScore().getScoreNow() - 1, new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            } else {
                // 增幅不超过现在有的星星数,第三段从之前涨到现在
                mSdvOwnLevel.starUp(mViewGroup, mScoreDetailModel.getRankStarScore().getScoreBefore(), mScoreDetailModel.getRankStarScore().getScoreNow() - 1, new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            }
        } else if (mScoreDetailModel.getStarChange() < 0) {
            // 星星减少
            if (mScoreDetailModel.getTotalStarLimit().getLimitNow() == 0 || mScoreDetailModel.getTotalStarLimit().getLimitNow() > 6) {
                // 星星限制已经取消,直接显示几颗星即可
                mSdvOwnLevel.bindStarData(mScoreDetailModel.getTotalStarLimit().getLimitNow(), mScoreDetailModel.getRankStarScore().getScoreNow());
            } else if (Math.abs(mScoreDetailModel.getStarChange()) >= mScoreDetailModel.getRankStarScore().getScoreBefore()) {
                // 减少的幅度超过之前,第三段现在的满掉到now
                mSdvOwnLevel.starLoss(mViewGroup, mScoreDetailModel.getTotalStarLimit().getLimitNow() - 1,
                        mScoreDetailModel.getRankStarScore().getScoreNow(), new NormalLevelView.SVGAListener() {
                            @Override
                            public void onFinish() {
                                if (listener != null) {
                                    listener.onFinish();
                                }
                            }
                        });
            } else {
                // 减少的幅度不超过之前,第三段现在的之前掉到now
                mSdvOwnLevel.starLoss(mViewGroup, mScoreDetailModel.getRankStarScore().getScoreBefore() - 1,
                        mScoreDetailModel.getRankStarScore().getScoreNow(), new NormalLevelView.SVGAListener() {
                            @Override
                            public void onFinish() {
                                if (listener != null) {
                                    listener.onFinish();
                                }
                            }
                        });
            }

        }
    }

    private void battleAnimationGo() {
        if (mScoreDetailModel.getBattleChange() > 0) {
            // 加分，三段动画
            if (mScoreDetailModel.getBattleChange() >= (mScoreDetailModel.getBattleTotalLimit().getLimitBefore() - mScoreDetailModel.getBattleRealScore().getScoreBefore())) {
                // 之前的表盘要走到头，然后从头开始算,三段动画，满，清空，再加
                mRecordCircleView.setData(0, mScoreDetailModel.getBattleTotalLimit().getLimitBefore(),
                        mScoreDetailModel.getBattleRealScore().getScoreBefore(), mScoreDetailModel.getBattleTotalLimit().getLimitBefore(),
                        mScoreDetailModel.getRankProtect().getLimitBefore(), new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                // 清空动画
                                step1();
                            }
                        });
            } else {
                // 当前表盘即可显示整个动画
                mRecordCircleView.setData(0, mScoreDetailModel.getBattleTotalLimit().getLimitBefore(),
                        mScoreDetailModel.getBattleRealScore().getScoreBefore(), mScoreDetailModel.getBattleRealScore().getScoreNow(),
                        mScoreDetailModel.getRankProtect().getLimitBefore(), null);
            }
        } else if (mScoreDetailModel.getBattleChange() < 0) {
            // 减分
            if (Math.abs(mScoreDetailModel.getBattleChange()) > mScoreDetailModel.getBattleRealScore().getScoreBefore()) {
                // 分数清零
                mRecordCircleView.setData(0, mScoreDetailModel.getBattleTotalLimit().getLimitBefore(),
                        mScoreDetailModel.getBattleRealScore().getScoreBefore(), 0,
                        mScoreDetailModel.getRankProtect().getLimitBefore(), new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                // TODO: 2019/1/11 产品确认换表盘逻辑 
                            }
                        });

            } else {
                // 一个表盘
                mRecordCircleView.setData(0, mScoreDetailModel.getBattleTotalLimit().getLimitBefore(),
                        mScoreDetailModel.getBattleRealScore().getScoreBefore(), mScoreDetailModel.getBattleRealScore().getScoreNow(),
                        mScoreDetailModel.getRankProtect().getLimitBefore(), null);
            }
        }
    }


    private void step1() {
        mRecordCircleView.setData(0, mScoreDetailModel.getBattleTotalLimit().getLimitBefore(),
                mScoreDetailModel.getBattleTotalLimit().getLimitBefore(), 0, mScoreDetailModel.getRankProtect().getLimitBefore(), new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                        // 判断是否还会有动画
                        step2();
                    }
                });
    }

    private void step2() {
        if (mScoreDetailModel.getBattleRealScore().getScoreNow() == 0) {
            // 战力值为0
            // TODO: 2019/1/11  产品确认

        } else {
            // 还有分要显示, 最后一段动画
            mRecordCircleView.setData(0, mScoreDetailModel.getBattleTotalLimit().getLimitNow(),
                    0, mScoreDetailModel.getBattleRealScore().getScoreNow(), mScoreDetailModel.getRankProtect().getLimitNow(), null);
        }
    }
}
