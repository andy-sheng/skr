package com.module.playways.room.gift;

import com.module.playways.db.GiftDBDao;
import com.module.playways.db.GreenDaoManager;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.model.GiftServerModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作本地关系数据库,提供给GiftManager使用
 */
public class GiftLocalApi {
    private static final String TAG = "GiftLocalApi";

    private static GiftDBDao getGiftDBDao() {
        return GreenDaoManager.getDaoSession().getGiftDBDao();
    }

    public static void insertAll(List<GiftServerModel> giftServerModelList) {
        for (GiftServerModel model : giftServerModelList) {
            getGiftDBDao().insertOrReplace(GiftServerModel.toGiftDB(model));
        }
    }

    public static List<BaseGift> getAllGift() {
        List<GiftDB> giftDBList = getGiftDBDao().queryBuilder().build().list();
        if (giftDBList == null || giftDBList.size() == 0) {
            new ArrayList<BaseGift>();
        }

        List<BaseGift> baseGiftList = BaseGift.parseFromGiftDB(giftDBList);
        return baseGiftList;
    }

    public void deleteAll() {
        getGiftDBDao().deleteAll();
    }

    public void deleteById(int id) {
        getGiftDBDao().deleteByKey(id);
    }
}
