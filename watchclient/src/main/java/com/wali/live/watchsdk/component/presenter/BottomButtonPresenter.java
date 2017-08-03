package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.view.View;

import com.base.log.MyLog;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.componentwrapper.BaseSdkController;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;

import org.greenrobot.eventbus.EventBus;

import static com.wali.live.componentwrapper.BaseSdkController.MSG_BOTTOM_POPUP_HIDDEN;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_BOTTOM_POPUP_SHOWED;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOE_GAME_ICON;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_GAME_DOWNLOAD;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_INPUT_VIEW;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_SHARE_PANEL;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends ComponentPresenter<WatchBottomButton.IView, BaseSdkController>
        implements WatchBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public BottomButtonPresenter(
            @NonNull BaseSdkController componentController,
            RoomBaseDataModel myRoomData) {
        super(componentController);
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_BOTTOM_POPUP_SHOWED);
        registerAction(MSG_BOTTOM_POPUP_HIDDEN);
        registerAction(MSG_SHOE_GAME_ICON);
        mMyRoomData = myRoomData;
    }

    @Override
    public void showInputView() {
        postEvent(MSG_SHOW_INPUT_VIEW);
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

    @Override
    public void rotateScreen() {
    }

    @Override
    public void showGameDownloadView() {
        postEvent(MSG_SHOW_GAME_DOWNLOAD);
    }

    @Override
    public void showShareView() {
        postEvent(MSG_SHOW_SHARE_PANEL);
    }

    @Override
    public void destroy() {
        super.destroy();
        mView.destroyView();
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            case MSG_BOTTOM_POPUP_SHOWED:
                mView.getRealView().setVisibility(View.GONE);
                return true;
            case MSG_BOTTOM_POPUP_HIDDEN:
                mView.getRealView().setVisibility(View.VISIBLE);
                return true;
            case MSG_SHOE_GAME_ICON:
                mView.showGameIcon((GameViewModel) params.getItem(0));
                return true;
            default:
                break;
        }
        return false;
    }
}
