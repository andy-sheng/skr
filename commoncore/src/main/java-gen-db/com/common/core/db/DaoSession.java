package com.common.core.db;

import com.common.core.account.UserAccount;
import com.common.core.userinfo.UserInfo;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig userInfoDaoConfig;
    private final DaoConfig userAccountDaoConfig;

    private final UserInfoDao userInfoDao;
    private final UserAccountDao userAccountDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        userInfoDaoConfig = daoConfigMap.get(UserInfoDao.class).clone();
        userInfoDaoConfig.initIdentityScope(type);

        userAccountDaoConfig = daoConfigMap.get(UserAccountDao.class).clone();
        userAccountDaoConfig.initIdentityScope(type);

        userInfoDao = new UserInfoDao(userInfoDaoConfig, this);
        userAccountDao = new UserAccountDao(userAccountDaoConfig, this);

        registerDao(UserInfo.class, userInfoDao);
        registerDao(UserAccount.class, userAccountDao);
    }
    
    public void clear() {
        userInfoDaoConfig.clearIdentityScope();
        userAccountDaoConfig.clearIdentityScope();
    }

    public UserInfoDao getUserInfoDao() {
        return userInfoDao;
    }

    public UserAccountDao getUserAccountDao() {
        return userAccountDao;
    }

}
