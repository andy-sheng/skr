package com.wali.live.watchsdk.personalcenter;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseRotateSdkActivity;
import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.utils.display.DisplayUtils;
import com.base.view.SlidingTabLayout;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.view.FansHomeView;
import com.wali.live.watchsdk.personalcenter.view.IViewProxy;
import com.wali.live.watchsdk.personalcenter.view.MyInfoBalanceView;
import com.wali.live.watchsdk.personalcenter.view.MyInfoChatThreadView;
import com.wali.live.watchsdk.personalcenter.view.MyInfoPrivilegeView;
import com.wali.live.watchsdk.personalcenter.view.MyInfoSummaryView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin
 *
 * @module 半屏个人主页
 */
public class MyInfoHalfFragment extends BaseFragment implements View.OnClickListener, FragmentDataListener {

    //粉丝团viewpager的page页面对应的position

    List<Pair<String, IViewProxy>> mTitleAndViewList = new ArrayList<>();

    private View mPlaceHolderView;
    private SlidingTabLayout mTabLayout;
    private PagerAdapter mTabPagerAdapter;
    private ViewPager mViewPager;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.my_info_half_layout, container, false);
    }

    @Override
    protected void bindView() {
        initData();
        initView();
        initPresenter();
    }

    private void initData() {
        mTitleAndViewList.add(new Pair<String, IViewProxy>("资料", new MyInfoSummaryView()));
        mTitleAndViewList.add(new Pair<String, IViewProxy>("消息", new MyInfoChatThreadView()));
        mTitleAndViewList.add(new Pair<String, IViewProxy>("钱包", new MyInfoBalanceView()));
        mTitleAndViewList.add(new Pair<String, IViewProxy>("特权", new MyInfoPrivilegeView()));
    }

    private void initView() {
        initTopContainer();
    }

    private void initPresenter() {
    }

    private void initTopContainer() {
        mPlaceHolderView = $(R.id.place_holder_view);
        $click(mPlaceHolderView, this);

        mTabLayout = $(R.id.my_info_tab);
        mTabLayout.setCustomTabView(R.layout.my_info_tab_view, R.id.tab_tv);
        mTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_14b9c7));
        mTabLayout.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER);
        mTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));
        mTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(6));
        mViewPager = $(R.id.my_info_pager);
        mTabPagerAdapter = new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup viewGroup, int position, Object arg2) {
                IViewProxy viewProxy = mTitleAndViewList.get(position).second;
                viewGroup.removeView(viewProxy.getRealView(getContext()));
            }

            @Override
            public Object instantiateItem(ViewGroup viewGroup, int position) {
                IViewProxy viewProxy = mTitleAndViewList.get(position).second;
                viewGroup.addView(viewProxy.getRealView(getContext()));
                return viewProxy.getRealView(getContext());
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitleAndViewList.get(position).first;
            }

            @Override
            public int getCount() {
                return mTitleAndViewList.size();
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

        mTabPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mTabPagerAdapter);
        mTabLayout.setViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }
        });
    }

    private void finish() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.place_holder_view) {
            finish();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public static void openFragment(BaseSdkActivity activity) {
        Bundle bundle = new Bundle();
        // 看设计需不需要横竖屏
        if(activity instanceof BaseRotateSdkActivity){

        }
        FragmentNaviUtils.openFragment(activity, MyInfoHalfFragment.class, bundle, R.id.main_act_container,
                true, R.anim.slide_bottom_in, R.anim.slide_bottom_out);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {

    }
}
