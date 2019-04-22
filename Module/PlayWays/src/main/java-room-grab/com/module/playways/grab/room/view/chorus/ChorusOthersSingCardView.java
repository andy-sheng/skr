package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.engine.EngineEvent;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Room.EQRoundStatus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 别人唱歌是，自己看到的板子
 */
public class ChorusOthersSingCardView extends RelativeLayout {

    public final static String TAG = "ChorusOthersSingCardView";
    final static int MSG_ENSURE_PLAY = 1;
    static final int MSG_SPEAK_OVER = 2;

    final static int COUNT_DOWN_STATUS_INIT = 1;
    final static int COUNT_DOWN_STATUS_WAIT = 2;
    final static int COUNT_DOWN_STATUS_PLAYING = 3;

    int mCountDownStatus = COUNT_DOWN_STATUS_INIT;

    SVGAImageView mLeftSingSvga;
    SVGAImageView mRightSingSvga;
    LinearLayout mChorusOtherArea;
    SimpleDraweeView mLeftIv;
    ExTextView mLeftName;
    SimpleDraweeView mRightIv;
    ExTextView mRightName;
    ImageView mIvTag;
    CircleCountDownView mCircleCountDownView;
    BitmapTextView mCountDownTv;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    boolean mHasPlayFullAnimation = false;
    boolean mCanStartFlag = false;

    HandlerTaskTimer mCounDownTask;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ENSURE_PLAY) {
                tryStartCountDown();
            } else if (msg.what == MSG_SPEAK_OVER) {
                if (msg.obj != null) {
                    stopSingAnimation((int) msg.obj);
                } else {
                    MyLog.w(TAG, "handleMessage" + " msg=" + msg);
                }
            }
        }
    };

    GrabRoomData mGrabRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;

    public ChorusOthersSingCardView(Context context) {
        super(context);
        init();
    }

    public ChorusOthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChorusOthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_chorus_other_sing_card_layout, this);

        mChorusOtherArea = (LinearLayout) findViewById(R.id.chorus_other_area);
        mLeftSingSvga = (SVGAImageView) findViewById(R.id.left_sing_svga);
        mRightSingSvga = (SVGAImageView) findViewById(R.id.right_sing_svga);
        mLeftIv = (SimpleDraweeView) findViewById(R.id.left_iv);
        mLeftName = (ExTextView) findViewById(R.id.left_name);
        mRightIv = (SimpleDraweeView) findViewById(R.id.right_iv);
        mRightName = (ExTextView) findViewById(R.id.right_name);
        mIvTag = (ImageView) findViewById(R.id.iv_tag);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);
        mCountDownTv = (BitmapTextView) findViewById(R.id.count_down_tv);

        int offsetX = (U.getDisplayUtils().getScreenWidth() / 2 - U.getDisplayUtils().dip2px(16)) / 2;
        mLeftSingSvga.setTranslationX(-offsetX);
        mRightSingSvga.setTranslationX(offsetX);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void bindData(GrabRoomData roomData, UserInfoModel left, UserInfoModel right) {
        if (roomData != null && left != null && right != null) {
            this.mGrabRoomData = roomData;
            this.mLeftUserInfoModel = left;
            this.mRightUserInfoModel = right;

            mHasPlayFullAnimation = false;
            mUiHandler.removeCallbacksAndMessages(null);
            setVisibility(VISIBLE);
            AvatarUtils.loadAvatarByUrl(mLeftIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mLeftName.setText(mLeftUserInfoModel.getNickname());
            AvatarUtils.loadAvatarByUrl(mRightIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mRightName.setText(mRightUserInfoModel.getNickname());
            animationGo();

            mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
            mCircleCountDownView.setProgress(0);

            GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
            if (grabRoundInfoModel == null) {
                return;
            }

            if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_SING.getValue()) {
                countDown("中途进来");
            } else {
                mCircleCountDownView.cancelAnim();
                mCircleCountDownView.setMax(360);
                mCircleCountDownView.setProgress(0);
                mUiHandler.removeMessages(MSG_ENSURE_PLAY);
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 3000);
            }
        } else {
            MyLog.w(TAG, "bindData" + " roomData=" + roomData + " left=" + left + " right=" + right);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        switch (event.getType()) {
            case EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION: {
                // 有人在说话,播2秒动画
                List<EngineEvent.UserVolumeInfo> l = event.getObj();
                for (EngineEvent.UserVolumeInfo u : l) {
                    int uid = u.getUid();
                    if (uid == 0) {
                        uid = (int) UserAccountManager.getInstance().getUuidAsLong();
                    }
                    if (mLeftUserInfoModel != null && mLeftUserInfoModel.getUserId() == uid) {
                        mUiHandler.removeMessages(MSG_SPEAK_OVER);
                        Message msg = new Message();
                        msg.what = MSG_SPEAK_OVER;
                        msg.obj = uid;
                        mUiHandler.sendMessageDelayed(msg, 2000);
                        playSingAnimation(mLeftSingSvga);
                    } else if (mRightUserInfoModel != null && mRightUserInfoModel.getUserId() == uid) {
                        mUiHandler.removeMessages(MSG_SPEAK_OVER);
                        Message msg = new Message();
                        msg.what = MSG_SPEAK_OVER;
                        msg.obj = uid;
                        mUiHandler.sendMessageDelayed(msg, 2000);
                        playSingAnimation(mRightSingSvga);
                    } else {
                        MyLog.w(TAG, "onEvent" + " 不是唱歌两人说话 event=" + event);
                    }
                }
                break;
            }
        }
    }


    // 播放声纹动画
    private void playSingAnimation(SVGAImageView svgaImageView) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation" + " svgaImageView=" + svgaImageView);
            return;
        }

        if (svgaImageView != null && svgaImageView.getVisibility() == VISIBLE) {
            // 正在播放
            return;
        }

        svgaImageView.setVisibility(View.VISIBLE);
        svgaImageView.setLoops(-1);

        SvgaParserAdapter.parse("grab_main_stage.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                svgaImageView.setImageDrawable(drawable);
                svgaImageView.startAnimation();
            }

            @Override
            public void onError() {

            }
        });
    }

    // 停止声纹动画
    private void stopSingAnimation(int uid) {
        if (uid == mLeftUserInfoModel.getUserId()) {
            stopSingAnimation(mLeftSingSvga);
        } else if (uid == mRightUserInfoModel.getUserId()) {
            stopSingAnimation(mRightSingSvga);
        } else {
            MyLog.w(TAG, "stopSingAnimation" + " uid=" + uid);
        }
    }

    private void stopSingAnimation(SVGAImageView svgaImageView) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "stopSingAnimation" + " svgaImageView=" + svgaImageView);
            return;
        }

        svgaImageView.setCallback(null);
        svgaImageView.stopAnimation(true);
        svgaImageView.setVisibility(GONE);
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

        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_SING.getValue()) {
            countDown("中途进来");
        } else {
            countDown("else full Animation");
        }
    }

    private void countDown(String from) {
        MyLog.d(TAG, "countDown" + " from=" + from);
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingEndMs() - infoModel.getSingBeginMs();
        if (totalMs <= 0) {
            MyLog.d(TAG, "playLyric" + " totalTs时间不合法,做矫正, infoModel=" + infoModel);
            if (infoModel.getWantSingType() == 0) {
                totalMs = 20 * 1000;
            } else if (infoModel.getWantSingType() == 1) {
                totalMs = 30 * 1000;
            } else if (infoModel.getWantSingType() == 2) {
                totalMs = 40 * 1000;
            } else if (infoModel.getWantSingType() == 3) {
                totalMs = 50 * 1000;
            }
        }
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            MyLog.d(TAG, "countDown mCountDownStatus == COUNT_DOWN_STATUS_WAIT");
            if (mCanStartFlag) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            } else {
                // 不需要播放countdown
//                mCountDownProcess.startCountDown(0, totalMs);
                mCircleCountDownView.go(0, totalMs);
                startCountDownText(totalMs / 1000);
                return;
            }
        }

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
//        mCountDownProcess.startCountDown(progress, leaveTime);
        mCircleCountDownView.go(progress, leaveTime);
        startCountDownText(leaveTime / 1000);
    }

    private void startCountDownText(int counDown) {
        mCounDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(counDown)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mCountDownTv.setText((counDown - integer) + "");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        stopCounDown();
                    }
                });
    }

    private void stopCounDown() {
        if (mCounDownTask != null) {
            mCounDownTask.dispose();
        }
    }

    private void animationGo() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        this.startAnimation(mEnterTranslateAnimation);
    }

    /**
     * 离场动画
     */
    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
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
            this.startAnimation(mLeaveTranslateAnimation);
        } else {
            clearAnimation();
            setVisibility(GONE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
        if (mLeftSingSvga != null) {
            mLeftSingSvga.setCallback(null);
            mRightSingSvga.stopAnimation(true);
        }
        if (mRightSingSvga != null) {
            mRightSingSvga.setCallback(null);
            mRightSingSvga.stopAnimation(true);
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
