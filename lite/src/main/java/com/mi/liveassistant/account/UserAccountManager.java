package com.mi.liveassistant.account;

import android.text.TextUtils;

import com.mi.liveassistant.account.task.AccountCaller;
import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.dao.UserAccount;
import com.mi.liveassistant.data.repository.AccountLocalStore;
import com.mi.liveassistant.event.AccountEventController;
import com.mi.liveassistant.event.SetUserAccountEvent;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.milink.event.MiLinkEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 16/7/1.
 */
public class UserAccountManager {
    private static final String TAG = UserAccountManager.class.getSimpleName();

    // 用户当前账号
    private UserAccount mAccount;

    // 用户匿名账号的id,当 mAccount 为空的时候使用
    private long anonymousId;

    private volatile int mChannelId;

    // 账户模式：标准和匿名
    private static UserAccountManager sInstance;

    private UserAccountManager() {
        EventBus.getDefault().register(this);
    }

    public UserAccount getAccount() {
        return mAccount;
    }

    public static UserAccountManager getInstance() {
        if (sInstance == null) {
            synchronized (UserAccountManager.class) {
                if (sInstance == null) {
                    sInstance = new UserAccountManager();
                }
            }
        }
        return sInstance;
    }

    public void login(UserAccount account) {
        MyLog.w(TAG, "login:" + account);
        // 有新的账号登录了
        if (account != null) {
            //先把这个账号写到数据库里
            AccountLocalStore.getInstance().replaceAccount(account);
            setAccount(account);
            // 取消匿名模式
            MiLinkClientAdapter.getsInstance().setIsTouristMode(false);
            // MilinkChannelClientAdapter.getInstance().destroy();
            // 进入实名模式
            MiLinkClientAdapter.getsInstance().initCallBackFirst();
            // 同步昵称等详细信息
            MyUserInfoManager.getInstance().init();
        }
    }

    public void logoff(long uuid) {
        MyLog.w(TAG, "logoff:" + uuid);
        AccountLocalStore.getInstance().deleteAccount(String.valueOf(uuid));
        // 和当前渠道一致,当前账号置为空
        MyUserInfoManager.getInstance().deleteUser();
        mAccount = null;

        // 实名模式登出
        MiLinkClientAdapter.getsInstance().logoff();
        // milink 切换成匿名模式
        MiLinkClientAdapter.getsInstance().setIsTouristMode(true);

        MyLog.w(TAG, "logoff post event");
        AccountEventController.onActionLogOff(AccountEventController.LogOffEvent.EVENT_TYPE_NORMAL_LOGOFF);
    }

    public void logoffWithoutClearAccount(long uuid) {
        MyLog.w(TAG, "logoffWithoutClearAccount:" + uuid);
        MyUserInfoManager.getInstance().deleteCache();
        // 和当前渠道一致,当前账号置为空
        mAccount = null;
        if (!MiLinkClientAdapter.getsInstance().isTouristMode()) {
            // 实名模式登出
            MiLinkClientAdapter.getsInstance().logoff();
            // milink 切换成匿名模式
            MiLinkClientAdapter.getsInstance().setIsTouristMode(true);

            MyLog.w(TAG, "logoffWithoutClearAccount post event");
            AccountEventController.onActionLogOff(AccountEventController.LogOffEvent.EVENT_TYPE_NORMAL_LOGOFF);
        }
    }

    public void setAccount(UserAccount account) {
        this.mAccount = account;
        // 只有非游客模式才发已有账号的事件
        if (!MiLinkClientAdapter.getsInstance().isTouristMode() && mAccount != null) {
            EventBus.getDefault().post(new SetUserAccountEvent());
        }
    }

    /**
     * 匿名模式初始化
     */
    public synchronized void initAnonymous() {
        MyLog.d(TAG, "initAnonymous()");
        if (mAccount == null) {
            MiLinkClientAdapter.getsInstance().setIsTouristMode(true);
        }
    }


    /**
     * token等信息获取完成，初始化一下milink。
     */
    public void completeToken() {
        if (mAccount != null) {
            //先把这个账号写到数据库里
            AccountLocalStore.getInstance().replaceAccount(mAccount);
            // 取消匿名模式
            MiLinkClientAdapter.getsInstance().setIsTouristMode(false);
            // 进入实名模式
            MiLinkClientAdapter.getsInstance().initCallBackFirst();
        }
    }

    public boolean hasAccount() {
        MyLog.d(TAG, "null != mAccount : " + (null != mAccount));
        return null != mAccount && ((mAccount.getIsLogOff() == null) || !mAccount.getIsLogOff().booleanValue());
    }

    public String getUuid() {
        if (MiLinkClientAdapter.getsInstance().isTouristMode()) {
            if (anonymousId == 0) {
                MiLinkClientAdapter.getsInstance().trySyncAnonymousAccountId();
            }
            return String.valueOf(anonymousId);
        }
        if (null != mAccount) {
            return mAccount.getUuid();
        }
        return "";
    }


    public long getUuidAsLong() {
        if (MiLinkClientAdapter.getsInstance().isTouristMode()) {
            if (anonymousId == 0) {
                MiLinkClientAdapter.getsInstance().trySyncAnonymousAccountId();
            }
            return anonymousId;
        }
        if (null != mAccount) {
            return Long.parseLong(mAccount.getUuid());
        }
        return 0;
    }

    public int getChannelId(){
        return mChannelId;
    }

    public synchronized void setmChannelId(int channelId){
        mChannelId = channelId;
    }

    public String getServiceToken() {
        if (null != mAccount) {
            return mAccount.getServiceToken();
        }
        return "";
    }

    public void setServiceToken(String serviceToken) {
        if (null != mAccount) {
            mAccount.setServiceToken(serviceToken);
        }
    }

    public String getSSecurity() {
        if (null != mAccount) {
            return mAccount.getSSecurity();
        }
        return "";
    }

    public void setSSecurity(String sSecurity) {
        if (null != mAccount) {
            mAccount.setSSecurity(sSecurity);
        }
    }

    public String getPassToken() {
        if (null != mAccount) {
            return mAccount.getPassToken();
        }
        return "";
    }

    public void setPassToken(String passToken) {
        if (null != mAccount) {
            mAccount.setPassToken(passToken);
        }
    }

    public String getNickname() {
        if (null != mAccount) {
            return mAccount.getNickName();
        }
        return "";
    }

    public void setNeedEditUserInfo(boolean needEditUserInfo) {
        if (null != mAccount) {
            mAccount.setNeedEditUserInfo(needEditUserInfo);
        }
    }

    public boolean getNeedEditUserInfo() {
        if (null != mAccount) {
            if (mAccount.getNeedEditUserInfo() == null) {
                return false;
            } else {
                return mAccount.getNeedEditUserInfo();
            }
        }
        return false;
    }

    public void setAnonymousId(long anonymousId) {
        this.anonymousId = anonymousId;
    }

    public long getAnonymousId() {
        return anonymousId;
    }

    /*
    * milink链接上的回调
    * */
    @Subscribe
    public void onEvent(MiLinkEvent.StatusLogined event) {
        if (event != null) {
            MyUserInfoManager.getInstance().updateUserInfoIfNeed();
        }
    }

    // TODO 全部移到data层后这里要重构
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(MiLinkEvent.Account event) {
        switch (event.op) {
            case MiLinkEvent.Account.KICK: {
                int type = (int) event.obj1;
                MiLinkClientAdapter.getsInstance().logoff();
                if (type == 1) {
                    AccountEventController.onActionLogOff(AccountEventController.LogOffEvent.EVENT_TYPE_KICK);
                } else if (type == 2) {
                    //账号封禁
                    AccountEventController.onActionLogOff(AccountEventController.LogOffEvent.EVENT_TYPE_ACCOUNT_FORBIDDEN);
                }
                UserAccountManager.getInstance().logoff(UserAccountManager.getInstance().getUuidAsLong());
//                new MyAlertDialog.Builder(GlobalData.app())
//                        .setMessage("账号被踢")
//                        .create()
//                        .show();
            }
            break;
            case MiLinkEvent.Account.GET_SERVICE_TOKEN: {
                MiLinkClientAdapter.getsInstance().initCallBack();
            }
            break;
            case MiLinkEvent.Account.SERVICE_TOKEN_EXPIRED: {
                UserAccountManager userAccountManager = UserAccountManager.getInstance();
                String uuid = userAccountManager.getUuid();
                String passToken = userAccountManager.getPassToken();
                if (!TextUtils.isEmpty(uuid) && !TextUtils.isEmpty(passToken)) {
                    UserAccountManager.getInstance().logoffWithoutClearAccount(UserAccountManager.getInstance().getUuidAsLong());
                    AccountCaller.getServiceToken(passToken, uuid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Integer>() {
                                @Override
                                public void onCompleted() {
                                    MyLog.w(TAG, "passToken to serviceToken onCompleted");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    MyLog.w(TAG, "passToken to serviceToken onError=" + e.getMessage());
                                }

                                @Override
                                public void onNext(Integer errCode) {
                                    MyLog.w(TAG, "passToken to serviceToken onNext code:" + errCode);
                                    if (errCode != null) {
                                        if (errCode == ErrorCode.CODE_SUCCESS) {
                                            MyLog.w(TAG, "passToken to serviceToken success");
                                        } else if (errCode == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
                                            //账号封禁
                                            UserAccountManager.getInstance().logoff(UserAccountManager.getInstance().getUuidAsLong());
                                        } else {
                                            MyLog.w(TAG, "passToken to serviceToken failure, kick off");
                                            UserAccountManager.getInstance().logoff(UserAccountManager.getInstance().getUuidAsLong());
                                        }
                                    }
                                }
                            });
                } else {
                    AccountEventController.onActionLogOff(AccountEventController.LogOffEvent.EVENT_TYPE_NORMAL_LOGOFF);
                    MyLog.e(TAG, "onEventServiceTokenExpired but uuid is empty!");
                }
            }
            break;
        }
    }
}
