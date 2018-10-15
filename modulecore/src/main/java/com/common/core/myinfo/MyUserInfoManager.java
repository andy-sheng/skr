package com.common.core.myinfo;


import com.common.core.account.UserAccountManager;
import com.wali.live.proto.User.GetOwnInfoRsp;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * 保存个人详细信息，我的信息的管理, 其实是对User的decorate
 * Created by chengsimin on 16/7/1.
 */
public class MyUserInfoManager {

    private UserInfo mUser;

    public void init() {
        load();

    }

    private void load() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (UserAccountManager.getInstance().hasAccount()) {
                    UserInfo userInfo = UserInfoLocalApi.getUserAccount(UserAccountManager.getInstance().getUuidAsLong());
                    setMyUserInfo(userInfo);
                    // 从服务器拉一次
                    GetOwnInfoRsp rsp = UserInfoServerApi.getOwnInfoRsp(UserAccountManager.getInstance().getUuidAsLong());
                    userInfo = UserInfo.loadFrom(rsp);
                    if (userInfo != null) {
                        UserInfoLocalApi.insertOrReplace(userInfo);
                        setMyUserInfo(userInfo);
                    }
                }
                emitter.onComplete();
            }
        })
//                .doOnSubscribe()
//                .doOnTerminate()
//                .debounce()
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void setMyUserInfo(UserInfo myUserInfo) {
        if (myUserInfo != null) {
            mUser = myUserInfo;
            //user信息设定成功了，发出eventbus
        }
    }

    public String getNickName() {
        if (mUser != null) {
            return mUser.getNickName();
        }
        return "";
    }

    private static class MyUserInfoManagerHolder {
        private static final MyUserInfoManager INSTANCE = new MyUserInfoManager();
    }

    private MyUserInfoManager() {

    }

    public static final MyUserInfoManager getInstance() {
        return MyUserInfoManagerHolder.INSTANCE;
    }
}
