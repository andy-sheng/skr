package com.module.playways.room.room.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.room.room.comment.model.CommentLightModel;
import com.module.playways.room.room.model.RankGameConfigModel;
import com.module.playways.room.room.model.PkScoreTipMsgModel;
import com.module.playways.room.prepare.model.PlayerInfoModel;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.room.event.PkSomeOneBurstLightEvent;
import com.module.playways.room.room.event.PkSomeOneLightOffEvent;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.model.RankRoundInfoModel;
import com.module.playways.room.room.score.RobotScoreHelper;
import com.module.playways.room.room.score.bar.EnergySlotView;
import com.module.playways.room.room.score.bar.ScoreTipsView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Room.ERoundOverReason;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RankTopContainerView2 extends RelativeLayout {
    public final static String TAG = "RankTopContainerView";
    static final int MAX_USER_NUM = 3;

    private int mMode = 0; // 模式默认为0，即rank模式  1为调音间

    ExImageView mMoreBtn;
    MoreOpView mMoreOpView;
    ExImageView mIvLed;
    EnergySlotView mEnergySlotView;
    ExImageView mIvGameRole;
    DialogPlus mGameRoleDialog;

    RankTopLEDView mLeftLedView;
    RankTopLEDView mMidLedView;
    RankTopLEDView mRightLedView;

    SVGAImageView mEnergyFillSvga1;   //能量满大动画
    SVGAImageView mEnergyFillSvga2;   //能量满大动画
    SVGAImageView mEnergyFillSvga3;   //能量满大动画

    RankTopContainerView1.Listener mListener;

    ScoreTipsView.Item mLastItem;

    RankRoomData mRoomData;

    UserLightInfo mStatusArr[] = new UserLightInfo[MAX_USER_NUM];

    Handler mUiHandler = new Handler();

    static class UserLightInfo {
        int mUserId;
        LightState mLightState;
    }

    public enum LightState {
        BAO, MIE;
    }

    int mTotalScore = -1;
    int mCurScore = 0;

    public RankTopContainerView2(Context context) {
        super(context);
        init(context, null);
    }

    public RankTopContainerView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setListener(RankTopContainerView1.Listener l) {
        mListener = l;
    }

    public void setRoomData(RankRoomData roomData) {
        mRoomData = roomData;
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.topContainer);
        mMode = typedArray.getInt(R.styleable.topContainer_mode, 0);
        typedArray.recycle();

        inflate(getContext(), R.layout.rank_top_container_view, this);
        U.getSoundUtils().preLoad(TAG, R.raw.rank_xlight, R.raw.rank_xxxstop);

        mMoreBtn = this.findViewById(R.id.more_btn);
        mIvLed = (ExImageView) findViewById(R.id.iv_led);
        mEnergySlotView = (EnergySlotView) findViewById(R.id.energy_slot_view);
        mIvGameRole = (ExImageView) findViewById(R.id.iv_game_role);
        mLeftLedView = (RankTopLEDView) findViewById(R.id.left_led_view);
        mMidLedView = (RankTopLEDView) findViewById(R.id.mid_led_view);
        mRightLedView = (RankTopLEDView) findViewById(R.id.right_led_view);

        mEnergyFillSvga1 = (SVGAImageView) findViewById(R.id.energy_fill_svga1);
        mEnergyFillSvga2 = (SVGAImageView) findViewById(R.id.energy_fill_svga2);
        mEnergyFillSvga3 = (SVGAImageView) findViewById(R.id.energy_fill_svga3);

        if (mMode == 1) {
            mMoreBtn.setVisibility(GONE);
            mIvGameRole.setVisibility(GONE);
            initRankLEDViews();
        }

        mIvGameRole.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mGameRoleDialog != null) {
                    mGameRoleDialog.dismiss();
                }

                mGameRoleDialog = DialogPlus.newDialog(getContext())
                        .setContentHolder(new ViewHolder(R.layout.game_role_view_layout))
                        .setContentBackgroundResource(R.color.transparent)
                        .setOverlayBackgroundResource(R.color.black_trans_50)
                        .setExpanded(false)
                        .setGravity(Gravity.CENTER)
                        .create();

                mGameRoleDialog.show();
            }
        });

        mMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreOpView == null) {
                    mMoreOpView = new MoreOpView(getContext());
                    mMoreOpView.setListener(new MoreOpView.Listener() {
                        @Override
                        public void onClostBtnClick() {
                            if (mListener != null) {
                                mListener.closeBtnClick();
                            }
                        }

                        @Override
                        public void onVoiceChange(boolean voiceOpen) {
                            // 打开或者关闭声音 只是不听别人的声音
                            if (mListener != null) {
                                mListener.onVoiceChange(voiceOpen);
                            }
                        }

                        @Override
                        public void onClickGameRule() {

                        }
                    });
                    mMoreOpView.setRoomData(mRoomData);
                }
                mMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mGameRoleDialog != null && mGameRoleDialog.isShowing()) {
            mGameRoleDialog.dismiss();
        }
        if (mEnergyFillSvga1 != null) {
            mEnergyFillSvga1.setCallback(null);
            mEnergyFillSvga1.stopAnimation(true);
        }
        if (mEnergyFillSvga2 != null) {
            mEnergyFillSvga2.setCallback(null);
            mEnergyFillSvga2.stopAnimation(true);
        }
        if (mEnergyFillSvga3 != null) {
            mEnergyFillSvga3.setCallback(null);
            mEnergyFillSvga3.stopAnimation(true);
        }
        mUiHandler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
        U.getSoundUtils().release(TAG);
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(PkMyBurstSuccessEvent event) {
//        MyLog.d(TAG, "PkMyBurstSuccessEvent onEvent" + " event=" + event);
//        parseBurstEvent((int) MyUserInfoManager.getInstance().getUid());
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(PkMyLightOffSuccessEvent event) {
//        MyLog.d(TAG, "PkMyLightOffSuccessEvent onEvent" + " event=" + event);
//        parseLightOffEvent((int) MyUserInfoManager.getInstance().getUid());
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PkSomeOneBurstLightEvent event) {
        MyLog.d(TAG, "PkSomeOneBurstLightEvent onEvent uid " + event.uid);
        parseBurstEvent(event.uid, event.roundInfo.getUserID());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PkSomeOneLightOffEvent event) {
        MyLog.d(TAG, "PkSomeOneLightOffEvent onEvent event.uid " + event.uid);
        parseLightOffEvent(event.uid, event.roundInfo.getUserID());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RobotScoreHelper.RobotSongLineNum event) {
        MyLog.d(TAG, "onEvent" + " event=" + event.lineNum);
        setScoreProgress(999, 0, event.lineNum);
    }

    /**
     * @param uid     投票人的id
     * @param currUid 被投票人id
     */
    private void parseLightOffEvent(int uid, int currUid) {
        MyLog.d(TAG, "parseLightOffEvent onEvent 5");
        UserLightInfo ul = new UserLightInfo();
        ul.mUserId = uid;
        ul.mLightState = LightState.MIE;
        pretendLightComment(currUid, uid, false);
        if (mStatusArr[0] == null) {
            setLight(0, ul);
        } else {
            if (mStatusArr[1] == null) {
                setLight(1, ul);
            } else if (mStatusArr[2] == null) {
                setLight(2, ul);
            }
        }
        mCurScore -= mRoomData.getGameConfigModel().getpKMLightEnergyPercentage() * mTotalScore;
        tryPlayProgressAnimation();
    }

    /**
     * @param currUid 被投票者
     * @param uid     投票者
     */
    private void pretendLightComment(int currUid, int uid, boolean isBao) {
        PlayerInfoModel voter = RoomDataUtils.getPlayerInfoById(mRoomData, uid);
        PlayerInfoModel model = RoomDataUtils.getPlayerInfoById(mRoomData, currUid);
        if (voter != null && model != null) {
            CommentLightModel commentLightModel = new CommentLightModel(mRoomData.getGameType(), voter, model, isBao);
            EventBus.getDefault().post(new PretendCommentMsgEvent(commentLightModel));
        }
    }

    private void parseBurstEvent(int uid, int curUid) {
        MyLog.d(TAG, "parseBurstEvent" + " uid=" + uid);
        UserLightInfo ul = new UserLightInfo();
        ul.mUserId = uid;
        ul.mLightState = LightState.BAO;
        pretendLightComment(curUid, uid, true);
        if (mStatusArr[1] == null) {
            setLight(1, ul);
        } else {
            if (mStatusArr[0] == null) {
                setLight(0, ul);
            } else if (mStatusArr[2] == null) {
                setLight(2, ul);
            }
        }
        mCurScore += mRoomData.getGameConfigModel().getpKBLightEnergyPercentage() * mTotalScore;
        tryPlayProgressAnimation();
    }

    //轮次结束
    public void roundOver(RankRoundInfoModel lastRoundInfoModel) {
        MyLog.d(TAG, "roundOver");
        if (lastRoundInfoModel != null && lastRoundInfoModel.getOverReason() == ERoundOverReason.EROR_ENOUGH_M_LIGHT.getValue()) {
            // 多人灭灯导致演唱结束
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initRankLEDViews();
                }
            }, 2000);
        } else {
            initRankLEDViews();
        }
        mEnergySlotView.setTarget(0, null);
        for (int i = 0; i < mStatusArr.length; i++) {
            mStatusArr[i] = null;
        }
        mCurScore = 0;
        mTotalScore = -1;
    }

    private void initRankLEDViews() {
        mLeftLedView.setVisibility(GONE);
        mRightLedView.setVisibility(GONE);
        mMidLedView.playMidSVGA(new SVGAListener() {
            @Override
            public void onFinished() {
                mLeftLedView.initSVGA();
                mMidLedView.initSVGA();
                mRightLedView.initSVGA();
            }
        });
    }

    private void setLight(int index, UserLightInfo userLightInfo) {
//        if (isEmpty(mStatusArr)) {
//            mLeftLedView.setVisibility(GONE);
//            mRightLedView.setVisibility(GONE);
//            mMidLedView.setVisibility(GONE);
//        }
        if (userLightInfo.mLightState == LightState.BAO) {
            // TODO: 2019/3/22 爆灯不做效果
            return;
        }
        mStatusArr[index] = userLightInfo;
        LightState lightState = userLightInfo.mLightState;
        MyLog.d(TAG, "setLight" + " index=" + index + " lightState=" + lightState);
        switch (index) {
            case 0:
                mLeftLedView.setSVGAMode(lightState == LightState.BAO);
                if (lightState == LightState.MIE) {
                    U.getSoundUtils().play(TAG, R.raw.rank_xlight);
                }
                break;
            case 1:
                mMidLedView.setSVGAMode(lightState == LightState.BAO);
                if (lightState == LightState.MIE) {
                    U.getSoundUtils().play(TAG, R.raw.rank_xlight);
                }
                break;
            case 2:
                mLeftLedView.setVisibility(GONE);
                mMidLedView.setVisibility(GONE);
                mRightLedView.setSVGAMode(lightState == LightState.BAO);
                if (lightState == LightState.MIE) {
                    U.getSoundUtils().play(TAG, R.raw.rank_xxxstop);
                }
                break;
        }
    }

    private boolean isEmpty(UserLightInfo mStatusArr[]) {
        if (mStatusArr == null) {
            return true;
        }

        if (mStatusArr.length == 0) {
            return true;
        }

        for (UserLightInfo userLightInfo : mStatusArr) {
            if (userLightInfo != null) {
                return false;
            }
        }

        return true;
    }

    public void onGameFinish() {
        if (mMoreOpView != null) {
            mMoreOpView.dismiss();
        }
    }

    public void setScoreProgress(int score1, int curTotalScore, int lineNum) {
        MyLog.d(TAG, "setScoreProgress" + " score=" + score1 + " curTotalScore=" + curTotalScore + " lineNum=" + lineNum);
        int score = score1;
        for (int i = 0; i < 1; i++) {
            score = (int) (Math.sqrt(score) * 10);
        }

        RankGameConfigModel gameConfigModel = null;
        if (mRoomData != null) {
            gameConfigModel = mRoomData.getGameConfigModel();
        }

        ScoreTipsView.Item item = new ScoreTipsView.Item();

        if (gameConfigModel != null) {
            // 总分是这个肯定没错
            if (mTotalScore <= 0) {
                float p = gameConfigModel.getpKFullEnergyPercentage();
                if (p <= 0) {
                    p = 0.6f;
                    MyLog.w(TAG, "服务器给的getpKFullEnergyPercentage不对，为0了");
                }
                if (lineNum == 0) {
                    lineNum = 6;
                    MyLog.w(TAG, "lineNum值不对，为0了");
                }
                mTotalScore = (int) (lineNum * 100 * p);
            }
            if (score1 == 999) {
                //与ios约定，如果传递是分数是999就代表只是想告诉这首歌的总分
                return;
            }
            List<PkScoreTipMsgModel> scoreTipMsgModelList = gameConfigModel.getPkScoreTipMsgModelList();
            if (scoreTipMsgModelList != null) {
                for (PkScoreTipMsgModel m : scoreTipMsgModelList) {
                    if (score >= m.getFromScore() && score < m.getToScore()) {
                        // 命中
                        switch (m.getScoreTipTypeModel()) {
                            case ST_UNKNOWN:
                                break;
                            case ST_TOO_BAD:
                                item.setLevel(ScoreTipsView.Level.Bad);
                                break;
                            case ST_NOT_BAD:
                                item.setLevel(ScoreTipsView.Level.Ok);
                                break;
                            case ST_VERY_GOOD:
                                item.setLevel(ScoreTipsView.Level.Good);
                                break;
                            case ST_NICE_PERFECT:
                                item.setLevel(ScoreTipsView.Level.Perfect);
                                break;
                        }
                        break;
                    }
                }
            }
            mCurScore += score;
            tryPlayProgressAnimation();
        } else {
            if (mTotalScore <= 0 && mMode == 1) {
                mTotalScore = (int) (lineNum * 100 * 0.6);
            }

            mCurScore += score;
            tryPlayProgressAnimation();

            if (score >= 95) {
                item.setLevel(ScoreTipsView.Level.Perfect);
            } else if (score >= 85) {
                item.setLevel(ScoreTipsView.Level.Good);
            } else if (score >= 60) {
                item.setLevel(ScoreTipsView.Level.Ok);
            } else if (score < 20) {
                item.setLevel(ScoreTipsView.Level.Bad);
            }
        }

        if (item.getLevel() != null) {
            if (mLastItem != null && item.getLevel() == mLastItem.getLevel()) {
                item.setNum(mLastItem.getNum() + 1);
            }
            mLastItem = item;
            ScoreTipsView.play(this, item);
        }
    }

    void tryPlayProgressAnimation() {
        MyLog.d(TAG, "tryPlayProgressAnimation mCurScore:" + mCurScore);

        if (mCurScore < 0) {
            mCurScore = 0;
        }
        if (mCurScore / mTotalScore >= 1) {
            // 能量槽满了,要触发大动画了
            mCurScore = mCurScore % mTotalScore;
            int progress = mCurScore * 100 / mTotalScore;
            playEnergyFillAnimation(mEnergyFillSvga1, "rank_fill_energy2.svga");
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playEnergyFillAnimation(mEnergyFillSvga2, "rank_fill_energy1.svga");
                }
            }, 200);
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playEnergyFillAnimation(mEnergyFillSvga3, "rank_fill_energy2.svga");
                }
            }, 1000);
            mEnergySlotView.setTarget(0, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mEnergySlotView.setTarget(progress, null);
                }
            });
        } else {
            mCurScore = mCurScore % mTotalScore;
            int progress = mCurScore * 100 / mTotalScore;
            mEnergySlotView.setTarget(progress, null);
        }
    }

    void playEnergyFillAnimation(SVGAImageView mEnergyFillSvga, String assetsName) {
        MyLog.d(TAG, "playFullEnergyAnimation");
        mEnergyFillSvga.setCallback(null);
        mEnergyFillSvga.stopAnimation(true);
        mEnergyFillSvga.setVisibility(VISIBLE);
        mEnergyFillSvga.setLoops(1);

        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    mEnergyFillSvga.setImageDrawable(drawable);
                    mEnergyFillSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }


        mEnergyFillSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mEnergyFillSvga != null) {
                    mEnergyFillSvga.setCallback(null);
                    mEnergyFillSvga.stopAnimation(true);
                    mEnergyFillSvga.setVisibility(GONE);
                }
            }

            @Override
            public void onRepeat() {
                if (mEnergyFillSvga != null && mEnergyFillSvga.isAnimating()) {
                    mEnergyFillSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    public void reset() {
        mTotalScore = -1;
        mCurScore = 0;
        tryPlayProgressAnimation();
    }


}
