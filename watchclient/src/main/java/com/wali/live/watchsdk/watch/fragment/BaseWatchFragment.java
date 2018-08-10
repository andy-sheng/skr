package com.wali.live.watchsdk.watch.fragment;

import android.support.annotation.CallSuper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.base.fragment.BaseEventBusFragment;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.gift.view.GiftRoomEffectView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivityInterface;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by zhujianning on 18-8-7.
 */

public class BaseWatchFragment extends BaseEventBusFragment {

    protected GiftRoomEffectView mGiftRoomEffectView;
    protected GiftContinueViewGroup mGiftContinueViewGroup;
    protected GiftAnimationView mGiftAnimationView;

    protected WatchComponentController mController;

    @Override
    public boolean needEventBus() {
        return false;
    }

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @CallSuper
    @Override
    protected void bindView() {
        setWatchComponentController(getWatchSdkInterface().getController());
        getWatchSdkInterface().getGiftMallPresenter().setViewStub((ViewStub) mRootView.findViewById(R.id.gift_mall_view_viewstub));

        initGiftContinueViewGroup();
        initGiftRoomEffectView();
        initGiftAnimationView();
    }

    @Override
    protected boolean needForceActivityOrientation() {
        return false;
    }

    protected void initGiftRoomEffectView() {
        mGiftRoomEffectView = (GiftRoomEffectView) mRootView.findViewById(R.id.gift_room_effect_view);
        if (mGiftRoomEffectView != null) {
            mGiftRoomEffectView.onActivityCreate();
        }
    }

    protected void initGiftAnimationView() {
        mGiftAnimationView = (GiftAnimationView) mRootView.findViewById(R.id.gift_animation_player_view);
        if (mGiftAnimationView != null) {
            mGiftAnimationView.onActivityCreate();
        }
    }

    protected void initGiftContinueViewGroup() {
        mGiftContinueViewGroup = (GiftContinueViewGroup) mRootView.findViewById(R.id.gift_continue_vg);
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.onActivityCreate();
        }
    }


    private void setWatchComponentController(WatchComponentController controller) {
        this.mController = controller;
    }

    public void pageUpEvent() {

    }

    public void PageDownEvent() {

    }

    public void playerReadyEvent() {

    }

    public void switchRoom() {
        //重置对应的view

        if (mGiftAnimationView != null) {
            mGiftAnimationView.reset();
        }

        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.reset();
        }

        if (mGiftRoomEffectView != null) {
            mGiftRoomEffectView.reset();
        }
    }

    protected void initPresenter() {
    }

    public void orientLandscape() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_LANDSCAPE);
        }

        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(true);
        }
    }

    public void orientPortrait() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_PORTRAIT);
        }

        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGiftRoomEffectView != null) {
            mGiftRoomEffectView.onActivityDestroy();
        }

        if (mGiftAnimationView != null) {
            mGiftAnimationView.onActivityDestroy();
        }

        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.onActivityDestroy();
        }
    }

    protected WatchSdkActivityInterface getWatchSdkInterface() {
        return (WatchSdkActivityInterface) getActivity();
    }
}
