package com.module.playways.rank.room.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.playways.RoomData;
import com.module.playways.rank.prepare.model.GameConfigModel;
import com.module.playways.rank.prepare.model.PkScoreTipMsgModel;
import com.module.playways.rank.room.event.PkSomeOneBurstLightEvent;
import com.module.playways.rank.room.event.PkSomeOneLightOffEvent;
import com.module.playways.rank.room.score.RobotScoreHelper;
import com.module.playways.rank.room.score.bar.EnergySlotView;
import com.module.playways.rank.room.score.bar.ScoreTipsView;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class RankTopContainerView2 extends RelativeLayout {
    public final static String TAG = "RankTopContainerView";
    static final int MAX_USER_NUM = 3;
    ExImageView mMoreBtn;
    MoreOpView mMoreOpView;
    ExImageView mIvLed;
    ExImageView mIvLeft;
    ExImageView mIvCenter;
    ExImageView mIvRignt;
    EnergySlotView mEnergySlotView;
    ExImageView mIvGameRole;
    DialogPlus mGameRoleDialog;

    RankTopLEDView mLeftLedView;
    RankTopLEDView mMidLedView;
    RankTopLEDView mRightLedView;

    RankTopContainerView1.Listener mListener;

    ScoreTipsView.Item mLastItem;

    RoomData mRoomData;

    UserLightInfo mStatusArr[] = new UserLightInfo[MAX_USER_NUM];

    static class UserLightInfo {
        int mUserId;
        LightState mLightState;
    }

    public enum LightState {
        BAO, MIE;
    }

    int mTotalScore = -1;
    int mCurScore;

    public RankTopContainerView2(Context context) {
        super(context);
        init();
    }

    public RankTopContainerView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setListener(RankTopContainerView1.Listener l) {
        mListener = l;
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    private void init() {
        inflate(getContext(), R.layout.rank_top_container_view, this);
        mMoreBtn = this.findViewById(R.id.more_btn);
        mIvLed = (ExImageView) findViewById(R.id.iv_led);
        mIvLeft = (ExImageView) findViewById(R.id.iv_left);
        mIvCenter = (ExImageView) findViewById(R.id.iv_center);
        mIvRignt = (ExImageView) findViewById(R.id.iv_rignt);
        mEnergySlotView = (EnergySlotView) findViewById(R.id.energy_slot_view);
        mIvGameRole = (ExImageView) findViewById(R.id.iv_game_role);
        mLeftLedView = (RankTopLEDView) findViewById(R.id.left_led_view);
        mMidLedView = (RankTopLEDView) findViewById(R.id.mid_led_view);
        mRightLedView = (RankTopLEDView) findViewById(R.id.right_led_view);

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
        EventBus.getDefault().unregister(this);
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
        parseBurstEvent(event.uid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PkSomeOneLightOffEvent event) {
        MyLog.d(TAG, "PkSomeOneLightOffEvent onEvent event.uid " + event.uid);
        parseLightOffEvent(event.uid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RobotScoreHelper.RobotSongLineNum event) {
        MyLog.d(TAG,"onEvent" + " event=" + event.lineNum);
        setScoreProgress(999, 0, event.lineNum);
    }


    private void parseLightOffEvent(int uid) {
        MyLog.d(TAG, "parseLightOffEvent onEvent 5");
        UserLightInfo ul = new UserLightInfo();
        ul.mUserId = uid;
        ul.mLightState = LightState.MIE;
        if (mStatusArr[0] == null) {
            setLight(0, ul);
        } else {
            if (mStatusArr[1] == null) {
                setLight(1, ul);
            } else if (mStatusArr[2] == null) {
                setLight(2, ul);
            }
        }
        mCurScore -= mRoomData.getGameConfigModel().getpKBLightEnergyPercentage() * mTotalScore;
        tryPlayProgressAnimation();
    }

    private void parseBurstEvent(int uid) {
        MyLog.d(TAG, "parseBurstEvent" + " uid=" + uid);
        UserLightInfo ul = new UserLightInfo();
        ul.mUserId = uid;
        ul.mLightState = LightState.BAO;
        if (mStatusArr[1] == null) {
            setLight(1, ul);
        } else {
            if (mStatusArr[0] == null) {
                setLight(0, ul);
            } else if (mStatusArr[2] == null) {
                setLight(2, ul);
            }
        }
        mCurScore += mRoomData.getGameConfigModel().getpKMLightEnergyPercentage() * mTotalScore;
        tryPlayProgressAnimation();
    }

    //轮次结束
    public void roundOver() {
        MyLog.d(TAG, "roundOver");
        mIvLeft.setImageDrawable(U.getDrawable(R.drawable.yanchang_xiaolian));
        mIvCenter.setImageDrawable(U.getDrawable(R.drawable.yanchang_xiaolian));
        mIvRignt.setImageDrawable(U.getDrawable(R.drawable.yanchang_xiaolian));

        mLeftLedView.initSVGA();
        mMidLedView.initSVGA();
        mRightLedView.initSVGA();
        mEnergySlotView.setTarget(0, null);
        for (int i = 0; i < mStatusArr.length; i++) {
            mStatusArr[i] = null;
        }
        mCurScore = 0;
        mTotalScore = -1;
    }

    private void setLight(int index, UserLightInfo userLightInfo) {
        mStatusArr[index] = userLightInfo;
        LightState lightState = userLightInfo.mLightState;
        MyLog.d(TAG, "setLight" + " index=" + index + " lightState=" + lightState);
        switch (index) {
            case 0:
                mIvLeft.setImageDrawable(lightState == LightState.BAO ? U.getDrawable(R.drawable.yanchang_bao) : U.getDrawable(R.drawable.yanchang_mie));
                mLeftLedView.setSVGAMode(lightState == LightState.BAO);
                break;
            case 1:
                mIvCenter.setImageDrawable(lightState == LightState.BAO ? U.getDrawable(R.drawable.yanchang_bao) : U.getDrawable(R.drawable.yanchang_mie));
                mMidLedView.setSVGAMode(lightState == LightState.BAO);
                break;
            case 2:
                mIvRignt.setImageDrawable(lightState == LightState.BAO ? U.getDrawable(R.drawable.yanchang_bao) : U.getDrawable(R.drawable.yanchang_mie));
                mRightLedView.setSVGAMode(lightState == LightState.BAO);
                break;
        }
    }

    public void onGameFinish() {
        if (mMoreOpView != null) {
            mMoreOpView.dismiss();
        }
    }

    public void setScoreProgress(int score, int curTotalScore, int lineNum) {
        MyLog.d(TAG, "setScoreProgress" + " score=" + score + " curTotalScore=" + curTotalScore + " lineNum=" + lineNum);
        for (int i = 0; i < 1; i++) {
            score = (int) (Math.sqrt(score) * 10);
        }
        GameConfigModel gameConfigModel = mRoomData.getGameConfigModel();
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
            if (score == 999) {
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
            if (score >= 95) {
                item.setLevel(ScoreTipsView.Level.Perfect);
            } else if (score >= 90) {
                item.setLevel(ScoreTipsView.Level.Good);
            } else if (score >= 70) {
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
}
