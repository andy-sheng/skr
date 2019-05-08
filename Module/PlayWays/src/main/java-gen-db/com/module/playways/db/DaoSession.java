package com.module.playways.db;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.module.playways.room.gift.GiftDB;

import com.module.playways.db.GiftDBDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig giftDBDaoConfig;

    private final GiftDBDao giftDBDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        giftDBDaoConfig = daoConfigMap.get(GiftDBDao.class).clone();
        giftDBDaoConfig.initIdentityScope(type);

        giftDBDao = new GiftDBDao(giftDBDaoConfig, this);

        registerDao(GiftDB.class, giftDBDao);
    }
    
    public void clear() {
        giftDBDaoConfig.clearIdentityScope();
    }

    public GiftDBDao getGiftDBDao() {
        return giftDBDao;
    }

}
