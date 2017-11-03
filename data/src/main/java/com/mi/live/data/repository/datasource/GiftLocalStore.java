package com.mi.live.data.repository.datasource;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.gift.mapper.GiftTypeMapper;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.Gift;
import com.wali.live.dao.GiftDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjn on 16-11-28.
 *
 * @module 礼物
 */
public class GiftLocalStore {
    private final String TAG = "GiftLocalStore";
    private static GiftLocalStore sInstance;

    private GiftDao mGiftDao;

    private GiftLocalStore() {
        mGiftDao = GreenDaoManager.getDaoSession(GlobalData.app()).getGiftDao();
    }

    public static GiftLocalStore getInstance() {
        if (sInstance == null) {
            synchronized (GiftLocalStore.class) {
                if (sInstance == null) {
                    sInstance = new GiftLocalStore();
                }
            }
        }
        return sInstance;
    }

    public void deleteAll() {
        mGiftDao.deleteAll();
    }

    public boolean insertGiftList(List<Gift> giftList) {
        boolean bret = false;
        if (null != giftList && giftList.size() > 0) {
            mGiftDao.insertInTx(giftList);
            bret = true;
        }
        return bret;
    }

    public List<Gift> getGiftList() {
        List<Gift> resultList = new ArrayList<>();
        if (mGiftDao != null) {
            try {
                List<Gift> temp = mGiftDao.queryBuilder().build().list();
                for (Gift gift : temp) {
                    resultList.add(GiftTypeMapper.loadExactGift(gift));
                }
            } catch (IllegalStateException e) {
                MyLog.e(TAG, "getGiftList failed e=" + e);
            }
        }
        return resultList;
    }

    public void updateGift(Gift gift) {
        if (mGiftDao != null) {
            try {
                mGiftDao.update(gift);
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
    }
}
