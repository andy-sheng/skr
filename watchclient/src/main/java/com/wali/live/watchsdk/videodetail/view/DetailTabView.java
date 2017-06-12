package com.wali.live.watchsdk.videodetail.view;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.base.utils.display.DisplayUtils;
import com.base.view.SlidingTabLayout;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.watchsdk.videodetail.presenter.DetailReplayPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.adapter.CommonTabPagerAdapter;
import com.wali.live.watchsdk.videodetail.presenter.DetailCommentPresenter;

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
    }

    public DetailTabView(
            View rootView,
            @NonNull ComponentPresenter.IComponentController componentController,
            @NonNull RoomBaseDataModel roomData) {
        mRootView = rootView;
        mAppBarLayout = $(R.id.app_bar_layout);
        mSlidingTabLayout = $(R.id.detail_tab);
        mViewPager = $(R.id.detail_pager);

        mMessageAdapter = new CommonTabPagerAdapter();
        {
            DetailCommentView view = new DetailCommentView(mRootView.getContext());
            DetailCommentPresenter presenter = new DetailCommentPresenter(componentController, roomData);
            presenter.setComponentView(view.getViewProxy());
            view.setPresenter(presenter);
            mMessageAdapter.addView(String.format(mRootView.getResources().getString(
                    R.string.feeds_detail_label_comment), "0"), view);
        }
        {
            DetailReplayView view = new DetailReplayView(mRootView.getContext());
            DetailReplayPresenter presenter = new DetailReplayPresenter(componentController, roomData);
            presenter.setComponentView(view.getViewProxy());
            view.setMyRoomData(roomData);
            view.setPresenter(presenter);
            mMessageAdapter.addView(String.format(mRootView.getResources().getString(
                    R.string.feeds_detail_label_replay), "0"), view);
        }
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
                return null;
            }

            @Override
            public void updateCommentTotalCnt(int cnt) {
                mMessageAdapter.updatePageTitle(0, String.format(mRootView.getResources()
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
    }

    public interface IView extends IViewProxy {
        /**
         * 更新评论总数
         */
        void updateCommentTotalCnt(int cnt);

        /**
         * 更新评论总数
         *
         * @param cnt
         */
        void updateReplayTotalCnt(int cnt);

        /**
         * 收起信息区
         */
        void onFoldInfoArea();
    }
}
