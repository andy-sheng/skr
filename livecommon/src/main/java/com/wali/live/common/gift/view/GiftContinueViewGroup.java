package com.wali.live.common.gift.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.event.SdkEventClass;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.wali.live.event.UserActionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/2/20.
 *
 * @Module 礼物连送动画区
 */
public class GiftContinueViewGroup extends RelativeLayout implements IBindActivityLIfeCycle {
    public static String TAG = GiftContinueViewGroup.class.getSimpleName();

    private List<GiftContinuousView> mFeedGiftContinueViews;

    private List<GiftContinuousView> mSingleFeedViewList;

    private IGiftScheduler giftScheduler=new GiftScheduler();


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
        GiftContinuousView v1 = (GiftContinuousView) findViewById(R.id.gift_continue_view1);
        v1.setMyId(1);
        getFeedGiftContinueViews().add(v1);
        getSingleFeedGiftContinueView().add(v1);

        GiftContinuousView v2 = (GiftContinuousView) findViewById(R.id.gift_continue_view2);
        v2.setMyId(2);
        getFeedGiftContinueViews().add(v2);

        giftScheduler.setGiftContinuousViews(getFeedViews());
    }

    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onActivityDestroy() {
        for (GiftContinuousView v : getFeedGiftContinueViews()) {
            v.onDestroy();
        }
        giftScheduler.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(UserActionEvent.SwitchAnchor event) {
        giftScheduler.clearQueue();
        for (GiftContinuousView v : getFeedGiftContinueViews()) {
            v.setVisibility(View.GONE);
            v.switchAnchor();
        }
    }



    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(SdkEventClass.OrientEvent event) {
        orient(event.isLandscape());
    }

    private boolean mIsLandscape = false;
    private int mGiftViewHeight = DisplayUtils.dip2px(140);
    private int mGiftViewMarginBottom = DisplayUtils.dip2px(110.33f);
    private int mLandscapeMarginBottom = DisplayUtils.dip2px(60f);

    public void orient(boolean isLandscape) {
        mIsLandscape = isLandscape;
        giftScheduler.setGiftContinuousViews(getFeedViews());
        MyLog.d(TAG, "isLandscape:" + isLandscape);
        if (isLandscape) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.getLayoutParams();
            lp.height = DisplayUtils.dip2px(100);
            lp.alignWithParent = true;
            lp.bottomMargin = mLandscapeMarginBottom;
        } else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.getLayoutParams();
            lp.height = DisplayUtils.dip2px(150);
            lp.alignWithParent = true;
            lp.bottomMargin = mGiftViewMarginBottom;
            for(GiftContinuousView v:getFeedViews()){
                v.tryAwake();
            }
        }
    }

    public void setOrient(boolean isLandscape) {
        mIsLandscape = isLandscape;
    }

    public void onShowInputView() {

        RelativeLayout.LayoutParams middleLayout = (RelativeLayout.LayoutParams) this.getLayoutParams();
        if (!mIsLandscape) {
//            middleLayout.bottomMargin = DisplayUtils.dip2px(10);
        } else {

            this.setVisibility(View.INVISIBLE);
//            middleLayout.bottomMargin = mLandscapeMarginBottom;
            //middleLayout.height=LayoutParams.WRAP_CONTENT;
//            middleLayout.height = mGiftViewHeight / 2;
        }
        setLayoutParams(middleLayout);
    }

    public void onHideInputView() {
        RelativeLayout.LayoutParams middleLayout = (RelativeLayout.LayoutParams) this.getLayoutParams();
        if (!mIsLandscape) {
//            middleLayout.bottomMargin = mGiftViewMarginBottom;
//            middleLayout.height = mGiftViewHeight;
        } else {
//            middleLayout.bottomMargin = mLandscapeMarginBottom;
//            middleLayout.height = mGiftViewHeight;
        }
        setLayoutParams(middleLayout);
        if (this.getVisibility() != View.VISIBLE) {
            this.setVisibility(View.VISIBLE);
        }
    }

    private List<GiftContinuousView> getFeedGiftContinueViews() {
        if (mFeedGiftContinueViews == null) {
            mFeedGiftContinueViews = new ArrayList<>(2);
        }
        return mFeedGiftContinueViews;
    }

    private List<GiftContinuousView> getSingleFeedGiftContinueView() {
        if (mSingleFeedViewList == null) {
            mSingleFeedViewList = new ArrayList<>(1);
        }
        return mSingleFeedViewList;
    }

    public List<GiftContinuousView> getFeedViews() {
        //当横屏时要防止上面的continueView遮挡运营位，故上面的continueView不显示
        if (mIsLandscape) {
            return getSingleFeedGiftContinueView();
        } else {
            return getFeedGiftContinueViews();
        }
    }

    /**
     * 目前主要用来切换房间时，重置内部状态
     */
    public void reset() {
        giftScheduler.onDestroy();
        for (GiftContinuousView v : mFeedGiftContinueViews) {
            v.setVisibility(View.GONE);
            v.switchAnchor();
        }
        mFeedGiftContinueViews.clear();
        for (GiftContinuousView v : mSingleFeedViewList) {
            v.setVisibility(View.GONE);
            v.switchAnchor();
        }
        mSingleFeedViewList.clear();
    }
}
