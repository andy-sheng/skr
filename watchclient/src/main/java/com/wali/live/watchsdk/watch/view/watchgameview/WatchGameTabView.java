package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.view.LazyNewView;
import com.base.view.SlidingTabLayout;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vera on 2018/8/7.
 * 游戏直播下面的几个tab
 */

public class WatchGameTabView extends RelativeLayout implements
        IComponentView<WatchGameTabView.IPresenter, WatchGameTabView.IView> {

    public final static String TAG = "WatchGameTabView";

    WatchComponentController mWatchComponentController;

    SlidingTabLayout mWatchGameTab;
    View mSplitLine;
    ViewPager mWatchGameTabPager;
    PagerAdapter mTabPagerAdapter;

    List<String> mTabTitleList = new ArrayList<>();
    HashMap<String, LazyNewView> mTitleAndViewMap = new HashMap<>();


    public WatchGameTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setComponentControler(WatchComponentController controller) {
        mWatchComponentController = controller;
    }

    public void init(Context context) {

        inflate(context, R.layout.watch_game_tab_layout, this);


        mTitleAndViewMap.put("聊天", new LazyNewView() {
            @Override
            public View newView() {
                return new WatchGameChatTabView(getContext(), mWatchComponentController);
            }
        });

        mTitleAndViewMap.put("游戏主页", new LazyNewView() {
            @Override
            public View newView() {
                return new WatchGameHomeTabView(getContext(), mWatchComponentController);
            }
        });

        mTitleAndViewMap.put("观众", new LazyNewView() {
            @Override
            public View newView() {
                return new WatchGameViewerTabView(getContext(), mWatchComponentController);
            }
        });
        mTabTitleList.add("聊天");
        mTabTitleList.add("观众");


        mWatchGameTab = (SlidingTabLayout) this.findViewById(R.id.watch_game_tab);

        mWatchGameTab.setCustomTabView(R.layout.watch_game_tab_button_view, R.id.tab_tv);
        mWatchGameTab.setSelectedIndicatorColors(getResources().getColor(R.color.color_14b9c7));
        mWatchGameTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE);
        mWatchGameTab.setIndicatorWidth(DisplayUtils.dip2px(25));
        mWatchGameTab.setIndicatorBottomMargin(DisplayUtils.dip2px(6));


        mSplitLine = (View) this.findViewById(R.id.split_line);
        mWatchGameTabPager = (ViewPager) this.findViewById(R.id.watch_game_tab_pager);


        mTabPagerAdapter = new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup viewGroup, int position, Object arg2) {
                MyLog.d(TAG, "destroyItem" + " viewGroup=" + viewGroup + " position=" + position + " arg2=" + arg2);
//                LazyNewView viewProxy = mTitleAndViewMap.get(mTabTitleList.get(position));
                viewGroup.removeView((View) arg2);
            }

            @Override
            public Object instantiateItem(ViewGroup viewGroup, int position) {
                MyLog.d(TAG, "instantiateItem" + " viewGroup=" + viewGroup + " position=" + position);
                LazyNewView viewProxy = mTitleAndViewMap.get(mTabTitleList.get(position));
                if (viewGroup.indexOfChild(viewProxy.getView()) == -1) {
                    viewGroup.addView(viewProxy.getView());
                }
                return viewProxy.getView();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabTitleList.get(position);
            }

            @Override
            public int getCount() {
                int size = mTabTitleList.size();
//                MyLog.d(TAG, "getCount size=" + size);
                return size;
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

        mWatchGameTabPager.setAdapter(mTabPagerAdapter);

        mWatchGameTab.setViewPager(mWatchGameTabPager);

        mTabPagerAdapter.notifyDataSetChanged();

        mWatchGameTabPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            GameTabChildView mPreSelectView;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                LazyNewView viewProxy = mTitleAndViewMap.get(mTabTitleList.get(position));
                View view = viewProxy.getView();
                if (mPreSelectView != null && view != mPreSelectView) {
                    mPreSelectView.unselect();
                }
                if (view != null) {
                    mPreSelectView = (GameTabChildView) view;
                    mPreSelectView.select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public WatchGameTabView.IView getViewProxy() {
        return new IView() {
            @Override
            public void updateGameHomePage(RoomBaseDataModel source) {
                boolean hasGameHomePage = true;
                if (source.getGameInfoModel() == null) {
                    hasGameHomePage = false;
                }
                MyLog.d(TAG, "hasGameHomePage=" + hasGameHomePage);
                if (hasGameHomePage) {
                    if (mTabTitleList.size() > 1 && mTabTitleList.get(1).equals("游戏主页")) {
                        // 游戏主页了
                    } else {
                        MyLog.d(TAG, "add 游戏主页");
                        mTabTitleList.add(1, "游戏主页");

                        mTabPagerAdapter.notifyDataSetChanged();

                        mWatchGameTab.notifyDataChange();
                    }
                } else {
                    if (mTabTitleList.size() > 1 && mTabTitleList.get(1).equals("游戏主页")) {
                        // 不应该有游戏主页
                        mTabTitleList.remove(1);
                        mTabPagerAdapter.notifyDataSetChanged();

                        mWatchGameTab.notifyDataChange();
                    } else {
                    }
                }
            }

            @Override
            public void switchMsgTab() {
                MyLog.d(TAG, "switchMsgTab");
                int currentItem = mWatchGameTabPager.getCurrentItem();
                if(currentItem != 0) {
                    mWatchGameTabPager.setCurrentItem(0);
                }
            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameTabView.this;
            }
        };
    }

    @Override
    public void setPresenter(WatchGameTabView.IPresenter iPresenter) {

    }


    public interface IPresenter {

    }

    public interface IView extends IViewProxy {

        void updateGameHomePage(RoomBaseDataModel source);

        void switchMsgTab();
    }

    public interface GameTabChildView {
        void select();

        void unselect();
    }
}
