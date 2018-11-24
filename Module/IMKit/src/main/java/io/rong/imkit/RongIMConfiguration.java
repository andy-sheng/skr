package io.rong.imkit;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.common.base.ConfigModule;
import com.common.base.GlobalParams;
import com.common.base.delegate.AppLifecycles;
import com.common.log.MyLog;
import com.common.utils.U;

import java.util.List;

import io.rong.imkit.token.RCTokenManager;
import io.rong.imlib.RongIMClient;

public class RongIMConfiguration implements ConfigModule {

    public final static String TAG = "RongIMConfiguration";

    @Override
    public void applyOptions(GlobalParams.Builder builder) {

    }

    @Override
    public void injectAppLifecycle(List<AppLifecycles> lifecycles) {
        lifecycles.add(new AppLifecycles() {
            @Override
            public void attachBaseContext(@NonNull Context base) {

            }

            @Override
            public void onMainProcessCreate(@NonNull Application application) {
                rcInit(application);
            }

            @Override
            public void onOtherProcessCreate(@NonNull Application application) {

            }


            @Override
            public void onTerminate(@NonNull Application application) {

            }
        });
    }

    @Override
    public void injectActivityLifecycle(List<Application.ActivityLifecycleCallbacks> lifecycles) {

    }

    @Override
    public void injectFragmentLifecycle(List<FragmentManager.FragmentLifecycleCallbacks> lifecycles) {

    }

    private void rcInit(Application application){
        // 初始化融云SDK
        RongIM.init(application);

        RongIM.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus status) {
                if (status == ConnectionStatus.TOKEN_INCORRECT) {
                    final String cacheToken = U.getPreferenceUtils().getSettingString("token", "");
                    if (!TextUtils.isEmpty(cacheToken)) {
                        RongIM.connect(cacheToken, new RongIMClient.ConnectCallback() {
                            /**
                             * 连接融云成功
                             * @param userid 当前 token 对应的用户 id
                             */
                            @Override
                            public void onSuccess(String userid) {
                                MyLog.d(TAG, "ConnectCallback connect Success");
                            }

                            /**
                             * 连接融云失败
                             * @param errorCode 错误码，可到官网 查看错误码对应的注释
                             *                  https://www.rongcloud.cn/docs/status_code.html#android_ios_code
                             */
                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                MyLog.d(TAG, "ConnectCallback " + "onError" + " errorCode=" + errorCode);
                            }

                            /**Token 错误。可以从下面两点检查
                             * 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                             * 2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                             */
                            @Override
                            public void onTokenIncorrect() {
                                MyLog.d(TAG, "ConnectCallback connect onTokenIncorrect");
                                // 重新拿token
                                RCTokenManager.getInstance().getToken();
                            }
                        });
                    } else {
                        Log.e("seal", "token is empty, can not reconnect");
                    }
                }
            }
        });
    }
}
