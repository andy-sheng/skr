package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_BARRAGE_ADMIN;
import static com.wali.live.component.BaseSdkController.MSG_BARRAGE_FANS;
import static com.wali.live.component.BaseSdkController.MSG_BARRAGE_VIP;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_INPUT_VIEW;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框表现
 */
public class InputAreaPresenter extends InputPresenter<InputAreaView.IView>
        implements InputAreaView.IPresenter {
    private static final String TAG = "InputAreaPresenter";

    private int mMinHeightLand;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public InputAreaPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager liveRoomChatMsgManager,
            boolean isWatchState) {
        super(controller, myRoomData, liveRoomChatMsgManager);
        setMinHeightLand(isWatchState);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_BACK_PRESSED);
        registerAction(MSG_SHOW_INPUT_VIEW);
        registerAction(MSG_HIDE_INPUT_VIEW);
        registerAction(MSG_BARRAGE_FANS);
        registerAction(MSG_BARRAGE_ADMIN);
        registerAction(MSG_BARRAGE_VIP);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    private void updateManagerView() {
        if (WatchRoomCharactorManager.getInstance().isManager()
                || mMyRoomData.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
            mView.enableBarrageSelectView(true);
        } else {
            mView.enableBarrageSelectView(false);
        }
    }

    private void setMinHeightLand(boolean isWatchState) {
        if (isWatchState) {
            mMinHeightLand = DisplayUtils.dip2px(38f + 6.67f);
        } else {
            mMinHeightLand = DisplayUtils.dip2px(6.67f);
        }
    }

    @Override
    public void notifyInputViewShowed() {
        postEvent(MSG_INPUT_VIEW_SHOWED);
        updateManagerView();
    }

    @Override
    public void notifyInputViewHidden() {
        postEvent(MSG_INPUT_VIEW_HIDDEN);
    }

    @Override
    public int getMinHeightLand() {
        return mMinHeightLand;
    }

    @Override
    public void updateInputHint(int barrageState) {
        super.updateInputHint(barrageState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.AdminChangeEvent event) {
        MyLog.w(TAG, "EventClass.AdminChangeEvent " + event.isAdmin());
        updateManagerView();
        mView.hideInputView();
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
            case MSG_ON_BACK_PRESSED:
                return mView.processBackPress();
            case MSG_SHOW_INPUT_VIEW:
                mViewIsShow = true;
                checkShowCountdownTimer();
                return mView.showInputView();
            case MSG_HIDE_INPUT_VIEW:
                mViewIsShow = false;
                return mView.hideInputView();
            case MSG_BARRAGE_FANS:
                LiveProto.LimitedInfo fansInfo = params.getItem(0);
                mFansPrivilegeModel.setHasSendFlyBarrageTimes(fansInfo.getCounter());
                mFansPrivilegeModel.setMaxCanSendFlyBarrageTimes(fansInfo.getMax());
                return true;
            case MSG_BARRAGE_ADMIN:
                //TODO zyh 管理員漂屏消息做多1000上限，这里的数据又不需要文案显示，感觉可以拿掉。
                LiveProto.LimitedInfo adminInfo = params.getItem(0);
                mManagerMaxCnt = adminInfo.getMax();
                mManagerCurCnt = adminInfo.getCounter();
                break;
            case MSG_BARRAGE_VIP:
                LiveProto.LimitedInfo vipInfo = params.getItem(0);
                mVipCurCnt = vipInfo.getCounter();
                mVipMaxCnt = vipInfo.getMax();
                break;
            default:
                break;
        }
        return false;
    }
}