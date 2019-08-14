package com.module.feeds.make

import com.alibaba.fastjson.JSON
import com.module.feeds.db.FeedsDraftDBDao
import com.module.feeds.db.GreenDaoManager
import com.module.feeds.make.make.FeedsDraftDB
import org.greenrobot.eventbus.EventBus

object FeedsMakeLocalApi {

    val TAG = "FeedsMakeLocalApi"

    private val draftDBDao: FeedsDraftDBDao
        get() = GreenDaoManager.getDaoSession().feedsDraftDBDao

    fun loadDraftFromChanllege(): ArrayList<FeedsMakeModel> {
        val list = ArrayList<FeedsMakeModel>()
        val listDB = draftDBDao.queryBuilder()
                .where(FeedsDraftDBDao.Properties.From.eq(1))
                .orderDesc(FeedsDraftDBDao.Properties.UpdateTs).list()
        listDB.forEach {
            list.add(JSON.parseObject(it.feedsMakeModelJson, FeedsMakeModel::class.java))
        }
        return list
    }

    fun loadDraftFromQuickSing(): ArrayList<FeedsMakeModel> {
        val list = ArrayList<FeedsMakeModel>()
        val listDB = draftDBDao.queryBuilder()
                .where(FeedsDraftDBDao.Properties.From.eq(2))
                .orderDesc(FeedsDraftDBDao.Properties.UpdateTs).list()
        listDB.forEach {
            list.add(JSON.parseObject(it.feedsMakeModelJson, FeedsMakeModel::class.java))
        }
        return list
    }

    fun insert(feedsMakeModel: FeedsMakeModel) {
        val draftDb = FeedsDraftDB()
        val now = System.currentTimeMillis()
        if (feedsMakeModel?.draftID != 0L) {
            draftDb.draftID = feedsMakeModel?.draftID
        } else {
            draftDb.draftID = now
        }
        draftDb.updateTs = now
        feedsMakeModel.draftUpdateTs = now
        if (feedsMakeModel.draftID == 0L) {
            feedsMakeModel.draftID = draftDb.draftID
        }
        draftDb.from = if (feedsMakeModel.songModel?.challengeID == 0L) 2 else 1
        draftDb.feedsMakeModelJson = JSON.toJSONString(feedsMakeModel)
        draftDBDao.insertOrReplace(draftDb)
        EventBus.getDefault().post(FeedsDraftUpdateEvent())
    }

    fun delete(draftID: Long) {
        draftDBDao.deleteByKey(draftID)
        EventBus.getDefault().post(FeedsDraftUpdateEvent())
    }

    fun deleteAll() {
        draftDBDao.deleteAll()
    }
}

class FeedsDraftUpdateEvent{

}