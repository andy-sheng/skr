package com.wali.live.modulechannel.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.account.event.AccountEvent;
import com.common.core.commonview.EmptyView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.adapter.ChannelTabPagerAdapter;
import com.wali.live.modulechannel.event.ChannelEvent;
import com.wali.live.modulechannel.model.channellist.ChannelShowModel;
import com.wali.live.modulechannel.presenter.channellist.ChannelListPresenter;
import com.wali.live.modulechannel.presenter.channellist.IChannelListView;
import com.wali.live.modulechannel.view.LiveChannelView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by zhujianning on 18-10-17.
 */

@Route(path = "/channel/ChannelListSdkActivity")
public class ChannelListSdkActivity extends BaseActivity implements IChannelListView {

    private SlidingTabLayout mSlidingTabLayout;
    private NestViewPager mViewPager;
    private EmptyView mEmptyView;

    private ChannelTabPagerAdapter mPagerAdapter;
    private ChannelListPresenter mChannelListPresenter;
    private boolean mIsFirstLoad = true;

    private CommonTitleBar mTitleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_activity_channel_list);
        bindViews();
        loadData();
    }

    private void report() {
        //TOdo-暂时去了
//        try {
//            String key = String.format(StatisticsKey.KEY_SDK_TONGGLE_CHANNEL_LIST_TYPE, HostChannelManager.getInstance().getChannelId(), 0);
//            StatisticsAlmightyWorker.getsInstance().recordImmediatelyDefault(key, 1);
//        } catch (Exception e) {
//        }
    }

    private void bindViews() {
        mTitleBar = findViewById(R.id.title_bar);
//        mTitleBar.getCenterTextView().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        mSlidingTabLayout = findViewById(R.id.channel_tab);
        mViewPager = findViewById(R.id.view_pager);
        mEmptyView = findViewById(R.id.empty_view);

        mSlidingTabLayout.setCustomTabView(R.layout.channel_slide_tab_view, R.id.tab_tv);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_ff2966));
        mSlidingTabLayout.setIndicatorWidth(U.getDisplayUtils().dip2px(12));
        mSlidingTabLayout.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(10));
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
                ChannelShowModel channelShow = null;
                if (mPagerAdapter != null && (channelShow = mPagerAdapter.getChannelShowByPosition(position)) != null) {
                    EventBus.getDefault().removeStickyEvent(ChannelEvent.SelectChannelEvent.class);
                    EventBus.getDefault().postSticky(new ChannelEvent.SelectChannelEvent(channelShow.getChannelId()));
                        //Todo-暂时去了
//                    // 频道切换打点
//                    MilinkStatistics.getInstance().statisticChannelChange((int) channelShow.getChannelId());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
    }

    private void loadData() {
        if (mChannelListPresenter == null) {
            mChannelListPresenter = new ChannelListPresenter(this);
            mChannelListPresenter.setFcId(0);
        }
        mChannelListPresenter.start();

    }

    @Override
    public void listUpdateView(List<? extends ChannelShowModel> models) {
        if (models != null) {
            mEmptyView.setVisibility(View.GONE);
            mPagerAdapter.setChannelList(models);
            mSlidingTabLayout.setViewPager(mViewPager);
            if (mIsFirstLoad) {
                int defaultSelected = 0;
                for (int i = 0; i < models.size(); i++) {
                    ChannelShowModel show = models.get(i);
                    if ("推荐".equals(show.getChannelName())) {
                        defaultSelected = i;
                        break;
                    }
                }
                if (!models.isEmpty()) {
                    if (defaultSelected != 0) {
                        // 由于defaultSelected不为0时setCurrentItem会触发onPageSelected 之后post和打点的步骤不用重复
                        mViewPager.setCurrentItem(defaultSelected);
                    } else {
                        long channelId = models.get(defaultSelected).getChannelId();
                        EventBus.getDefault().postSticky(new ChannelEvent.SelectChannelEvent(channelId));

                        //Todo-暂时去了
//                        // 频道切换打点 首次展示频道
//                        MilinkStatistics.getInstance().statisticChannelChange((int) channelId);
                    }
                }
                mIsFirstLoad = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new ChannelEvent.LiveListActivityLiveCycle(ChannelEvent.LiveListActivityLiveCycle.Event.RESUME));
        //Todo-暂时去了
//        SelfUpdateManager.selfUpdateAsnc(new WeakReference(this));
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().post(new ChannelEvent.LiveListActivityLiveCycle(ChannelEvent.LiveListActivityLiveCycle.Event.PAUSE));
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (mViewPager != null) {
            for (int i = 0; i < mViewPager.getChildCount(); i++) {
                View view = mViewPager.getChildAt(i);
                if (view != null && view instanceof LiveChannelView) {
                    ((LiveChannelView) view).onDestroy();
                }
            }
        }
        if (mPagerAdapter != null) {
            mPagerAdapter.destroyMap();
        }
    }

    @Override
    public void finish() {
        MyLog.w(TAG, "finish");
        super.finish();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent event) {
        MyLog.w(TAG, "onEvent" + " SetUserAccountEvent=" + event);
        loadData();
    }
}
