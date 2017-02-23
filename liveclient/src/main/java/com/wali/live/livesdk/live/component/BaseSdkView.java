package com.wali.live.livesdk.live.component;

import android.app.Activity;
import android.support.annotation.CheckResult;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.wali.live.livesdk.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.component.view.IComponentView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/2/17.
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 */
public abstract class BaseSdkView<T extends ComponentController> {
    private static final String TAG = "BaseSdkView";

    protected @NonNull Activity mActivity;
    protected @NonNull T mComponentController;
    protected final List<IComponentView> mModuleViewSet = new ArrayList<>();

    @Nullable
    @CheckResult
    protected final <V extends View> V $(@IdRes int id) {
        return (V) mActivity.findViewById(id);
    }

    protected final void addComponentView(
            @NonNull IComponentView view,
            @NonNull ComponentPresenter presenter) {
        view.setPresenter(presenter);
        presenter.setComponentView(view.getViewProxy());
        mModuleViewSet.add(view);
    }

    public BaseSdkView(@NonNull Activity activity,
                       @NonNull T componentController) {
        mActivity = activity;
        mComponentController = componentController;
    }

    /**
     * 初始化SdkView
     */
    public abstract void setupSdkView();

    public void onActivityCreated() {
    }

    public void onActivityDestroyed() {
    }

}
