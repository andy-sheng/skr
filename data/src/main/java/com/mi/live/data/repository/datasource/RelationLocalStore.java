package com.mi.live.data.repository.datasource;

import com.base.global.GlobalData;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.Relation;
import com.wali.live.dao.RelationDao;

/**
 * Created by anping on 16-10-10.
 */
public class RelationLocalStore {

    public static Relation getRelationByUUid(long uuid) {
        return GreenDaoManager.getDaoSession(GlobalData.app()).getRelationDao().queryBuilder().where(RelationDao.Properties.UserId.eq(uuid)).unique();
    }
}
