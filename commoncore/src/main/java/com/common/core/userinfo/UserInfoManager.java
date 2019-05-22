package com.common.core.userinfo;

import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.OnlineModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.remark.RemarkDB;
import com.common.core.userinfo.remark.RemarkLocalApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class UserInfoManager {
    public final static String TAG = "UserInfoManager";
    static final String PREF_KEY_FOLLOW_MARKER_WATER = "follow_marker_water";
    static final String PREF_KEY_HAS_PULL_REMARK = "remark_marker_water";

    /**
     * 黑名单逻辑
     */
    // 从存储的角度来说双方关注的人即会存在我关注的人之中，也会存在粉丝之中，存数据库时做个区分

    public static final int RELATION_BLACKLIST = 5;

    public enum RELATION {
        NO_RELATION(0),           //未知
        FOLLOW(1),       //我关注的人，关注
        FANS(2),//关注我的人，粉丝
        FRIENDS(3);//双方关注，好友

        private int value;

        RELATION(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum FROM {
        DB("数据库"), SERVER_PAGE("服务器分页"), SERVER_INCREMENT("服务器增量");
        private String desc;

        FROM(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public static final int RA_UNKNOWN = 0;       //未知
    public static final int RA_BUILD = 1;         //创建关系
    public static final int RA_UNBUILD = 2;       //解除关系

    UserInfoServerApi userInfoServerApi;

    /**
     * 备注名的映射缓存住了
     */
    SparseArray<String> mRemarkMap = new SparseArray<>();

    /**
     * 在线状态缓存一份
     */
    LruCache<Integer, OnlineModel> mStatusMap = new LruCache<>(50);


    boolean hasLoadRemarkFromDB = false;

    private UserInfoManager() {
        userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        if (hasLoadRemarkFromDB) {
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    //先从数据库里取我的关注
                    List<RemarkDB> remarks1 = RemarkLocalApi.getRemarkList();
                    for (RemarkDB remarkDB : remarks1) {
                        if (!TextUtils.isEmpty(remarkDB.getRemarkContent())) {
                            mRemarkMap.put(remarkDB.getUserID().intValue(), remarkDB.getRemarkContent());
                        }
                    }
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe();
            hasLoadRemarkFromDB = true;
        }
        // 从数据库加载
        if (U.getPreferenceUtils().getSettingBoolean(PREF_KEY_HAS_PULL_REMARK, false)) {
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    syncRemarkNames();
                    emitter.onComplete();
                }
            }).subscribeOn(U.getThreadUtils().singleThreadPoll())
                    .subscribe();
        }
    }

    private static class UserAccountManagerHolder {
        private static final UserInfoManager INSTANCE = new UserInfoManager();
    }

    public static final UserInfoManager getInstance() {
        return UserAccountManagerHolder.INSTANCE;
    }

    /**
     * 泛型类，主要用于 API 中功能的回调处理,需要查询数据库的
     *
     * @param <T> 声明一个泛型 T。
     */
    public static abstract class ResultCallback<T> {

        public ResultCallback() {

        }

        /**
         * 本地数据库，成功时回调。
         *
         * @param t 已声明的类型。
         */
        public abstract boolean onGetLocalDB(T t);

        /**
         * 服务器，成功时回调。
         *
         * @param t 已声明的类型。
         */
        public abstract boolean onGetServer(T t);

    }


    /**
     * 泛型类，主要用于 API 中功能的回调处理。
     *
     * @param <T> 声明一个泛型 T。
     */
    public static abstract class ResponseCallBack<T> {


        public ResponseCallBack() {

        }

        /**
         * 服务器，成功时回调。
         *
         * @param t 已声明的类型。
         */
        public abstract void onServerSucess(T t);


        /**
         * 服务器，失败时回调。
         */
        public abstract void onServerFailed();

    }


    public static abstract class UserInfoListCallback {


        /**
         * 服务器，成功时回调。
         */
        public abstract void onSuccess(FROM from, int offset, List<UserInfoModel> list);


    }

    /**
     * 获取一个用户的信息
     *
     * @uuid 用户id
     */
    public void getUserInfoByUuid(final int uuid, final boolean isNeedRelation, final ResultCallback resultCallback) {
        if (uuid <= 0) {
            MyLog.w(TAG, "getUserInfoByUuid Illegal parameter");
            return;
        }

        UserInfoModel local = UserInfoLocalApi.getUserInfoByUUid(uuid);
        if (local == null || resultCallback == null || (resultCallback != null && !resultCallback.onGetLocalDB(local))) {
            Observable<ApiResult> apiResultObservable = userInfoServerApi.getUserInfo(uuid);
            ApiMethods.subscribe(apiResultObservable, new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    if (obj.getErrno() == 0) {
                        final UserInfoModel jsonUserInfo = JSON.parseObject(obj.getData().toString(), UserInfoModel.class);
                        //写入数据库,
                        insertUpdateDBAndCache(jsonUserInfo);
                        if (isNeedRelation) {
                            ApiMethods.subscribe(userInfoServerApi.getRelation(uuid), new ApiObserver<ApiResult>() {
                                @Override
                                public void process(ApiResult obj) {
                                    if (obj.getErrno() == 0) {
                                        boolean isFriend = obj.getData().getBooleanValue("isFriend");
                                        boolean isFollow = obj.getData().getBooleanValue("isFollow");
                                        jsonUserInfo.setFollow(isFollow);
                                        jsonUserInfo.setFriend(isFriend);
                                        if (resultCallback != null) {
                                            resultCallback.onGetServer(jsonUserInfo);
                                        }
                                    } else {
                                        if (resultCallback != null) {
                                            resultCallback.onGetServer(jsonUserInfo);
                                        }
                                    }
                                }
                            });
                        } else {
                            if (resultCallback != null) {
                                resultCallback.onGetServer(jsonUserInfo);
                            }
                        }

                    }
                }
            });
        }
    }

    private void insertUpdateDBAndCache(final UserInfoModel userInfoModel) {
        Observable.create(new ObservableOnSubscribe<UserInfoModel>() {
            @Override
            public void subscribe(ObservableEmitter<UserInfoModel> emitter) throws Exception {
                // 写入数据库
                UserInfoLocalApi.insertOrUpdate(userInfoModel);

                if (userInfoModel != null) {
                    emitter.onNext(userInfoModel);
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void mateRelation(final int userId, final int action, final boolean isOldFriend) {
        mateRelation(userId, action, isOldFriend, null);
    }

    /**
     * 处理关系(不带房间id)
     *
     * @param userId
     * @param action
     */
    public void mateRelation(final int userId, final int action, final boolean isOldFriend, final ResponseCallBack responseCallBack) {
        mateRelation(userId, action, isOldFriend, 0, responseCallBack);
    }


    /**
     * 处理关系(带房间id)
     *
     * @param userId
     * @param action
     */
    public void mateRelation(final int userId, final int action, final boolean isOldFriend, final int roomID, final ResponseCallBack responseCallBack) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("toUserID", userId);
        map.put("action", action);
        if (roomID != 0) {
            map.put("roomID", roomID);
        }

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        Observable<ApiResult> apiResultObservable = userInfoServerApi.mateRelation(body);
        ApiMethods.subscribe(apiResultObservable, new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    final boolean isFriend = obj.getData().getBooleanValue("isFriend");
                    final boolean isFollow = obj.getData().getBooleanValue("isFollow");
                    if (responseCallBack != null) {
                        responseCallBack.onServerSucess(isFriend);
                    }
                    if (action == RA_BUILD) {
                        if (isOldFriend) {
                            EventBus.getDefault().post(new RelationChangeEvent(RelationChangeEvent.FOLLOW_TYPE, userId, true, isFriend, isFollow));
                        } else {
                            EventBus.getDefault().post(new RelationChangeEvent(RelationChangeEvent.FOLLOW_TYPE, userId, false, isFriend, isFollow));
                        }
                    } else if (action == RA_UNBUILD) {
                        if (isOldFriend) {
                            EventBus.getDefault().post(new RelationChangeEvent(RelationChangeEvent.UNFOLLOW_TYPE, userId, true, isFriend, isFollow));
                        } else {
                            EventBus.getDefault().post(new RelationChangeEvent(RelationChangeEvent.UNFOLLOW_TYPE, userId, false, isFriend, isFollow));
                        }
                    }
                } else {
                    U.getToastUtil().showShort("关系请求处理出错");
                }
            }
        });
    }

    /**
     * 成为好友
     *
     * @param userId
     */
    public void beFriend(int userId, final ResponseCallBack responseCallBack) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("toUserID", userId);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        Observable<ApiResult> apiResultObservable = userInfoServerApi.beFriend(body);
        ApiMethods.subscribe(apiResultObservable, new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    if (responseCallBack != null) {
                        responseCallBack.onServerSucess(null);
                    } else {
                        U.getToastUtil().showShort("添加好友成功");
                    }
                } else {
                    if (responseCallBack != null) {
                        responseCallBack.onServerFailed();
                    } else {
                        U.getToastUtil().showShort("" + obj.getErrmsg());
                    }
                }
            }
        });
    }

    /**
     * 获取我的关注
     * 注意返回的不在主线程
     */
    /**
     * @param pullOnlineStatus     是否拉取在线状态
     * @param userInfoListCallback
     */
    public void getMyFollow(final boolean pullOnlineStatus, final UserInfoListCallback userInfoListCallback) {
        Observable.create(new ObservableOnSubscribe<List<UserInfoModel>>() {
            @Override
            public void subscribe(ObservableEmitter<List<UserInfoModel>> emitter) {
                //先从数据库里取我的关注
                LinkedHashSet<UserInfoModel> resutlSet = new LinkedHashSet();
                List<UserInfoModel> userInfoModels = UserInfoLocalApi.getFollowUserInfoList();
                resutlSet.addAll(userInfoModels);

                long followMarkerWater = U.getPreferenceUtils().getSettingLong(PREF_KEY_FOLLOW_MARKER_WATER, -1);
                if (followMarkerWater == -1) {
                    // 全量分页拉
                    int offset = 0;
                    int baohu = 0;
                    while (baohu < 100) {
                        baohu++;
                        Call<ApiResult> call = userInfoServerApi.listFollowsByPage(offset, 50);
                        try {
                            Response<ApiResult> response = call.execute();
                            ApiResult obj = response.body();
                            if (obj == null || obj.getData() == null || obj.getErrno() != 0) {
                                break;
                            }
                            if (offset == 0) {
                                // offset为0是记录水位
                                if (obj.getData() != null) {
                                    followMarkerWater = obj.getData().getLongValue("lastIndexID");
                                }
                                // 同步下备注名
                                syncRemarkNames();
                            }
                            offset = obj.getData().getIntValue("offset");
                            List<UserInfoModel> userInfoModels2 = JSON.parseArray(obj.getData().getString("contacts"), UserInfoModel.class);
                            if (userInfoModels2 == null || userInfoModels2.isEmpty()) {
                                // 水位持久化
                                U.getPreferenceUtils().setSettingLong(PREF_KEY_FOLLOW_MARKER_WATER, followMarkerWater);
                                break;
                            } else {
                                // 存到数据库
                                UserInfoLocalApi.insertOrUpdate(userInfoModels2);
                                // 应该返回给上层
                                resutlSet.addAll(userInfoModels2);
                            }
                        } catch (IOException e) {
                            MyLog.e(e);
                            break;
                        }
                    }
                } else {
                    // 增量拉取
                    Call<ApiResult> call = userInfoServerApi.listFollowsByIndexId((int) followMarkerWater);
                    try {
                        Response<ApiResult> response = call.execute();
                        ApiResult obj = response.body();
                        if (obj != null && obj.getData() != null && obj.getErrno() == 0) {
                            List<UserInfoModel> l3 = new ArrayList<>();
                            List<UserInfoModel> userInfoModels2 = JSON.parseArray(obj.getData().getString("adds"), UserInfoModel.class);
                            List<UserInfoModel> userInfoModels3 = JSON.parseArray(obj.getData().getString("updates"), UserInfoModel.class);
                            if (userInfoModels2 != null) {
                                l3.addAll(userInfoModels2);
                            }
                            if (userInfoModels3 != null) {
                                l3.addAll(userInfoModels3);
                            }
                            boolean hasUpdate = false;
                            if (!l3.isEmpty()) {
                                UserInfoLocalApi.insertOrUpdate(l3);
                                // 更新最终表
                                resutlSet.addAll(l3);
                                hasUpdate = true;
                            }
                            List<Integer> delIds = JSON.parseArray(obj.getData().getString("dels"), Integer.class);
                            if (delIds != null && !delIds.isEmpty()) {
                                //批量删除
                                UserInfoLocalApi.deleUserInfoByUUids(delIds);
                                for (Integer userId : delIds) {
                                    resutlSet.remove(new UserInfoModel(userId));
                                }
                                hasUpdate = true;
                            }
                            followMarkerWater = obj.getData().getLongValue("lastIndexID");
                            U.getPreferenceUtils().setSettingLong(PREF_KEY_FOLLOW_MARKER_WATER, followMarkerWater);
                        }
                    } catch (IOException e) {
                        MyLog.e(e);
                    }
                }
                List<UserInfoModel> resultList = new ArrayList<>();
                for (UserInfoModel userInfoModel : resutlSet) {
                    resultList.add(userInfoModel);
                }
                if (pullOnlineStatus) {
                    checkUserOnlineStatus(resultList);
                }
                if (userInfoListCallback != null) {
                    userInfoListCallback.onSuccess(FROM.DB, resultList.size(), resultList);
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(U.getThreadUtils().singleThreadPoll())
                .subscribe();

    }

    /**
     * 获取我的好友
     */
    public void getMyFriends(final boolean pullOnlineStatus, final UserInfoListCallback userInfoListCallback) {
        //先从数据库里取我的关注
        getMyFollow(false, new UserInfoListCallback() {
            @Override
            public void onSuccess(FROM from, int offset, List<UserInfoModel> list) {
                List<UserInfoModel> resultList = new ArrayList<>();
                for (UserInfoModel userInfoModel : list) {
                    if (userInfoModel.isFriend()) {
                        resultList.add(userInfoModel);
                    }
                }
                if (pullOnlineStatus) {
                    checkUserOnlineStatus(resultList);
                }
                if (userInfoListCallback != null) {
                    userInfoListCallback.onSuccess(from, resultList.size(), resultList);
                }
            }
        });
    }

    /**
     * 搜索我的关注
     *
     * @param userInfoListCallback
     */
    public void searchFollow(final String key, final UserInfoListCallback userInfoListCallback) {
        Observable.create(new ObservableOnSubscribe<List<UserInfoModel>>() {
            @Override
            public void subscribe(ObservableEmitter<List<UserInfoModel>> emitter) throws Exception {
                List<UserInfoModel> userInfoModels = UserInfoLocalApi.searchFollow(key);
                emitter.onNext(userInfoModels);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<UserInfoModel>>() {
                    @Override
                    public void accept(List<UserInfoModel> l) throws Exception {
                        if (userInfoListCallback != null) {
                            userInfoListCallback.onSuccess(FROM.DB, -1, l);
                        }
                    }
                });
    }

    /**
     * 获取备注名
     *
     * @param uid
     * @param defualtName 没有备注名时的缺省 name
     * @return
     */
    public String getRemarkName(int uid, String defualtName) {
        // 数据库加载备注
        String remark = mRemarkMap.get(uid);
        if (TextUtils.isEmpty(remark)) {
            return defualtName;
        } else {
            return remark;
        }
    }


    /**
     * 同步备注名
     */
    public void syncRemarkNames() {
        if (U.getPreferenceUtils().getSettingBoolean(PREF_KEY_HAS_PULL_REMARK, false)) {
            return;
        }
        /**
         * 获取我的关注
         * 注意返回的不在主线程
         */
        // 全量分页拉
        int offset = 0;
        int baohu = 0;
        while (baohu < 20) {
            baohu++;
            Call<ApiResult> call = userInfoServerApi.listRemarkByPage(offset, 50);
            try {
                Response<ApiResult> response = call.execute();
                ApiResult obj = response.body();
                if (obj == null || obj.getData() == null || obj.getErrno() != 0) {
                    break;
                }
                offset = obj.getData().getIntValue("offset");
                List<RemarkDB> remarks2 = JSON.parseArray(obj.getData().getString("info"), RemarkDB.class);
                if (remarks2 == null || remarks2.isEmpty()) {
                    U.getPreferenceUtils().setSettingBoolean(PREF_KEY_HAS_PULL_REMARK, true);
                    // 水位持久化
                    break;
                } else {
                    // 存到数据库
                    RemarkLocalApi.insertOrUpdate(remarks2);
                    // 修改关系数据库

                    // 存入缓存
                    for (RemarkDB remarkDB : remarks2) {
                        if (!TextUtils.isEmpty(remarkDB.getRemarkContent())) {
                            mRemarkMap.put(remarkDB.getUserID().intValue(), remarkDB.getRemarkContent());
                        }
                    }
                }
            } catch (IOException e) {
                MyLog.e(e);
                break;
            }
        }
    }


    /**
     * 拉取粉丝
     *
     * @param offset
     * @param cnt
     * @param userInfoListCallback
     */
    public void getFans(final int offset, final int cnt, final UserInfoListCallback userInfoListCallback) {
        ApiMethods.subscribe(userInfoServerApi.listFansByPage(offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                List<UserInfoModel> list = JSON.parseArray(obj.getData().getString("fans"), UserInfoModel.class);
                int newOffset = obj.getData().getIntValue("offset");
                if (userInfoListCallback != null) {
                    userInfoListCallback.onSuccess(FROM.SERVER_PAGE, newOffset, list);
                }
            }
        });

    }

    /**
     * 更新备注名
     *
     * @param remark
     * @param userId
     */
    public void updateRemark(String remark, int userId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("remarkContent", remark);
        map.put("remarkUserID", userId);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        // 修改备注
        ApiMethods.subscribe(userInfoServerApi.writeUserRemark(body), null);
        // 本地缓存更新，无论本地失败与否
        if (TextUtils.isEmpty(remark)) {
            mRemarkMap.remove(userId);
            //写入备注数据库
            RemarkLocalApi.delete(userId);
            //关系数据库
            UserInfoLocalApi.updateRemark(userId, "");
        } else {
            mRemarkMap.put(userId, remark);
            //写入备注数据库
            RemarkLocalApi.insertOrUpdate(userId, remark);
            //关系数据库
            UserInfoLocalApi.updateRemark(userId, remark);
        }
    }

    public void checkUserOnlineStatus(final List<UserInfoModel> list) {

        final HashSet<Integer> idSets = new HashSet();
        for (UserInfoModel userInfoModel : list) {
            OnlineModel onlineModel = mStatusMap.get(userInfoModel.getUserId());
            if (onlineModel == null) {
                idSets.add(userInfoModel.getUserId());
            } else {
                long t = System.currentTimeMillis() - onlineModel.getRecordTs();
                if (Math.abs(t) < 30 * 1000) {
                    // 认为状态缓存有效，不去这个id的状态了
                    if (onlineModel.isOnline()) {
                        userInfoModel.setStatus(UserInfoModel.EF_OnLine);
                        userInfoModel.setStatusDesc("在线");
                    } else {
                        userInfoModel.setStatus(UserInfoModel.EF_OffLine);
                        String timeDesc = "";
                        if (onlineModel.getOfflineTime() > 0) {
                            timeDesc = U.getDateTimeUtils().formatHumanableDate(onlineModel.getOfflineTime(), System.currentTimeMillis());
                        }
                        // 显示
                        userInfoModel.setStatusDesc("离线 " + timeDesc);
                    }
                } else {
                    idSets.add(userInfoModel.getUserId());
                }
            }
        }
        if (!idSets.isEmpty()) {
            checkUserOnlineStatusByIds(idSets)
                    .map(new Function<HashMap<Integer, OnlineModel>, List<UserInfoModel>>() {
                        @Override
                        public List<UserInfoModel> apply(HashMap<Integer, OnlineModel> map) {
                            for (UserInfoModel userInfoModel : list) {
                                if (idSets.contains(userInfoModel.getUserId())) {
                                    OnlineModel onlineModel = map.get(userInfoModel.getUserId());
                                    if (onlineModel != null) {
                                        if (onlineModel.isOnline()) {
                                            userInfoModel.setStatus(UserInfoModel.EF_OnLine);
                                            userInfoModel.setStatusDesc("在线");
                                        } else {
                                            userInfoModel.setStatus(UserInfoModel.EF_OffLine);
                                            String timeDesc = "";
                                            if (onlineModel.getOfflineTime() > 0) {
                                                timeDesc = U.getDateTimeUtils().formatHumanableDate(onlineModel.getOfflineTime(), System.currentTimeMillis());
                                            }
                                            // 显示
                                            userInfoModel.setStatusDesc("离线 " + timeDesc);
                                        }
                                    } else {
                                        userInfoModel.setStatus(UserInfoModel.EF_OffLine);
                                    }
                                }
                            }

                            return list;
                        }
                    })
                    .subscribe();
        }
        Collections.sort(list, new Comparator<UserInfoModel>() {
            @Override
            public int compare(UserInfoModel o1, UserInfoModel o2) {
                int r = o1.getStatus() - o2.getStatus();
                if (r == 0) {
                    return o1.getUserId() - o2.getUserId();
                } else {
                    return r;
                }
            }
        });
    }

    public Observable<HashMap<Integer, OnlineModel>> checkUserOnlineStatusByIds(Collection<Integer> list) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("userIDs", list);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        return userInfoServerApi.checkUserOnlineStatus(body)
                .map(new Function<ApiResult, HashMap<Integer, OnlineModel>>() {
                    @Override
                    public HashMap<Integer, OnlineModel> apply(ApiResult obj) {
                        if (obj != null && obj.getData() != null && obj.getErrno() == 0) {
                            HashMap<Integer, OnlineModel> hashSet = new HashMap<>();

                            List<OnlineModel> onlineModelList = JSON.parseArray(obj.getData().getString("userOnlineList"), OnlineModel.class);
                            if (onlineModelList != null) {
                                for (OnlineModel onlineModel : onlineModelList) {
                                    onlineModel.setRecordTs(System.currentTimeMillis());
                                    hashSet.put(onlineModel.getUserID(), onlineModel);
                                    mStatusMap.put(onlineModel.getUserID(), onlineModel);
                                }
                            }
                            List<OnlineModel> offlineModelList = JSON.parseArray(obj.getData().getString("userOfflineList"), OnlineModel.class);
                            if (offlineModelList != null) {
                                for (OnlineModel offlineModel : offlineModelList) {
                                    offlineModel.setRecordTs(System.currentTimeMillis());
                                    hashSet.put(offlineModel.getUserID(), offlineModel);
                                    mStatusMap.put(offlineModel.getUserID(), offlineModel);
                                }
                            }
                            return hashSet;
                        }
                        return null;
                    }
                });
    }

}
