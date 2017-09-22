package com.wali.live.livesdk.live.component;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

import com.base.fragment.FragmentDataListener;
import com.wali.live.component.BaseSdkController;
import com.wali.live.component.BaseSdkView;

/**
 * Created by yangli on 2017/9/8.
 *
 * @module 直播页面基类
 */
public abstract class BaseLiveSdkView<VIEW extends View, CONTROLLER extends BaseSdkController>
        extends BaseSdkView<VIEW, CONTROLLER> {

    public BaseLiveSdkView(@NonNull Activity activity, @NonNull CONTROLLER controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
    }

    /**
     * 进入准备页
     */
    public abstract void enterPreparePage(
            @NonNull FragmentActivity activity,
            int requestCode,
            FragmentDataListener listener);
}
