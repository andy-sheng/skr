package com.common.core.userinfo;

import android.net.Uri;

import com.common.log.MyLog;

import java.util.ArrayList;
import java.util.List;

public class UserInfoManager {

    public static final int NO_RELATION = 1; //双方未关注
    public static final int MY_FOLLOWING = 2; //我关注
    public static final int MY_FOLLOWER = 3; //对方关注我 (等同于我未关注他)
    public static final int BOTH_FOLLOWED = 4; //双方互相关注

    protected static final int QUERY_FOLLOWED_LIST = 1; //查询关注列表
    protected static final int QUERY_FOLLOWER_LIST = 2; //查询粉丝列表
    protected static final int QUERY_BLOCKER_LIST = 3;

    private static final String TAG = UserInfoManager.class.getSimpleName();

    private static class UserAccountManagerHolder {
        private static final UserInfoManager INSTANCE = new UserInfoManager();
    }

    public static final UserInfoManager getInstance() {
        return UserAccountManagerHolder.INSTANCE;
    }

    /**
     * 个人信息回调接口
     */
    public interface UserInfoCallBack {
        /**
         * 本地数据库中查询个人信息
         *
         * @param userInfo
         * @return
         */
        boolean onGetLocalDB(UserInfo userInfo);

//        /**
//         * 从服务器获取个人信息
//         *
//         * @param response
//         * @return
//         */
//        boolean onGetServer(GetUserInfoByIdRsp response);
    }

    /**
     * 获取一个用户的信息
     *
     * @uuid 用户id
     */
    public void getUserInfoByUuid(final long uuid, UserInfoCallBack userInfoCallBack) {
        if (uuid <= 0 || userInfoCallBack == null) {
            MyLog.w(TAG, "getUserInfoByUuid Illegal parameter");
            return;
        }

        UserInfo local = UserInfoLocalApi.getUserInfoByUUid(uuid);
        if (local == null || !userInfoCallBack.onGetLocalDB(local)) {
//            GetUserInfoByIdRsp response = UserInfoServerApi.getUserInfoByUuid(uuid);
//            if (response != null && response.getErrorCode() == 0) {
//                UserInfo userInfo = new UserInfo();
//                userInfo.parse(response.getPersonalInfo());
//                MyLog.w(TAG, "getUserInfoByUuid userInfo = " + userInfo.toString());
//                UserInfoLocalApi.insertOrUpdate(userInfo, false, false);
//            }
//            userInfoCallBack.onGetServer(response);
        }
    }

    /**
     * 个人主页信息回调接口
     */
    public interface UserInfoPageCallBack {
        /**
         * 本地数据库中查询个人主页信息
         */
        boolean onGetLocalDB(UserInfo userInfo);

//        /**
//         * 从服务器获取个人主页信息
//         */
//        boolean onGetServer(GetHomepageResp response);
    }

    /**
     * 获取一个用户个人主页信息
     *
     * @param needPullLiveInfo 是否需要拉取直播信息
     * @uuid 用户id
     */
    public void getHomepageByUuid(long uuid, boolean needPullLiveInfo, UserInfoPageCallBack callBack) {
        if (uuid <= 0 || callBack == null) {
            MyLog.w(TAG, "getHomepageByUuid Illegal parameter");
            return;
        }

        UserInfo local = UserInfoLocalApi.getUserInfoByUUid(uuid);
        if (local == null || !callBack.onGetLocalDB(local)) {
//            GetHomepageResp response = UserInfoServerApi.getHomepageByUuid(uuid, needPullLiveInfo);
//            if (response != null && response.getRetCode() == 0) {
//                UserInfo userInfo = new UserInfo();
//                userInfo.parse(response.getPersonalInfo());
//                MyLog.w(TAG, "getHomepageByUuid userInfo = " + userInfo.toString());
//                UserInfoLocalApi.insertOrUpdate(userInfo, false, false);
//            }
//            callBack.onGetServer(response);
        }
    }

    /**
     * 个人信息list回调接口
     */
    public interface UserListCallBack {
        /**
         * 本地数据库中查询个人主页信息(list)
         */
        boolean onGetLocalDB(List<UserInfo> list);

//        /**
//         * 本地数据库中查询个人主页信息(list)
//         */
//        boolean onGetServer(MutiGetUserInfoRsp rsp);
    }

    public void getUserInfoList(List<Long> uuidList, UserListCallBack callBack) {
        if (uuidList == null || uuidList.size() <= 0 || callBack == null) {
            MyLog.w(TAG, "getUserInfoList Illegal parameter");
            return;
        }

        List<UserInfo> list = UserInfoLocalApi.getUserInfoByUUidList(uuidList);
        boolean queryServer = false;
        if (list == null || list.size() == 0) {
            queryServer = true;
        } else if (list.size() < uuidList.size()) {
            queryServer = true;
        }

//        if (queryServer || !callBack.onGetLocalDB(list)) {
//            callBack.onGetServer(getHomepageListById(uuidList));
//        }
    }

    /**
     * 获取list用户的个人主页信息list
     * (注:不要一次性拉去过多数据)
     *
     * @param uuidList
     * @return
     */
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

    /**
     * @param followType 关注类别
     * @param bothway    是否包含相互关注
     * @param isBlock    是否包含黑名单用户
     * @return
     */
    public List<UserInfo> getFriendsUserInfoFromDB(int followType, boolean bothway, boolean isBlock) {
        List<UserInfo> localRelations = new ArrayList<>();
        if (followType == BOTH_FOLLOWED && bothway) {
            localRelations.addAll(UserInfoLocalApi.getFriendUserInfoList(BOTH_FOLLOWED, isBlock));
        } else {
            localRelations.addAll(UserInfoLocalApi.getFriendUserInfoList(followType, isBlock));
            if (bothway) {
                localRelations.addAll(UserInfoLocalApi.getFriendUserInfoList(BOTH_FOLLOWED, isBlock));
            }
        }

        MyLog.w(TAG, "getFriendsUserInfoFromDB followType = " + followType + " userInfoList.size() = " + localRelations.size() +
                " contain BothFollow = " + bothway);
        return localRelations;
    }

    /**
     * 查询关注列表(我关注的人)
     *
     * @param uuid
     * @param count
     * @param offset
     * @param bothway
     * @param loadByWater
     * @return
     */
    public List<UserInfo> syncFollowingFromServer(long uuid, int count, int offset, boolean bothway, boolean loadByWater) {
        List<UserInfo> userInfoList = getFollowingFromServer(uuid, count, offset, bothway, loadByWater);
        UserInfoLocalApi.insertOrUpdate(userInfoList);
        MyLog.w(TAG, "syncFollowingFromServer userInfoList.size() = " + userInfoList.size());
        return userInfoList;
    }

    /**
     * @param uuid
     * @param count
     * @param offset
     * @param bothway
     * @param loadByWater
     * @return
     */
    private List<UserInfo> getFollowingFromServer(long uuid, int count, int offset, boolean bothway, boolean loadByWater) {
        List<UserInfo> userInfoList = new ArrayList<>();
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
        return userInfoList;
    }


    /**
     * 查询粉丝列表(关注我的人)
     *
     * @param uuid
     * @param count
     * @param offset
     * @return
     */
    public List<UserInfo> syncFollowerListFromServer(long uuid, int count, int offset) {
        List<UserInfo> userInfoList = getFollowerListFromServer(uuid, count, offset);
        UserInfoLocalApi.insertOrUpdate(userInfoList);
        MyLog.w(TAG, "syncFollowerListFromServer userInfoList.size() = " + userInfoList.size());
        return userInfoList;
    }

    private List<UserInfo> getFollowerListFromServer(long uuid, int count, int offset) {
        List<UserInfo> userInfoList = new ArrayList<>();
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
        return userInfoList;
    }

    /**
     * 查询黑名单(我拉黑的)
     *
     * @param uuid
     * @param count
     * @param offset
     * @return
     */
    public List<UserInfo> syncBockerListFromServer(long uuid, int count, int offset) {
        List<UserInfo> userInfoList = getBockerListFromServer(uuid, count, offset);
        UserInfoLocalApi.insertOrUpdate(userInfoList);
        MyLog.w(TAG, "syncBockerListFromServer userInfoList.size() = " + userInfoList.size());
        return userInfoList;
    }

    private List<UserInfo> getBockerListFromServer(long uuid, int count, int offset) {
        List<UserInfo> userInfoList = new ArrayList<>();
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
        return userInfoList;
    }

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

    /**
     * 泛型类，主要用于 API 中功能的回调处理。
     *
     * @param <T> 声明一个泛型 T。
     */
    public static abstract class ResultCallback<T> {

        public static class Result<T> {
            public T t;
        }

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

    public void getFriends(ResultCallback resultCallback) {
        // 拿到本地存储所有好友关系
        // 根据返回值判断是否去服务器查询
        // todo 仅测试
        Uri testUri = Uri.parse("http://cms-bucket.nosdn.127.net/a2482c0b2b984dc88a479e6b7438da6020161219074944.jpeg");

        UserInfo friend1 = new UserInfo();
        friend1.setUserId(1001);
        friend1.setUserNickname("帅哥");

        UserInfo friend2 = new UserInfo();
        friend2.setUserId(1002);
        friend2.setUserNickname("美女");
        
        List<UserInfo> list = new ArrayList<>();
        list.add(friend1);
        list.add(friend2);
        if (resultCallback != null) {
            resultCallback.onGetLocalDB(list);
        }
    }
}
