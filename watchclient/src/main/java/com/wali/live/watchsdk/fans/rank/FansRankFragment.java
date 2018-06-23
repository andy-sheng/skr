package com.wali.live.watchsdk.fans.rank;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.base.view.SlidingTabLayout;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.common.photopicker.adapter.CommonTabPagerAdapter;
import com.wali.live.watchsdk.fans.rank.view.FansRankView;

public class FansRankFragment extends RxFragment implements View.OnClickListener {
    public static final String EXTRA_ZUID = "mZuid";
    public static final String EXTRA_IS_GROUP_TYPE = "is_group";

    private BackTitleBar mTitleBar;

    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout;
    private CommonTabPagerAdapter mPagerAdapter;

    private FansRankView mDayRankView;
    private FansRankView mWeekRankView;
    private FansRankView mTotalRankView;

    private long mZuid;
    private boolean mIsGroup; // 团排行榜

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        initData(args);
    }

    private void initData(Bundle bundle) {
        if (bundle == null) {
            finish();
            return;
        }
        mZuid = bundle.getLong(EXTRA_ZUID);
        mIsGroup = bundle.getBoolean(EXTRA_IS_GROUP_TYPE, false);

    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_fans_rank, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.back_title_bar);
        mTitleBar.getTitleTv().setText(mIsGroup ? R.string.group_ranking : R.string.vfans_rank_title);
        mTitleBar.getTitleTv().setOnClickListener(this);

        mTabLayout = $(R.id.sliding_tab);
        mViewPager = $(R.id.view_pager);

        mTabLayout.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER);
        mTabLayout.setCustomTabView(R.layout.fans_rank_tab_view, R.id.tab_tv);
        mTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_ff2966));
        mTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));
        mTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(6));

        mDayRankView = new FansRankView(getContext(), VFansCommonProto.RankDateType.YESTERDAY_TYPE_VALUE, mZuid, mIsGroup);
        mWeekRankView = new FansRankView(getContext(), VFansCommonProto.RankDateType.WEEK_TYPE_VALUE, mZuid, mIsGroup);
        mTotalRankView = new FansRankView(getContext(), VFansCommonProto.RankDateType.TOTAL_TYPE_VALUE, mZuid, mIsGroup);

        mPagerAdapter = new CommonTabPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);

        mPagerAdapter.addView(getString(R.string.rank_day_list), mDayRankView);
        mPagerAdapter.addView(getString(R.string.rank_week_list), mWeekRankView);
        mPagerAdapter.addView(getString(R.string.rank_total_list), mTotalRankView);
        mPagerAdapter.notifyDataSetChanged();
        mTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_iv) {
            finish();
        }
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void open(BaseActivity activity, long zuid, boolean isGroup) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ZUID, zuid);
        bundle.putBoolean(EXTRA_IS_GROUP_TYPE, isGroup);
        FragmentNaviUtils.addFragment(activity, FansRankFragment.class, bundle, R.id.main_act_container);
    }
}