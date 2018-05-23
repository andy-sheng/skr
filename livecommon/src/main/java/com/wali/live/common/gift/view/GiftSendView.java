package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.mi.live.data.event.GiftEventClass;
import com.wali.live.common.gift.utils.RxCountDown;
import com.wali.live.dao.Gift;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by jiang on 18-5-18.
 */

public class GiftSendView extends RelativeLayout{


    public static String TAG = "GiftSendView";

    public static final int BIGGEST_SMALLGIFT_PIRCE = 10;

    //大圆盘的直径
    int circleRadio = DisplayUtils.dip2px(86.67f);
    //大圆盘的中心到小圆盘的中心的距离
    int offRadio = DisplayUtils.dip2px(61.33f);
    //小圆盘的直径
    int smallCirleRadio = DisplayUtils.dip2px(18.33f);

    Gift mSelectedGift = null;

    //位置优先级
    int[] arryList = new int[]{2, 3, 1, 4};


    ArrayList<SmallSendGiftBtn> itemViewList = new ArrayList<SmallSendGiftBtn>();
    //第一级别
    int firstLevel = 2;
    //第二级别
    int secondLevel = 2;
    //礼物数量
    int[] levelGiftNum = new int[]{188, 66};

    GiftMallView.SelectedViewInfo mSelectViewInfo = null;

    BuyGiftCallBack mCallBack;

    View mCircleCenter;
    //记录上次购买的礼物的数量
    private int giftCount = 1;

    TextView mTimes;

    public SendGiftCircleCountDownView circle;

    ValueAnimator animator;

    public static int TIME = 5;

    //private TextView mNumText;

    private GiftContinueNumView continueNumView;

    private View mContinueText;

    private View mRoot;

    private View mCenterRoot;

    //如果包裹礼物用完再刷新的就不需要触发刷新逻辑，默认为true
    private boolean isRemovedByPacket = false;

    //当前赠送的数量，如66,188,1314
    private int currentSendNum = 0;

    //这个是当前正在赠送的按钮，如中间的按钮和旁边的小圆圈
    private View currentSendBtn;

    public GiftSendView(Context context, Gift selectedGift, GiftMallView.SelectedViewInfo selectViewInfo, BuyGiftCallBack callBack) {
        super(context);
        mSelectedGift = selectedGift;
        mSelectViewInfo = selectViewInfo;
        mCallBack = callBack;
        init(context);
    }

    public GiftSendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftSendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        View.inflate(context, R.layout.send_gift_circle, this);
        mRoot = findViewById(R.id.send_gift_root);
        mCircleCenter = findViewById(R.id.view_center);
        mCenterRoot = findViewById(R.id.circle_root);
        mTimes = (TextView) findViewById(R.id.tv_times);
        circle = (SendGiftCircleCountDownView) findViewById(R.id.count_down_view);
        circle.setVisibility(View.GONE);
        circle.setMax(360);
        circle.setProgress(360);

        int x = mSelectViewInfo.position[0] - (circleRadio - mSelectViewInfo.width / 2);
        int y = mSelectViewInfo.position[1] - (circleRadio - mSelectViewInfo.height / 2);

        //直接add的话屏幕右边会显示缩小，执行一个动画，机智
        AnimatorSet smallBtnAddAnimation = new AnimatorSet();//组合动画
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mRoot, "translationX", 0, x);
        animatorX.setDuration(0);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mRoot, "translationY", 0, y);
        animatorY.setDuration(0);
        smallBtnAddAnimation.play(animatorX).with(animatorY);
        smallBtnAddAnimation.start();

        addSendItemView();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) v.getParent()).removeView(GiftSendView.this);
            }
        });

        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //中间的按钮默认为发送1
        mCircleCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (giftCount != 1) {
                    mCallBack.resetContinueSend(mSelectedGift);
                    //mNumText.setText("");
                }

                mCircleCenter.clearAnimation();
                AnimatorSet animatorSet = getAnimation(mCenterRoot);
                animatorSet.start();

                currentSendBtn = mCircleCenter;
//            layoutContinueBuyNum(mCircleCenter);
                giftCount = 1;
                buy(mSelectedGift, 1);

            }
        });


        layoutContinueBuyNum(mContinueText);
        continueNumView.setVisibility(GONE);
    }

    public void setRemovedByPacket() {
        isRemovedByPacket = true;
    }

    private void startCircleAnimation() {
        startCountDown();
        circle.setVisibility(View.VISIBLE);
        start();
    }

    void addSendItemView() {
        removeAllItemView();
        int num = getSmallItemNum();
        RelativeLayout circleView = (RelativeLayout) findViewById(R.id.send_gift_root);

        for (int i = 0; i < num; i++) {
           final SmallSendGiftBtn smallSendBtn = new SmallSendGiftBtn.SmallSendGiftBtnBuilder(getContext())
                    .setCircleRadio(circleRadio)
                    .setSmallCirleRadio(smallCirleRadio)
                    .setOffRadio(offRadio)
                    .setFace(mCallBack.getFace(mSelectedGift))
                    .setPosition(arryList[i])
                    .setNum(getSendGiftNum(i)).build();

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(smallCirleRadio * 2, smallCirleRadio * 2);
            final int margin = circleRadio - (smallCirleRadio);
            layoutParams.setMargins(margin, margin, 0, 0);
            smallSendBtn.setLayoutParams(layoutParams);
            circleView.addView(smallSendBtn, 0);
            smallSendBtn.setTextSize(11.33f);
            itemViewList.add(smallSendBtn);

            Observable.timer((arryList[i] - 1) * 30, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            AnimatorSet smallBtnAddAnimation = new AnimatorSet();//组合动画
                            ObjectAnimator animatorX = ObjectAnimator.ofFloat(smallSendBtn, "translationX", 0, smallSendBtn.horizel - margin);
                            animatorX.setInterpolator(new OvershootInterpolator());
                            animatorX.setDuration(800);
                            ObjectAnimator animatorY = ObjectAnimator.ofFloat(smallSendBtn, "translationY", 0, smallSendBtn.vertical - margin);
                            animatorY.setInterpolator(new OvershootInterpolator());
                            animatorY.setDuration(800);
                            smallBtnAddAnimation.play(animatorX).with(animatorY);
                            smallBtnAddAnimation.start();

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });

            smallSendBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (giftCount != Integer.parseInt(smallSendBtn.getText().toString())) {
                        mCallBack.resetContinueSend(mSelectedGift);
                        //mNumText.setText("");
                    }

                    smallSendBtn.clearAnimation();
                    AnimatorSet animatorSet = getAnimation(smallSendBtn);
                    animatorSet.start();

                    currentSendBtn = smallSendBtn;

//                layoutContinueBuyNum(smallSendBtn);
                    giftCount = Integer.parseInt(smallSendBtn.getText().toString());
                    buy(mSelectedGift, Integer.parseInt(smallSendBtn.getText().toString()));
                }
            });
        }

        addGiftBuyNumText(circleView);
    }

    private void addGiftBuyNumText(ViewGroup root) {
        //mNumText = new TextView(getContext());
        continueNumView = new GiftContinueNumView(getContext());
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        continueNumView.setLayoutParams(layoutParams);
//        TextPaint tp=mNumText.getPaint();
//        tp.setFakeBoldText(true);
//        mNumText.setLayoutParams(layoutParams);
//        mNumText.setTextSize(12.67f);
//        mNumText.setMaxLines(1);
//        mNumText.setGravity(Gravity.CENTER);
//        mNumText.setTextColor(GlobalData.app().getResources().getColor(R.color.white));
        //mNumText.setOutTextColor(R.color.transparent);
        root.addView(continueNumView);
        continueNumView.disableClipOnParents(continueNumView);

    }

    private AnimatorSet getAnimation(View sendBtn) {
        AnimatorSet animatorSet = new AnimatorSet();//组合动画

        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(sendBtn, "scaleX", 1.0f, 0.8f);
        scaleX1.setDuration(150);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(sendBtn, "scaleY", 1.0f, 0.8f);
        scaleY1.setDuration(150);
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(sendBtn, "scaleX", 0.8f, 1.0f);
        scaleX2.setInterpolator(new OvershootInterpolator(5.0f));
        scaleX2.setDuration(300);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(sendBtn, "scaleY", 0.8f, 1.0f);
        scaleY2.setInterpolator(new OvershootInterpolator(5.0f));
        scaleY2.setDuration(300);

        animatorSet.play(scaleX2).with(scaleY2);
        animatorSet.play(scaleX1).before(scaleX2);
        animatorSet.play(scaleX1).with(scaleY1);

        return animatorSet;
    }

    private void layoutContinueBuyNum(View sendBtn) {
        if (sendBtn == mContinueText) {
            return;
        }
        //只有点击批量赠送按钮才显示连送
        if (sendBtn == mCircleCenter) {
            //mNumText.setVisibility(GONE);
            continueNumView.setVisibility(GONE);
        } else {
            //mNumText.setVisibility(VISIBLE);
            continueNumView.setVisibility(VISIBLE);
        }

        mContinueText = sendBtn;

        int marginLeft;
        int marginTop;
        if (sendBtn instanceof SmallSendGiftBtn) {
            SmallSendGiftBtn smallSendGiftBtn = (SmallSendGiftBtn) sendBtn;
            marginLeft = smallSendGiftBtn.horizel + smallSendGiftBtn.getWidth() - DisplayUtils.dip2px(40f);
            marginTop = smallSendGiftBtn.vertical - DisplayUtils.dip2px(5.3f);
        } else {
            marginLeft = DisplayUtils.dip2px(110f);
            marginTop = DisplayUtils.dip2px(51.33f);
        }

        if (mContinueText != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) continueNumView.getLayoutParams();
            layoutParams.setMargins(marginLeft, marginTop, 0, 0);
            continueNumView.setLayoutParams(layoutParams);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GiftEventClass.ContinueGiftSendNum event) {
        layoutContinueBuyNum(currentSendBtn);
        showContinueBuyNum(event.num);
    }

    public void showContinueBuyNum(int num) {
        startCircleAnimation();
        //mNumText.setText("X" + num);
        continueNumView.showNum(num);
    }

    public void buy(Gift gift, int buyCount) {
        try {
            currentSendNum = buyCount;
            mCallBack.buy(gift, buyCount);
            startCircleAnimation();
        } catch (NumberFormatException e) {
            MyLog.w(TAG, e);
        }
    }

    void removeAllItemView() {
        for (int i = 0; i < itemViewList.size(); i++) {
            if (itemViewList.get(i).getParent() != null) {
                removeView(itemViewList.get(i));
            }
        }
        itemViewList.clear();
    }

    int getSmallItemNum() {
        int giftPrice = mSelectedGift.getPrice();
        if (1 <= giftPrice && giftPrice <= 10) {
            return firstLevel;
        }
        return 0;
    }

    Subscription countDownTimer;

    private void startCountDown() {
        unsubscribeTimer();
        countDownTimer = RxCountDown.countdown(5)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        mTimes.setText(integer + "s");
                        if (integer == 0) {
                            mTimes.setText("5s");
                            mCallBack.resetContinueSend(mSelectedGift);
                            //mNumText.setText("");
                            unsubscribeTimer();
                            circle.setVisibility(View.GONE);
                            ((ViewGroup) getParent()).removeView(GiftSendView.this);
                        }
                    }
                });
    }

    private void unsubscribeTimer() {
        if (countDownTimer != null && !countDownTimer.isUnsubscribed()) {
            countDownTimer.unsubscribe();
        }
    }

    public void start() {
        if (animator != null) {
            stop();
        }

        //这里用的是nineold的属性动画向下兼容包
        animator = ValueAnimator.ofInt(0, 360);
        animator.setDuration(TIME * 1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (Integer) animation.getAnimatedValue();
                circle.setProgress(animatedValue);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                circle.setProgress(360);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                circle.setProgress(360);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public void stop() {
        if (circle != null) {
            circle.setProgress(0);
        }
        if (animator != null) {
            animator.cancel();
            animator.removeAllUpdateListeners();
            animator = null;
        }
//        handler.removeCallbacksAndMessages(null); 有BUG
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unsubscribeTimer();
        if (!isRemovedByPacket) {
            if (mCallBack != null) {
                mCallBack.isRemoved(mSelectedGift);
            }
        }
        EventBus.getDefault().unregister(this);
    }

    public Observable<Integer> countdown(int time) {
        if (time < 0) time = 0;
        final int countTime = time;
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long increaseTime) {
                        return countTime - increaseTime.intValue();
                    }
                })
                .take(countTime + 1);
    }


    int getSendGiftNum(int position) {
        return levelGiftNum[position];
    }

    public interface BuyGiftCallBack {
        /**
         * @param gift
         * @param buyCount 购买数量
         */
        void buy(Gift gift, int buyCount);

        /**
         * @param gift 重置连送数量为0
         */
        void resetContinueSend(Gift gift);

        void isRemoved(Gift gift);

        int getFace(Gift gift);
    }
}
