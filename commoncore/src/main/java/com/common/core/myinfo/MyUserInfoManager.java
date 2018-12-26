package com.common.core.myinfo;


import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;

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
                    MyUserInfo userInfo = MyUserInfoLocalApi.getUserInfoByUUid(UserAccountManager.getInstance().getUuidAsLong());
                    if (userInfo != null) {
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
    public void updateInfo(String nickName, int sex, String birthday, String avatar, String sign, Location location) {

        HashMap<String, Object> map = new HashMap<>();
        if (nickName != null) {
            map.put("nickname", nickName);
            mUser.setUserNickname(nickName);
        }
        if (sex != -1) {
            map.put("sex", sex);
            mUser.setSex(sex);
        }
        if (birthday != null) {
            map.put("birthday", birthday);
            mUser.setBirthday(birthday);
        }
        if (avatar != null) {
            map.put("avatar", avatar);
            mUser.setAvatar(avatar);
        }
        if (sign != null) {
            map.put("signature", sign);
            mUser.setSignature(sign);
        }
        if (location != null) {
            map.put("location", JSON.toJSONString(location));
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
                            MyUserInfoLocalApi.insertOrUpdate(mUser);
                            // 取得个人信息
                            MyUserInfo userInfo = MyUserInfoLocalApi.getUserInfoByUUid(UserAccountManager.getInstance().getUuidAsLong());
                            if (userInfo != null) {
                                setMyUserInfo(mUser);
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
        return mUser != null ? mUser.getUserId() : 0;
    }

    public String getNickName() {
        return mUser != null ? mUser.getUserNickname() : "";
    }

    public String getAvatar() {
        return mUser != null ? mUser.getAvatar() : "";
    }

    public String getSignature() {
        return mUser != null ? mUser.getSignature() : "";
    }

    public int getSex() {
        return mUser != null ? mUser.getSex() : 0;
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
