package com.common.rxretrofit.utils;

import android.text.TextUtils;

import com.common.base.db.CookieResulteDao;
import com.common.base.db.GreenDaoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.http.cookie.CookieResulte;
import com.common.utils.U;
import java.util.List;

/**
 * 数据缓存
 * 数据库工具类-geendao运用
 * Created by WZG on 2016/10/25.
 */

public class CookieDbUtil {
    private static final String TAG = "CookieDbUtil";

    private static CookieResulteDao getCookieResultDao() {
        return GreenDaoManager.getDaoSession(U.app().getApplicationContext()).getCookieResulteDao();
    }

    /**
     * 插入
     *
     * @param info
     */
    public static void saveCookie(CookieResulte info) {
        if (info == null) {
            MyLog.w(TAG, "saveCookie info == null");
            return;
        }
        getCookieResultDao().insert(info);
    }

    /**
     * 更新
     *
     * @param info
     */
    public static void updateCookie(CookieResulte info) {
        if (info == null) {
            MyLog.w(TAG, "updateCookie info == null");
            return;
        }
        getCookieResultDao().insertOrReplace(info);
    }

    /**
     * 删除
     *
     * @param info
     */
    public static void deleteCookie(CookieResulte info) {
        if (info == null) {
            MyLog.w(TAG, "deleteCookie info == null");
            return;
        }
        getCookieResultDao().delete(info);
    }

    /**
     * 清空
     */
    public static void deleteAll() {
        getCookieResultDao().deleteAll();
    }

    /**
     * 查询
     *
     * @param url
     * @return
     */
    public static CookieResulte queryCookieBy(String url) {
        if (TextUtils.isEmpty(url)){
            MyLog.w(TAG, "queryCookieBy url == null");
            return null;
        }
        return getCookieResultDao().queryBuilder().where(CookieResulteDao.Properties.Url.eq(url)).unique();
    }

    /**
     * 查询全部
     *
     * @return
     */
    public static List<CookieResulte> queryCookieAll() {
        return getCookieResultDao().queryBuilder().list();
    }
}
