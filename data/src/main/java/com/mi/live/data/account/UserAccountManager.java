package com.mi.live.data.account;

import android.text.TextUtils;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.event.AccountEventController;
import com.mi.live.data.account.event.SetUserAccountEvent;
import com.mi.live.data.account.task.AccountCaller;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.repository.datasource.AccountLocalStore;
import com.wali.live.dao.UserAccount;

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

    // 账户模式：标准和匿名
    private static UserAccountManager sInstance;

    // private UserAccountDao mAccountDao;

    private UserAccountManager() {
        EventBus.getDefault().register(this);
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

    public void logoff(int channelid) {
        MyLog.w(TAG, "logoff:" + channelid);
        AccountLocalStore.getInstance().deleteAccount(channelid);
        if (channelid == HostChannelManager.getInstance().getChannelId()) {
            // 和当前渠道一致,当前账号置为空
            MyUserInfoManager.getInstance().deleteUser();
            mAccount = null;
            // 实名模式登出
            MiLinkClientAdapter.getsInstance().logoff();
            // milink 切换成匿名模式
            MiLinkClientAdapter.getsInstance().setIsTouristMode(true);
        } else {
            //
        }
    }

    public void setAccount(UserAccount account) {
        this.mAccount = account;
        HostChannelManager.getInstance().setChannelId(account.getChannelid());
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
            // MilinkChannelClientAdapter.getInstance().destroy();
            // 进入实名模式
            MiLinkClientAdapter.getsInstance().initCallBackFirst();
        }
    }
//    public boolean isInitAccount() {
//        return mAccount != null || mInitAccount;
//    }
//
//    public synchronized void initAccount() {
//        MyLog.d(TAG, "initAccount()");
//        if (mAccountDao == null || null == mAccount) {
//            mAccountDao = GreenDaoManager.getDaoSession(GlobalData.app()).getUserAccountDao();
//            List<UserAccount> accountList = mAccountDao.queryBuilder().build().list();
//            if (null != accountList && accountList.size() > 0) {
//                setAccount(accountList.get(0));
//                AccountEventController.onActionInitAccountFinish();
//            }
//        }
//        if (mAccount != null) {
//            MiLinkClientAdapter.getsInstance().initCallBackFirst();
//        }
//        mInitAccount = true;
//    }

    public boolean hasAccount() {
        MyLog.d(TAG, "null != mAccount" + (null != mAccount));
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

//    public void recordUserAccount() {
//        MyLog.d(TAG, "recordUserAccount mAccount=" + mAccount);
//        if (null != mAccount) {
//            mAccountDao.deleteAll();
//            mAccountDao.insertOrReplace(mAccount);
//            MyLog.d(TAG, "post EventClass.AccountChangeEvent.EVENT_TYPE_CHANGED");
//            MiLinkClientAdapter.getsInstance().initCallBackFirst();
//        }
//    }
//
//    public void deleteUserAccount() {
//        mAccountDao.deleteAll();
//        clearAccount();
//    }


    // sdk中也使用这个策略
    /**
     * 更改登出账号策略，登出时标记为登出。
     * 不清除账号内存以及数据库，下次账号登入，
     * 匹配账号是否一致，不一致再清除账号数据。
     */
//    public void setUserAccountLogOff() {
//        if (null != mAccount) {
//            mAccount.setIsLogOff(true);
//            mAccount.setPassToken("");
//            mAccount.setServiceToken("");
//            mAccount.setSSecurity("");
//            mAccountDao.insertOrReplace(mAccount);
//        }
//    }
//
//    public void clearAccount() {
//        MyLog.d(TAG, "clearAccount");
//        setAccount(null);
//    }
//
//    public void recordMyAccount() {
//        MyLog.d(TAG, "recordMyAccount");
//        mAccountDao.insertInTx(mAccount);
//    }
//
//
//    public UserAccount createNewFromAccountInfo(AccountInfo info) {
//        UserAccount account = null;
//        if (info != null) {
//            account = new UserAccount();
//            account.setUuid(info.getUserId());
//            account.setPassToken(info.getPassToken());
//            account.setServiceToken(info.getServiceToken());
//            account.setPSecurity(info.getPsecurity());
//            account.setSSecurity(info.getSecurity());
//        }
//        return account;
//    }
//
//    public UserAccount createNewFromSystemLogin(String userId, String serviceToken, String security) {
//        UserAccount account = new UserAccount();
//        account.setUuid(userId);
//        account.setServiceToken(serviceToken);
//        account.setSSecurity(security);
//        return account;
//    }


    /**
     * 不是同一个账号需要清理相应数据的接口
     *
     * @notice 其他上层数据清理操作，可以在DataManager的SwitchAccountEvent中处理
     */
//    public void clearDataFromDifAccount() {
//        MyUserInfoManager.getInstance().deleteUser();
//
//        WatchHistoryInfoDaoAdapter.getInstance().deleteAll();
//
//        MLPreferenceUtils.setIsLogOff(GlobalData.app(), true);
//        PreferenceUtils.removePreference(GlobalData.app(), PreferenceUtils.PREF_KEY_FACE_BEAUTY_LEVEL);
//        PreferenceUtils.clear(GlobalData.app(), PreferenceKeys.PRE_KEY_SEARCH_SONG_HISTORY);
//
//        EventBus.getDefault().post(new SwitchAccountEvent());
//    }

    /*
    * milink链接上的回调
    * */
    @Subscribe
    public void onEvent(MiLinkEvent.StatusLogined event) {
        MyUserInfoManager.getInstance().updateUserInfoIfNeed();
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
                UserAccountManager.getInstance().logoff(HostChannelManager.getInstance().getChannelId());
                new MyAlertDialog.Builder(GlobalData.app())
                        .setMessage("账号被踢")
                        .create()
                        .show();
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
                                            UserAccountManager.getInstance().logoff(HostChannelManager.getInstance().getChannelId());
//                                            AccountEventController.onActionLogOff(AccountEventController.LogOffEvent.EVENT_TYPE_ACCOUNT_FORBIDDEN);
                                        } else {
                                            MyLog.w(TAG, "passToken to serviceToken failure, kick off");
                                            UserAccountManager.getInstance().logoff(HostChannelManager.getInstance().getChannelId());
//                                            MiLinkClientAdapter.getsInstance().logoff();
//                                            AccountEventController.onActionLogOff(AccountEventController.LogOffEvent.EVENT_TYPE_NORMAL_LOGOFF);
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
