package com.module.playways.room.gift;

import com.module.playways.db.GiftDBDao;
import com.module.playways.db.GreenDaoManager;

/**
 * 操作本地关系数据库,提供给GiftManager使用
 */
public class GiftLocalApi {
    //Relation中的relative  0为未关注，1为已关注 2为互关
    private static final String TAG = "GiftLocalApi";

    private static GiftDBDao getUserInfoDao() {
        return GreenDaoManager.getDaoSession().getGiftDBDao();
    }


}
