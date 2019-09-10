package com.module.playways.grab.room.view.minigame;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.engine.EngineEvent;
import com.engine.UserStatus;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.component.person.event.ShowPersonCardEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.MINIGameRoundInfoModel;
import com.common.view.ExViewStub;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.room.song.model.MiniGameInfoModel;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 小游戏别人视角的我，和合唱板子
 */
public class MiniGameOtherSingCardView extends ExViewStub {

    public final String TAG = "ChorusOthersSingCardView";
    final static int MSG_ENSURE_PLAY = 1;
    static final int MSG_LEFT_SPEAK_OVER = 2;
    static final int MSG_RIGHT_SPEAK_OVER = 3;

    final static int COUNT_DOWN_STATUS_WAIT = 2;
    final static int COUNT_DOWN_STATUS_PLAYING = 3;

    int mCountDownStatus = COUNT_DOWN_STATUS_WAIT;


    SVGAImageView mLeftSingSvga;
    SVGAImageView mRightSingSvga;
    LinearLayout mChorusOtherArea;

    SimpleDraweeView mLeftIv;
    ExTextView mLeftName;

    SimpleDraweeView mRightIv;
    ExTextView mRightName;

    SingCountDownView2 mSingCountDownView;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    boolean mHasPlayFullAnimation = false;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ENSURE_PLAY) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
                countDown("handleMessage");
            } else if (msg.what == MSG_LEFT_SPEAK_OVER) {
                stopSingAnimation(mLeftSingSvga);
            } else if (msg.what == MSG_RIGHT_SPEAK_OVER) {
                stopSingAnimation(mRightSingSvga);
            }
        }
    };

    GrabRoomData mGrabRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;
    MINIGameRoundInfoModel mLeftMINIGameRoundInfoModel;
    MINIGameRoundInfoModel mRightMINIGameRoundInfoModel;
    MiniGameInfoModel mMiniGameInfoModel;

    public MiniGameOtherSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mGrabRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mChorusOtherArea = (LinearLayout) getMParentView().findViewById(R.id.chorus_other_area);
        mLeftSingSvga = (SVGAImageView) getMParentView().findViewById(R.id.left_sing_svga);
        mRightSingSvga = (SVGAImageView) getMParentView().findViewById(R.id.right_sing_svga);

        mLeftIv = (SimpleDraweeView) getMParentView().findViewById(R.id.left_iv);
        mLeftName = (ExTextView) getMParentView().findViewById(R.id.left_name);

        mRightIv = (SimpleDraweeView) getMParentView().findViewById(R.id.right_iv);
        mRightName = (ExTextView) getMParentView().findViewById(R.id.right_name);

        mSingCountDownView = getMParentView().findViewById(R.id.sing_count_down_view);

        int offsetX = (U.getDisplayUtils().getScreenWidth() / 2 - U.getDisplayUtils().dip2px(16)) / 2;
        mLeftSingSvga.setTranslationX(-offsetX);
        mRightSingSvga.setTranslationX(offsetX);

        mLeftIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mLeftUserInfoModel != null) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mLeftUserInfoModel.getUserId()));
                }
            }
        });

        mRightIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mRightUserInfoModel != null) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mRightUserInfoModel.getUserId()));
                }
            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_mini_game_other_sing_card_stub_layout;
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        super.onViewAttachedToWindow(v);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
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

    public void bindData() {
        GrabRoundInfoModel now = mGrabRoomData.getRealRoundInfo();
        if (now == null) {
            return;
        }
        tryInflate();
        mLeftMINIGameRoundInfoModel = null;
        mRightMINIGameRoundInfoModel = null;
        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;

        List<MINIGameRoundInfoModel> list = now.getMINIGameRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftMINIGameRoundInfoModel = list.get(0);
            mRightMINIGameRoundInfoModel = list.get(1);
            mLeftUserInfoModel = mGrabRoomData.getPlayerOrWaiterInfo(mLeftMINIGameRoundInfoModel.getUserID());
            mRightUserInfoModel = mGrabRoomData.getPlayerOrWaiterInfo(mRightMINIGameRoundInfoModel.getUserID());
            mMiniGameInfoModel = now.getMusic().getMiniGame();
        }

        if (mLeftUserInfoModel != null && mRightUserInfoModel != null && mLeftMINIGameRoundInfoModel != null && mRightMINIGameRoundInfoModel != null) {
            mHasPlayFullAnimation = false;
            mUiHandler.removeCallbacksAndMessages(null);
            getMParentView().setVisibility(View.VISIBLE);
            AvatarUtils.loadAvatarByUrl(mLeftIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mLeftName.setText(mLeftUserInfoModel.getNicknameRemark());

            AvatarUtils.loadAvatarByUrl(mRightIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mRightName.setText(mRightUserInfoModel.getNicknameRemark());

            animationGo();

            mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
            mSingCountDownView.reset();

            GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
            if (grabRoundInfoModel == null) {
                return;
            }

            if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.isEnterInSingStatus()) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
                countDown("中途进来,直接播放");
            } else {
                mSingCountDownView.startPlay(0, grabRoundInfoModel.getSingTotalMs(), false);
                mUiHandler.removeMessages(MSG_ENSURE_PLAY);
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000);
            }
        }
    }

    // 播放声纹动画
    private void playSingAnimation(SVGAImageView svgaImageView) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation" + " svgaImageView=" + svgaImageView);
            return;
        }

        if (svgaImageView != null && svgaImageView.isAnimating()) {
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
        svgaImageView.setVisibility(View.GONE);
    }

    public void tryStartCountDown() {
        if (getMParentView() == null || getMParentView().getVisibility() == View.GONE) {
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
        if (!infoModel.isParticipant() && infoModel.isEnterInSingStatus()) {
            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
            progress = infoModel.getElapsedTimeMs() * 100 / totalMs;
            leaveTime = totalMs - infoModel.getElapsedTimeMs();
        } else {
            progress = 1;
            leaveTime = totalMs;
        }
        mSingCountDownView.startPlay(progress, leaveTime, true);
    }

    private void animationGo() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        getMParentView().startAnimation(mEnterTranslateAnimation);
    }

    /**
     * 离场动画
     */
    public void hide() {
        if (getMParentView() != null) {
            if (getMParentView().getVisibility() == View.VISIBLE) {
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
                        getMParentView().clearAnimation();
                        getMParentView().setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                getMParentView().startAnimation(mLeaveTranslateAnimation);
            } else {
                getMParentView().clearAnimation();
                getMParentView().setVisibility(View.GONE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        if (getMParentView() == null || getMParentView().getVisibility() == View.GONE) {
            return;
        }
        switch (event.getType()) {
            case EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION: {
                // 有人在说话,播2秒动画
                List<EngineEvent.UserVolumeInfo> l = event.getObj();
                for (EngineEvent.UserVolumeInfo u : l) {
                    int uid = u.getUid();
                    if (uid == 0) {
                        uid = (int) UserAccountManager.getInstance().getUuidAsLong();
                    }
                    if (u.getVolume() > 30) {
                        if (mLeftUserInfoModel != null && mLeftUserInfoModel.getUserId() == uid) {
                            mUiHandler.removeMessages(MSG_LEFT_SPEAK_OVER);
                            mUiHandler.sendEmptyMessageDelayed(MSG_LEFT_SPEAK_OVER, 2000);
                            playSingAnimation(mLeftSingSvga);
                        } else if (mRightUserInfoModel != null && mRightUserInfoModel.getUserId() == uid) {
                            mUiHandler.removeMessages(MSG_RIGHT_SPEAK_OVER);
                            mUiHandler.sendEmptyMessageDelayed(MSG_RIGHT_SPEAK_OVER, 2000);
                            playSingAnimation(mRightSingSvga);
                        } else {
                            MyLog.w(TAG, "onEvent" + " 不是唱歌两人说话 event=" + event);
                        }
                    }
                }
                break;
            }
            case EngineEvent.TYPE_USER_MUTE_AUDIO: {
                //用户闭麦，开麦
                UserStatus userStatus = event.getUserStatus();
                if (userStatus != null) {
                    int userId = userStatus.getUserId();
                    if (userStatus.isAudioMute()) {
                        stopSingAnimation(userId);
                    }
                }
                break;
            }
        }
    }

}
