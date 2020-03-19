package com.common.core.userinfo;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.NoRemindInfoDBDao;
import com.common.core.userinfo.model.NoRemindInfoModel;
import com.common.log.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息免打扰 本地数据库操作
 */
public class NoRemindInfoLocalApi {
    private static final String TAG = "DisturbedInfoLocalApi";

    private static NoRemindInfoDBDao getDisturbedDao(){
        return GreenDaoManager.getDaoSession().getNoRemindInfoDBDao();
    }

    public static int insertOrReplace(NoRemindInfoModel model){
        MyLog.d(TAG, "insertOrUpdate" + model);
        if (model == null) {
            MyLog.w(TAG, "insertOrUpdate relation == null");
            return 0;
        }
        NoRemindInfoDB disturbedInfoDB = NoRemindInfoModel.toDisturbedInfoDB(model);
        getDisturbedDao().insertOrReplace(disturbedInfoDB);
        return 1;
    }

    public static int insertOrReplace(List<Integer> models){
        List<NoRemindInfoDB> disturbedInfoDBS = new ArrayList<>();
        for (int userID: models){
            NoRemindInfoDB disturbedInfoDB = NoRemindInfoModel.toDisturbedInfoDB(userID);
            disturbedInfoDBS.add(disturbedInfoDB);
        }

        getDisturbedDao().insertOrReplaceInTx(disturbedInfoDBS);
        return disturbedInfoDBS.size();
    }

    public static void deleteDisturbed(NoRemindInfoModel model){
        getDisturbedDao().deleteByKey(model.getUserId());
    }

    public static boolean isNoReminded(int userID){
        return getDisturbedDao().queryBuilder().where(NoRemindInfoDBDao.Properties.UserId.eq(userID)).list().size() > 0;
    }

    public static void clearNoRemind(){
        getDisturbedDao().deleteAll();
    }
}
