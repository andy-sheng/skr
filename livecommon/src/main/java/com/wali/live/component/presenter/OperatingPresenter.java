package com.wali.live.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.view.OperatingView;

/**
 * Created by chenyong on 2017/03/24.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 运营位操作类
 */
public class OperatingPresenter extends ComponentPresenter<OperatingView.IView>
        implements OperatingView.IPresenter {
    private static final String TAG = "OperatingPresenter";

    public OperatingPresenter(@NonNull IComponentController componentController) {
        super(componentController);
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
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }
}
