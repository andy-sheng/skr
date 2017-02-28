package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.mi.live.data.event.GiftEventClass;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.WatchBottomButton;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends
        ComponentPresenter<WatchBottomButton.IView> implements WatchBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    public BottomButtonPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
        registerAction(WatchComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(WatchComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(WatchComponentController.MSG_BOTTOM_POPUP_SHOWED);
        registerAction(WatchComponentController.MSG_BOTTOM_POPUP_HIDDEN);
    }

    @Override
    public void showInputView() {
        mComponentController.onEvent(WatchComponentController.MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public void showGiftView() {
        if (AccountAuthManager.triggerActionNeedAccount(mView.getRealView().getContext())) {
            // 飘屏测试
//            FlyBarrageManager.testFlyBarrage(mMyRoomData.getRoomId(),String.valueOf(mMyRoomData.getUid()));
            EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(
                    GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST));
        }
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                return false;
            }
            switch (source) {
                case WatchComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mView.onOrientation(false);
                    return true;
                case WatchComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mView.onOrientation(true);
                    return true;
                case WatchComponentController.MSG_BOTTOM_POPUP_SHOWED:
                    mView.getRealView().setVisibility(View.GONE);
                    return true;
                case WatchComponentController.MSG_BOTTOM_POPUP_HIDDEN:
                    mView.getRealView().setVisibility(View.VISIBLE);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
