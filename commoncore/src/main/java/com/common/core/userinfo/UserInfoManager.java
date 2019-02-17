package com.common.core.userinfo;

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
    public void getUserInfoByUuid(final int uuid, final ResultCallback resultCallback) {
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
                        //写入数据库
                        Observable.create(new ObservableOnSubscribe<UserInfoModel>() {
                            @Override
                            public void subscribe(ObservableEmitter<UserInfoModel> emitter) throws Exception {
                                // 写入数据库
                                UserInfoLocalApi.insertOrUpdate(jsonUserInfo, false, false);
                                UserInfoModel userInfo = UserInfoLocalApi.getUserInfoByUUid(uuid);
                                BuddyCache.getInstance().putBuddy(new BuddyCache.BuddyCacheEntry(userInfo));

                                emitter.onNext(userInfo);
                                emitter.onComplete();
                            }
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<UserInfoModel>() {
                                    @Override
                                    public void accept(UserInfoModel userInfo) throws Exception {
                                        if (resultCallback != null) {
                                            resultCallback.onGetServer(userInfo);
                                        }
                                    }
                                });
                    }
                }
            });
        }
    }

    /**
     * 处理关系
     *
     * @param userId
     * @param action
     */
    public void mateRelation(final int userId, final int action, final boolean isOldFriend) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("toUserID", userId);
        map.put("action", action);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        Observable<ApiResult> apiResultObservable = userInfoServerApi.mateRelation(body);
        ApiMethods.subscribe(apiResultObservable, new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    final boolean isFriend = obj.getData().getBoolean("isFriend");
                    final boolean isFollow = obj.getData().getBoolean("isFollow");
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

//    /**
//     * 个人信息回调接口
//     */
//    public interface UserInfoCallBack {
//        /**
//         * 本地数据库中查询个人信息
//         *
//         * @param userInfo
//         * @return
//         */
//        boolean onGetLocalDB(UserInfoModel userInfo);
//
//        /**
//         * 从服务器获取个人信息
//         *
//         * @param userInfo
//         * @return
//         */
//        boolean onGetServer(UserInfoModel userInfo);
//    }
//    /**
//     * 个人信息list回调接口
//     */
//    public interface UserListCallBack {
//        /**
//         * 本地数据库中查询个人主页信息(list)
//         */
//        boolean onGetLocalDB(List<UserInfoModel> list);
//
//        /**
//         * 本地数据库中查询个人主页信息(list)
//         */
//        boolean onGetServer(List<UserInfoModel> list);
//    }
//
//
//
//
//    /**
//     * 个人主页信息回调接口
//     */
//    public interface UserInfoPageCallBack {
//        /**
//         * 本地数据库中查询个人主页信息
//         */
//        boolean onGetLocalDB(UserInfoModel userInfo);
//
////        /**
////         * 从服务器获取个人主页信息
////         */
////        boolean onGetServer(GetHomepageResp response);
//    }
//
//    /**
//     * 获取一个用户个人主页信息
//     *
//     * @param needPullLiveInfo 是否需要拉取直播信息
//     * @uuid 用户id
//     */
//    public void getHomepageByUuid(long uuid, boolean needPullLiveInfo, UserInfoPageCallBack callBack) {
//        if (uuid <= 0 || callBack == null) {
//            MyLog.w(TAG, "getHomepageByUuid Illegal parameter");
//            return;
//        }
//
//        UserInfoModel local = UserInfoLocalApi.getUserInfoByUUid(uuid);
//        if (local == null || !callBack.onGetLocalDB(local)) {
//            GetHomepageResp response = UserInfoServerApi.getHomepageByUuid(uuid, needPullLiveInfo);
//            if (response != null && response.getRetCode() == 0) {
//                UserInfo userInfo = new UserInfo();
//                userInfo.parse(response.getPersonalInfo());
//                MyLog.w(TAG, "getHomepageByUuid userInfo = " + userInfo.toString());
//                UserInfoLocalApi.insertOrUpdate(userInfo, false, false);
//            }
//            callBack.onGetServer(response);
//        }
//    }

//    public void getUserInfoList(List<Long> uuidList, UserListCallBack callBack) {
//        if (uuidList == null || uuidList.size() <= 0 || callBack == null) {
//            MyLog.w(TAG, "getUserInfoList Illegal parameter");
//            return;
//        }
//
//        List<UserInfoModel> list = UserInfoLocalApi.getUserInfoByUUidList(uuidList);
//        boolean queryServer = false;
//        if (list == null || list.size() == 0) {
//            queryServer = true;
//        } else if (list.size() < uuidList.size()) {
//            queryServer = true;
//        }
//
////        if (queryServer || !callBack.onGetLocalDB(list)) {
////            callBack.onGetServer(getHomepageListById(uuidList));
////        }
//    }

//    /**
//     * 获取list用户的个人主页信息list
//     * (注:不要一次性拉去过多数据)
//     *
//     * @param uuidList
//     * @return
//     */
//    private MutiGetUserInfoRsp getHomepageListById(List<Long> uuidList) {
//        if (uuidList == null || uuidList.size() <= 0) {
//            MyLog.w(TAG, "getHomepageListById Illegal parameter");
//            return null;
//        }
////        MutiGetUserInfoRsp response = UserInfoServerApi.getHomepageListById(uuidList);
////        if (response != null && response.getRetCode() == 0) {
////            List<UserInfo> userInfoList = new ArrayList<>();
////            List<PersonalInfo> personalInfos = response.getPersonalInfoList();
////            if (personalInfos != null && personalInfos.size() > 0) {
////                for (PersonalInfo personalInfo : personalInfos) {
////                    UserInfo userInfo = new UserInfo();
////                    userInfo.parse(personalInfo);
////                    userInfoList.add(userInfo);
////                }
////
////                UserInfoLocalApi.insertOrUpdate(userInfoList);
////            }
////            return response;
////        }
//
//        return null;
//    }

//    /**
//     * @param followType 关注类别
//     * @param bothway    是否包含相互关注
//     * @param isBlock    是否包含黑名单用户
//     * @return
//     */
//    public List<UserInfoModel> getFriendsUserInfoFromDB(int followType, boolean bothway, boolean isBlock) {
//        List<UserInfoModel> localRelations = new ArrayList<>();
//        if (followType == BOTH_FOLLOWED && bothway) {
//            localRelations.addAll(UserInfoLocalApi.getFriendUserInfoList(BOTH_FOLLOWED, isBlock));
//        } else {
//            localRelations.addAll(UserInfoLocalApi.getFriendUserInfoList(followType, isBlock));
//            if (bothway) {
//                localRelations.addAll(UserInfoLocalApi.getFriendUserInfoList(BOTH_FOLLOWED, isBlock));
//            }
//        }
//
//        MyLog.w(TAG, "getFriendsUserInfoFromDB followType = " + followType + " userInfoList.size() = " + localRelations.size() +
//                " contain BothFollow = " + bothway);
//        return localRelations;
//    }
//
//    /**
//     * 查询关注列表(我关注的人)
//     *
//     * @param uuid
//     * @param count
//     * @param offset
//     * @param bothway
//     * @param loadByWater
//     * @return
//     */
//    public List<UserInfoModel> syncFollowingFromServer(long uuid, int count, int offset, boolean bothway, boolean loadByWater) {
//        List<UserInfoModel> userInfoList = getFollowingFromServer(uuid, count, offset, bothway, loadByWater);
//        UserInfoLocalApi.insertOrUpdate(userInfoList);
//        MyLog.w(TAG, "syncFollowingFromServer userInfoList.size() = " + userInfoList.size());
//        return userInfoList;
//    }

//    /**
//     * @param uuid
//     * @param count
//     * @param offset
//     * @param bothway
//     * @param loadByWater
//     * @return
//     */
//    private List<UserInfoModel> getFollowingFromServer(long uuid, int count, int offset, boolean bothway, boolean loadByWater) {
//        List<UserInfoModel> userInfoList = new ArrayList<>();
//        FollowingListResponse response = UserInfoServerApi.getFollowingListResponse(uuid, count, offset, bothway, loadByWater);
//        if (response != null && response.getCode() == 0) {
//            List<com.wali.live.proto.Relation.UserInfo> userInfos = response.getUsersList();
//            for (com.wali.live.proto.Relation.UserInfo userInfo : userInfos) {
//                userInfoList.add(UserInfo.loadFrom(userInfo, QUERY_FOLLOWED_LIST));
//            }
//
//            int total = response.getTotal();
//            if (userInfoList != null && userInfoList.size() > 0) {
//                if (total > 0 && offset < total) {
//                    offset += count;
//                    List<UserInfo> list = getFollowingFromServer(uuid, count, offset, bothway, loadByWater);
//                    userInfoList.addAll(list);
//                }
//            }
//        }
//        MyLog.w(TAG, "getFollowingFromServer userInfoList.size() = " + userInfoList.size());
//        return userInfoList;
//    }


//    /**
//     * 查询粉丝列表(关注我的人)
//     *
//     * @param uuid
//     * @param count
//     * @param offset
//     * @return
//     */
//    public List<UserInfoModel> syncFollowerListFromServer(long uuid, int count, int offset) {
//        List<UserInfoModel> userInfoList = getFollowerListFromServer(uuid, count, offset);
//        UserInfoLocalApi.insertOrUpdate(userInfoList);
//        MyLog.w(TAG, "syncFollowerListFromServer userInfoList.size() = " + userInfoList.size());
//        return userInfoList;
//    }

//    private List<UserInfoModel> getFollowerListFromServer(long uuid, int count, int offset) {
//        List<UserInfoModel> userInfoList = new ArrayList<>();
//        FollowerListResponse response = UserInfoServerApi.getFollowerListResponse(uuid, count, offset);
//        if (response != null && response.getCode() == 0) {
//            List<com.wali.live.proto.Relation.UserInfo> userInfos = response.getUsersList();
//            for (com.wali.live.proto.Relation.UserInfo userInfo : userInfos) {
//                userInfoList.add(UserInfo.loadFrom(userInfo, QUERY_FOLLOWER_LIST));
//            }
//
//            int total = response.getTotal();
//            if (userInfoList != null && userInfoList.size() > 0) {
//                if (total > 0 && offset < total) {
//                    offset += count;
//                    List<UserInfo> list = getFollowerListFromServer(uuid, count, offset);
//                    userInfoList.addAll(list);
//                }
//            }
//        }
//        MyLog.w(TAG, "getFollowerListFromServer userInfoList.size() = " + userInfoList.size());
//        return userInfoList;
//    }

//    /**
//     * 查询黑名单(我拉黑的)
//     *
//     * @param uuid
//     * @param count
//     * @param offset
//     * @return
//     */
//    public List<UserInfoModel> syncBockerListFromServer(long uuid, int count, int offset) {
//        List<UserInfoModel> userInfoList = getBockerListFromServer(uuid, count, offset);
//        UserInfoLocalApi.insertOrUpdate(userInfoList);
//        MyLog.w(TAG, "syncBockerListFromServer userInfoList.size() = " + userInfoList.size());
//        return userInfoList;
//    }

//    private List<UserInfoModel> getBockerListFromServer(long uuid, int count, int offset) {
//        List<UserInfoModel> userInfoList = new ArrayList<>();
//        BlockerListResponse response = UserInfoServerApi.getBlockerListResponse(uuid, count, offset);
//        if (response != null && response.getCode() == 0) {
//            List<com.wali.live.proto.Relation.UserInfo> userInfos = response.getUsersList();
//            for (com.wali.live.proto.Relation.UserInfo userInfo : userInfos) {
//                userInfoList.add(UserInfo.loadFrom(userInfo, QUERY_BLOCKER_LIST));
//            }
//            int total = response.getTotal();
//            if (userInfoList != null && userInfoList.size() > 0) {
//                if (total > 0 && offset < total) {
//                    offset += count;
//                    List<UserInfo> list = getBockerListFromServer(uuid, count, offset);
//                    userInfoList.addAll(list);
//                }
//            }
//        }
//
//        MyLog.w(TAG, "getBockerListFromServer userInfoList.size() = " + userInfoList.size());
//        return userInfoList;
//    }

//    /**
//     * 关注
//     *
//     * @param uuid
//     * @param target
//     * @param roomId 仅在房间关注主播时设置
//     */
//    public Observable<Integer> follow(final long uuid, final long target, final String roomId) {
//        return Observable.create(new ObservableOnSubscribe<Integer>() {
//            @Override
//            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//                FollowResponse followResponse = UserInfoServerApi.follow(uuid, target, roomId);
//                int retCode = -1;
//                if (followResponse != null && (retCode = followResponse.getCode()) == 0) {
//                    // todo 插入到本地数据库
//                    UserInfo userInfo = new UserInfo();
//                    userInfo.setUserId(target);
//                    if (followResponse.getIsBothway()) {
//                        userInfo.setRelative(UserInfoManager.BOTH_FOLLOWED);
//                    } else {
//                        userInfo.setRelative(UserInfoManager.MY_FOLLOWING);
//                    }
//                    UserInfoLocalApi.insertOrUpdate(userInfo, true, false);
//                }
//                emitter.onNext(retCode);
//                emitter.onComplete();
//            }
//        });
//    }

//    /**
//     * 取消关注
//     *
//     * @param uuid
//     * @param target
//     */
//    public Observable<Integer> unFollow(final long uuid, final long target) {
//        return Observable.create(new ObservableOnSubscribe<Integer>() {
//            @Override
//            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//                UnFollowResponse unFollowResponse = UserInfoServerApi.unFollow(uuid, target);
//                int retCode = -1;
//                if (unFollowResponse != null && (retCode = unFollowResponse.getCode()) == 0) {
//                    // todo 更新本地数据库,未关注,直接删除
//                    UserInfoLocalApi.deleUserInfoByUUid(target);
//                }
//                emitter.onNext(retCode);
//                emitter.onComplete();
//            }
//        });
//    }

//    /**
//     * 拉黑
//     *
//     * @param uuid
//     * @param target
//     */
//    public Observable<Integer> block(final long uuid, final long target) {
//        return Observable.create(new ObservableOnSubscribe<Integer>() {
//            @Override
//            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//                BlockResponse blockResponse = UserInfoServerApi.block(uuid, target);
//                int retCode = -1;
//                if (blockResponse != null && (retCode = blockResponse.getCode()) == 0) {
//                    // todo 插入到本地数据库
//                    UserInfo relation = new UserInfo();
//                    relation.setUserId(target);
//                    relation.setBlock(true);
//                    UserInfoLocalApi.insertOrUpdate(relation, false, true);
//                }
//                emitter.onNext(retCode);
//                emitter.onComplete();
//            }
//        });
//    }


//    /**
//     * 取消拉黑
//     *
//     * @param uuid
//     * @param target
//     */
//    public Observable<Integer> unBlock(final long uuid, final long target) {
//        return Observable.create(new ObservableOnSubscribe<Integer>() {
//            @Override
//            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//                UnBlockResponse unBlockResponse = UserInfoServerApi.unBlock(uuid, target);
//                int retCode = -1;
//                if (unBlockResponse != null && (retCode = unBlockResponse.getCode()) == 0) {
//                    UserInfo relation = new UserInfo();
//                    relation.setUserId(target);
//                    relation.setBlock(false);
//
//                    UserInfoLocalApi.insertOrUpdate(relation, false, true);
//                }
//                emitter.onNext(retCode);
//                emitter.onComplete();
//            }
//        });
//    }
}
