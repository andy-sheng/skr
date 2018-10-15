package com.wali.live.dao;

import android.os.AsyncTask;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.StringUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.event.DatabaseChangedEvent;
import com.mi.live.data.greendao.GreenDaoManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AsyncTaskUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.relation.RelationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.greenrobot.dao.query.WhereCondition;

/**
 * Created by lan on 16/3/9.
 */
public class RelationDaoAdapter {
    private static final String TAG = RelationDaoAdapter.class.getSimpleName();

    private static RelationDaoAdapter sInstance;

    private RelationDao mRelationDao;

    private RelationDaoAdapter() {
        mRelationDao = GreenDaoManager.getDaoSession(GlobalData.app()).getRelationDao();
    }

    public static RelationDaoAdapter getInstance() {
        synchronized (RelationDaoAdapter.class) {
            if (sInstance == null) {
                sInstance = new RelationDaoAdapter();
            }
        }
        return sInstance;
    }

    private void notifyChanged(List<Relation> relationList, int action) {
        DatabaseChangedEvent.post(DatabaseChangedEvent.TYPE_DB_RELATION, action, relationList);
    }

    public boolean deleteAll() {
        if (null != mRelationDao) {
            mRelationDao.deleteAll();
            return true;
        }
        return false;
    }

    public boolean insertRelationList(List<Relation> relationList) {
        if (null != relationList && relationList.size() > 0) {
            try {
                mRelationDao.insertOrReplaceInTx(relationList);
                notifyChanged(relationList, DatabaseChangedEvent.ACTION_ADD);
                return true;
            } catch (Exception e) {
                MyLog.e(TAG, e);
                return false;
            }
        }
        return false;
    }

    public boolean insertRelation(Relation Relation) {
        if (null != Relation) {
            try {
                mRelationDao.insertOrReplace(Relation);
                List<Relation> relationList = new ArrayList();
                relationList.add(Relation);
                notifyChanged(relationList, DatabaseChangedEvent.ACTION_ADD);
                return true;
            } catch (Exception e) {
                MyLog.e(TAG, e);
                return false;
            }
        }
        return false;
    }

    public boolean deleteRelation(long uid) {
        if (uid > 0) {
            try {
                mRelationDao.deleteByKey(uid);
                List<Relation> RelationList = new ArrayList();
                Relation relation = new Relation();
                relation.setUserId(uid);
                RelationList.add(relation);
                notifyChanged(RelationList, DatabaseChangedEvent.ACTION_REMOVE);
            } catch (Exception e) {
                MyLog.e(TAG, e);
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean deleteRelationList(List<Relation> RelationList) {
        if (null != RelationList && !RelationList.isEmpty()) {
            try {
                mRelationDao.deleteInTx(RelationList);
                notifyChanged(RelationList, DatabaseChangedEvent.ACTION_REMOVE);
            } catch (Exception e) {
                MyLog.e(TAG, e);
                return false;
            }
            return true;
        }
        return false;
    }

    public List<Relation> getRelationList() {
        List<Relation> resultList = null;
        if (mRelationDao != null) {
            try {
                resultList = mRelationDao.queryBuilder().build().list();
                if (resultList != null && resultList.size() > 0) {
                    for (Relation relation : resultList) {
                        AvatarUtils.updateMyFollowAvatarTimeStamp(relation.getUserId(), relation.getAvatar());
                    }
                }
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
        return resultList;
    }

    public List<Relation> getRelationListWithGivenIds(Set<Long> RelationIds) {
        List<Relation> resultList = null;
        if (mRelationDao != null) {
            try {
                String query = RelationDao.Properties.UserId.columnName + " IN (" + StringUtils.join(RelationIds, ",") + ")";
                MyLog.d(TAG, "query =" + query);
                resultList = mRelationDao.queryBuilder().where(
                        new WhereCondition.StringCondition(query)
                ).build().list();
            } catch (IllegalStateException e) {
                MyLog.e(TAG, e);
            }
        }
        return resultList;
    }

    public void updateRelation(Relation Relation) {
        if (mRelationDao != null) {
            try {
                mRelationDao.update(Relation);
                List<Relation> RelationList = new ArrayList<>();
                RelationList.add(Relation);
                notifyChanged(RelationList, DatabaseChangedEvent.ACTION_UPDATE);
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
    }

    public void updateRelationAndNotNotify(Relation relation) {
        if (mRelationDao != null) {
            try {
                mRelationDao.update(relation);
            } catch (IllegalStateException e) {
                MyLog.e(e);
            }
        }
    }

    private boolean mIsDbUpdating = false;

    public void updateRelationDb() {
        if (mIsDbUpdating) {
            return;
        }
        mIsDbUpdating = true;

        AsyncTaskUtils.exeIOTask(new AsyncTask<Object, Object, List<Object>>() {
            List<Relation> relationListFromDb;

            @Override
            protected List<Object> doInBackground(Object... params) {
                relationListFromDb = RelationDaoAdapter.getInstance().getRelationList();
                List<Object> list = new ArrayList();
                long uuid = UserAccountManager.getInstance().getUuidAsLong();
                RelationUtils.loadFollowingData(uuid, RelationUtils.LOADING_FOLLOWING_PAGE_COUNT, 0, list, false, true);
                if (list.size() > 0) {
                    List<Relation> relationToInsert = new ArrayList();
                    List<Relation> relationToDelete = new ArrayList();
                    List<UserListData> dataList = new ArrayList();
                    for (Object object : list) {
                        UserListData data = (UserListData) object;
                        if (data.isFollowing) {
                            relationToInsert.add(data.toRelation());
                            dataList.add(data);
                        }

                    }
                    if (relationListFromDb != null && relationListFromDb.size() > 0) {
                        for (Relation object : relationListFromDb) {
                            if (!dataList.contains(new UserListData(object))) {
                                relationToDelete.add(object);
                            }
                        }
                    }
                    if (relationToDelete.size() > 0) {
                        RelationDaoAdapter.getInstance().deleteRelationList(relationToDelete);
                    }
                    RelationDaoAdapter.getInstance().insertRelationList(relationToInsert);
                }
                mIsDbUpdating = false;
                return list;
            }
        });

    }

    public void updateRelationFromServer(final long uuid) {
        AsyncTaskUtils.exeIOTask(new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                User user = UserInfoManager.getUserInfoById(uuid);
                if (user == null) {
                    return null;
                }
                if (user.isFocused()) {
                    insertRelation(user.getRelation());
                } else {
                    deleteRelation(uuid);
                }
                return null;
            }
        });
    }

    public void updateRelationListFromServer(final List<Long> uuid) {
        AsyncTaskUtils.exeIOTask(new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                List<User> mUserList = UserInfoManager.getUserListById(uuid);
                if (mUserList == null || mUserList.size() == 0) {
                    return null;
                }

                List<Relation> relationToInsert = new ArrayList();
                List<Relation> relationToDelete = new ArrayList();
                for (User mUser : mUserList) {
                    if (mUser.isFocused()) {
                        relationToInsert.add(mUser.getRelation());
                    } else {
                        relationToDelete.add(mUser.getRelation());
                    }
                }

                if (relationToDelete.size() > 0) {
                    RelationDaoAdapter.getInstance().deleteRelationList(relationToDelete);
                }
                RelationDaoAdapter.getInstance().insertRelationList(relationToInsert);
                return null;
            }
        });
    }

    public Relation getRelationByUUid(long uuid) {
        try {
            return mRelationDao.queryBuilder().where(RelationDao.Properties.UserId.eq(uuid)).unique();
        } catch (Exception e){
            MyLog.e(TAG ,"getRelationByUUid failed e=" + e);
        }
        return null;
    }
}
