package com.common.core.myinfo;


import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.account.UserAccountServerApi;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.UserInfo;
import com.common.core.userinfo.UserInfoLocalApi;
import com.common.core.userinfo.UserInfoManager;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiResult;
import com.wali.live.proto.User.GetOwnInfoRsp;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 保存个人详细信息，我的信息的管理, 其实是对User的decorate
 * Created by chengsimin on 16/7/1.
 */
public class MyUserInfoManager {

    private MyUserInfo mUser;

    public void init() {
        load();
    }

    private void load() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (UserAccountManager.getInstance().hasAccount()) {
                    MyUserInfo myUserInfo = new MyUserInfo();
                    UserInfo userInfo = UserInfoLocalApi.getUserInfoByUUid(UserAccountManager.getInstance().getUuidAsLong());
                    if (userInfo != null) {
                        myUserInfo.setUserInfo(userInfo);
                        setMyUserInfo(myUserInfo);
                    }
                    // 从服务器拉一次
//                    GetOwnInfoRsp rsp = MyUserInfoServerApi.getOwnInfoRsp(UserAccountManager.getInstance().getUuidAsLong());
//                    if (rsp != null) {
//                        myUserInfo = MyUserInfo.loadFrom(rsp);
//                        if (myUserInfo != null) {
//                            UserInfoLocalApi.insertOrUpdate(myUserInfo.getUserInfo(), false, false);
//                            setMyUserInfo(myUserInfo);
//                        }
//                    }
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

    public MyUserInfo getMyUserInfo() {
        return mUser;
    }

    public void setMyUserInfo(MyUserInfo myUserInfo) {
        if (myUserInfo != null) {
            mUser = myUserInfo;
            //user信息设定成功了，发出eventbus
            EventBus.getDefault().post(new MyUserInfoEvent.UserInfoChangeEvent());
        }
    }

    /**
     * 更新用户信息
     *
     * @param nickName
     * @param sex
     * @param birthday
     */
    public void updateInfo(String nickName, int sex, String birthday,String avatar) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nickname", nickName);
        map.put("sex", sex);
        map.put("birthday", birthday);
        map.put("avatar", avatar);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(map));
        MyUserInfoServerApi myUserAccountServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        Observable<ApiResult> apiResultObservable = myUserAccountServerApi.updateInfo(body);
        ApiMethods.subscribe(apiResultObservable, null);
    }

    public long getUid() {
        return mUser != null ? mUser.getUid() : 0;
    }

    public String getNickName() {
        return mUser != null ? mUser.getNickName() : "";
    }

    public long getAvatar() {
        return mUser != null ? mUser.getAvatar() : 0;
    }

    public int getLevel() {
        return mUser != null ? mUser.getLevel() : 0;
    }

    public int getVipLevel() {
        return mUser != null ? mUser.getVipLevel() : 0;
    }

    public boolean isRedName() {
        return mUser != null ? mUser.getRedName() : false;
    }

    public boolean isVipFrozen() {
        return mUser != null ? mUser.getIsVipFrozen() : false;
    }

    public synchronized int getNobleLevel() {
        return mUser != null ? mUser.getNobleLevel() : 0;
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
