package com.common.webview;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;

public class JsBridgeImpl {
    BaseActivity mBaseActivity;

    public JsBridgeImpl(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
    }

    public void openSchema(String shema){
        ARouter.getInstance().build("/core/SchemeSdkActivity")
                .withString("uri", shema)
                .navigation();
    }
}
