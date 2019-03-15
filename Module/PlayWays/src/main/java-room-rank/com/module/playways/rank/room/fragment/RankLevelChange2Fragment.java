package com.module.playways.rank.room.fragment;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.RouterConstants;
import com.module.playways.BaseRoomData;
import com.module.playways.rank.room.RankRoomData;
import com.module.playways.rank.room.RoomServerApi;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.score.ScoreItemModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.module.playways.rank.room.model.score.ScoreStateModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.level.utils.LevelConfigUtils;
import com.zq.level.view.LevelStarProgressBar;
import com.zq.live.proto.Room.EExpWhy;
import com.zq.live.proto.Room.EWinType;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.List;

import static android.view.View.ALPHA;
import static android.view.View.VISIBLE;

public class RankLevelChange2Fragment extends BaseFragment {

    public final static String TAG = "RankLevelChange2Fragment";

    RelativeLayout mMainActContainer;
    ImageView mBackgroundIv;
    SVGAImageView mResultSvga;
    RelativeLayout mRankArea;
    ExTextView mLevelDescTv;
    LevelStarProgressBar mLevelProgress;
    ExTextView mResultText;
    ExTextView mResultNum;
    ExTextView mExperTextTv;
    ExTextView mExperNumTv;

    RankRoomData mRoomData;

    RotateAnimation mBgAnimation;    //背景转动动画
    ObjectAnimator mAlphAnimation;   //文字和进度条出现动画

    Handler mUiHanlder;

    ScoreStateModel mScoreStateModel;

    ScoreResultModel scoreResultModel;

    @Override
    public int initView() {
        return R.layout.rank_level2_change_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mBackgroundIv = (ImageView) mRootView.findViewById(R.id.background_iv);
        mResultSvga = (SVGAImageView) mRootView.findViewById(R.id.result_svga);
        mRankArea = (RelativeLayout) mRootView.findViewById(R.id.rank_area);
        mLevelDescTv = (ExTextView) mRootView.findViewById(R.id.level_desc_tv);
        mLevelProgress = (LevelStarProgressBar) mRootView.findViewById(R.id.level_progress);
        mResultText = (ExTextView) mRootView.findViewById(R.id.result_text);
        mResultNum = (ExTextView) mRootView.findViewById(R.id.result_num);
        mExperTextTv = (ExTextView) mRootView.findViewById(R.id.exper_text_tv);
        mExperNumTv = (ExTextView) mRootView.findViewById(R.id.exper_num_tv);

        mUiHanlder = new Handler();

        if (mRoomData != null && mRoomData.getRecordData() != null) {
            animationGo();
        } else {
            getGameResult();
        }

        // 加入保护，最多
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                goVoiceRoom("proctec");
            }
        }, 6000);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 1) {
            mRoomData = (RankRoomData) data;
        }
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

    private void animationGo() {
        boolean isWin = false;
        String url = "";
        scoreResultModel = mRoomData.getRecordData().mScoreResultModel;
        mScoreStateModel = mRoomData.getRecordData().mScoreResultModel.getSeq(3);
        if (mRoomData.getRecordData().getSelfWinType() == EWinType.Win.getValue()) {
            BgAnimationGo();
            url = BaseRoomData.RANK_RESULT_WIN_SVGA;
            isWin = true;
        } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Draw.getValue()) {
            url = BaseRoomData.RANK_RESULT_DRAW_SVGA;
            isWin = false;
        } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Lose.getValue()) {
            url = BaseRoomData.RANK_RESULT_LOSE_SVGA;
            isWin = false;
        }

        mResultSvga.setVisibility(VISIBLE);
        mResultSvga.setLoops(1);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            final boolean finalIsWin = isWin;
            parser.parse(new URL(url), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity, requestDynamicItem());
                    mResultSvga.setImageDrawable(drawable);
                    mResultSvga.startAnimation();
                    if (finalIsWin) {
                        U.getSoundUtils().play(TAG, R.raw.rank_win);
                    } else {
                        U.getSoundUtils().play(TAG, R.raw.rank_lose);
                    }

                    mUiHanlder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showOtherViews();
                        }
                    }, 1000);

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
                    mResultSvga.stopAnimation(false);
                }

                goVoiceRoom("endAnimation");
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
    }

    private SVGADynamicEntity requestDynamicItem() {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();

        HttpImage image = ImageFactory.newHttpImage(MyUserInfoManager.getInstance().getAvatar())
                .addOssProcessors(OssImgFactory.newResizeBuilder()
                                .setW(ImageUtils.SIZE.SIZE_160.getW())
                                .build()
                        , OssImgFactory.newCircleBuilder()
                                .setR(500)
                                .build()
                )
                .build();
        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
        if (file != null && file.exists()) {
            dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar");
        } else {
            dynamicEntity.setDynamicImage(image.getUrl(), "avatar");
        }
        dynamicEntity.setDynamicImage(BitmapFactory.decodeResource(getResources(), LevelConfigUtils.getImageResoucesLevel(mScoreStateModel.getMainRanking())), "keyMedalNew");
        dynamicEntity.setDynamicImage(BitmapFactory.decodeResource(getResources(),
                LevelConfigUtils.getImageResoucesSubLevel(mScoreStateModel.getMainRanking(), mScoreStateModel.getSubRanking())),
                "keyLevelNew");


        return dynamicEntity;
    }

    private void BgAnimationGo() {
        mBackgroundIv.setVisibility(VISIBLE);
        mBgAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mBgAnimation.setDuration(5000);
        mBgAnimation.setInterpolator(new LinearInterpolator());
        mBgAnimation.setRepeatMode(Animation.RESTART);
        mBgAnimation.setRepeatCount(Animation.INFINITE);
        mBackgroundIv.startAnimation(mBgAnimation);
    }

    private void showOtherViews() {
        MyLog.d(TAG, "showOtherViews" + mScoreStateModel);
        mRankArea.setVisibility(VISIBLE);
        mLevelDescTv.setText(mScoreStateModel.getRankingDesc());
        int progress = 0;
        if (mScoreStateModel.getMaxStar() != 0) {
            progress = mScoreStateModel.getCurrExp() * 100 / mScoreStateModel.getMaxExp();
        }
        mLevelProgress.setCurProgress(progress);
        for (ScoreItemModel scoreItemModel : scoreResultModel.getExpChange()) {
            if (scoreItemModel.getIndex() == EExpWhy.GameWin.getValue()) {
                mResultText.setText("胜利:");
                if (scoreItemModel.getScore() >= 0) {
                    mResultNum.setText("+" + scoreItemModel.getScore());
                } else {
                    mResultNum.setText("" + scoreItemModel.getScore());
                }
            } else if (scoreItemModel.getIndex() == EExpWhy.GameLose.getValue()) {
                mResultText.setText("失败:");
                if (scoreItemModel.getScore() >= 0) {
                    mResultNum.setText("+" + scoreItemModel.getScore());
                } else {
                    mResultNum.setText("" + scoreItemModel.getScore());
                }
            } else if (scoreItemModel.getIndex() == EExpWhy.GameBonus.getValue()) {
                if (scoreItemModel.getScore() >= 0) {
                    mExperNumTv.setText("+" + scoreItemModel.getScore());
                } else {
                    mExperNumTv.setText("" + scoreItemModel.getScore());
                }

            }
        }

        mAlphAnimation = ObjectAnimator.ofFloat(mRankArea, ALPHA, 0f, 1f);
        mAlphAnimation.setDuration(1000);
        mAlphAnimation.start();
    }

    private void goVoiceRoom(String from) {
        MyLog.d(TAG, "goVoiceRoom" + " from=" + from);
        if (mUiHanlder != null) {
            mUiHanlder.removeCallbacksAndMessages(null);
        }
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity != null) {
                    mRoomData.setHasGoVoiceRoom(true);
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_VOICEROOM)
                            .withSerializable("voice_room_data", mRoomData)
                            .navigation();
                    activity.finish();
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK),
                            StatConstants.KEY_GAME_FINISH, null);
                }
            }
        }, 1000);
    }

    @Override
    public void destroy() {
        super.destroy();
        mUiHanlder.removeCallbacksAndMessages(null);
        if (mResultSvga != null) {
            mResultSvga.setCallback(null);
            mResultSvga.stopAnimation(false);
        }
        if (mBgAnimation != null) {
            mBgAnimation.cancel();
        }
        if (mAlphAnimation != null) {
            mAlphAnimation.cancel();
        }
        U.getSoundUtils().release(TAG);
    }

    @Override
    protected boolean onBackPressed() {
        return true;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }


}
