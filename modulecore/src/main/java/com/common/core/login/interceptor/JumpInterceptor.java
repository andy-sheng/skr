package com.common.core.login.interceptor;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.account.UserAccountManager;
import com.common.core.login.execption.UnloginException;
import com.common.log.MyLog;
import com.common.utils.U;

@Interceptor(priority = 1)
public class JumpInterceptor implements IInterceptor {
    public final static String TAG = "JumpInterceptor";

    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        MyLog.d(TAG, "process" + " postcard=" + postcard + " path:" + postcard.getPath() + " callback=" + callback);
//        if (true) {
//            callback.onContinue(postcard);
//            return;
//        }
        if ("/core/login".equals(postcard.getPath())) {
            callback.onContinue(postcard);
            return;
        }

        if (UserAccountManager.getInstance().hasAccount()) {
            callback.onContinue(postcard);
        } else {
            // 跳到登录页面

            ARouter.getInstance().build("/core/login")
                    .withBoolean("key_show_toast",true)
                    .greenChannel().navigation();

            callback.onInterrupt(new UnloginException());
        }
    }

    @Override
    public void init(Context context) {

    }
}
