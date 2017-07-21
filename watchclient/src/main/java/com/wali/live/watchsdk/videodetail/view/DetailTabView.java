package com.wali.live.watchsdk.videodetail.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.View;

import com.base.utils.display.DisplayUtils;
import com.base.view.SlidingTabLayout;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.adapter.CommonTabPagerAdapter;

import java.util.List;

/**
 * Created by yangli on 2017/06/02.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 详情TAB视图
 */
public class DetailTabView implements IComponentView<DetailTabView.IPresenter, DetailTabView.IView> {
    private static final String TAG = "DetailTabView";

    private View mRootView;

    @Nullable
    protected IPresenter mPresenter;

    private AppBarLayout mAppBarLayout;
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private CommonTabPagerAdapter mMessageAdapter;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) mRootView.findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
        if (mPresenter != null) {
            mPresenter.syncTabPageList(mRootView.getContext());
        }
    }

    public DetailTabView(@NonNull View rootView) {
        mRootView = rootView;
        mAppBarLayout = $(R.id.app_bar_layout);
        mSlidingTabLayout = $(R.id.detail_tab);
        mViewPager = $(R.id.detail_pager);

        mSlidingTabLayout.setCustomTabView(R.layout.feeds_detail_slide_tab_view, R.id.tab_tv);
        mSlidingTabLayout.setCustomTabColorizer(
                new SlidingTabLayout.TabColorizer() {
                    @Override
                    public int getIndicatorColor(int position) {
                        return mRootView.getResources().getColor(R.color.white);
                    }
                });
        mSlidingTabLayout.setSelectedIndicatorColors(
                mRootView.getResources().getColor(R.color.color_red_ff2966));
        mSlidingTabLayout.setSelectedTitleColor(
                mRootView.getResources().getColorStateList(R.color.color_feeds_detail_tab));
        mSlidingTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));
        mSlidingTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(4));

        mMessageAdapter = new CommonTabPagerAdapter();
        mViewPager.setAdapter(mMessageAdapter);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mRootView;
            }

            @Override
            public void onTabPageList(List<Pair<String, ? extends View>> tabPageList) {
                mMessageAdapter.removeAll();
                for (Pair<String, ? extends View> elem : tabPageList) {
                    mMessageAdapter.addView(elem.first, elem.second);
                }
                mMessageAdapter.notifyDataSetChanged();
                mSlidingTabLayout.notifyDataChange();
            }

            @Override
            public void updateCommentTotalCnt(int cnt, boolean isReplay) {
                mMessageAdapter.updatePageTitle(isReplay ? 0 : 1, String.format(mRootView.getResources()
                        .getString(R.string.feeds_detail_label_comment), "" + cnt));
                mSlidingTabLayout.notifyDataChange();
            }

            @Override
            public void updateReplayTotalCnt(int cnt) {
                mMessageAdapter.updatePageTitle(1, String.format(mRootView.getResources()
                        .getString(R.string.feeds_detail_label_replay), "" + cnt));
                mSlidingTabLayout.notifyDataChange();
            }

            @Override
            public void onFoldInfoArea() {
                mAppBarLayout.setExpanded(false);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 同步Tab的展示列表
         */
        void syncTabPageList(Context context);
    }

    public interface IView extends IViewProxy {
        /**
         * 更新TAB页面列表
         *
         * @param tabPageList <标题，页面>对列表
         */
        void onTabPageList(List<Pair<String, ? extends View>> tabPageList);

        /**
         * 更新评论总数
         *
         * @param cnt 评论数目
         */
        void updateCommentTotalCnt(int cnt, boolean isReplay);

        /**
         * 更新回放总数
         *
         * @param cnt 回放数目
         */
        void updateReplayTotalCnt(int cnt);

        /**
         * 收起信息区
         */
        void onFoldInfoArea();
    }
}
