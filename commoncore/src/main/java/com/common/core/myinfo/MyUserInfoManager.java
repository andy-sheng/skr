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
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.wali.live.proto.User.GetOwnInfoRsp;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
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
     * @param sex      -1 代表不更新
     * @param birthday
     */
    public void updateInfo(String nickName, int sex, String birthday, String avatar) {

        final UserInfo userInfo = new UserInfo();
        userInfo.setUserId(UserAccountManager.getInstance().getUuidAsLong());
        HashMap<String, Object> map = new HashMap<>();
        if (nickName != null) {
            map.put("nickname", nickName);
            userInfo.setUserNickname(nickName);
        }
        if (sex != -1) {
            map.put("sex", sex);
            userInfo.setSex(sex);
        }
        if (birthday != null) {
            map.put("birthday", birthday);
            userInfo.setBirthday(birthday);
        }
        if (avatar != null) {
            map.put("avatar", avatar);
            userInfo.setAvatar(avatar);
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(map));
        MyUserInfoServerApi myUserAccountServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        Observable<ApiResult> apiResultObservable = myUserAccountServerApi.updateInfo(body);
        ApiMethods.subscribe(apiResultObservable, new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    U.getToastUtil().showShort("个人信息更新成功");
                    //写入数据库
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                            UserInfoLocalApi.insertOrUpdate(userInfo, false, false);

                            // 取得个人信息
                            UserInfo userInfo = UserInfoLocalApi.getUserInfoByUUid(UserAccountManager.getInstance().getUuidAsLong());
                            if (userInfo != null) {
                                if (mUser != null) {
                                    mUser.setUserInfo(userInfo);
                                    setMyUserInfo(mUser);
                                } else {
                                    MyUserInfo myUserInfo = new MyUserInfo();
                                    myUserInfo.setUserInfo(userInfo);
                                    setMyUserInfo(mUser);
                                }
                            }
                            emitter.onComplete();
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                }
            }
        });
    }

    public long getUid() {
        return mUser != null ? mUser.getUid() : 0;
    }

    public String getNickName() {
        return mUser != null ? mUser.getNickName() : "";
    }

    public String getAvatar() {
        if (mUser != null) {

        }
        return mUser != null ? mUser.getAvatar() : "";
    }

    public boolean isRedName() {
        return mUser != null ? mUser.getRedName() : false;
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
