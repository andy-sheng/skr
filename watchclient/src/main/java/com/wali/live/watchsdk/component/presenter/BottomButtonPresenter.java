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
        registerAction(WatchComponentController.MSG_ON_ORIENTATION);
        registerAction(WatchComponentController.MSG_GIFT_MALL_SHOWED);
        registerAction(WatchComponentController.MSG_GIFT_MALL_HIDDEN);
    }

    @Override
    public void showInputView() {
        mComponentController.onEvent(WatchComponentController.MSG_CTRL_INPUT_VIEW,
                new Params().putItem(true));
    }

    @Override
    public void showGiftView() {
        mComponentController.onEvent(WatchComponentController.MSG_CTRL_GIFT_VIEW,
                new Params().putItem(true));
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
                case WatchComponentController.MSG_ON_ORIENTATION:
                    if (params != null) {
                        Boolean isLandscape = params.firstItem();
                        if (isLandscape != null) {
                            mView.onOrientation(isLandscape);
                            return true;
                        }
                    }
                    break;
                case WatchComponentController.MSG_GIFT_MALL_SHOWED:
                    mView.getRealView().setVisibility(View.GONE);
                    break;
                case WatchComponentController.MSG_GIFT_MALL_HIDDEN:
                    mView.getRealView().setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
