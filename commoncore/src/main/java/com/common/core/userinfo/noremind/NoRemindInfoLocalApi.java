package com.common.core.userinfo.noremind;

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
        NoRemindInfoDB disturbedInfoDB = NoRemindInfoModel.toNoRemindInfoDB(model);
        getDisturbedDao().insertOrReplace(disturbedInfoDB);
        return 1;
    }

    public static int insertOrReplace(List<NoRemindInfoModel> models){
        MyLog.d(TAG, "deleteDisturbed" + models);

        List<NoRemindInfoDB> disturbedInfoDBS = new ArrayList<>();
        for (NoRemindInfoModel model: models){
            NoRemindInfoDB disturbedInfoDB = NoRemindInfoModel.toNoRemindInfoDB(model);
            disturbedInfoDBS.add(disturbedInfoDB);
        }

        getDisturbedDao().insertOrReplaceInTx(disturbedInfoDBS);
        return disturbedInfoDBS.size();
    }

    public static void deleteDisturbed(NoRemindInfoModel model){
        MyLog.d(TAG, "deleteDisturbed" + model);
        getDisturbedDao().deleteByKey(model.getUserId());
    }

    public static boolean isNoReminded(int userID, int msgType){
        return getDisturbedDao().queryBuilder().where(NoRemindInfoDBDao.Properties.UserId.eq(userID)).where(NoRemindInfoDBDao.Properties.MsgType.eq(msgType)).list().size() > 0;
    }

    public static void clearNoRemind(){
        MyLog.d(TAG, "clearNoRemind");

        getDisturbedDao().deleteAll();
    }
}
