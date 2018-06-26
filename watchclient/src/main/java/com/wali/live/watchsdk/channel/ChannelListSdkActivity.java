package com.wali.live.watchsdk.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.base.view.NestViewPager;
import com.base.view.SlidingTabLayout;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.account.event.SetUserAccountEvent;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.adapter.ChannelTabPagerAdapter;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.list.presenter.ChannelListPresenter;
import com.wali.live.watchsdk.channel.list.presenter.IChannelListView;
import com.wali.live.watchsdk.channel.view.LiveChannelView;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.view.EmptyView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by lan on 16/11/25.
 */
public class ChannelListSdkActivity extends BaseSdkActivity implements IChannelListView {

    private SlidingTabLayout mSlidingTabLayout;
    private NestViewPager mViewPager;
    private EmptyView mEmptyView;

    private ChannelTabPagerAdapter mPagerAdapter;
    private ChannelListPresenter mChannelListPresenter;
    private boolean mIsFirstLoad = true;

    private BackTitleBar mTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);
        bindViews();
        loadData();
        report();
    }

    private void report() {
        try {
            String key = String.format(StatisticsKey.KEY_SDK_TONGGLE_CHANNEL_LIST_TYPE, HostChannelManager.getInstance().getChannelId(), 0);
            StatisticsAlmightyWorker.getsInstance().recordImmediatelyDefault(key, 1);
        } catch (Exception e) {
        }
    }

    private void bindViews() {
        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle("直播");
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.channel_tab);
        mViewPager = (NestViewPager) findViewById(R.id.view_pager);
        mEmptyView = (EmptyView) findViewById(R.id.empty_view);

        mSlidingTabLayout.setCustomTabView(R.layout.channel_slide_tab_view, R.id.tab_tv);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_ff2966));
        mSlidingTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));
        mSlidingTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(10));
        mSlidingTabLayout.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_TAIL);
//        if (BaseSdkActivity.isProfileMode()) {
//            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSlidingTabLayout.getLayoutParams();
//            lp.height += BaseSdkActivity.getStatusBarHeight();
//            mSlidingTabLayout.setPadding(0, BaseSdkActivity.getStatusBarHeight(), 0, 0);
//        }

        mPagerAdapter = new ChannelTabPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                View view = mViewPager.getChildAt(position);
                if (view instanceof LiveChannelView) {
                    EventBus.getDefault().removeStickyEvent(EventClass.SelectChannelEvent.class);
                    EventBus.getDefault().postSticky(new EventClass.SelectChannelEvent(((LiveChannelView) view).getChannelId()));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void loadData() {
        if (mChannelListPresenter == null) {
            mChannelListPresenter = new ChannelListPresenter(this, this);
            mChannelListPresenter.setFcId(0);
        }
        mChannelListPresenter.start();
    }

    @Override
    public void listUpdateView(List<? extends ChannelShow> models) {
        if (models != null) {
            mEmptyView.setVisibility(View.GONE);
            mPagerAdapter.setChannelList(models);
            mSlidingTabLayout.setViewPager(mViewPager);
            if (mIsFirstLoad) {
                if (!models.isEmpty()) {
                    long channelId = models.get(0).getChannelId();
                    EventBus.getDefault().postSticky(new EventClass.SelectChannelEvent(channelId));
                }
                mIsFirstLoad = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new EventClass.LiveListActivityLiveCycle(EventClass.LiveListActivityLiveCycle.Event.RESUME));
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().post(new EventClass.LiveListActivityLiveCycle(EventClass.LiveListActivityLiveCycle.Event.PAUSE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetUserAccountEvent event) {
        MyLog.w(TAG, "onEvent" + " SetUserAccountEvent=" + event);
        loadData();
    }

    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, ChannelListSdkActivity.class);
        activity.startActivity(intent);
//        activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }
}
