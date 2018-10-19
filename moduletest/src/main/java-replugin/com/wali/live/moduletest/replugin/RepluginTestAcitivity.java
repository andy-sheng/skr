package com.wali.live.moduletest.replugin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.SlidingTabLayout;
import com.wali.live.moduletest.R;

import java.util.ArrayList;
import java.util.List;

@Route(path = "/test/RepluginTestAcitivity")
public class RepluginTestAcitivity extends BaseActivity {
    CommonTitleBar mTitlebar;
    LinearLayout mContainer;
    SlidingTabLayout mTitleTab;
    View mSplitLine;
    ViewPager mOpPager;
    PagerAdapter mPagerAdapter;

    List<H> mDataList = new ArrayList<>();

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.test_plugin_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) this.findViewById(R.id.titlebar);
        mContainer = (LinearLayout) this.findViewById(R.id.container);
        mTitleTab = (SlidingTabLayout) this.findViewById(R.id.title_tab);

        mTitleTab.setCustomTabView(R.layout.test_droidplugin_tab_view, R.id.tab_tv);
        mTitleTab.setSelectedIndicatorColors(getResources().getColor(R.color.black));
        mTitleTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mTitleTab.setIndicatorWidth(U.getDisplayUtils().dip2px(12));
        mTitleTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(6));


        mSplitLine = (View) this.findViewById(R.id.split_line);
        mOpPager = (ViewPager) this.findViewById(R.id.op_pager);

        mDataList.add(new H("已安装",new com.wali.live.moduletest.replugin.InstallView(this)));
        mDataList.add(new H("SdCard",new com.wali.live.moduletest.replugin.UninstallView(this)));
        mPagerAdapter = new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup viewGroup, int position, Object arg2) {
                viewGroup.removeView(mDataList.get(position).view);
            }

            @Override
            public Object instantiateItem(ViewGroup viewGroup, int position) {
                View view = mDataList.get(position).view;
                viewGroup.addView(view);
                return view;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mDataList.get(position).title;
            }

            @Override
            public int getCount() {
                return mDataList.size();
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == (object);
            }
        };

        mOpPager.setAdapter(mPagerAdapter);
        mTitleTab.setViewPager(mOpPager);

    }

    @Override
    public boolean useEventBus() {
        return false;
    }


    public boolean canSlide() {
        return false;
    }

    static class H {
        String title;
        View view;

        public H(String title, View view) {
            this.title = title;
            this.view = view;
        }
    }
}
