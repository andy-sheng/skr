package com.mi.live.data.account;

import com.base.log.MyLog;
import com.mi.live.data.repository.datasource.AccountLocalStore;
import com.wali.live.dao.UserAccount;

import rx.Observable;
import rx.Subscriber;

/**
 * 宿主管理
 * Created by chengsimin on 2017/2/10.
 */

public class HostChannelManager {
    public final static String TAG = HostChannelManager.class.getSimpleName();
    // 当前账号的渠道
    private final static int NO_CHANNEL = 0;
    private int mChannelId = NO_CHANNEL;
    private String mPackageName = "";

    // 账户模式：标准和匿名
    private static HostChannelManager sInstance;

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

    public int getChannelId() {
        return mChannelId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    /**
     * 检查channelid
     */
    public synchronized Observable<Boolean> checkChannel(final int channelId, final String packageName) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                if (channelId == mChannelId) {
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                    return;
                }
                MyLog.w(TAG, "old channel=" + mChannelId + ",new channel=%s" + channelId);
                UserAccount account = AccountLocalStore.getInstance().getAccount(channelId);
                if (account != null
                        && UserAccountManager.getInstance().getAccount() != null
                        && account.getUuid().equals(UserAccountManager.getInstance().getAccount().getUuid())) {
                    mChannelId = channelId;
                    mPackageName = packageName;
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                    return;
                }
                if (UserAccountManager.getInstance().getAccount() != null) {
                    MyLog.w(TAG, "uuid " + UserAccountManager.getInstance().getAccount().getUuid() + " logoff");
                    UserAccountManager.getInstance().logoffWithoutClearAccount(mChannelId);
                }
                mChannelId = channelId;
                mPackageName = packageName;
                if (account != null) {
                    MyLog.w(TAG, "uuid " + account.getUuid() + " login");
                    UserAccountManager.getInstance().login(account);
                }
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });
    }

    public synchronized void setChannelData(int channelId, String packageName) {
        mChannelId = channelId;
        mPackageName = packageName;
    }
}
