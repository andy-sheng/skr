package com.mi.live.data.account;

import com.base.log.MyLog;
import com.mi.live.data.repository.datasource.AccountLocalStore;
import com.wali.live.dao.UserAccount;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 宿主管理
 * Created by chengsimin on 2017/2/10.
 */

public class HostChannelManager {
    public final static String TAG = HostChannelManager.class.getSimpleName();
    // 当前账号的渠道
    private final static int NO_CHANNEL = 0;
    private int mCurrentChannelId = NO_CHANNEL;
    private String mPackageName = "";

    // 账户模式：标准和匿名
    private static HostChannelManager sInstance;

    //    private UserAccountDao mAccountDao;

    private HostChannelManager() {
    }

    public static HostChannelManager getInstance() {
        if (sInstance == null) {
            synchronized (HostChannelManager.class) {
                if (sInstance == null) {
                    sInstance = new HostChannelManager();
                }
            }
        }
        return sInstance;
    }

    public int getmCurrentChannelId() {
        return mCurrentChannelId;
    }

    public String getmPackageName() {
        return mPackageName;
    }

    /**
     * 关键，这时已经知道调用方的channelid了，尝试读取账号，如果没有账号进入匿名模式
     * 是不是不要在主线程干这些
     * TODO 渠道逻辑还有不少问题，之后重新写这部分逻辑
     */
    public synchronized void setChannelId(final int channelId) {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        MyLog.w(TAG, "setChannelId channelId:" + channelId + ", mChannelId:" + mCurrentChannelId);
                        if (channelId < 0) {
                            // demo id说来自demo，所以就不用进行后续操作了
                            return;
                        }
                        if (mCurrentChannelId != channelId) {
                            // 渠道号不一致,尝试查找当前渠道的账号
                            UserAccount account = AccountLocalStore.getInstance().getAccount(channelId);
                            MyLog.w(TAG, "setChannelId account:" + account);
                            if (account == null) {
                                // 没找到账号，
                                mCurrentChannelId = channelId;
                                UserAccountManager.getInstance().logoff(mCurrentChannelId);
                            } else {
                                // 有账号，登录这个账号
                                UserAccountManager.getInstance().login(account);
                            }
                        } else {
                            // 渠道号一致
                        }
                    }
                });

    }

    public void setHostPackageName(String packageName) {
        mPackageName = packageName;
    }
}
