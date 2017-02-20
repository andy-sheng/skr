package com.mi.live.data.repository.datasource;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.WatchHistoryInfo;
import com.wali.live.dao.WatchHistoryInfoDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyh on 2016/11/6.
 */
public class WatchHistoryInfoDaoAdapter {
    private static WatchHistoryInfoDaoAdapter sInstance;

    private WatchHistoryInfoDao mWatchHistoryInfoDao;
    private static final int COUNT_SHOW = 50; //产品要求,最多显示50条

    private WatchHistoryInfoDaoAdapter() {
        mWatchHistoryInfoDao = GreenDaoManager.getDaoSession(GlobalData.app()).getWatchHistoryInfoDao();
    }

    public static WatchHistoryInfoDaoAdapter getInstance() {
        if (sInstance == null) {
            synchronized (WatchHistoryInfoDaoAdapter.class) {
                if (sInstance == null) {
                    sInstance = new WatchHistoryInfoDaoAdapter();
                }
            }
        }
        return sInstance;
    }

    public void deleteAll() {
        mWatchHistoryInfoDao.deleteAll();
    }

    public long getWatchHistoryInfoCount() {
        if (mWatchHistoryInfoDao != null) {
            try {
                return mWatchHistoryInfoDao.queryBuilder().count();
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
        return 0;
    }

    public boolean insertWatchHistoryInfoList(List<WatchHistoryInfo> WatchHistoryInfoList) {
        boolean bret = false;
        if (null != WatchHistoryInfoList && WatchHistoryInfoList.size() > 0) {
            mWatchHistoryInfoDao.insertInTx(WatchHistoryInfoList);
            bret = true;
        }
        return bret;
    }

    //显示只是显示前50条记录
    public List<WatchHistoryInfo> getWatchHistoryInfoList() {
        List<WatchHistoryInfo> resultList = new ArrayList<>();
        if (mWatchHistoryInfoDao != null) {
            try {
                resultList = mWatchHistoryInfoDao.queryBuilder().orderDesc(WatchHistoryInfoDao.Properties.WatchTime).limit(COUNT_SHOW).list();
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
        return resultList;
    }

    //显示只是显示前50条记录,其余的删除.
    public void deleteRedundantWatchHistory() {
        if (mWatchHistoryInfoDao != null) {
            try {
                List<WatchHistoryInfo> resultList = mWatchHistoryInfoDao.queryBuilder().orderDesc(WatchHistoryInfoDao.Properties.WatchTime).limit(COUNT_SHOW).list();
                if (resultList != null) {
                    int size = resultList.size();
                    if (size == COUNT_SHOW && resultList.get(size - 1) != null) {
                        long watchTime = resultList.get(size - 1).getWatchTime();
                        mWatchHistoryInfoDao.deleteInTx(mWatchHistoryInfoDao.queryBuilder().where(WatchHistoryInfoDao.Properties.WatchTime.lt(watchTime)).list());
                    }
                }
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
    }


    public void insertOrReplaceWatchHistoryInfo(WatchHistoryInfo WatchHistoryInfo) {
        if (mWatchHistoryInfoDao != null) {
            try {
                mWatchHistoryInfoDao.insertOrReplace(WatchHistoryInfo);
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
    }

    public void updateWatchHistoryInfo(FollowOrUnfollowEvent event) {
        if (mWatchHistoryInfoDao != null && event != null) {
            try {
                List<WatchHistoryInfo> watchHistoryInfoList = mWatchHistoryInfoDao.queryBuilder().
                        where(WatchHistoryInfoDao.Properties.UserId.eq(event.uuid)).list();
                if (watchHistoryInfoList != null && watchHistoryInfoList.size() > 0) {
                    for (WatchHistoryInfo watchHistoryInfo : watchHistoryInfoList) {
                        watchHistoryInfo.setIsBothway(event.isBothFollow);
                        watchHistoryInfo.setIsFollowing(event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW);
                    }
                    mWatchHistoryInfoDao.updateInTx(watchHistoryInfoList);
                }
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
    }

}
