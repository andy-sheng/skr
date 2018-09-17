package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.view.NestViewPager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.channel.adapter.RecChannelPagerAdapter;
import com.wali.live.watchsdk.channel.data.ChannelDataStore;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.presenter.WatchGameMoreTabPresenter;


/**
 * Created by liuting on 18-9-13.
 */

public abstract class WatchGameMoreLiveView extends RelativeLayout
        implements TwoSelectTabLayout.OnTabSelectListener, IComponentView<WatchGameMoreLiveView.IPresenter, WatchGameMoreLiveView.IView> {
    protected TwoSelectTabLayout mTwoSelsetTab;
    protected NestViewPager mViewPager;

    protected RecChannelPagerAdapter mPagerAdapter;

    protected RoomBaseDataModel mMyRoomData;
    protected WatchGameMoreTabPresenter mMoreTabPresenter;

    public WatchGameMoreLiveView(Context context, WatchComponentController componentController) {
        super(context);
        mMyRoomData = componentController.getRoomBaseDataModel();
        init(context, componentController);
    }

    protected abstract void inflateLayout(Context context);

    protected abstract @ChannelDataStore.ReqFrom int getLiveReqFrom();


    protected void init(Context context, WatchComponentController componentController) {
        inflateLayout(context);

        mTwoSelsetTab = (TwoSelectTabLayout) findViewById(R.id.two_select_tab);
        mViewPager = (NestViewPager) findViewById(R.id.view_pager);

        mTwoSelsetTab.setOnTabSelectListener(this);

        mPagerAdapter = new RecChannelPagerAdapter();
        setRequestParam();
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setViewPagerCanScroll(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTwoSelsetTab.setTabSelectedWithoutCallBack(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mMoreTabPresenter = new WatchGameMoreTabPresenter(componentController);
        mMoreTabPresenter.setView(getViewProxy());
        setPresenter(mMoreTabPresenter);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMoreTabPresenter.startPresenter();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMoreTabPresenter.stopPresenter();
    }

    private void setRequestParam() {
        if (mPagerAdapter == null || mMyRoomData == null) {
            return;
        }
        GameInfoModel gameInfoModel = mMyRoomData.getGameInfoModel();
        String gamePackage = "";
        long gameId = 0;
        if (gameInfoModel != null) {
            if (!TextUtils.isEmpty(gameInfoModel.getPackageName())) {
                gamePackage = gameInfoModel.getPackageName();
            }
            gameId = gameInfoModel.getGameId();
        }
        mPagerAdapter.setRequestParam(mMyRoomData.getUid(), gamePackage, gameId, getLiveReqFrom());
    }

    @Override
    public boolean onLeftTabSelect() {
        if (mViewPager.getChildCount() >= 1) {
            mViewPager.setCurrentItem(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onRightTabSelect() {
        if (mViewPager.getChildCount() >= 2) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mViewPager.setCurrentItem(1);
                return true;
            }
        }
        return false;
    }

    protected void destroyView() {
        if (mPagerAdapter != null) {
            mPagerAdapter.destroy();
        }
    }

    protected void onViewOrientChange(boolean isLandscape) {

    }

    protected void onViewFullScreenMoreLiveClick() {

    }

    protected void onViewVideoTouchViewClick() {

    }

    protected void onRecLiveChannelShowTabBarEvent(boolean show, long channelId) {

    }



    @Override
    public WatchGameMoreTabView.IView getViewProxy() {
        class ComponentView implements WatchGameMoreTabView.IView {
            @Override
            public void updateGameInfo(RoomBaseDataModel roomBaseDataModel) {
                setRequestParam();
                mPagerAdapter.reloadData();
            }

            @Override
            public void onOrientChange(boolean isLandscape) {
                onViewOrientChange(isLandscape);
            }

            @Override
            public void onFullScreenMoreLiveClick() {
                onViewFullScreenMoreLiveClick();
            }

            @Override
            public void onVideoTouchViewClick() {
                onViewVideoTouchViewClick();
            }

            @Override
            public void onTabBarEvent(boolean show, long channelId) {
                onRecLiveChannelShowTabBarEvent(show, channelId);
            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameMoreLiveView.this;
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(WatchGameMoreTabView.IPresenter iPresenter) {

    }

    public interface IPresenter {

    }
    public interface IView extends IViewProxy {
        void updateGameInfo(RoomBaseDataModel roomBaseDataModel);
        void onOrientChange(boolean isLandscape);
        void onFullScreenMoreLiveClick();
        void onVideoTouchViewClick();
        void onTabBarEvent(boolean show, long channelId);
    }
}
