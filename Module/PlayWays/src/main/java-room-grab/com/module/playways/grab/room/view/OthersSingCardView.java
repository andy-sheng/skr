package com.module.playways.grab.room.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.greenrobot.eventbus.EventBus;

/**
 * 其他人主场景收音机
 */
public class OthersSingCardView extends RelativeLayout {
    public final static String TAG = "OthersSingCardView";
    final static int MSG_ENSURE_PLAY = 1;

    final static int COUNT_DOWN_STATUS_INIT = 1;
    final static int COUNT_DOWN_STATUS_WAIT = 2;
    final static int COUNT_DOWN_STATUS_PLAYING = 3;

    int mCountDownStatus = COUNT_DOWN_STATUS_INIT;

    int mUseId;   // 当前唱歌人的id

    SVGAImageView mGrabStageView;
    BaseImageView mSingAvatarView;
    CircleCountDownView mCircleCountDownView;
    ExTextView mTvSingerName;

    AlphaAnimation mEnterAlphaAnimation;                // 进场动画
    TranslateAnimation mLeaveTranslateAnimation;   // 出场动画

    GrabRoomData mGrabRoomData;

    Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_ENSURE_PLAY){
                tryStartCountDown();
            }
        }
    };

    boolean mHasPlayFullAnimation = false;
    boolean mCanStartFlag = false;

    public OthersSingCardView(Context context) {
        super(context);
        init(context);
    }

    public OthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.grab_others_sing_card_layout, this);
        mGrabStageView = (SVGAImageView) findViewById(R.id.grab_stage_view);
        mSingAvatarView = (BaseImageView) findViewById(R.id.sing_avatar_view);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);
        mTvSingerName = (ExTextView) findViewById(R.id.tv_singer_name);

        mSingAvatarView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mUseId != 0) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mUseId));
                }
            }
        });
    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
    }

    public void bindData(UserInfoModel userInfoModel) {
        mUiHandler.removeCallbacksAndMessages(null);
        mHasPlayFullAnimation = false;
        setVisibility(VISIBLE);
        if (userInfoModel != null) {
            this.mUseId = userInfoModel.getUserId();
            AvatarUtils.loadAvatarByUrl(mSingAvatarView,
                    AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(5))
                            .setCircle(true)
                            .build());
            mTvSingerName.setText(userInfoModel.getNickname());
        } else {
            MyLog.w(TAG, "userInfoModel==null 加载选手信息失败");
        }
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
        mCircleCountDownView.setProgress(0);
        // 淡出效果
        if (mEnterAlphaAnimation == null) {
            mEnterAlphaAnimation = new AlphaAnimation(0f, 1f);
            mEnterAlphaAnimation.setDuration(1000);
        }
        this.startAnimation(mEnterAlphaAnimation);

        mGrabStageView.setVisibility(View.VISIBLE);
        mGrabStageView.setLoops(1);

        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse("grab_main_stage.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mGrabStageView.setLoops(-1);
                    mGrabStageView.setImageDrawable(drawable);
                    mGrabStageView.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }

        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            countDown("中途进来");
        } else {
            mCircleCountDownView.cancelAnim();
            mCircleCountDownView.setMax(360);
            mCircleCountDownView.setProgress(0);
            mUiHandler.removeMessages(MSG_ENSURE_PLAY);
            mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 3000);
        }
    }

    public void tryStartCountDown() {
        MyLog.d(TAG, "tryStartCountDown");
        mCanStartFlag = true;
        mUiHandler.removeMessages(MSG_ENSURE_PLAY);
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            countDownAfterAnimation("tryStartCountDown");
        }
    }

    //给所有倒计时加上一个前面到满的动画
    private void countDownAfterAnimation(String from) {
        MyLog.d(TAG, "countDownAfterAnimation from=" + from);
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }

        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            countDown("中途进来");
        } else {
            countDown("else full Animation");
        }
    }

    private void countDown(String from) {
        MyLog.d(TAG, "countDown" + " from=" + from);
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }
        int totalMs = grabRoundInfoModel.getSingEndMs() - grabRoundInfoModel.getSingBeginMs();
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            MyLog.d(TAG, "countDown mCountDownStatus == COUNT_DOWN_STATUS_WAIT");
            if (mCanStartFlag) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            } else {
                // 不需要播放countdown
//                mCountDownProcess.startCountDown(0, totalMs);
                mCircleCountDownView.go(0, totalMs);
                return;
            }
        }

        int progress;  //当前进度条
        int leaveTime; //剩余时间
        MyLog.d(TAG, "countDown isParticipant:" + grabRoundInfoModel.isParticipant() + " enterStatus=" + grabRoundInfoModel.getEnterStatus());
        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
            progress = grabRoundInfoModel.getElapsedTimeMs() * 100 / totalMs;
            leaveTime = totalMs - grabRoundInfoModel.getElapsedTimeMs();
        } else {
            progress = 1;
            leaveTime = totalMs;
        }
//        mCountDownProcess.startCountDown(progress, leaveTime);
        mCircleCountDownView.go(progress, leaveTime);
    }

    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
            this.startAnimation(mLeaveTranslateAnimation);
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            clearAnimation();
            setVisibility(GONE);
        }
        mCircleCountDownView.cancelAnim();
        mCircleCountDownView.setMax(360);
        mCircleCountDownView.setProgress(0);

        mUiHandler.removeMessages(MSG_ENSURE_PLAY);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mCountDownStatus = COUNT_DOWN_STATUS_INIT;
            mCanStartFlag = false;
            mHasPlayFullAnimation = false;
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterAlphaAnimation != null) {
            mEnterAlphaAnimation.setAnimationListener(null);
            mEnterAlphaAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
        if(mUiHandler != null){
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }
}
