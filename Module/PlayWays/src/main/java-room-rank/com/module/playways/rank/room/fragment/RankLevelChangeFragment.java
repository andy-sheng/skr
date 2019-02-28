package com.module.playways.rank.room.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.BaseRoomData;
import com.module.playways.rank.room.RankRoomData;
import com.module.playways.rank.room.RoomServerApi;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.module.playways.rank.room.model.score.ScoreStateModel;
import com.module.playways.rank.room.utils.ScoreAnimationHelp;
import com.module.playways.rank.room.view.RecordCircleView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.level.view.NormalLevelView;
import com.zq.live.proto.Room.EWinType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static android.view.View.VISIBLE;

public class RankLevelChangeFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    ImageView mBackgroundIv;
    SVGAImageView mResultSvga;
    NormalLevelView mLevelView;
    RecordCircleView mRecordCircleView;

    RotateAnimation mBgAnimation;

    RankRoomData mRoomData;

    ScoreResultModel scoreResultModel;

    Handler mUiHanlder;

    @Override
    public int initView() {
        return R.layout.rank_level_change_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mBackgroundIv = (ImageView) mRootView.findViewById(R.id.background_iv);
        mResultSvga = (SVGAImageView) mRootView.findViewById(R.id.result_svga);
        mLevelView = (NormalLevelView) mRootView.findViewById(R.id.level_view);
        mRecordCircleView = (RecordCircleView) mRootView.findViewById(R.id.record_circle_view);

        mUiHanlder = new Handler();

        BgAnimationGo();
        if (mRoomData != null && mRoomData.getRecordData() != null) {
            animationGo();
        } else {
            getGameResult();
        }
        U.getSoundUtils().preLoad(NormalLevelView.TAG, R.raw.result_addstar,
                R.raw.result_deductstar, R.raw.song_pairbutton);

    }

    private void BgAnimationGo() {
        mBgAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mBgAnimation.setDuration(5000);
        mBgAnimation.setInterpolator(new LinearInterpolator());
        mBgAnimation.setRepeatMode(Animation.RESTART);
        mBgAnimation.setRepeatCount(Animation.INFINITE);
        mBackgroundIv.startAnimation(mBgAnimation);
    }

    private void getGameResult() {
        RoomServerApi roomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);
        ApiMethods.subscribe(roomServerApi.getVoteResult(mRoomData.getGameId()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<VoteInfoModel> voteInfoModelList = JSON.parseArray(result.getData().getString("voteInfo"), VoteInfoModel.class);
                    List<ScoreResultModel> scoreResultModels = JSON.parseArray(result.getData().getString("userScoreResult"), ScoreResultModel.class);
                    List<UserGameResultModel> userGameResults = JSON.parseArray(result.getData().getString("userGameResult"), UserGameResultModel.class);

                    // TODO: 2019/2/21 结果会由 scoreResultModels 和 userGameResults来呈现
                    if (scoreResultModels != null && scoreResultModels.size() > 0) {
                        ScoreResultModel myScoreResultModel = new ScoreResultModel();
                        for (ScoreResultModel scoreResultModel : scoreResultModels) {
                            if (scoreResultModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                                myScoreResultModel = scoreResultModel;
                            }
                        }
                        MyLog.d(TAG, " getVoteResult " + " voteInfoModelList " + voteInfoModelList.toString());
                        MyLog.d(TAG, " getVoteResult " + " scoreResultModel " + myScoreResultModel.toString());
                        MyLog.d(TAG, " getVoteResult " + " UserGameResultModel " + userGameResults.toString());
                        mRoomData.setRecordData(new RecordData(voteInfoModelList, myScoreResultModel, userGameResults));
                        animationGo();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 1) {
            mRoomData = (RankRoomData) data;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mUiHanlder != null) {
            mUiHanlder.removeCallbacksAndMessages(null);
        }
        if (mBgAnimation != null) {
            mBgAnimation.cancel();
        }
        U.getSoundUtils().release(NormalLevelView.TAG);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    private void animationGo() {
        String assetsName = "";
        if (mRoomData.getRecordData().getSelfWinType() == EWinType.Win.getValue()) {
            mBackgroundIv.setBackground(getResources().getDrawable(R.drawable.zhanji_win_guangquan));
            assetsName = "rank_result_win.svga";
        } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Draw.getValue()) {
            mBackgroundIv.setBackground(getResources().getDrawable(R.drawable.zhanji_draw_guangquan));
            assetsName = "rank_result_draw.svga";
        } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Lose.getValue()) {
            mBackgroundIv.setBackground(getResources().getDrawable(R.drawable.zhanji_lose_guangquan));
            assetsName = "rank_result_lose.svga";
        }

        mResultSvga.setVisibility(VISIBLE);
        mResultSvga.setLoops(1);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    mResultSvga.setImageDrawable(drawable);
                    mResultSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }

        mResultSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mResultSvga != null) {
                    mResultSvga.setCallback(null);
                    mResultSvga.stopAnimation(true);
                    mResultSvga.setVisibility(View.GONE);

                    mLevelView.setVisibility(VISIBLE);
                    mRecordCircleView.setVisibility(VISIBLE);
                    levelAnimationGo(mRoomData.getRecordData().mScoreResultModel);
                }
            }

            @Override
            public void onRepeat() {
                if (mResultSvga != null && mResultSvga.isAnimating()) {
                    mResultSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });

        // 加入保护，最多
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                goVoiceRoom();
            }
        }, 5000);
    }


    private void levelAnimationGo(ScoreResultModel scoreResultModel) {
        MyLog.d(TAG, "animationGo" + " ScoreResultModel = " + scoreResultModel);
        if (scoreResultModel == null || scoreResultModel.getStates() == null || scoreResultModel.getStates().size() <= 0) {
            return;
        }

        this.scoreResultModel = scoreResultModel;
        ScoreStateModel before = scoreResultModel.getStates().get(0);
        if (before != null) {
            mLevelView.bindData(before.getMainRanking(), before.getSubRanking(), before.getMaxStar(), before.getCurrStar());
            if (before.getMaxBattleIndex() == 0) {
                // TODO: 2019/1/22 满级
                mRecordCircleView.fullLevel();
            } else {
                mRecordCircleView.setData(0, before.getMaxBattleIndex(), before.getCurrBattleIndex(), before.getCurrBattleIndex(), before.getProtectBattleIndex(), null);
            }

            mLevelView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    step1();
                }
            }, 1000);
        }
    }

    private void step1() {
        ScoreAnimationHelp.starChangeAnimation(mLevelView, mMainActContainer,
                scoreResultModel.getSeq(1), scoreResultModel.getSeq(2),
                new ScoreAnimationHelp.AnimationListener() {
                    @Override
                    public void onFinish() {
                        step2();
                    }
                });
    }

    private void step2() {
        ScoreAnimationHelp.battleChangeAnimation(mRecordCircleView, scoreResultModel,
                scoreResultModel.getSeq(1), scoreResultModel.getSeq(3),
                new ScoreAnimationHelp.AnimationListener() {
                    @Override
                    public void onFinish() {
                        step3();
                    }
                });
    }

    private void step3() {
        ScoreAnimationHelp.starChangeAnimation(mLevelView, mMainActContainer,
                scoreResultModel.getSeq(2), scoreResultModel.getSeq(3),
                new ScoreAnimationHelp.AnimationListener() {
                    @Override
                    public void onFinish() {
                        goVoiceRoom();
                    }
                });
    }

    private void goVoiceRoom() {
        MyLog.d(TAG, "goVoiceRoom" + mRoomData);
        if (mUiHanlder != null) {
            mUiHanlder.removeCallbacksAndMessages(null);
        }
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_VOICEROOM)
                            .withSerializable("voice_room_data", mRoomData)
                            .navigation();
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK),
                            StatConstants.KEY_GAME_FINISH, null);
                }
            }
        }, 1000);
    }

    @Override
    protected boolean onBackPressed() {
        return true;
    }
}
