package com.mi.liveassistant.account;

import com.mi.liveassistant.account.callback.IAccountCallback;
import com.mi.liveassistant.account.callback.IAccountListener;
import com.mi.liveassistant.account.login.LoginType;
import com.mi.liveassistant.account.task.AccountCaller;
import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.dao.UserAccount;
import com.mi.liveassistant.data.repository.AccountLocalStore;
import com.mi.liveassistant.event.AccountEvent;
import com.mi.liveassistant.proto.AccountProto;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chenyong on 2017/4/28.
 */
public class AccountManager {

    private static final String TAG = AccountManager.class.getSimpleName();

    private static AccountManager mInstance = new AccountManager();

    public static AccountManager getInstance() {
        return mInstance;
    }

    private AccountManager() {
    }

    private IAccountListener mAccountListener;

    public void setAccountListener(IAccountListener listener) {
        mAccountListener = listener;
    }

    /**
     * 请上层异步处理
     */
    public String getAccount() {
        if (UserAccountManager.getInstance().hasAccount()) {
            return UserAccountManager.getInstance().getUuid();
        }
        UserAccount account = AccountLocalStore.getInstance().getAccount();
        UserAccountManager.getInstance().login(account);
        return UserAccountManager.getInstance().hasAccount() ? account.getUuid() : null;
    }

    public void loginByMiAccountOAuth(final int channelId, final String code, final IAccountCallback callback) {
        AccountCaller.login(channelId, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.LoginRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "miLoginByCode login onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "miLoginByCode login onError=" + e.getMessage());
                        callback.notifyFail(ErrorCode.CODE_ERROR_NORMAL);
                    }

                    @Override
                    public void onNext(AccountProto.LoginRsp rsp) {
                        MyLog.w(TAG, "miLoginByCode login onNext");
                        int code = rsp.getRetCode();
                        if (code == ErrorCode.CODE_SUCCESS) {
                            callback.notifySuccess(UserAccountManager.getInstance().getUuid());
                        } else {
                            callback.notifyFail(code);
                        }
                    }
                });
    }

    public void thirdPartLogin(final int channelId, final String xuid, final String name,
                               final String headUrl, final int sex, final String sign,
                               final IAccountCallback callback) {
        AccountCaller.login(channelId, xuid, sex, name, headUrl, sign)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.ThirdPartSignLoginRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "thirdPartLogin login onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "thirdPartLogin login onError=" + e);
                        callback.notifyFail(ErrorCode.CODE_ERROR_NORMAL);
                    }

                    @Override
                    public void onNext(AccountProto.ThirdPartSignLoginRsp rsp) {
                        MyLog.w(TAG, "thirdPartLogin login onNext, rsp=" + rsp);
                        int code = rsp.getRetCode();
                        if (code == ErrorCode.CODE_SUCCESS) {
                            callback.notifySuccess(UserAccountManager.getInstance().getUuid());
                        } else {
                            callback.notifyFail(code);
                        }
                    }
                });
    }

    public void logoff() {
        UserAccountManager.getInstance().logoff(UserAccountManager.getInstance().getUuidAsLong());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.LogOffEvent event) {
        if (event == null) {
            return;
        }
        MyLog.d(TAG, "event type=" + event.getEventType());
        switch (event.getEventType()) {
            case AccountEvent.LogOffEvent.EVENT_TYPE_FORBIDDEN:
                mAccountListener.forbidAccount();
                break;
            case AccountEvent.LogOffEvent.EVENT_TYPE_EXPIRE_LOGOFF:
                mAccountListener.logoffAccount();
                break;
            case AccountEvent.LogOffEvent.EVENT_TYPE_KICK:
                mAccountListener.kickAccount();
                break;
        }
    }
}
