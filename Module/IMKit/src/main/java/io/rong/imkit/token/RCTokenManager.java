package io.rong.imkit.token;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.test.RongIMAPIManager;

/**
 * 仅用来测试直接用从融云获取token
 */
public class RCTokenManager {

    public final static String TAG = "GetTokenManager";

    private static class GetTokenManagerHolder {
        private static final RCTokenManager INSTANCE = new RCTokenManager();
    }

    private RCTokenManager() {

    }

    public static final RCTokenManager getInstance() {
        return GetTokenManagerHolder.INSTANCE;
    }

    /**
     * 融云服务器拿token
     *
     * @param userId      用户id
     * @param name        用户名
     * @param portraitUri 头像Url
     */
    public void getToken(String userId, String name, String portraitUri) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(name) || TextUtils.isEmpty(portraitUri)) {
            MyLog.d(TAG, "getToken userId name or portraitUri error");
            return;
        }

        RCTokenServerApi getTokenServerApi = RongIMAPIManager.getInstance().createService(RCTokenServerApi.class);
        getTokenServerApi.getToken(userId, name, portraitUri)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        int code = jsonObject.getIntValue("code");
                        String token = jsonObject.getString("token");
                        String userid = jsonObject.getString("userId");
                        // todo token保存
                        U.getPreferenceUtils().setSettingString("token", token);
                        U.getToastUtil().showShort(jsonObject.toString());
                        connectRongIM(token);
                    }

                    @Override
                    public void onError(Throwable e) {
                        U.getToastUtil().showShort(e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 重新拿token
     */
    public void getToken() {
        String useId = String.valueOf(MyUserInfoManager.getInstance().getUid());
        String name = MyUserInfoManager.getInstance().getNickName();
        // todo 后期优化头像部分
        String portraitUri = String.valueOf(MyUserInfoManager.getInstance().getAvatar());
        getToken(useId, name, portraitUri);
    }

    private void connectRongIM(String token) {
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
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
                getToken();
            }
        });
    }
}
