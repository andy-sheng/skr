package com.wali.live.watchsdk.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.activity.BaseSdkActivity;
import com.base.utils.display.DisplayUtils;
import com.base.view.NestViewPager;
import com.base.view.SlidingTabLayout;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.adapter.ChannelTabPagerAdapter;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.list.presenter.ChannelListPresenter;
import com.wali.live.watchsdk.channel.list.presenter.IChannelListView;
import com.wali.live.watchsdk.view.EmptyView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);
        bindViews();
        loadData();
    }

    private void bindViews() {
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.channel_tab);
        mViewPager = (NestViewPager) findViewById(R.id.view_pager);
        mEmptyView = (EmptyView) findViewById(R.id.empty_view);

        mSlidingTabLayout.setCustomTabView(R.layout.channel_slide_tab_view, R.id.tab_tv);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_ff2966));
        mSlidingTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));
        mSlidingTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(10));
        mSlidingTabLayout.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_TAIL);
        if (BaseSdkActivity.isProfileMode()) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSlidingTabLayout.getLayoutParams();
            lp.height += BaseSdkActivity.getStatusBarHeight();
            mSlidingTabLayout.setPadding(0, BaseSdkActivity.getStatusBarHeight(), 0, 0);
        }

        mPagerAdapter = new ChannelTabPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
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
        }
    }

    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, ChannelListSdkActivity.class);
        activity.startActivity(intent);
    }
}
