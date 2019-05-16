package com.common.core.userinfo;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.cache.BuddyCache;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.zq.live.proto.Common.UserInfo;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class UserInfoManager {

    // 从存储的角度来说双方关注的人即会存在我关注的人之中，也会存在粉丝之中，存数据库时做个区分
    public static final int NO_RELATION = 0;           //未知
    public static final int RELATION_FOLLOW = 1;       //我关注的人，关注
    public static final int RELATION_FANS = 2;         //关注我的人，粉丝
    public static final int RELATION_FRIENDS = 3;      //双方关注，好友
    public static final int RELATION_BLACKLIST = 4;    //黑名单

    public static final int RA_UNKNOWN = 0;       //未知
    public static final int RA_BUILD = 1;         //创建关系
    public static final int RA_UNBUILD = 2;       //解除关系

    private static final String TAG = UserInfoManager.class.getSimpleName();
    UserInfoServerApi userInfoServerApi;

    private UserInfoManager() {
        userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
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
                    BuddyCache.getInstance().putBuddy(new BuddyCache.BuddyCacheEntry(userInfoModel));
                }

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
     * 获取关系列表
     *
     * @param relation
     * @param offset
     * @param limit
     * @param responseCallBack
     */
    public void getRelationList(final int relation, int offset, final int limit, final ResponseCallBack responseCallBack) {
        if (relation == 0) {
            MyLog.w(TAG, "getRelationList Illegal parameter");
            return;
        }

        final WeakReference<ResponseCallBack> responseCallBackWeakReference = new WeakReference<>(responseCallBack);

        Observable<ApiResult> apiResultObservable = userInfoServerApi.getRelationList(relation, offset, limit);
        ApiMethods.subscribe(apiResultObservable, new ApiObserver<ApiResult>() {
            @Override
            public void process(final ApiResult obj) {
                if (obj.getErrno() == 0) {
                    //写入数据库
                    Observable.create(new ObservableOnSubscribe<ApiResult>() {
                        @Override
                        public void subscribe(ObservableEmitter<ApiResult> emitter) throws Exception {
                            // 写入数据库
                            List<UserInfoModel> userInfoModels = JSON.parseArray(obj.getData().getString("users"), UserInfoModel.class);
                            if (userInfoModels != null && userInfoModels.size() > 0) {
                                UserInfoLocalApi.insertOrUpdate(userInfoModels);

                                List<BuddyCache.BuddyCacheEntry> list = new ArrayList<>();
                                for (UserInfoModel userInfoModel : userInfoModels) {
                                    list.add(new BuddyCache.BuddyCacheEntry(userInfoModel));
                                }
                                BuddyCache.getInstance().putBuddyList(list);
                            }
                            emitter.onNext(obj);
                            emitter.onComplete();
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<ApiResult>() {
                                @Override
                                public void accept(ApiResult userInfo) throws Exception {
                                    if (responseCallBackWeakReference.get() != null) {
                                        responseCallBackWeakReference.get().onServerSucess(obj);
                                    }
                                }
                            });
                } else {
                    if (responseCallBackWeakReference.get() != null) {
                        responseCallBackWeakReference.get().onServerFailed();
                    }
                }

            }
        });
    }

}
