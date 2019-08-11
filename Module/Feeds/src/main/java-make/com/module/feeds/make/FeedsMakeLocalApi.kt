package com.module.feeds.make

import com.alibaba.fastjson.JSON
import com.module.feeds.db.FeedsDraftDBDao
import com.module.feeds.db.GreenDaoManager
import com.module.feeds.make.make.FeedsDraftDB

object FeedsMakeLocalApi {

    val TAG = "FeedsMakeLocalApi"

    private val draftDBDao: FeedsDraftDBDao
        get() = GreenDaoManager.getDaoSession().feedsDraftDBDao

    fun loadAll(): ArrayList<FeedsMakeModel> {
        val list = ArrayList<FeedsMakeModel>()
        val listDB = draftDBDao.queryBuilder().orderDesc(FeedsDraftDBDao.Properties.UpdateTs).list()
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
        draftDb.feedsMakeModelJson = JSON.toJSONString(feedsMakeModel)
        draftDBDao.insertOrReplace(draftDb)
    }

    fun delete(draftID: Long) {
        draftDBDao.deleteByKey(draftID)
    }

    fun deleteAll() {
        draftDBDao.deleteAll()
    }
}
