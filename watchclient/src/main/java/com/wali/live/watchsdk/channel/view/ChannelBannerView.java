package com.wali.live.watchsdk.channel.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.activity.RxActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.view.ViewPagerWithCircleIndicator;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.holder.listener.BannerClickListener;
import com.wali.live.watchsdk.channel.viewmodel.ChannelBannerViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by lan on 16/4/26.
 *
 * @module 频道
 * @description 频道广告View
 */
public class ChannelBannerView extends RelativeLayout {
    public static final String TAG = ChannelBannerView.class.getSimpleName();

    protected static final int BANNER_IMAGE_WIDTH = 1080;
    protected static final int BANNER_IMAGE_HEIGHT = 300;

    protected static int sBannerHeight;

    static {
        sBannerHeight = GlobalData.screenWidth * BANNER_IMAGE_HEIGHT / BANNER_IMAGE_WIDTH;
    }

    ViewPagerWithCircleIndicator mViewPagerWithCircleIndicator;

    private PagerAdapter mPagerAdapter;
    private List<AbsSingleBannerView> mCacheList = new LinkedList();
    private List<ChannelBannerViewModel.Banner> mBannerModels = new ArrayList();

    private int mBannerPosition = 0;
    private boolean mForbidAutoScroll = false;
    // 是否开启连续滚动
    private boolean mOpenRepeatScroll = true;

    private Subscription mAutoScrollSubscription;
    private BannerStateListener mBannerStateListener;

    private BannerClickListener mBannerClickListener;

    protected <V extends View> V $(int resId) {
        return (V) findViewById(resId);
    }

    public void setBannerStateListener(BannerStateListener listener) {
        mBannerStateListener = listener;
    }

    public void setBannerClickListener(BannerClickListener bannerClickListener) {
        mBannerClickListener = bannerClickListener;
    }

    public ChannelBannerView(Context context) {
        super(context);
        init(context);
    }

    public ChannelBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChannelBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void init(Context context) {
        inflate(context, R.layout.banner_view, this);
        mViewPagerWithCircleIndicator = $(R.id.ad_viewpager);


        mPagerAdapter = new PagerAdapter() {
            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public int getCount() {
                if (mBannerModels == null) {
                    return 0;
                }
                if (mBannerModels.size() < 2) {
                    return mBannerModels.size();
                }
                return mOpenRepeatScroll ? Integer.MAX_VALUE : mBannerModels.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                AbsSingleBannerView v = (AbsSingleBannerView) object;
                container.removeView(v);
                mCacheList.add(v);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ChannelBannerViewModel.Banner banner = mBannerModels.get(position % mBannerModels.size());

                AbsSingleBannerView view;
                if (mCacheList.isEmpty()) {
                    view = newSingleBannerView();
                    view.setBannerClickListener(mBannerClickListener);
                } else {
                    view = mCacheList.remove(0);
                }
                view.setBanner(banner);
                ViewPager.LayoutParams params = new ViewPager.LayoutParams();
                container.addView(view, params);
                return view;
            }
        };
        mViewPagerWithCircleIndicator.setAdapter(mPagerAdapter);
        mViewPagerWithCircleIndicator.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mBannerPosition = position;
                MyLog.d(TAG, "onPageSelected mBannerPosition : " + mBannerPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (mBannerStateListener != null) {
                        mBannerStateListener.onBannerIdle();
                    }
                } else {
                    if (mBannerStateListener != null) {
                        mBannerStateListener.onBannerScroll();
                    }
                }
            }
        });
        mViewPagerWithCircleIndicator.setDrawCycleColor(0xffffffff, 0x40ffffff);
        mViewPagerWithCircleIndicator.setItemWidth(12);
        mViewPagerWithCircleIndicator.setItemHeight(4);
        mViewPagerWithCircleIndicator.setDrawCycleGravity(ViewPagerWithCircleIndicator.MODE_RIGHT);
        mViewPagerWithCircleIndicator.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mForbidAutoScroll = true;
                        mViewPagerWithCircleIndicator.requestDisallowInterceptTouchEvent(true);
                        stopBannerAutoScroll();
                        break;
                    case MotionEvent.ACTION_UP:
                        mForbidAutoScroll = false;
                        mViewPagerWithCircleIndicator.requestDisallowInterceptTouchEvent(false);
                        startBannerAutoScroll();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        mForbidAutoScroll = false;
                        mViewPagerWithCircleIndicator.requestDisallowInterceptTouchEvent(false);
                        startBannerAutoScroll();
                        break;
                }
                return false;
            }
        });
    }

    protected AbsSingleBannerView newSingleBannerView() {
        return new SingleBannerView(getContext());
    }

    public void adjustSize() {
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        if (mlp != null) {
            mlp.height = sBannerHeight;
        }
    }

    public void setData(List<ChannelBannerViewModel.Banner> banners) {
        if (banners == null) {
            return;
        }
        mBannerModels.clear();
        mBannerModels.addAll(banners);
        // 连续滑动，增加以下逻辑
        if (mOpenRepeatScroll && banners.size() > 1) {
            mViewPagerWithCircleIndicator.setRepeatScroll(true);
            mViewPagerWithCircleIndicator.setActualCount(mBannerModels.size());
        } else {
            mViewPagerWithCircleIndicator.setRepeatScroll(false);
            mViewPagerWithCircleIndicator.setActualCount(0);
        }
        mPagerAdapter.notifyDataSetChanged();
        if (mOpenRepeatScroll) {
            mViewPagerWithCircleIndicator.setCurrentItem(100 * mBannerModels.size());
        }
    }

    private RxActivity getRxActivity() {
        return (RxActivity) getContext();
    }

    public void startBannerAutoScroll() {
        if (mForbidAutoScroll) {
            return;
        }
        if (mAutoScrollSubscription == null || mAutoScrollSubscription.isUnsubscribed()) {
            mAutoScrollSubscription = Observable
                    .interval(5000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(getRxActivity().<Long>bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(Long l) {
                            if (!mForbidAutoScroll) {
                                int count = mPagerAdapter.getCount();
                                mViewPagerWithCircleIndicator.setCurrentItem((mBannerPosition + 1) % count);
                            }
                        }
                    });
        }
    }

    public void stopBannerAutoScroll() {
        if (mAutoScrollSubscription != null && !mAutoScrollSubscription.isUnsubscribed()) {
            mAutoScrollSubscription.unsubscribe();
        }
    }

    public interface BannerStateListener {
        void onBannerScroll();

        void onBannerIdle();
    }
}
