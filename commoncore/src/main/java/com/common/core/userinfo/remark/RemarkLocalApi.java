package com.common.core.userinfo.remark;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.RemarkDBDao;
import com.common.core.db.UserInfoDBDao;

import java.util.List;

/**
 * 操作本地关系数据库,提供给RelationManager使用
 */
public class RemarkLocalApi {

    private static RemarkDBDao getDao() {
        return GreenDaoManager.getDaoSession().getRemarkDBDao();
    }

    public static List<RemarkDB> getRemarkList() {
        return getDao().queryBuilder().list();
    }

    public static void delete(int userId) {
        getDao().deleteByKey((long) userId);
    }

    public static void insertOrUpdate(int userId, String remark) {
        RemarkDB remarkDB = new RemarkDB();
        remarkDB.setUserID((long) userId);
        remarkDB.setRemarkContent(remark);
        getDao().insertOrReplace(remarkDB);
    }


    public static void insertOrUpdate(List<RemarkDB> list) {
        getDao().insertOrReplaceInTx(list);
    }

    public static void deleUserInfoByUUids(List<Integer> delIds) {
        if (delIds.isEmpty()) {
            return;
        }
        String sql = String.format("DELETE FROM %s WHERE %s IN (", getDao().getTablename(), RemarkDBDao.Properties.UserID.columnName);
        for (int i = 0; i < delIds.size(); i++) {
            int userId = delIds.get(i);
            if (i == 0) {
                sql += userId;
            } else {
                sql += "," + userId;
            }
        }
        sql += ")";
        getDao().getDatabase().execSQL(sql);
    }

}
