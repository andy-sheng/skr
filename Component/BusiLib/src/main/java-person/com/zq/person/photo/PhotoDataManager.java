package com.zq.person.photo;


import com.common.callback.Callback;
import com.common.log.MyLog;
import com.component.busilib.db.GreenDaoManager;
import com.component.busilib.db.PhotoModelDBDao;
import com.zq.person.model.PhotoModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 操作本地关系数据库,提供给PhotoModel使用
 */
public class PhotoDataManager {
    public static final String TAG = "PhotoLocalApi";

    public static void insertOrUpdate(final List<PhotoModel> list) {
        Observable.create(new ObservableOnSubscribe<List<PhotoModel>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PhotoModel>> emitter) throws Exception {
                PhotoLocalApi.insertOrUpdate(list);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        return;
    }

    public static void delete(final PhotoModel photo) {
        MyLog.d(TAG,"delete" + " photo=" + photo);
        Observable.create(new ObservableOnSubscribe<List<PhotoModel>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PhotoModel>> emitter) throws Exception {
                PhotoLocalApi.delete(photo);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        return;
    }

    public static void getAllPhotoFromDB(final Callback<List<PhotoModel>> listCallback) {
        Observable.create(new ObservableOnSubscribe<List<PhotoModel>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PhotoModel>> emitter) throws Exception {
                List<PhotoModel> list = PhotoLocalApi.getAllPhotoFromDB();
                emitter.onNext(list);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PhotoModel>>() {
                    @Override
                    public void accept(List<PhotoModel> photoModels) throws Exception {
                        if (listCallback != null) {
                            listCallback.onCallback(0,photoModels);
                        }
                    }
                });
    }

//    /**
//     * 插入或者更新 db数据
//     * <p>
//     * 这个函数每次插入的时候都会清空本地的数据，所以Relation这个标最好所有字段都跟服务器
//     * 保持一z致，不要有额外的其他字段，否则以后从服务器拉取了数据回来本地之前的数据会被覆盖
//     *
//     * @param userInfoList
//     * @return
//     */
//    public static int insertOrUpdate(List<UserInfoModel> userInfoList) {
//        MyLog.d(TAG, "insertOrUpdate" + " relationList.size=" + userInfoList.size());
//        if (userInfoList == null || userInfoList.isEmpty()) {
//            MyLog.w(TAG + " insertOrUpdate relationList == null");
//            return 0;
//        }
//        List<UserInfoDB> userInfoDBList = new ArrayList<>();
//        for (UserInfoModel userInfoModel : userInfoList) {
//            UserInfoDB userInfoDB = UserInfoModel.toUserInfoDB(userInfoModel);
//            userInfoDBList.add(userInfoDB);
//        }
//        getUserInfoDao().insertOrReplaceInTx(userInfoDBList);
//        // 默认插入list都是从服务器直接获取,即一次初始化
//        EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_INIT, null));
//        return userInfoList.size();
//    }
//
//    /**
//     * 插入或者更新 db数据
//     * <p>
//     * 由于relation表存储关系和关系人信息,注意默认值引起的影响
//     *
//     * @param userInfoModel
//     * @return
//     */
//    public static int insertOrUpdate(UserInfoModel userInfoModel) {
//        MyLog.d(TAG, "insertOrUpdate" + userInfoModel);
//        if (userInfoModel == null) {
//            MyLog.w(TAG, "insertOrUpdate relation == null");
//            return 0;
//        }
//
//        UserInfoModel userInfoModelInDB = getUserInfoByUUid(userInfoModel.getUserId());
//        if (userInfoModelInDB != null) {
//            UserInfoDataUtils.fill(userInfoModel, userInfoModelInDB);
//            EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_UPDATE, userInfoModel));
//        } else {
//            EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_INSERT, userInfoModel));
//        }
//        getUserInfoDao().insertOrReplaceInTx(UserInfoModel.toUserInfoDB(userInfoModel));
//        return 1;
//    }
//
//    /**
//     * 全部删除
//     *
//     * @return
//     */
//    public static void deleteAll() {
//        getUserInfoDao().deleteAll();
//    }
//
//    /**
//     * 删除指定关系人
//     *
//     * @param uid
//     * @return
//     */
//    public static boolean deleUserInfoByUUid(long uid) {
//        if (uid > 0) {
//            UserInfoModel userInfo = getUserInfoByUUid(uid);
//            if (userInfo != null) {
//                MyLog.w(TAG, "deleteUserInfo in DB");
//                getUserInfoDao().deleteByKey(uid);
//                EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_REMOVE, userInfo));
//                return true;
//            }
//            MyLog.w(TAG, "deleteRelation but not exist in DB");
//        }
//        return false;
//    }
//
//    /**
//     * 查询和某人的关系
//     *
//     * @param uid
//     * @return
//     */
//    public static UserInfoModel getUserInfoByUUid(long uid) {
//        if (uid <= 0) {
//            return null;
//        }
//        UserInfoDB userInfoDB = getUserInfoDao().queryBuilder().where(UserInfoDBDao.Properties.UserId.eq(uid)).unique();
//        if (userInfoDB == null) {
//            return null;
//        } else {
//            return UserInfoModel.parseFromDB(userInfoDB);
//        }
//    }
//
//    /**
//     * 查询某些人关系的集合
//     *
//     * @param longs
//     * @return
//     */
//    public static List<UserInfoModel> getUserInfoByUUidList(List<Long> longs) {
//        if (longs == null || longs.isEmpty()) {
//            MyLog.w(TAG, "getRelationByUUidList but uids == null");
//            return null;
//        }
//
//        List<UserInfoDB> dbList = getUserInfoDao().queryBuilder().where(
//                UserInfoDBDao.Properties.UserId.in(longs)
//        ).list();
//        List<UserInfoModel> l = new ArrayList<>();
//        for (UserInfoDB db : dbList) {
//            l.add(UserInfoModel.parseFromDB(db));
//        }
//        return l;
//    }
//
//    /**
//     * 获取好友列表
//     *
//     * @param relative 关系类别
//     * @param isBlock  是否包含黑名单
//     * @return
//     */
//    public static List<UserInfoModel> getFriendUserInfoList(int relative, boolean isBlock) {
//        List<UserInfoDB> dbList = null;
//        if (isBlock) {
//            dbList = getUserInfoDao().queryBuilder().where(
//                    UserInfoDBDao.Properties.Relative.eq(relative)
//            ).build().list();
//        } else {
//            dbList = getUserInfoDao().queryBuilder().where(
//                    UserInfoDBDao.Properties.Relative.eq(relative),
//                    UserInfoDBDao.Properties.Block.eq(false)
//            ).build().list();
//        }
//
//        List<UserInfoModel> l = new ArrayList<>();
//        for (UserInfoDB db : dbList) {
//            l.add(UserInfoModel.parseFromDB(db));
//        }
//        return l;
//    }
//
//    /**
//     * 获取黑名单
//     *
//     * @returnf
//     */
//    public static List<UserInfoModel> getBockerList() {
//        List<UserInfoDB> dbList = getUserInfoDao().queryBuilder().where(
//                UserInfoDBDao.Properties.Block.eq(true)
//        ).build().list();
//        List<UserInfoModel> l = new ArrayList<>();
//        for (UserInfoDB db : dbList) {
//            l.add(UserInfoModel.parseFromDB(db));
//        }
//        return l;
//    }

}
