package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.view.View;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.BarrageBtnView;

import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_INPUT_VIEW;

/**
 * Created by wangmengjie on 17-7-24.
 *
 * @module 底部输入框表现
 */
public class BarrageBtnPresenter extends ComponentPresenter<BarrageBtnView.IView>
        implements BarrageBtnView.IPresenter {
    private static final String TAG = "BarrageBtnPresenter";

    private boolean mIsGameMode = false;
    private boolean mIsLandscape = false;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public BarrageBtnPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    public void setGameMode(boolean isGameMode){
        this.mIsGameMode = isGameMode;
    }
    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_BOTTOM_POPUP_SHOWED);
        registerAction(MSG_BOTTOM_POPUP_HIDDEN);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_ORIENT_PORTRAIT);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public void showInputView() {
        MyLog.w(TAG, "showInputView()");
        postEvent(MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                break;
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                break;
            case MSG_BOTTOM_POPUP_SHOWED:
                mView.getRealView().setVisibility(View.GONE);
                return true;
            case MSG_BOTTOM_POPUP_HIDDEN:
                if(mIsGameMode && mIsLandscape){
                    mView.getRealView().setVisibility(View.GONE);
                }else{
                    mView.getRealView().setVisibility(View.VISIBLE);
                }
                return true;
            default:
                break;
        }
        return false;
    }
}
