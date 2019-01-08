package com.common.core.myinfo;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.ModuleServiceManager;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Query;

/**
 * 保存个人详细信息，我的信息的管理, 其实是对User的decorate
 * Created by chengsimin on 16/7/1.
 */
public class MyUserInfoManager {

    public final static String TAG = "MyUserInfoManager";

    private MyUserInfo mUser = new MyUserInfo();

//    private boolean mHasLoadFromDB = false;

    public void init() {
        load();
    }

    private void load() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (UserAccountManager.getInstance().hasAccount()) {
                    MyUserInfo userInfo = MyUserInfoLocalApi.getUserInfoByUUid(UserAccountManager.getInstance().getUuidAsLong());
                    MyLog.d(TAG, "load myUserInfo uid =" + UserAccountManager.getInstance().getUuidAsLong());
                    MyLog.d(TAG, "load myUserInfo=" + userInfo);
                    if (userInfo != null) {
                        setMyUserInfo(userInfo);
                    }
                    // 从服务器同步个人信息
                    syncMyInfoFromServer();
                }
//                mHasLoadFromDB = true;
//                EventBus.getDefault().post(new MyUserInfoEvent.UserInfoLoadOkEvent());
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public MyUserInfo getMyUserInfo() {
        return mUser;
    }

    public void setMyUserInfo(MyUserInfo myUserInfo) {
        MyLog.d(TAG, "setMyUserInfo" + " myUserInfo=" + myUserInfo);
        if (myUserInfo != null) {
            mUser = myUserInfo;
            ModuleServiceManager.getInstance().getMsgService().updateCurrentUserInfo();
            //user信息设定成功了，发出eventbus
            EventBus.getDefault().post(new MyUserInfoEvent.UserInfoChangeEvent());
        }
    }

    /**
     * 从服务器同步个人信息
     */
    private void syncMyInfoFromServer() {
        MyUserInfoServerApi api = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        Call<ApiResult> apiResultCall = api.getUserInfo((int) getUid());
        try {
            Response<ApiResult> resultResponse = apiResultCall.execute();
            ApiResult obj = resultResponse.body();
            if (obj != null) {
                if (obj.getErrno() == 0) {
                    final UserInfoModel userInfoModel = JSON.parseObject(obj.getData().toString(), UserInfoModel.class);
                    MyUserInfo myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel);
                    MyUserInfoLocalApi.insertOrUpdate(myUserInfo);
                    setMyUserInfo(myUserInfo);
                } else if (obj.getErrno() == 107) {
                    UserAccountManager.getInstance().notifyAccountExpired();
                }
            } else {
                MyLog.w(TAG, "syncMyInfoFromServer obj==null");
            }
        } catch (IOException e) {
            MyLog.d(e);
        }
    }

    /**
     * 更新用户信息
     */
    public void updateInfo(MyInfoUpdateParams updateParams) {

        HashMap<String, Object> map = new HashMap<>();
        if (updateParams.nickName != null) {
            map.put("nickname", updateParams.nickName);
            mUser.setUserNickname(updateParams.nickName);
        }
        if (updateParams.sex != -1) {
            map.put("sex", updateParams.sex);
            mUser.setSex(updateParams.sex);
        }
        if (updateParams.birthday != null) {
            map.put("birthday", updateParams.birthday);
            mUser.setBirthday(updateParams.birthday);
        }
        if (updateParams.avatar != null) {
            map.put("avatar", updateParams.avatar);
            mUser.setAvatar(updateParams.avatar);
        }
        if (updateParams.sign != null) {
            map.put("signature", updateParams.sign);
            mUser.setSignature(updateParams.sign);
        }
        if (updateParams.location != null) {
            map.put("location", updateParams.location);
            mUser.setLocation(updateParams.location);
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
        if (mUser != null && mUser.getUserId() != 0) {
            return mUser.getUserId();
        } else {
            return UserAccountManager.getInstance().getUuidAsLong();
        }
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

    public String getBirthday() {
        return mUser != null ? mUser.getBirthday() : "";
    }

    public Location getLocation() {
        return mUser.getLocation();
    }

    public String getLocationDesc() {
        if (mUser.getLocation() == null) {
            return "未知位置";
        }
        return mUser.getLocation().getDesc();
    }

    public boolean hasMyUserInfo() {
        return mUser != null && mUser.getUserId() > 0;
    }

//    public boolean hasLoadFromDB() {
//        return mHasLoadFromDB;
//    }

    private static class MyUserInfoManagerHolder {
        private static final MyUserInfoManager INSTANCE = new MyUserInfoManager();
    }

    private MyUserInfoManager() {

    }

    public static final MyUserInfoManager getInstance() {
        return MyUserInfoManagerHolder.INSTANCE;
    }

    public static MyInfoUpdateParams.Builder newMyInfoUpdateParamsBuilder() {
        return new MyInfoUpdateParams.Builder();
    }

    public static class MyInfoUpdateParams {
        String nickName;
        int sex = -1;
        String birthday;
        String avatar;
        String sign;
        Location location;

        private MyInfoUpdateParams() {
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public static class Builder {
            MyInfoUpdateParams mParams = new MyInfoUpdateParams();

            Builder() {
            }

            public Builder setNickName(String nickName) {
                mParams.setNickName(nickName);
                return this;
            }

            public Builder setSex(int sex) {
                mParams.setSex(sex);
                return this;
            }

            public Builder setBirthday(String birthday) {
                mParams.setBirthday(birthday);
                return this;
            }

            public Builder setAvatar(String avatar) {
                mParams.setAvatar(avatar);
                return this;
            }

            public Builder setSign(String sign) {
                mParams.setSign(sign);
                return this;
            }

            public Builder setLocation(Location location) {
                mParams.setLocation(location);
                return this;
            }

            public MyInfoUpdateParams build() {
                return mParams;
            }
        }
    }
}
