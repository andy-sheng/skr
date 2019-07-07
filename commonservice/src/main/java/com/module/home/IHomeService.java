package com.module.home;

import android.app.Activity;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * channel module 对外提供服务的接口
 */
public interface IHomeService extends IProvider {
    Object getData(int type, Object object);
    void authSuccess();

    void goUploadAccountInfoActivity(Activity activity);

    void goHomeActivity(Activity loginActivity);
}
