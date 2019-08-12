package com.module.feeds.watch.manager

import com.module.feeds.db.FeedCollectDBDao
import com.module.feeds.db.GreenDaoManager
import com.module.feeds.watch.model.FeedsCollectModel

object FeedCollectLocalApi {
    private fun getDao(): FeedCollectDBDao {
        return GreenDaoManager.getDaoSession().feedCollectDBDao
    }

    fun insertOrUpdate(feedCollectList: List<FeedsCollectModel>?) {
        feedCollectList?.let {
            val entities = ArrayList<FeedCollectDB>()
            for (model in it) {
                entities.add(FeedsCollectModel.toFeedCollectDB(model))
            }
            getDao().insertOrReplaceInTx(entities)
        }
    }

    fun insertOrUpdate(feedCollect: FeedsCollectModel?) {
        feedCollect?.let {
            getDao().insertOrReplaceInTx(FeedsCollectModel.toFeedCollectDB(it))
        }
    }

    fun deleteAll() {
        getDao().deleteAll()
    }

    fun deleteFeedCollectByFeedIDs(feedIDs: List<Int>?) {
        feedIDs?.let {
            var sql = String.format("DELETE FROM %s WHERE %s IN (", getDao().tablename, FeedCollectDBDao.Properties.FeedID.columnName)
            for (i in feedIDs.indices) {
                val feedID = feedIDs[i]
                if (i == 0) {
                    sql += feedID
                } else {
                    sql += ",$feedID"
                }
            }
            sql += ")"
            getDao().database.execSQL(sql)
        }

    }

    fun getFeedCollects(): List<FeedsCollectModel>? {
        val dbList = getDao().queryBuilder().build().list()
        val l = ArrayList<FeedsCollectModel>()
        for (db in dbList) {
            l.add(FeedsCollectModel.parseFeedCollectModel(db))
        }
        return l
    }

}