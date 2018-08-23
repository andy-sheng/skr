package com.wali.live.watchsdk.personalcenter;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.base.activity.BaseRotateSdkActivity;
import com.base.activity.BaseSdkActivity;
import com.base.activity.RxActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.view.LazyNewView;
import com.base.view.SlidingTabLayout;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.personalcenter.fragment.ChatThreadHalfFragment;
import com.wali.live.watchsdk.personalcenter.fragment.EditInfoFragment;
import com.wali.live.watchsdk.personalcenter.relation.FansListHalfFragment;
import com.wali.live.watchsdk.personalcenter.relation.FollowListHalfFragment;
import com.wali.live.watchsdk.personalcenter.view.MyInfoBalanceView;
import com.wali.live.watchsdk.personalcenter.view.MyInfoChatThreadView;
import com.wali.live.watchsdk.personalcenter.view.MyInfoSummaryView;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by chengsimin
 *
 * @module 半屏个人主页
 */
public class MyInfoHalfFragment extends BaseFragment implements View.OnClickListener, FragmentDataListener {

    //粉丝团viewpager的page页面对应的position

    List<Pair<String, LazyNewView>> mTitleAndViewList = new ArrayList<>();

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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initData();
        initView();
        initPresenter();
    }

    private void initData() {
        mTitleAndViewList.add(new Pair<String, LazyNewView>("资料", new LazyNewView() {
            @Override
            public View newView() {
                return new MyInfoSummaryView(getContext());
            }
        }));
        mTitleAndViewList.add(new Pair<String, LazyNewView>("消息", new LazyNewView() {
            @Override
            public View newView() {
                return new MyInfoChatThreadView(getContext());
            }

        }));
        mTitleAndViewList.add(new Pair<String, LazyNewView>("钱包", new LazyNewView() {
            @Override
            public View newView() {
                return new MyInfoBalanceView(getContext());
            }
        }));
//        mTitleAndViewList.add(new Pair<String, IViewProxy>("特权", new MyInfoPrivilegeView()));
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
        mTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_ff2966));
        mTabLayout.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));
        mTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(6));
        mViewPager = $(R.id.my_info_pager);
        mTabPagerAdapter = new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup viewGroup, int position, Object arg2) {
                LazyNewView viewProxy = mTitleAndViewList.get(position).second;
                viewGroup.removeView(viewProxy.getView());
            }

            @Override
            public Object instantiateItem(ViewGroup viewGroup, int position) {
                LazyNewView viewProxy = mTitleAndViewList.get(position).second;
                viewGroup.addView(viewProxy.getView());
                return viewProxy.getView();
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

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation animation;

        if(enter) {
            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_in);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mPlaceHolderView != null) {
                        mPlaceHolderView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        } else {
            if (mPlaceHolderView != null) {
                mPlaceHolderView.setVisibility(View.GONE);
            }
            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_out);
        }

        return animation;
    }



    public static void openFragment(BaseSdkActivity activity) {
        FragmentNaviUtils.openFragment(activity, MyInfoHalfFragment.class, null, R.id.main_act_container,
                true, R.anim.slide_bottom_in, R.anim.slide_bottom_out);
    }

    private void finish() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        FragmentNaviUtils.popFragment(getActivity());
        EventBus.getDefault().post(new ConversationLocalStore.NotifyUnreadCountChangeEvent(0));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.JumpHalfEditFragEvent event) {
        if (event == null) {
            return;
        }

        MyLog.d(TAG, "JumpHalfEditFragEvent");
        EditInfoFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_container);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.JumpHalfFansFragEvent event) {
        if (event == null) {
            return;
        }

        MyLog.d(TAG, "JumpHalfFansFragEvent");
        FansListHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_container);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.JumpHalfFollowsFragEvent event) {
        if (event == null) {
            return;
        }

        MyLog.d(TAG, "JumpHalfFollowsFragEvent");
        FollowListHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_container);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.JumpNoFocusChatThreadFragEvent event) {
        if (event == null) {
            return;
        }

        MyLog.d(TAG, "JumpHalfEditFragEvent");
        ChatThreadHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_container, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.PersonalInfoChangeEvent event) {
        if (event == null) {
            return;
        }

        MyLog.d(TAG, "PersonalInfoChangeEvent");
        if (mTitleAndViewList != null && !mTitleAndViewList.isEmpty()) {
            for (int i = 0; i < mTitleAndViewList.size(); i++) {
                LazyNewView viewProxy = mTitleAndViewList.get(i).second;
                if (viewProxy.getView() instanceof MyInfoSummaryView) {
                    ((MyInfoSummaryView) viewProxy.getView()).refreshUi();
                    break;
                }

            }
        }
    }


}
