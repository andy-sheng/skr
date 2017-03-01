package com.wali.live.common.gift.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.live.module.common.R;
import com.wali.live.base.BaseRotateSdkActivity;
import com.mi.live.data.event.GiftEventClass;
import com.wali.live.event.SdkEventClass;
import com.mi.live.data.gift.model.GiftContinueStrategyQueue;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 16/2/20.
 *
 * @Module 礼物连送动画区
 */
public class GiftContinueViewGroup extends RelativeLayout implements IBindActivityLIfeCycle {
    public static String TAG = GiftContinueViewGroup.class.getSimpleName();

    private List<GiftContinueView> mFeedGiftContinueViews = new ArrayList<GiftContinueView>(2);

    private GiftContinueStrategyQueue mQueue = new GiftContinueStrategyQueue();

    private List<GiftContinueView> mFeedGiftContinueRightViews = new ArrayList<GiftContinueView>(2);

    private GiftContinueStrategyQueue mRightQueue = new GiftContinueStrategyQueue();


    public GiftContinueViewGroup(Context context) {
        super(context);
        init(context);
    }

    public GiftContinueViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftContinueViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    public void init(Context context) {
        inflate(context, R.layout.gift_continue_view_group, this);
        bindView();
    }

    protected void bindView() {
        {
            GiftContinueView v1 = (GiftContinueView) findViewById(R.id.gift_continue_view1);
            v1.setMyId(1);
            mFeedGiftContinueViews.add(v1);
        }
        {
            GiftContinueView v2 = (GiftContinueView) findViewById(R.id.gift_continue_view2);
            v2.setMyId(2);
            mFeedGiftContinueViews.add(v2);
        }
//        {
//            GiftContinueView v3 = (GiftContinueView) findViewById(R.id.gift_continue_right_view1);
//            v3.setMyId(3);
//            mFeedGiftContinueRightViews.add(v3);
//        }
//        {
//            GiftContinueView v4 = (GiftContinueView) findViewById(R.id.gift_continue_right_view2);
//            v4.setMyId(4);
//            mFeedGiftContinueRightViews.add(v4);
//        }
    }

    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onActivityDestroy() {
        for (GiftContinueView v : mFeedGiftContinueViews) {
            v.onDestroy();
        }
        EventBus.getDefault().unregister(this);
        if (singleThreadForBuyGift != null) {
            singleThreadForBuyGift.shutdown();
        }
    }

//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(EventClass.SwitchAnchor event) {
//        mQueue.clear();
//        mRightQueue.clear();
//        for (GiftContinueView v : mFeedGiftContinueViews) {
//            v.setVisibility(View.GONE);
//            v.switchAnchor();
//        }
//        for (GiftContinueView v : mFeedGiftContinueRightViews) {
//            v.setVisibility(View.GONE);
//            v.switchAnchor();
//        }
//    }

    private ExecutorService singleThreadForBuyGift = Executors.newSingleThreadExecutor();

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftEventClass.GiftAttrMessage.Normal event) {
        Observable.just((GiftRecvModel) event.obj1)
                .filter(new Func1<GiftRecvModel, Boolean>() {
                    @Override
                    public Boolean call(GiftRecvModel giftRecvModel) {
                        return giftRecvModel != null;
                    }
                })
                .filter(new Func1<GiftRecvModel, Boolean>() {
                    @Override
                    public Boolean call(GiftRecvModel model) {
                        return !TextUtils.isEmpty(model.getPicPath());
                    }
                })
                .subscribeOn(Schedulers.from(singleThreadForBuyGift))
                .subscribe(new Action1<GiftRecvModel>() {
                    @Override
                    public void call(GiftRecvModel model) {
                        if (model.isLeft()) {
                            process(model, mQueue, mFeedGiftContinueViews);
                        } else {
                            process(model, mRightQueue, mFeedGiftContinueRightViews);
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(final GiftEventClass.GiftMallEvent event) {

        switch (event.eventType) {
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_PLAY_COMPLETE: {
                Observable.create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        GiftContinueView v = (GiftContinueView) event.obj1;
                        if (v.isLeft()) {
                            // 左边结束
                            GiftRecvModel data = mQueue.poll();
                            if (data != null) {
                                MyLog.d(TAG, "v" + v.getMyid() + "已经完成，取" + data);
                                // 如果view不接受这个data，重回队列
                                v.addGiftContinueMode(data, mQueue.size());
                            }
                        } else {
                            // 右边结束
                            GiftRecvModel data = mRightQueue.poll();
                            if (data != null) {
                                MyLog.d(TAG, "v" + v.getMyid() + "已经完成，取" + data);
                                // 如果view不接受这个data，重回队列
                                if (!v.addGiftContinueMode(data, mRightQueue.size())) {
                                    mRightQueue.offer(data);
                                }
                            }
                        }
                        subscriber.onCompleted();
                    }
                })
                        .subscribeOn(Schedulers.from(singleThreadForBuyGift))
                        .subscribe();
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_PLAY_BREAK: {
                Observable.create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        GiftContinueView v = (GiftContinueView) event.obj1;
                        if (v.isLeft()) {
                            // 左边结束
                            mQueue.offerToBreakQueue((GiftRecvModel) event.obj2);
                        } else {
                            // 右边结束
                        }
                        subscriber.onCompleted();
                    }
                })
                        .subscribeOn(Schedulers.from(singleThreadForBuyGift))
                        .subscribe();
            }
            break;
        }
    }

    private void process(GiftRecvModel model, GiftContinueStrategyQueue queue, List<GiftContinueView> views) {
        MyLog.d(TAG, "get a model:" + model);
        //如果是自己的礼物肯定要找个加进去
        //优先，找到能合并的
        for (GiftContinueView v : views) {
            if (v.canMerge(model)) {
                return;
            }
        }
        // 如果是自己的，先看看有没有空位
        if (model.isFromSelf()) {
            for (GiftContinueView v : views) {
                if (v.isIdle()) {
                    //自己的优先加入队头
                    if (v.addGiftContinueMode(model, mQueue.size())) {
                        return;
                    }
                }
            }
            //空闲的也没有，找个不是自己的强制替代
            for (GiftContinueView v : views) {
                if (v.setForceReplaceFlag(model)) {
                    // 替代成功
                    return;
                }
            }
        }
        //没人收留这个model
        queue.offer(model);
        for (GiftContinueView v : views) {
            if (v.isIdle()) {
                GiftRecvModel data = queue.poll();
                if (data != null) {
                    MyLog.d(TAG, "有空闲，添加" + data + "到v" + v.getMyid());
                    // 如果view不接受这个data，重回队列
                    if (!v.addGiftContinueMode(data, mQueue.size())) {
                        queue.offer(data);
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.OrientEvent event) {
        boolean isLandscape = false;
        if (event.orientation == BaseRotateSdkActivity.ORIENTATION_DEFAULT) {
            return;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_REVERSED) {
            isLandscape = true;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_REVERSED) {
            isLandscape = false;
        }
        orient(isLandscape);
    }

    private boolean mIsLandscape = false;
    private int mGiftViewHeight = DisplayUtils.dip2px(140);
    private int mGiftViewMarginBottom = DisplayUtils.dip2px(61.33f);
    private int mLandscapeMarginBottom = DisplayUtils.dip2px(20f);

    private void orient(boolean isLandscape) {
        mIsLandscape = isLandscape;
        MyLog.d(TAG, "isLandscape:" + isLandscape);
        if (isLandscape) {
            LayoutParams lp = (LayoutParams) this.getLayoutParams();
            lp.bottomMargin = mLandscapeMarginBottom;
        } else {
            LayoutParams lp = (LayoutParams) this.getLayoutParams();
            lp.bottomMargin = mGiftViewMarginBottom;
        }
    }

    public void setOrient(boolean isLandscape) {
        mIsLandscape = isLandscape;
    }

    public void onShowInputView() {

        RelativeLayout.LayoutParams middleLayout = (RelativeLayout.LayoutParams) this.getLayoutParams();
        if (!mIsLandscape) {
            middleLayout.bottomMargin = DisplayUtils.dip2px(10);
        } else {

            this.setVisibility(View.INVISIBLE);
            middleLayout.bottomMargin = mLandscapeMarginBottom;
            //middleLayout.height=LayoutParams.WRAP_CONTENT;
            middleLayout.height = mGiftViewHeight / 2;
        }
        setLayoutParams(middleLayout);
    }

    public void onHideInputView() {
        RelativeLayout.LayoutParams middleLayout = (RelativeLayout.LayoutParams) this.getLayoutParams();
        if (!mIsLandscape) {
            middleLayout.bottomMargin = mGiftViewMarginBottom;
            middleLayout.height = mGiftViewHeight;
        } else {
            middleLayout.bottomMargin = mLandscapeMarginBottom;
            middleLayout.height = mGiftViewHeight;
        }
        setLayoutParams(middleLayout);
        if (this.getVisibility() != View.VISIBLE) {
            this.setVisibility(View.VISIBLE);
        }
    }
}
