package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.gift.model.giftEntity.NormalEffectGift;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.dao.Gift;
import com.wali.live.event.UserActionEvent;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by yangjiawei on 2017/8/7.
 */

public class GiftContinuousView extends RelativeLayout implements IGiftContinueView {
    private String TAG = "GiftContinuousView";
    public static final int SPEED_NORMAL = 1;
    public static final int SPEED_FAST = 2;


    private int myId;

    public int getMyId() {
        return myId;
    }

    public void setMyId(int myId) {
        this.myId = myId;
        TAG = "GiftContinuousView" + myId;
    }

    private IGiftScheduler mScheduler;
    private GiftRecvModel curModel;
    private volatile STATUS mStatus = STATUS.IDLE;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public GiftContinuousView(Context context) {
        this(context, null);
    }

    public GiftContinuousView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftContinuousView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private GiftPictureAnimationView mGiftPictureAnimationView;
    private GiftNumberAnimationView mGiftNumberAniamtionView;
    private TextView mNameTv;
    private TextView mInfoTv;
    private RelativeLayout mContainer;
    private BaseImageView mOwnerIv;
    private ImageView mUserBadgeIv;

    RelativeLayout numberS;
    LinearLayout numberB;
    private FrameLayout mRootView;
    private ImageView img1s;
    private ImageView img2s;
    private ImageView img3s;
    private ImageView img4s;
    private ImageView img1b;
    private ImageView img2b;
    private ImageView img3b;
    private ImageView img4b;
    private ImageView imgStar;
    Animation animation;
    AnimationSet animationSet;
    Animation animationStar;

    ImageView[] picSmall = new ImageView[]{img1s, img2s, img3s, img4s};
    ImageView[] picBig = new ImageView[]{img1b, img2b, img3b, img4b};

    int[] smallNumbers = new int[]{R.drawable.lianfa_number_0_s, R.drawable.lianfa_number_1_s, R.drawable.lianfa_number_2_s,
            R.drawable.lianfa_number_3_s, R.drawable.lianfa_number_4_s, R.drawable.lianfa_number_5_s,
            R.drawable.lianfa_number_6_s, R.drawable.lianfa_number_7_s, R.drawable.lianfa_number_8_s,
            R.drawable.lianfa_number_9_s};

    int[] bigNumbers = new int[]{R.drawable.lianfa_number_0_b, R.drawable.lianfa_number_1_b, R.drawable.lianfa_number_2_b,
            R.drawable.lianfa_number_3_b, R.drawable.lianfa_number_4_b, R.drawable.lianfa_number_5_b,
            R.drawable.lianfa_number_6_b, R.drawable.lianfa_number_7_b, R.drawable.lianfa_number_8_b,
            R.drawable.lianfa_number_9_b};
    // view Y轴初始坐标
    private float mTranslationY;
    ObjectAnimator mPlayEnterAnimator;
    AnimatorListenerAdapter mGiftNumberAniamtionListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            waiting();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            onAnimationEnd(animation);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            mGiftNumberAniamtionView.setVisibility(View.VISIBLE);
        }
    };

    AnimatorSet mPlayDismissAnimSet;
    Runnable playDismissRunnable = new Runnable() {
        @Override
        public void run() {
            MyLog.d(TAG, "playDismissing");
            if (mPlayDismissAnimSet == null) {
                float curTranslationY = GiftContinuousView.this.getTranslationY();
                ObjectAnimator moveY = ObjectAnimator.ofFloat(GiftContinuousView.this, "translationY", curTranslationY, curTranslationY - 300);
                ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(GiftContinuousView.this, "alpha", 1f, 0f);
                mPlayDismissAnimSet = new AnimatorSet();
                mPlayDismissAnimSet.play(moveY).with(fadeInOut);
                mPlayDismissAnimSet.setDuration(500);
                mPlayDismissAnimSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        idle();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }
                });
            }
            mPlayDismissAnimSet.start();
        }
    };

    Runnable waitingRunnable = new Runnable() {
        @Override
        public void run() {
            MyLog.d(TAG, "waitingRunnable");
            if (tryTriggerPlay()) {
                return;
            }
            dismissing();
        }
    };


    private void init(Context context) {
        inflate(context, R.layout.gift_continue_view, this);

        mRootView = (FrameLayout) findViewById(R.id.continue_view_rlytRoot);
        numberS = (RelativeLayout) findViewById(R.id.numberS);
        numberB = (LinearLayout) findViewById(R.id.numberB);
        imgStar = (ImageView) findViewById(R.id.imgStar);
        picSmall[0] = (ImageView) findViewById(R.id.number1s);
        picSmall[1] = (ImageView) findViewById(R.id.number2s);
        picSmall[2] = (ImageView) findViewById(R.id.number3s);
        picSmall[3] = (ImageView) findViewById(R.id.number4s);
        picBig[0] = (ImageView) findViewById(R.id.number1b);
        picBig[1] = (ImageView) findViewById(R.id.number2b);
        picBig[2] = (ImageView) findViewById(R.id.number3b);
        picBig[3] = (ImageView) findViewById(R.id.number4b);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mOwnerIv = (BaseImageView) findViewById(R.id.sender_iv);
        mGiftPictureAnimationView = (GiftPictureAnimationView) findViewById(R.id.gift_picture_vg);
        mGiftNumberAniamtionView = (GiftNumberAnimationView) findViewById(R.id.gift_number_vg);
        mNameTv = (TextView) findViewById(R.id.name_tv);
        mInfoTv = (TextView) findViewById(R.id.info_tv);
        mUserBadgeIv = (ImageView) findViewById(R.id.user_badge_iv);
        mTranslationY = GiftContinuousView.this.getTranslationY();

        RxView.clicks(mContainer)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (curModel == null || CommonUtils.isFastDoubleClick(1000)) {
                            return;
                        }
                        EventBus.getDefault().post(new UserActionEvent(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, curModel.getUserId(), null));

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    @Override
    public void setStatus(STATUS status) {
        mStatus = status;
    }

    @Override
    public STATUS getStatus() {
        return mStatus;
    }

    private void idle() {
        MyLog.d(TAG, "idle");
        mStatus = STATUS.IDLE;
        curModel = null;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setTranslationY(mTranslationY);
                setAlpha(1);
                setVisibility(GONE);
                mGiftPictureAnimationView.reset();
            }
        } );


        tryAwake();
    }

    //播放进场动画
    @UiThread
    private void entering() {
        MyLog.d(TAG, "entering");
        if (checkNotNull()) {
            return;
        }
        mStatus = STATUS.ENTERING;
        prepare();
        if (mPlayEnterAnimator == null) {
            float curTranslationX = this.getTranslationX();
            int offset = -300;
            mPlayEnterAnimator = ObjectAnimator.ofFloat(this, "translationX", curTranslationX + offset, curTranslationX);
            mPlayEnterAnimator.setDuration(200);
            mPlayEnterAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    GiftContinuousView.this.setLayerType(View.LAYER_TYPE_NONE, null);
                    playing();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    GiftContinuousView.this.setVisibility(View.VISIBLE);
                    GiftContinuousView.this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            });
        }
        mPlayEnterAnimator.start();
    }

    //播放数字缩放动画
    @UiThread
    private void playing() {
        MyLog.d(TAG, "playing");
        if (checkNotNull()) {
            return;
        }
        mStatus = STATUS.PLAYING;
        MyLog.d(TAG, "number=" + curModel.getStartNumber() + "  endNumber=" + curModel.getEndNumber());
        genBigGift();
        if (curModel.isBatchGift()) {
            startBatchGift(curModel.getBatchCount(), curModel.getStartNumber());
        } else {
            startNormalGift();
        }
    }

    @UiThread
    private void waiting() {
        MyLog.d(TAG, "waiting");
        mStatus = STATUS.WAITING;
        if (tryTriggerPlay()) {
            return;
        }
        mHandler.removeCallbacks(waitingRunnable);
        mHandler.postDelayed(waitingRunnable, 500);
    }

    @UiThread
    private void dismissing() {
        MyLog.d(TAG, "dismissing");
        mStatus = STATUS.DISMISSING;
        mHandler.removeCallbacks(playDismissRunnable);
        mHandler.post(playDismissRunnable);
    }

    public synchronized boolean tryTriggerPlay() {
        GiftRecvModel copy = curModel;
        if (curModel == null) {
            return false;
        }
        curModel = mScheduler.tryGetNextModel(curModel, this);
        MyLog.d(TAG, "tryGetNextModel model=" + curModel);
        if (curModel != null) {
            playing();
            return true;
        }
        curModel = copy;
        return false;
    }

    @Override
    public synchronized void tryAwake() {
        if (mStatus == STATUS.IDLE) {
            curModel = mScheduler.nextModel(this);
            MyLog.d(TAG, "tryAwake success  get model:" + curModel);
            if (curModel != null) {
                //使得状态的改变所有线程立即感知
                mStatus = STATUS.ENTERING;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        entering();
                    }
                });
            }
        } else {
            MyLog.d(TAG, "tryAwake failed, status=" + mStatus);
        }
    }

    @UiThread
    private void prepare() {
        MyLog.d(TAG, "prepare");
        mGiftNumberAniamtionView.setPlayNumber(curModel.getStartNumber());

        numberS.setVisibility(View.INVISIBLE);
        numberB.setVisibility(View.INVISIBLE);
        mNameTv.setText("" + curModel.getSenderName());
        mInfoTv.setText(curModel.getSendDescribe());
        long userId = curModel.getUserId();
        AvatarUtils.loadAvatarByUidTs(mOwnerIv, userId, curModel.getAvatarTimestamp(), true);
        // 加载图像
        if (curModel.getGifType() == GiftType.NORMAL_EFFECTS_GIFT ||
                (curModel.getGifType() == GiftType.PRIVILEGE_GIFT && curModel.getGiftOriginType() == GiftType.NORMAL_EFFECTS_GIFT)) {
            NormalEffectGift effectGift = (NormalEffectGift) curModel.getGift();
            //设立标记
            mGiftPictureAnimationView.setFlags(effectGift.getFlags());
            mGiftPictureAnimationView.setBigCons(effectGift.getBigCons());
        }
        mGiftPictureAnimationView.fillPictureByNumber(curModel.getPicPath(), curModel.getStartNumber());
        mGiftPictureAnimationView.setVisibility(VISIBLE);

        animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
        animationSet = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.scale_alpha);
        animationStar = AnimationUtils.loadAnimation(getContext(), R.anim.scale_aplha_star);

        if (curModel.getCertificationType() > 0) {
            mUserBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(15f);
            mUserBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(15f);
            mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(curModel.getCertificationType()));
        } else {
            mUserBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(10f);
            mUserBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(10f);
            mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(curModel.getLevel()));
        }
    }

    @Override
    public void setGiftScheduler(IGiftScheduler scheduler) {
        mScheduler = scheduler;
    }

    @Override
    public boolean playingBatchGift() {
        return curModel != null && curModel.isBatchGift();
    }

    @Override
    public GiftRecvModel getModel() {
        return curModel;
    }

    @Override
    public void switchAnchor() {
        MyLog.d(TAG, "switchAnchor");
        clearAll();
        idle();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        clearAll();
    }

    @Override
    public boolean isPlayingModel(GiftRecvModel model) {
        return curModel != null
                && curModel.getUserId() == model.getUserId()
                && curModel.getGiftId() == model.getGiftId()
                && curModel.getContinueId() == model.getContinueId();
    }

    private void clearAll() {
        mStatus = STATUS.IDLE;
        curModel = null;
        if (mPlayDismissAnimSet != null) {
            mPlayDismissAnimSet.removeAllListeners();
            mPlayDismissAnimSet.cancel();
            mPlayDismissAnimSet = null;
        }
        if (mPlayEnterAnimator != null) {
            mPlayEnterAnimator.removeAllListeners();
            mPlayEnterAnimator.cancel();
            mPlayEnterAnimator = null;
        }

        if (mGiftNumberAniamtionView != null) {
            mGiftNumberAniamtionView.destroy();
        }

        if(mGiftPictureAnimationView != null){
            mGiftPictureAnimationView.destroy();
        }

        if (animation != null) {
            animation.cancel();
        }
        if (animationStar != null) {
            animationStar.cancel();
        }
        if (animationSet != null) {
            animationSet.cancel();
        }
    }

    private boolean checkNotNull() {
        if (curModel == null) {
            MyLog.d(TAG, "curModel=null set to idle");
            idle();
            return true;
        }
        return false;
    }

    private void genBigGift() {
        // 检查一下是否需要变化图片
        if (curModel != null && curModel.getGifType() == GiftType.NORMAL_EFFECTS_GIFT) {
            mGiftPictureAnimationView.tryPlayIfNeed(curModel.getStartNumber(), curModel.isBatchGift());
            for (NormalEffectGift.BigContinue f : ((NormalEffectGift) curModel.getGift()).getBigCons()) {
                if (curModel.getStartNumber() < f.startCount) {
                    break;
                }
                if (curModel.getStartNumber() == f.startCount && f.startCount != 1) {
                    Gift gift = GiftRepository.findGiftById(f.giftId);
                    GiftRecvModel model = new GiftRecvModel();
                    model.setFromSelf(curModel.isFromSelf());
                    model.setGift(gift);
                    model.setSenderName(curModel.getSenderName());
                    model.setUserId(curModel.getUserId());
                    model.setCertificationType(curModel.getCertificationType());
                    model.setLevel(curModel.getLevel());
                    EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Big(model));
                    break;
                }
            }
        }
    }

    private void startNormalGift() {
        if (mScheduler.getQueueSize() > 10) {
            mGiftNumberAniamtionView.setSpeedMode(SPEED_FAST);
        } else {
            mGiftNumberAniamtionView.setSpeedMode(SPEED_NORMAL);
        }
        mGiftNumberAniamtionView.setPlayNumber(curModel.getStartNumber());
        mGiftNumberAniamtionView.setIsMySendFastMode(curModel.getEndNumber() - curModel.getStartNumber() > 5);
        mGiftNumberAniamtionView.play(mGiftNumberAniamtionListenerAdapter, 1, false);
    }

    private void startBatchGift(int number, int time) {
        MyLog.d(TAG, "batch gift startBatchGift @ :" + time);
        mGiftNumberAniamtionView.hideNumberBgIcon();
        numberS.setVisibility(View.INVISIBLE);
        numberB.setVisibility(View.INVISIBLE);
        initNumber(time, bigNumbers, picBig);

        initNumber(number, smallNumbers, picSmall);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) imgStar.getLayoutParams();
        if (time > 999) {
            lp.setMargins(DisplayUtils.dip2px(250), 0, 0, DisplayUtils.dip2px(3.33f));
        } else if (time > 99) {
            lp.setMargins(DisplayUtils.dip2px(240), 0, 0, DisplayUtils.dip2px(3.33f));
        } else if (time > 9) {
            lp.setMargins(DisplayUtils.dip2px(230), 0, 0, DisplayUtils.dip2px(3.33f));
        } else {
            lp.setMargins(DisplayUtils.dip2px(220), 0, 0, DisplayUtils.dip2px(3.33f));
        }

        lp = (FrameLayout.LayoutParams) numberB.getLayoutParams();
        if (number > 999) {
            lp.setMargins(DisplayUtils.dip2px(210), 0, 0, 0);
        } else if (number > 99) {
            lp.setMargins(DisplayUtils.dip2px(200), 0, 0, 0);
        } else if (number > 9) {
            lp.setMargins(DisplayUtils.dip2px(190), 0, 0, 0);
        } else {
            lp.setMargins(DisplayUtils.dip2px(180), 0, 0, 0);
        }


        mRootView.setBackgroundResource(R.drawable.new_gift_anim);
        AnimationDrawable anim = (AnimationDrawable) mRootView.getBackground();
        anim.setVisible(true, true);
        anim.start();

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationStar.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imgStar.setVisibility(View.GONE);
                        mRootView.setBackground(null);
                        waiting();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imgStar.setVisibility(View.VISIBLE);
                imgStar.startAnimation(animationStar);

                numberB.setVisibility(View.VISIBLE);
                numberB.startAnimation(animationSet);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                numberS.startAnimation(animation);
                numberS.setVisibility(View.VISIBLE);
            }
        }, 200);
    }

    private void initNumber(int number, int[] picNumbers, ImageView[] imgPic) {

        imgPic[0].setVisibility(View.GONE);
        imgPic[1].setVisibility(View.GONE);
        imgPic[2].setVisibility(View.GONE);
        imgPic[3].setVisibility(View.GONE);

        if (number > 999) {
            imgPic[0].setVisibility(View.VISIBLE);
            imgPic[1].setVisibility(View.VISIBLE);
            imgPic[2].setVisibility(View.VISIBLE);
            imgPic[3].setVisibility(View.VISIBLE);

            imgPic[0].setImageResource(picNumbers[(int) (number * 1.0 / 1000)]);
            imgPic[1].setImageResource(picNumbers[(int) (number % 1000 * 1.0 / 100)]);
            imgPic[2].setImageResource(picNumbers[(int) (number % 1000 % 100 * 1.0 / 10)]);
            imgPic[3].setImageResource(picNumbers[number % 1000 % 100 % 10]);
        } else if (number > 99) {
            imgPic[0].setVisibility(View.VISIBLE);
            imgPic[1].setVisibility(View.VISIBLE);
            imgPic[2].setVisibility(View.VISIBLE);
            imgPic[0].setImageResource(picNumbers[(int) (number * 1.0 / 100)]);
            imgPic[1].setImageResource(picNumbers[(int) (number % 100 * 1.0) / 10]);
            imgPic[2].setImageResource(picNumbers[number % 100 % 10]);
        } else if (number > 9) {
            imgPic[0].setVisibility(View.VISIBLE);
            imgPic[1].setVisibility(View.VISIBLE);
            imgPic[0].setImageResource(picNumbers[(int) (number * 1.0 / 10)]);
            imgPic[1].setImageResource(picNumbers[number % 10]);
        } else {
            imgPic[0].setVisibility(View.VISIBLE);
            imgPic[0].setImageResource(picNumbers[number]);
        }

    }


}
