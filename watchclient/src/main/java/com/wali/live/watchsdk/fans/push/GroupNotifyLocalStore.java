package com.wali.live.watchsdk.fans.push;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.GroupNotify;
import com.wali.live.dao.GroupNotifyDao;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.fans.push.event.GroupNotifyUpdateEvent;
import com.wali.live.watchsdk.fans.model.notification.ApplyJoinFansModel;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.model.notification.HandleJoinFansGroupNotifyModel;
import com.wali.live.watchsdk.fans.push.mapper.GroupNotifyMapper;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjn on 16-10-30.
 */
public class GroupNotifyLocalStore {

    public final static String TAG = GroupNotifyLocalStore.class.getSimpleName();
    private GroupNotifyDao mGroupNotifyDao;

    private static class SingletonHolder {
        static GroupNotifyLocalStore groupNotifyLocalStore = new GroupNotifyLocalStore();
    }

    private GroupNotifyLocalStore() {
        mGroupNotifyDao = GreenDaoManager.getDaoSession(GlobalData.app()).getGroupNotifyDao();
    }

    public static GroupNotifyLocalStore getInstance() {
        return SingletonHolder.groupNotifyLocalStore;
    }

    public void deleteAll() {
        long uuid = MyUserInfoManager.getInstance().getUuid();
        if (uuid == 0) {
            uuid = UserAccountManager.getInstance().getUuidAsLong();
        }
        String sql = "delete from " + GroupNotifyDao.TABLENAME + " where " +
                GroupNotifyDao.Properties.LocalUserId.columnName + " = " + uuid;
        try {
            mGroupNotifyDao.getDatabase().execSQL(sql);
        } catch (Exception e) {
            MyLog.e(e);
        } finally {
        }
    }

    /**
     * 插入群通知消息
     */
    public boolean insertOrReplaceGroupNotifyBaseModelList(List<GroupNotifyBaseModel> groupNotifyBaseModelList) {
        boolean bret = false;
        if (null != groupNotifyBaseModelList && groupNotifyBaseModelList.size() > 0) {
            // 先转成在数据库中存储的类型
            List<GroupNotify> groupNotifyList = new ArrayList<>();
            List<GroupNotify> groupNotifyListInDB = null;
            List<GroupNotify> deleteGroupNotifyList = new ArrayList<>();
            for (int i = 0; i < groupNotifyBaseModelList.size(); i++) {
                GroupNotifyBaseModel groupNotifyBaseModel = groupNotifyBaseModelList.get(i);
                GroupNotify groupNotify = GroupNotifyMapper.transformGroupNotifyBaseModelToGroupNotify(groupNotifyBaseModel);
                if (groupNotify == null || groupNotify.getType() == null) {
                    continue;
                }
                //如果是同意加群或者是拒绝加群的话，要删除申请加群的通知
                if (groupNotifyBaseModel.getNotificationType() == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY || groupNotifyBaseModel.getNotificationType() == GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY) {
                    VFansCommonProto.ApplyJoinResult type = groupNotifyBaseModel.getNotificationType() == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY ? VFansCommonProto.ApplyJoinResult.PASS : VFansCommonProto.ApplyJoinResult.REFUSE;
                    VFansCommonProto.ApplyJoinResult vfansJoinResult = groupNotifyBaseModel.getNotificationType() == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY ? VFansCommonProto.ApplyJoinResult.PASS : VFansCommonProto.ApplyJoinResult.REFUSE;

                    HandleJoinFansGroupNotifyModel handleJoinFansGroupNotifyModel = (HandleJoinFansGroupNotifyModel) groupNotifyBaseModel;
                    if (handleJoinFansGroupNotifyModel.getHandler() == MyUserInfoManager.getInstance().getUuid()) {
                        if (deleteAllApplyJoinAndInsert(groupNotifyBaseModel.getCandidate(), groupNotifyBaseModel.getGroupId(), type, vfansJoinResult)) {
                            //TODO 先简单点，一发现有数据更新，抛出事件，这个事件带着所有通知。
                            GroupNotifyUpdateEvent event = GroupNotifyLocalStore.getInstance().getGroupNotifyBaseModelListEventFromDB();
                            EventBus.getDefault().post(event);
                        }
                    } else {
                        if (deleteAllApplyJoin(groupNotifyBaseModel.getCandidate(), groupNotifyBaseModel.getGroupId(), type)) {
                            //TODO 先简单点，一发现有数据更新，抛出事件，这个事件带着所有通知。
                            GroupNotifyUpdateEvent event = GroupNotifyLocalStore.getInstance().getGroupNotifyBaseModelListEventFromDB();
                            EventBus.getDefault().post(event);
                        }
                    }
                }

                if (groupNotify.getType() == GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY) {
                    // 如果是申请加群，则要看数据库中有没有这个用户的历史申请，有的话要删除，产品要求只保存最新一条
                    if (groupNotifyListInDB == null) {
                        groupNotifyListInDB = getAllGroupNotifyFromDB();
                    }
                    for (GroupNotify gnInDB : groupNotifyListInDB) {
                        if (gnInDB.getType() == groupNotify.getType() &&
                                gnInDB.getCandidate().longValue() == groupNotify.getCandidate().longValue() &&
                                gnInDB.getGroupId().longValue() == groupNotify.getGroupId().longValue()) {
                            deleteGroupNotifyList.add(gnInDB);
                        }
                    }
                }
                groupNotifyList.add(groupNotify);
            }
            if (!deleteGroupNotifyList.isEmpty()) {
                mGroupNotifyDao.deleteInTx(deleteGroupNotifyList);
            }
            MyLog.d(TAG, "insertOrReplaceGroupNotifyBaseModelList groupNotifyBaseModelList:" + groupNotifyBaseModelList);
            // 得到数据库所有数据m,如果数据中有这个元素就忽略不插入
            mGroupNotifyDao.insertOrReplaceInTx(groupNotifyList);
            bret = true;
        }
        return bret;
    }

    private List<GroupNotify> getAllGroupNotifyFromDB() {
        List<GroupNotify> groupNotifyList = mGroupNotifyDao.queryBuilder()
                .where(GroupNotifyDao.Properties.LocalUserId.eq(UserAccountManager.getInstance().getUuidAsLong()))
                .orderDesc(GroupNotifyDao.Properties.Time) // 根据时间戳降序
                .build()
                .list();
        return groupNotifyList;
    }

    /**
     * 得到所有关于群通知list的事件
     *
     * @return
     */
    public GroupNotifyUpdateEvent getGroupNotifyBaseModelListEventFromDB() {
        GroupNotifyUpdateEvent event = new GroupNotifyUpdateEvent();
        List<GroupNotify> groupNotifyList = getAllGroupNotifyFromDB();

        for (int i = 0; i < groupNotifyList.size(); i++) {
            GroupNotify groupNotify = groupNotifyList.get(i);
            GroupNotifyBaseModel groupNotifyBaseModel = GroupNotifyMapper.transformGroupNotifyToGroupNotifyBaseModel(groupNotify);
            event.allGroupNotifyList.add(groupNotifyBaseModel);
            if (!groupNotifyBaseModel.hasDeal()) {
                event.unDealGroupNotifyList.add(groupNotifyBaseModel);
            }
            if (!groupNotifyBaseModel.hasRead()) {
                event.unReadGroupNotifyList.add(groupNotifyBaseModel);
            }
        }
        MyLog.d(TAG, "getGroupNotifyBaseModelListEventFromDB event.allGroupNotifyList:" + event.allGroupNotifyList);
        return event;
    }

    /**
     * 更新单挑通知
     *
     * @param groupNotifyBaseModelList
     * @return
     */
    public boolean updateGroupNotifyBaseModel(List<GroupNotifyBaseModel> groupNotifyBaseModelList) {
        boolean bret = false;
        if (null != groupNotifyBaseModelList && groupNotifyBaseModelList.size() > 0) {
            // 先转成在数据库中存储的类型
            List<GroupNotify> groupNotifyList = new ArrayList<>();
            for (int i = 0; i < groupNotifyBaseModelList.size(); i++) {
                GroupNotifyBaseModel groupNotifyBaseModel = groupNotifyBaseModelList.get(i);
                groupNotifyList.add(GroupNotifyMapper.transformGroupNotifyBaseModelToGroupNotify(groupNotifyBaseModel));
            }
            MyLog.d(TAG, "insertOrReplaceGroupNotifyBaseModelList groupNotifyBaseModelList:" + groupNotifyBaseModelList);
            mGroupNotifyDao.updateInTx(groupNotifyList);
            bret = true;
        }
        return bret;
    }

    public boolean insertOrReplaceGroupNotifyBaseModel(GroupNotifyBaseModel groupNotifyBaseModel) {
        if (groupNotifyBaseModel == null) {
            return false;
        }
        List<GroupNotifyBaseModel> list = new ArrayList<>(1);
        list.add(groupNotifyBaseModel);
        return insertOrReplaceGroupNotifyBaseModelList(list);
    }

    public boolean delete(GroupNotifyBaseModel groupNotifyBaseModel) {
        if (groupNotifyBaseModel == null) {
            return false;
        }
        mGroupNotifyDao.delete(GroupNotifyMapper.transformGroupNotifyBaseModelToGroupNotify(groupNotifyBaseModel));
        return true;
    }

    public boolean deleteAllApplyJoinAndInsert(long candidate, long groupid, VFansCommonProto.ApplyJoinResult resultType, VFansCommonProto.ApplyJoinResult applyJoinResult) {
        boolean bret = false;
        List<GroupNotify> list = getAllGroupNotifyFromDB();
        for (GroupNotify gn : list) {
            if (gn.getType() == GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY
                    && gn.getCandidate() == candidate
                    && gn.getGroupId() == groupid) {
                mGroupNotifyDao.delete(gn);
                ApplyJoinFansModel model = (ApplyJoinFansModel) GroupNotifyMapper.transformGroupNotifyToGroupNotifyBaseModel(gn);
                insertOrReplaceGroupNotifyBaseModel(model.toHandleJoinFansGroupNotifyModel(resultType, applyJoinResult));
                bret = true;
            }
        }
        return bret;
    }

    public boolean deleteAllApplyJoin(long candidate, long groupId, VFansCommonProto.ApplyJoinResult resultType) {
        boolean bret = false;
        List<GroupNotify> list = getAllGroupNotifyFromDB();
        for (GroupNotify gn : list) {
            if (gn.getType() == GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY
                    && gn.getCandidate() == candidate
                    && gn.getGroupId() == groupId) {
                mGroupNotifyDao.delete(gn);
                bret = true;
            }
        }
        return bret;
    }

    public void deleteAllGroupNotify() {
        String sql = String.format("delete from %s WHERE %s=%s",
                mGroupNotifyDao.getTablename(),
                GroupNotifyDao.Properties.LocalUserId.columnName,
                UserAccountManager.getInstance().getUuidAsLong());
        mGroupNotifyDao.getDatabase().execSQL(sql);

        GroupNotifyUpdateEvent event = new GroupNotifyUpdateEvent();
        event.empty = true;
        EventBus.getDefault().post(event);
    }
}
