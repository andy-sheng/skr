package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
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
import com.wali.live.common.gift.utils.DataformatUtils;
import com.wali.live.dao.Gift;
import com.wali.live.event.UserActionEvent;
import com.wali.live.utils.AvatarUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @module 礼物连送
 * Created by chengsimin on 16/2/20.
 */
public class GiftContinueView extends RelativeLayout {
    public static final int SPEED_FAST = 2;
    public static final int SPEED_NORMAL = 1;
    private static String TAG = "GiftContinueView";

    private int myid;

    public void setMyId(int myid) {
        this.myid = myid;
    }

    public int getMyid() {
        return myid;
    }

    private GiftRecvModel mCur = null;

    private int mCurNumber = 1;

    private boolean mIsLeft = true;

    public boolean isLeft() {
        return mIsLeft;
    }

    public GiftContinueView(Context context) {
        super(context);
        init(context);
    }

    public GiftContinueView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GiftContinueView);
        mIsLeft = a.getBoolean(0, true);
        a.recycle();
        init(context);
    }

    public GiftContinueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GiftContinueView);
        mIsLeft = a.getBoolean(0, true);
        a.recycle();
        init(context);
    }

    private void init(Context context) {
        if (mIsLeft) {
            inflate(context, R.layout.gift_continue_view, this);
        } else {
            inflate(context, R.layout.gift_continue_right_view, this);
        }
        initViews();
    }

    private GiftPictureAnimationView mGiftPictureAnimationView;
    private GiftNumberAnimationView mGiftNumberAniamtionView;
    private TextView mNameTv;
    private TextView mInfoTv;
    private RelativeLayout mContainer;
    private BaseImageView mOwnerIv;
    private ImageView mUserBadgeIv;

    // view Y轴初始坐标
    private float mTranslationY;

    // 是否正在播放
    private boolean mPlaying = false;

    // 正在等待消失
    private boolean mWaitingDismiss = false;

    private void initViews() {

        mContainer = (RelativeLayout) findViewById(R.id.container);
        mOwnerIv = (BaseImageView) findViewById(R.id.sender_iv);
        mGiftPictureAnimationView = (GiftPictureAnimationView) findViewById(R.id.gift_picture_vg);
        mGiftNumberAniamtionView = (GiftNumberAnimationView) findViewById(R.id.gift_number_vg);
        mNameTv = (TextView) findViewById(R.id.name_tv);
        mInfoTv = (TextView) findViewById(R.id.info_tv);
        mUserBadgeIv = (ImageView) findViewById(R.id.user_badge_iv);
        //还原
        mTranslationY = GiftContinueView.this.getTranslationY();
        mPlaying = false;
        mWaitingDismiss = false;

        RxView.clicks(mContainer)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        if (CommonUtils.isFastDoubleClick(1000)) {
                            return;
                        }
                        if (mCur == null) {
                            return;
                        }
                        long id = mCur.getUserId();
                        if (id != 0) {
                            UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, id, null);
                        }
                    }
                });
    }

    long mDisplayId = 0;

    //  初始化第一次填充
    private void prepare() {
        if (mCur != null) {
            MyLog.d(TAG, "prepare");
            mGiftNumberAniamtionView.setPlayNumber(mCurNumber);

            mNameTv.setText("" + mCur.getSenderName());
            mInfoTv.setText(mCur.getSendDescribe());
            long userId = mCur.getUserId();
            mDisplayId = userId;
            MyLog.d(TAG, "userId:" + userId + "mCur.getAvatarTimestamp():" + mCur.getAvatarTimestamp());
            AvatarUtils.loadAvatarByUidTs(mOwnerIv, userId, mCur.getAvatarTimestamp(), true);
            // 加载图像
            if (mCur.getGifType() == GiftType.NORMAL_EFFECTS_GIFT ||
                    (mCur.getGifType() == GiftType.PRIVILEGE_GIFT && mCur.getGiftOriginType() == GiftType.NORMAL_EFFECTS_GIFT)) {
                NormalEffectGift effectGift = (NormalEffectGift) mCur.getGift();
                //设立标记
                mGiftPictureAnimationView.setFlags(effectGift.getFlags());
                mGiftPictureAnimationView.setBigCons(effectGift.getBigCons());
            }
            mGiftPictureAnimationView.fillPictureByNumber(mCur.getPicPath(), mCurNumber);
            mGiftPictureAnimationView.setVisibility(VISIBLE);
            if (mCur.getCertificationType() > 0) {
                mUserBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(15f);
                mUserBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(15f);
                mUserBadgeIv.setImageDrawable(DataformatUtils.getCertificationImgSource(mCur.getCertificationType()));
            } else {
                mUserBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(10f);
                mUserBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(10f);
                mUserBadgeIv.setImageDrawable(DataformatUtils.getLevelSmallImgSource(mCur.getLevel()));
            }
        }
    }

    ObjectAnimator mPlayEnterAnimator;

    // 播放从左到右进入动画
    private void playEnter() {
        if (mCur == null) {
            return;
        }
        mPlaying = true;
        if (mPlayEnterAnimator == null) {
            float curTranslationX = this.getTranslationX();
            int offset = -300;
            if (!mIsLeft) {
                offset = 300;
            }
            mPlayEnterAnimator = ObjectAnimator.ofFloat(this, "translationX", curTranslationX + offset, curTranslationX);
            mPlayEnterAnimator.setDuration(200);
            mPlayEnterAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    GiftContinueView.this.setLayerType(View.LAYER_TYPE_NONE, null);
                    mGiftAnimatorPlayOnlyNumFlag = false;
                    playTextScale();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    GiftContinueView.this.setVisibility(View.VISIBLE);
                    GiftContinueView.this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            });
        }
        mPlayEnterAnimator.start();
    }

    TimeInterpolator timeInterpolators[] = new TimeInterpolator[]{
//            new AccelerateDecelerateInterpolator()
//            ,new AccelerateInterpolator()
//            ,new AnticipateInterpolator() // 还不错
//            new AnticipateOvershootInterpolator() //还可以，应该就这个
            new BounceInterpolator()
//            ,new OvershootInterpolator()
    };

    AnimatorListenerAdapter mGiftNumberAniamtionListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            MyLog.d(TAG, "playTextScale onAnimationEnd");
            // 准备消失
            mH.post(new Runnable() {
                @Override
                public void run() {
                    tryPlayDismissDelay();
                }
            });
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            onAnimationEnd(animation);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            MyLog.d(TAG, "playTextScale onAnimationStart");
            mGiftNumberAniamtionView.setVisibility(View.VISIBLE);
            mWaitingDismiss = false;
        }
    };

    /**
     * 字体放大
     */
    private void playTextScale() {
        if (mCur == null) {
            mGiftNumberAniamtionListenerAdapter.onAnimationEnd(null);
            return;
        }
        // 如果显示的送礼信息不对，尝试纠正一下。
        if (mCur.getUserId() != mDisplayId) {
            prepare();
        }
        MyLog.d(TAG, "playTextScale mCurNumber=" + mCurNumber + ",getEndNumber=" + mCur.getEndNumber());
        // 检查一下是否需要变化图片
        if (mCur.getGifType() == GiftType.NORMAL_EFFECTS_GIFT) {
            mGiftPictureAnimationView.tryPlayIfNeed(mCurNumber);
            for (NormalEffectGift.BigContinue f : ((NormalEffectGift) mCur.getGift()).getBigCons()) {
                if (mCurNumber < f.startCount) {
                    break;
                }
                if (mCurNumber == f.startCount && f.startCount != 1) {
                    Gift gift = GiftRepository.findGiftById(f.giftId);
                    GiftRecvModel model = new GiftRecvModel();
                    model.setFromSelf(mCur.isFromSelf());
                    model.setGift(gift);
                    model.setSenderName(mCur.getSenderName());
                    model.setUserId(mCur.getUserId());
                    model.setCertificationType(mCur.getCertificationType());
                    model.setLevel(mCur.getLevel());
                    EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Big(model));
                    break;
                }
            }
        }
        mGiftNumberAniamtionView.setPlayNumber(mCurNumber);
        if (mKeepAdding && mCur.isFromSelf() && mCurNumber < mCur.getEndNumber()) {
            // 不播动画了，防止跟不上手速
//            mGiftNumberAniamtionListenerAdapter.onAnimationEnd(null);
            mGiftNumberAniamtionView.setIsMySendFastMode(true);
            mGiftNumberAniamtionView.play(mGiftNumberAniamtionListenerAdapter, mGiftNumValue, true);
        } else {
            mGiftNumberAniamtionView.setIsMySendFastMode(false);
            mGiftNumberAniamtionView.play(mGiftNumberAniamtionListenerAdapter, mGiftNumValue, mGiftAnimatorPlayOnlyNumFlag);
        }
    }

    /**
     * 有几个数字能快播
     */
    private int mGiftNumValue = 1;

    private boolean mGiftAnimatorPlayOnlyNumFlag = false;

    private void tryPlayDismissDelay() {
        MyLog.d(TAG, "playDismissDelay");

        if (mForceReplace) {
            // 有人在等直接走消息
            mWaitingDismiss = true;
            mH.postDelayed(playDismissRunnable, 200);
            return;
        }
        //消失检查一下有没有必要消失
        // 不用消失了,再次播放缩放动画
        if (mCur != null && mCurNumber < mCur.getEndNumber()) {
            mCurNumber++;

            getGiftNumOnlyPlayParameter();

            playTextScale();
            return;
        }
        mH.removeCallbacks(playDismissRunnable);
        mH.postDelayed(playDismissRunnable, 2000);
        mWaitingDismiss = true;
    }

    /**
     * 判断是否只播放数字
     * 获取连续播放的次数
     */
    private void getGiftNumOnlyPlayParameter() {
        mGiftNumValue--;
        if (mGiftNumValue < 1) {
            mGiftNumValue = mCur.getEndNumber() - mCurNumber;
            if (mGiftNumValue > 5) {
                mGiftNumValue = 5;
            }
            //不符合情况也会执行到这里
            mGiftAnimatorPlayOnlyNumFlag = mGiftNumValue > 1;
        } else {
            mGiftAnimatorPlayOnlyNumFlag = true;
        }
    }

    AnimatorSet mPlayDismissAnimSet;

    Runnable playDismissRunnable = new Runnable() {
        @Override
        public void run() {
            MyLog.d(TAG, "playDismissing");
            if (mPlayDismissAnimSet == null) {
                float curTranslationY = GiftContinueView.this.getTranslationY();
                ObjectAnimator moveY = ObjectAnimator.ofFloat(GiftContinueView.this, "translationY", curTranslationY, curTranslationY - 300);
                ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(GiftContinueView.this, "alpha", 1f, 0f);
                mPlayDismissAnimSet = new AnimatorSet();
                mPlayDismissAnimSet.play(moveY).with(fadeInOut);
                mPlayDismissAnimSet.setDuration(500);
                mPlayDismissAnimSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 如果没人在等
                        mPlaying = false;
                        reset();
                        if (!tryReplace()) {
                            if (mCur != null && mCurNumber < mCur.getEndNumber()) {
                                mCurNumber++;
                                playEnter();
                            } else {
                                mCurNumber = 1;
                                mCur = null;
                                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_PLAY_COMPLETE, GiftContinueView.this, null));
                            }
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // 还是走end
                        onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        mWaitingDismiss = false;
                    }
                });
            }
            mPlayDismissAnimSet.start();
        }
    };

    private void reset() {
        //还原
        GiftContinueView.this.setTranslationY(mTranslationY);
        GiftContinueView.this.setAlpha(1f);
        GiftContinueView.this.setVisibility(View.GONE);
        mGiftPictureAnimationView.reset();
        mKeepAdding = false;
    }

    private Handler mH = new Handler(Looper.getMainLooper());

    private boolean mKeepAdding = false;

    public synchronized boolean canMerge(GiftRecvModel model) {
        GiftRecvModel cur = mCur;
        MyLog.d(TAG, "GiftBigPackOfGift：model=" + model + ",cur=" + cur);
        if (cur == null || model == null) {
            return false;
        }

        if (cur.getUserId() != model.getUserId()
                || cur.getGiftId() != model.getGiftId()
                || cur.getContinueId() != model.getContinueId()) {
            return false;
        }

        // 都一样 吃掉这个
        if (model.getEndNumber() > cur.getEndNumber()) {
            cur.setEndNumber(model.getEndNumber());
            triggerPlayIfNeed(cur);
            mKeepAdding = true;
        }
        return true;
    }

    private boolean mForceReplace = false;

    private GiftRecvModel mReplaceGift = null;

    public boolean setForceReplaceFlag(GiftRecvModel model) {
        // 如果已经有在正在等待被替代的
        if (mForceReplace && mReplaceGift != null) {
            if (mReplaceGift.getUserId() == model.getUserId()
                    && mReplaceGift.getGiftId() == model.getGiftId()
                    && mReplaceGift.getContinueId() == model.getContinueId()) {
                // 都一样 吃掉这个
                if (model.getEndNumber() > mReplaceGift.getEndNumber()) {
                    mReplaceGift.setEndNumber(model.getEndNumber());
                }
                return true;
            }
        }
        if (mCur != null && mCur.isFromSelf()) {
            //也来自自己，替代不了,老实排队
            return false;
        }
        // 如果正在播放，取消当前播放，强制替换掉，再次播放
        if (mPlaying) {
            mForceReplace = true;
            mReplaceGift = model;
            return true;
        } else {
            // 已经不再播放了应该会被空闲吃掉呀，讲道理的话不会走到这里
        }
        return false;
    }

    // 尝试替换
    public boolean tryReplace() {
        if (mForceReplace) {
            mForceReplace = false;
            // 剩余没播完的
            if (mReplaceGift != null) {
                GiftRecvModel left = mCur;
                mCur = mReplaceGift;
                mReplaceGift = null;
                if (left != null) {
                    left.setStartNumber(mCurNumber + 1);
                    if (left.getStartNumber() < left.getEndNumber()) {
                        // 还没播完的进队，下次重新播
                        EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_PLAY_BREAK, GiftContinueView.this, left));
                    }
                }
                // 播放当前的
                mCurNumber = mCur.getStartNumber();
                prepare();
                playEnter();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isIdle() {
        return mPlaying == false;
    }

    public synchronized boolean addGiftContinueMode(GiftRecvModel model, int queueSize) {
        if (mPlaying) {
            return false;
        }
        if (queueSize > 10) {
            mGiftNumberAniamtionView.setSpeedMode(SPEED_FAST);
        } else {
            mGiftNumberAniamtionView.setSpeedMode(SPEED_NORMAL);
        }
        triggerPlayIfNeed(model);
        return true;
    }

    private void triggerPlayIfNeed(GiftRecvModel model) {
        if (mPlaying && !mWaitingDismiss) {
            return;
        }
        if (model == null) {
            return;
        }

        MyLog.d(TAG, "GiftBigPackOfGift：canMerge" + model.getGift().getGiftId() + "model:" + model.getEndNumber());
        mCur = model;
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                MyLog.d(TAG, "onNext");
                if (!mPlaying) {
                    // 之前队列为空，播放
                    MyLog.d(TAG, "playEnter");
                    mCurNumber = mCur.getStartNumber();
                    prepare();
                    playEnter();
                } else if (mWaitingDismiss) {
                    // 不用消失了,再次播放缩放动画
                    if (mCurNumber < mCur.getEndNumber()) {
                        mH.removeCallbacks(playDismissRunnable);
                        mCurNumber++;
                        mGiftAnimatorPlayOnlyNumFlag = false;
                        playTextScale();
                    }
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .compose(((RxActivity) getContext()).bindToLifecycle())
                .subscribe();
    }

    // 加入队列，以何种方式播放当前的呢
    // 从队列取出，若之前没有过播放记录，判断当前的动画执行的状态。。
    // 这里可能要决定是否播放下一个了，如果下一个和当前一样就直接播放下一个
    // 如果有下一个，判断下一个和当前是否同一个用户和礼物，如果是，直接播放字体放大动画，移除当前。如果不是，移除当前，播放消失动画。
    // 如果没有下一个，移除当前，播放消失动画。

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb
                .append("id:")
                .append(myid)
                .append("playing=")
                .append(mPlaying)
                .append(",waiting dismiss=")
                .append(mWaitingDismiss)
                .append("\r\n")
                .append("mCurNumber:")
                .append(mCurNumber)
                .append(",mCur:")
                .append(mCur);
        return sb.toString();
    }

    public void onDestroy() {
        MyLog.d(TAG, "onDestroy");
        clearAll();
    }

    public void switchAnchor() {
        clearAll();
    }

    private void clearAll() {
        //TODO 以后做优化
        mH.removeCallbacksAndMessages(null);
        mPlaying = false;
        if (mCur != null) {
            //变得很小，相当于结束
            mCur.setEndNumber(1);
        }

        if (mPlayDismissAnimSet != null) {
            mPlayDismissAnimSet.cancel();
        }
        if (mPlayEnterAnimator != null) {
            mPlayEnterAnimator.cancel();
        }

        mGiftNumValue = 1;

        mGiftAnimatorPlayOnlyNumFlag = false;
    }

}
