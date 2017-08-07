package com.wali.live.livesdk.live.liveshow.data;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.utils.version.VersionManager;
import com.thornbirds.component.IEventController;
import com.wali.live.component.presenter.BaseSdkRxPresenter;

/**
 * Created by yangli on 2017/3/8.
 *
 * @module 云端配参数据
 */
public abstract class BaseParamPresenter extends BaseSdkRxPresenter {

    @NonNull
    protected Context mContext;

    public BaseParamPresenter(@NonNull IEventController controller, @NonNull Context context) {
        super(controller);
        mContext = context;
    }

    protected final String formatParam(String str) {
        return TextUtils.isEmpty(str) ? "" : str.replaceAll(",", " ").replaceAll("=", " ").toLowerCase();
    }

    // 获取机型
    protected final String getModelInfo() {
        return formatParam(Build.MODEL);
    }

    // 获取操作系统版本
    protected final String getOsInfo() {
        return formatParam(Build.VERSION.INCREMENTAL + "-" + Build.VERSION.SDK_INT);
    }

    // 获取操作App版本号
    protected final String getAppVersion() {
        return formatParam("1.1"/*VersionCheckManager.getCurrentVersionName(GlobalData.app())*/);
    }

    // 获取操作App版本名称
    protected final String getAppVersionName() {
        return formatParam(VersionManager.getVersionName(mContext));
    }
}
