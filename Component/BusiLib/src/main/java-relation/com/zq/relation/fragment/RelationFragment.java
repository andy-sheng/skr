package com.zq.relation.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.zq.relation.view.RelationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * 关系列表
 */
public class RelationFragment extends BaseFragment {

    public static final int FROM_FRIENDS = 0;
    public static final int FROM_FANS = 1;
    public static final int FROM_FOLLOW = 2;

    public static final String FROM_PAGE_KEY = "from_page_key";

    CommonTitleBar mTitlebar;
    LinearLayout mContainer;
    SlidingTabLayout mRelationTab;
    View mSplitLine;
    NestViewPager mRelationVp;

    PagerAdapter mTabPagerAdapter;

    List<String> mTabTitleList = new ArrayList<>();
    HashMap<String, RelationView> mTitleAndViewMap = new HashMap<>();


    @Override
    public int initView() {
        return R.layout.relation_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mContainer = (LinearLayout) mRootView.findViewById(R.id.container);
        mRelationTab = (SlidingTabLayout) mRootView.findViewById(R.id.relation_tab);
        mSplitLine = (View) mRootView.findViewById(R.id.split_line);
        mRelationVp = (NestViewPager) mRootView.findViewById(R.id.relation_vp);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                        U.getFragmentUtils().popFragment(RelationFragment.this);
                    }
                });

        mTabTitleList.add("好友");
        mTabTitleList.add("粉丝");
        mTabTitleList.add("关注");
        mTitleAndViewMap.put("好友", new RelationView(getContext(), UserInfoManager.RELATION_FRIENDS));
        mTitleAndViewMap.put("粉丝", new RelationView(getContext(), UserInfoManager.RELATION_FANS));
        mTitleAndViewMap.put("关注", new RelationView(getContext(), UserInfoManager.RELATION_FOLLOW));

        mRelationTab.setCustomTabView(R.layout.relation_tab_view, R.id.tab_tv);
        mRelationTab.setSelectedIndicatorColors(Color.parseColor("#FE8400"));
        mRelationTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE);
        mRelationTab.setIndicatorWidth(U.getDisplayUtils().dip2px(27));
        mRelationTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(5));
        mRelationTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(4));
        mRelationTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(2));


        mTabPagerAdapter = new PagerAdapter() {

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                MyLog.d(TAG, "destroyItem" + " container=" + container + " position=" + position + " object=" + object);
                container.removeView((View) object);
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                MyLog.d(TAG, "instantiateItem" + " container=" + container + " position=" + position);
                View view = mTitleAndViewMap.get(mTabTitleList.get(position));
                if (container.indexOfChild(view) == -1) {
                    container.addView(view);
                }
                return view;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return mTabTitleList.get(position);
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }

            @Override
            public int getCount() {
                return mTabTitleList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == (object);
            }
        };

        mRelationVp.setAdapter(mTabPagerAdapter);
        mRelationTab.setViewPager(mRelationVp);
        mTabPagerAdapter.notifyDataSetChanged();

        Bundle bundle = getArguments();
        if (bundle != null) {
            int from = bundle.getInt(FROM_PAGE_KEY);
            mRelationVp.setCurrentItem(from);
        }

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mTitleAndViewMap != null) {
            for (RelationView view : mTitleAndViewMap.values()) {
                view.destroy();
            }
        }

        U.getSoundUtils().release(TAG);
    }
}
