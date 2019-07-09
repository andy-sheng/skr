package com.common.core.myinfo;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.LbsUtils;
import com.common.utils.U;
import com.module.ModuleServiceManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 保存个人详细信息，我的信息的管理, 其实是对User的decorate
 * Created by chengsimin on 16/7/1.
 */
public class MyUserInfoManager {

    public final static String TAG = "MyUserInfoManager";

    static final String PREF_KEY_UPDATE_LACATION_TS = "update_location_ts";

    private MyUserInfo mUser = new MyUserInfo();
    private boolean mUserInfoFromServer = false;
    private boolean needBeginnerGuide = false;
    private boolean mIsFirstLogin = false;    // 标记是否第一次登录
    //    private boolean mHasLoadFromDB = false;
    private boolean mHasGrabCertifyPassed = false;// 抢唱时的认证
    private int mRealNameVerified = 0; // 实名认证

    public void init() {
        load();
    }

    private void load() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (UserAccountManager.getInstance().hasAccount()) {
                    if (mUserInfoFromServer && mUser != null) {
                        MyLog.d(TAG, "load。 mUser 有效 来自server，取消本次");
                    } else {
                        MyUserInfo userInfo = MyUserInfoLocalApi.getUserInfoByUUid(UserAccountManager.getInstance().getUuidAsLong());
                        MyLog.d(TAG, "load myUserInfo uid =" + UserAccountManager.getInstance().getUuidAsLong());
                        MyLog.d(TAG, "load myUserInfo=" + userInfo);
                        if (userInfo != null) {
                            setMyUserInfo(userInfo, false, "load");
                        }
                        // 从服务器同步个人信息
                        syncMyInfoFromServer();
                    }

                }
//                mHasLoadFromDB = true;
//                EventBus.getDefault().post(new MyUserInfoEvent.UserInfoLoadOkEvent());
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public boolean isFirstLogin() {
        return mIsFirstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        mIsFirstLogin = firstLogin;
    }

    public boolean isNeedBeginnerGuide() {
        // TODO: 2019/5/9 先下掉新手引导入口
        return false;
    }

    public void setNeedBeginnerGuide(boolean needBeginnerGuide) {
        this.needBeginnerGuide = needBeginnerGuide;
    }

    public void logoff() {
        mUser = new MyUserInfo();
        mUserInfoFromServer = false;
    }

    public MyUserInfo getMyUserInfo() {
        return mUser;
    }

    public void setMyUserInfo(MyUserInfo myUserInfo, boolean fromServer, String from) {
        MyLog.d(TAG, "setMyUserInfo" + " myUserInfo=" + myUserInfo + " fromServer=" + fromServer + " from=" + from);
        if (myUserInfo != null) {
            mUser = myUserInfo;
            if (!mUserInfoFromServer) {
                mUserInfoFromServer = fromServer;
            }
            ModuleServiceManager.getInstance().getMsgService().updateCurrentUserInfo();
            //user信息设定成功了，发出eventbus
            EventBus.getDefault().post(new MyUserInfoEvent.UserInfoChangeEvent());
        }
    }

    /**
     * 当前的 mUser 信息是从服务器同步过的么，标记下
     *
     * @return
     */
    public boolean isUserInfoFromServer() {
        return mUserInfoFromServer;
    }

    /**
     * 从服务器同步个人信息
     */
    private void syncMyInfoFromServer() {
        MyUserInfoServerApi api = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        Call<ApiResult> apiResultCall = api.getUserInfo((int) getUid());
        if (apiResultCall != null) {
            try {
                Response<ApiResult> resultResponse = apiResultCall.execute();
                if (resultResponse != null) {
                    ApiResult obj = resultResponse.body();
                    if (obj != null) {
                        if (obj.getErrno() == 0) {
                            final UserInfoModel userInfoModel = JSON.parseObject(obj.getData().toString(), UserInfoModel.class);
                            MyUserInfo myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel);
                            MyUserInfoLocalApi.insertOrUpdate(myUserInfo);
                            setMyUserInfo(myUserInfo, true, "syncMyInfoFromServer");
                        } else if (obj.getErrno() == 107) {
                            UserAccountManager.getInstance().notifyAccountExpired();
                        }
                    } else {
                        MyLog.w(TAG, "syncMyInfoFromServer obj==null");
                    }
                }
            } catch (Exception e) {
                MyLog.d(e);
            }
        }
    }

    public void updateInfo(final MyInfoUpdateParams updateParams) {
        updateInfo(updateParams, true);
    }

    /**
     * 更新用户信息
     */
    public void updateInfo(final MyInfoUpdateParams updateParams, final boolean updateLocalIfServerFailed) {
        updateInfo(updateParams, updateLocalIfServerFailed, false, null);
    }


    /**
     * 更新用户信息
     */
    public void updateInfo(final MyInfoUpdateParams updateParams, final boolean updateLocalIfServerFailed, final boolean isCompleteInfo, final ServerCallback callback) {

        HashMap<String, Object> map = new HashMap<>();
        if (updateParams.nickName != null) {
            map.put("nickname", updateParams.nickName);
            if (updateLocalIfServerFailed) {
                mUser.setUserNickname(updateParams.nickName);
            }
        }
        if (updateParams.sex != -1) {
            map.put("sex", updateParams.sex);
            if (updateLocalIfServerFailed) {
                mUser.setSex(updateParams.sex);
            }
        }
        if (updateParams.birthday != null) {
            map.put("birthday", updateParams.birthday);
            if (updateLocalIfServerFailed) {
                mUser.setBirthday(updateParams.birthday);
            }
        }
        if (updateParams.avatar != null) {
            map.put("avatar", updateParams.avatar);
            if (updateLocalIfServerFailed) {
                mUser.setAvatar(updateParams.avatar);
            }
        }
        if (updateParams.sign != null) {
            map.put("signature", updateParams.sign);
            if (updateLocalIfServerFailed) {
                mUser.setSignature(updateParams.sign);
            }
        }
        if (updateParams.location != null) {
            map.put("location", updateParams.location);
            if (updateLocalIfServerFailed) {
                mUser.setLocation(updateParams.location);
            }
        }

        if (updateParams.ageStage != 0) {
            map.put("ageStage", updateParams.ageStage);
            if (updateLocalIfServerFailed) {
                mUser.setAgeStage(updateParams.ageStage);
            }
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(map));
        MyUserInfoServerApi myUserAccountServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        Observable<ApiResult> apiResultObservable = myUserAccountServerApi.updateInfo(body);
        ApiMethods.subscribe(apiResultObservable.retryWhen(new RxRetryAssist(2, 5, true)), new ApiObserver<ApiResult>() {
            @Override
            public void process(final ApiResult obj) {
                if (obj.getErrno() == 0) {
                    if (!updateLocalIfServerFailed) {
                        if (updateParams.nickName != null) {
                            mUser.setUserNickname(updateParams.nickName);
                        }
                        if (updateParams.sex != -1) {
                            mUser.setSex(updateParams.sex);
                        }
                        if (updateParams.birthday != null) {
                            mUser.setBirthday(updateParams.birthday);
                        }
                        if (updateParams.avatar != null) {
                            mUser.setAvatar(updateParams.avatar);
                        }
                        if (updateParams.sign != null) {
                            mUser.setSignature(updateParams.sign);
                        }
                        if (updateParams.location != null) {
                            mUser.setLocation(updateParams.location);
                        }
                        if (updateParams.ageStage != 0) {
                            mUser.setAgeStage(updateParams.ageStage);
                        }
                    }

                    if (!updateLocalIfServerFailed) {
                        UserInfoModel userInfoModel = JSON.parseObject(obj.getData().toString(), UserInfoModel.class);
                        mUser.setUserId(userInfoModel.getUserId());
                        mUser.setUserNickname(userInfoModel.getNickname());
                        mUser.setAvatar(userInfoModel.getAvatar());
                        mUser.setBirthday(userInfoModel.getBirthday());
                        mUser.setLocation(userInfoModel.getLocation());
                        mUser.setSex(userInfoModel.getSex());
                        mUser.setSignature(userInfoModel.getSignature());
                        mUser.setUserDisplayname(userInfoModel.getNickname());
                        mUser.setAgeStage(userInfoModel.getAgeStage());
                    }

                    if (callback != null) {
                        callback.onSucess();
                    }
                    //写入数据库
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                            MyUserInfoLocalApi.insertOrUpdate(mUser);
                            // 取得个人信息
                            MyUserInfo userInfo = MyUserInfoLocalApi.getUserInfoByUUid(UserAccountManager.getInstance().getUuidAsLong());
                            if (userInfo != null) {
                                setMyUserInfo(mUser, true, "updateInfo");
                            }
                            if (updateParams.location != null) {
                                // 有传地址位置
                                U.getPreferenceUtils().setSettingLong(PREF_KEY_UPDATE_LACATION_TS, System.currentTimeMillis());
                            }
                            emitter.onComplete();
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                } else {
                    U.getToastUtil().showShort(obj.getErrmsg());
                    if (callback != null) {
                        callback.onFail();
                    }
                }
            }
        });
    }

    //是否需要完善资料
    public boolean isNeedCompleteInfo() {
//        if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
//            MyLog.d(TAG, "isNeedCompleteInfo nickName is null");
//            return true;
//        }
//        if (MyUserInfoManager.getInstance().getSex() == 0) {
//            MyLog.d(TAG, "isNeedCompleteInfo sex == 0");
//            return true;
//        }
        return isFirstLogin();
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

    public int getAge() {
        if (mUser != null && !TextUtils.isEmpty(mUser.getBirthday())) {
            String[] array = mUser.getBirthday().split("-");
            if (!TextUtils.isEmpty(array[0])) {
                int year = Integer.valueOf(array[0]);
                return Calendar.getInstance().get(Calendar.YEAR) - year;
            }
        }
        return 0;
    }

    public int getAgeStage() {
        return mUser != null ? mUser.getAgeStage() : 0;
    }

    public boolean hasAgeStage() {
        return mUser != null && mUser.getAgeStage() != 0;
    }

    public String getAgeStageString() {
        if (mUser != null && mUser.getAgeStage() != 0) {
            if (mUser.getAgeStage() == 1) {
                return "小学党";
            } else if (mUser.getAgeStage() == 2) {
                return "中学党";
            } else if (mUser.getAgeStage() == 3) {
                return "大学党";
            } else if (mUser.getAgeStage() == 4) {
                return "工作党";
            }
        }
        return "";
    }

    public String getConstellation() {
        if (mUser != null && !TextUtils.isEmpty(mUser.getBirthday())) {
            String[] array = mUser.getBirthday().split("-");
            if (array != null && array.length >= 3) {
                if (!TextUtils.isEmpty(array[1]) && !TextUtils.isEmpty(array[2])) {
                    int month = Integer.valueOf(array[1]);
                    int day = Integer.valueOf(array[2]);
                    return U.getDateTimeUtils().getConstellation(month, day);
                }
            }
        }
        return "";
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
        if (!hasLocation()) {
            return "火星";
        }
        return mUser.getLocation().getDesc();
    }

    public String getLocationProvince() {
        if (!hasLocation()) {
            return "火星";
        }
        return mUser.getLocation().getProvince();
    }

    public boolean hasMyUserInfo() {
        return mUser != null && mUser.getUserId() > 0;
    }

    public boolean hasLocation() {
        return mUser.getLocation() != null && mUser.getLocation().getDesc().length() > 0;
    }

    public void trySyncLocation() {
        if (!MyUserInfoManager.getInstance().hasLocation()) {
            // 没有地理位置
            uploadLocation();
        }
        // TODO: 2019/2/8 去掉位置更新策略，除了第一次，让用户主动触发 
//        else {
//            long lastUpdateLocationTs = U.getPreferenceUtils().getSettingLong(PREF_KEY_UPDATE_LACATION_TS, 0);
//            if (System.currentTimeMillis() - lastUpdateLocationTs > 3600 * 1000 * 6) {
//                uploadLocation();
//            }
//        }
    }

    public void uploadLocation() {
        uploadLocation(null);
    }

    /**
     * 上传地理位置
     */
    public void uploadLocation(final LbsUtils.Callback callback) {
        U.getLbsUtils().getLocation(false, new LbsUtils.Callback() {
            @Override
            public void onReceive(LbsUtils.Location location) {
                MyLog.d(TAG, "onReceive" + " location=" + location);
                if (location != null && location.isValid()) {
                    Location l = new Location();
                    l.setProvince(location.getProvince());
                    l.setCity(location.getCity());
                    l.setDistrict(location.getDistrict());
                    MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager
                            .newMyInfoUpdateParamsBuilder()
                            .setLocation(l)
                            .build(), true);
                }
                if (callback != null) {
                    callback.onReceive(location);
                }
            }
        });
    }

    /**
     * 是否通过了抢唱时的认证
     *
     * @return
     */
    public boolean hasGrabCertifyPassed() {
        if (MyLog.isDebugLogOpen()) {
            return true;
        }
        if (!mHasGrabCertifyPassed) {
            mHasGrabCertifyPassed = U.getPreferenceUtils().getSettingBoolean("hasGrabCertifyPassed", false);
        }
        return mHasGrabCertifyPassed;
    }

    public void setGrabCertifyPassed(boolean mHasPassedCertify) {
        if (!mHasPassedCertify) {
            U.getPreferenceUtils().setSettingBoolean("hasGrabCertifyPassed", mHasPassedCertify);
            mHasGrabCertifyPassed = true;
        }
    }

    /**
     * 是否实名认证
     *
     * @return
     */
    public boolean isRealNameVerified() {
        if (mRealNameVerified == 0) {
            boolean v = U.getPreferenceUtils().getSettingBoolean("mRealNameVerified", false);
            if (v) {
                mRealNameVerified = 1;
            } else {
                mRealNameVerified = -1;
            }
        }
        return mRealNameVerified == 1;
    }

    public void setRealNameVerified(boolean realNameVerified) {
        if (realNameVerified) {
            U.getPreferenceUtils().setSettingBoolean("mRealNameVerified", realNameVerified);
        }
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
        int ageStage;

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

        public int getAgeStage() {
            return ageStage;
        }

        public void setAgeStage(int ageStage) {
            this.ageStage = ageStage;
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

            public Builder setAgeStage(int ageStage) {
                mParams.setAgeStage(ageStage);
                return this;
            }

            public MyInfoUpdateParams build() {
                return mParams;
            }
        }
    }

    public interface ServerCallback {
        void onSucess();

        void onFail();
    }
}
