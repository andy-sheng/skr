package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.ImagePagerView;

/**
 * Created by lan on 2017/07/17.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 模仿viewpager增加上下滑动的效果
 */
public class ImagePagerPresenter extends ComponentPresenter<ImagePagerView.IView>
        implements ImagePagerView.IPresenter {
    private static final String TAG = "ImagePagerPresenter";

    public ImagePagerPresenter(@NonNull IComponentController componentController) {
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
