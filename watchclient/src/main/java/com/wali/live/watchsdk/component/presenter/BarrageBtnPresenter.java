package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.BarrageBtnView;

/**
 * Created by wangmengjie on 17-7-24.
 *
 * @module 底部输入框表现
 */
public class BarrageBtnPresenter extends ComponentPresenter<BarrageBtnView.IView>
        implements BarrageBtnView.IPresenter {
    private static final String TAG = "BarrageBtnPresenter";

    public BarrageBtnPresenter(@NonNull IComponentController componentController) {
        super(componentController);
        registerAction(WatchComponentController.MSG_BOTTOM_POPUP_SHOWED);
        registerAction(WatchComponentController.MSG_BOTTOM_POPUP_HIDDEN);
    }

    @Override
    public void showInputView() {
        MyLog.w(TAG, "showInputView()");
        mComponentController.onEvent(WatchComponentController.MSG_SHOW_INPUT_VIEW);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new IAction() {
            @Override
            public boolean onAction(int source, @Nullable Params params) {
                if (mView == null) {
                    MyLog.e(TAG, "onAction but mView is null, source=" + source);
                    return false;
                }
                switch (source) {
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
        };
    }
}
