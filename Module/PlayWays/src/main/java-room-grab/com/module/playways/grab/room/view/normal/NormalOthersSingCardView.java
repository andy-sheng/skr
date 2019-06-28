package com.module.playways.grab.room.view.normal;


import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExTextView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.zq.person.event.ShowPersonCardEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.common.view.ExViewStub;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Room.EQRoundStatus;

import org.greenrobot.eventbus.EventBus;

/**
 * 其他人主场景
 */
public class NormalOthersSingCardView extends ExViewStub {
    public final static String TAG = "OthersSingCardView";
    final static int MSG_ENSURE_PLAY = 1;

    final static int COUNT_DOWN_STATUS_WAIT = 2;
    final static int COUNT_DOWN_STATUS_PLAYING = 3;

    int mCountDownStatus = COUNT_DOWN_STATUS_WAIT;

    int mUseId;   // 当前唱歌人的id

    SVGAImageView mGrabStageView;
    BaseImageView mSingAvatarView;
    CircleCountDownView mCircleCountDownView;
    ExTextView mTvSingerName;

    AlphaAnimation mEnterAlphaAnimation;                // 进场动画
    TranslateAnimation mLeaveTranslateAnimation;   // 出场动画

    GrabRoomData mGrabRoomData;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ENSURE_PLAY) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
                countDown("handleMessage");
            }
        }
    };

    boolean mHasPlayFullAnimation = false;

    public NormalOthersSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mGrabRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mGrabStageView = (SVGAImageView) mParentView.findViewById(R.id.grab_stage_view);
        mSingAvatarView = (BaseImageView) mParentView.findViewById(R.id.sing_avatar_view);
        mCircleCountDownView = (CircleCountDownView) mParentView.findViewById(R.id.circle_count_down_view);
        mTvSingerName = (ExTextView) mParentView.findViewById(R.id.tv_singer_name);

        mSingAvatarView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mUseId != 0) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mUseId));
                }
            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_normal_other_sing_card_stub_layout;
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        if (mEnterAlphaAnimation != null) {
            mEnterAlphaAnimation.setAnimationListener(null);
            mEnterAlphaAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }

    public void bindData() {
        tryInflate();
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        UserInfoModel userInfoModel = mGrabRoomData.getUserInfo(infoModel.getUserID());
        mUiHandler.removeCallbacksAndMessages(null);
        mHasPlayFullAnimation = false;
        setVisibility(View.VISIBLE);
        if (userInfoModel != null) {
            this.mUseId = userInfoModel.getUserId();
            AvatarUtils.loadAvatarByUrl(mSingAvatarView,
                    AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(5))
                            .setCircle(true)
                            .build());
            mTvSingerName.setText(userInfoModel.getNicknameRemark());
        } else {
            MyLog.w(TAG, "userInfoModel==null 加载选手信息失败");
        }

        // 淡出效果
        if (mEnterAlphaAnimation == null) {
            mEnterAlphaAnimation = new AlphaAnimation(0f, 1f);
            mEnterAlphaAnimation.setDuration(1000);
        }
        mParentView.startAnimation(mEnterAlphaAnimation);

        mGrabStageView.setVisibility(View.VISIBLE);
        mGrabStageView.setLoops(1);

        SvgaParserAdapter.parse("grab_main_stage.svga", new SVGAParser.ParseCompletion() {
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

        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
        mCircleCountDownView.cancelAnim();
        mCircleCountDownView.setMax(360);
        mCircleCountDownView.setProgress(0);
        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_SING.getValue()) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            countDown("中途进来");
        } else {
            mUiHandler.removeMessages(MSG_ENSURE_PLAY);
            mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000);
        }
    }

    public void tryStartCountDown() {
        if (mParentView==null || mParentView.getVisibility() == View.GONE) {
            return;
        }
        MyLog.d(TAG, "tryStartCountDown");
        mUiHandler.removeMessages(MSG_ENSURE_PLAY);
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            countDown("tryStartCountDown");
        }
    }

    private void countDown(String from) {
        MyLog.d(TAG, "countDown" + " from=" + from);
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingTotalMs();
        int progress;  //当前进度条
        int leaveTime; //剩余时间
        MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant() + " enterStatus=" + infoModel.getEnterStatus());
        if (!infoModel.isParticipant() && infoModel.getEnterStatus() == EQRoundStatus.QRS_SING.getValue()) {
            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
            progress = infoModel.getElapsedTimeMs() * 100 / totalMs;
            leaveTime = totalMs - infoModel.getElapsedTimeMs();
        } else {
            progress = 1;
            leaveTime = totalMs;
        }
        mCircleCountDownView.go(progress, leaveTime);
    }

    public void hide() {
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
        mHasPlayFullAnimation = false;
        mUiHandler.removeCallbacksAndMessages(null);
        if(mParentView != null){
            if ( mParentView.getVisibility() == View.VISIBLE) {
                if (mLeaveTranslateAnimation == null) {
                    mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                    mLeaveTranslateAnimation.setDuration(200);
                }
                mParentView.startAnimation(mLeaveTranslateAnimation);
                mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mParentView.clearAnimation();
                        setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                mParentView.clearAnimation();
                setVisibility(View.GONE);
            }
            if (mCircleCountDownView != null) {
                mCircleCountDownView.cancelAnim();
                mCircleCountDownView.setMax(360);
                mCircleCountDownView.setProgress(0);

                mUiHandler.removeMessages(MSG_ENSURE_PLAY);
            }
        }
    }
}
