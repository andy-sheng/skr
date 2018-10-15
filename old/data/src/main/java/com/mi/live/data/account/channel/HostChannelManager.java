package com.mi.live.data.account.channel;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.repository.datasource.AccountLocalStore;
import com.wali.live.dao.UserAccount;

import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;

/**
 * 宿主管理
 * Created by chengsimin on 2017/2/10.
 */

public class HostChannelManager {
    public final static String TAG = HostChannelManager.class.getSimpleName();
    public final static String KEY_SHARE_ENABLE = "key_share_enable";
    public final static String KEY_FOLLOW_ENABLE = "key_follow_enable";
    private final static int GAME_CENTER_CHANNEL_ID = 50010;

    // 当前账号的渠道
    private final static int NO_CHANNEL = 0;
    private volatile int mChannelId = NO_CHANNEL;
    private String mPackageName = "";

    // 账户模式：标准和匿名
    private static HostChannelManager sInstance;


    private HashMap<Integer, HashMap<String, Object>> mDataMap = new HashMap<>();

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
     * 返回的boolean值代表渠道是否有变化
     * <p>
     * 检查channelid
     */
    public synchronized Observable<Boolean> checkChannel(final int channelId, final String packageName) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                if (channelId == mChannelId) {
                    subscriber.onNext(false);
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

    public void put(int channelId, String key, Object obj) {
        HashMap<String, Object> map = mDataMap.get(channelId);
        if (map == null) {
            map = new HashMap<>();
            mDataMap.put(channelId, map);
        }
        map.put(key, obj);
    }

    public boolean isFromGameCenter() {
        return mChannelId == GAME_CENTER_CHANNEL_ID;
    }

    public Object get(String key) {
        HashMap<String, Object> map = mDataMap.get(HostChannelManager.getInstance().getChannelId());
        if (map == null) {
            return null;
        }
        return map.get(key);
    }
}
