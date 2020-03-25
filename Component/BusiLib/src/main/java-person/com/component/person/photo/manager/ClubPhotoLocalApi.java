package com.component.person.photo.manager;


import com.common.log.MyLog;
import com.component.busilib.db.ClubPhotoModelDBDao;
import com.component.busilib.db.GreenDaoManager;
import com.component.person.photo.model.PhotoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作本地关系数据库,提供给PhotoModel使用
 */
public class ClubPhotoLocalApi {
    public static final String TAG = "ClubPhotoLocalApi";

    private static ClubPhotoModelDBDao getPhotoDao() {
        return GreenDaoManager.getDaoSession().getClubPhotoModelDBDao();
    }

    public static void insertOrUpdate(List<PhotoModel> list) {
        MyLog.d(TAG, "insertOrUpdate" + " list=" + list);
        if (list == null || list.isEmpty()) {
            MyLog.w(TAG + " insertOrUpdate relationList == null");
            return;
        }
        List<ClubPhotoModelDB> photoDBList = new ArrayList<>();
        for (PhotoModel userInfoModel : list) {
            ClubPhotoModelDB photoDB = PhotoModel.Companion.toClubPhotoDB(userInfoModel);
            photoDBList.add(photoDB);
        }
        getPhotoDao().insertOrReplaceInTx(photoDBList);
        // 默认插入list都是从服务器直接获取,即一次初始化
        return;
    }

    /**
     * //     * 删除指定关系人
     * //     *
     * //     * @param uid
     * //     * @return
     * //
     */
    public static boolean delete(PhotoModel photoModel) {
        getPhotoDao().delete(PhotoModel.Companion.toClubPhotoDB(photoModel));
        return false;
    }

    public static List<PhotoModel> getAllPhotoFromDB() {
        List<ClubPhotoModelDB> list = getPhotoDao().queryBuilder().list();
        List<PhotoModel> rl = new ArrayList<>();
        for (ClubPhotoModelDB photoModelDB : list) {
            rl.add(PhotoModel.Companion.fromDB(photoModelDB));
        }
        return rl;
    }

    public static void deleteAll() {
        getPhotoDao().deleteAll();
    }
}
