package com.module.feeds.make

import com.module.feeds.db.FeedsDraftDBDao
import com.module.feeds.db.GreenDaoManager

object FeedsMakeLocalApi {

    val TAG = "FeedsMakeLocalApi"

    private val accountDao: FeedsDraftDBDao
        get() = GreenDaoManager.getDaoSession().feedsDraftDBDao


    fun delete(draftID:Long) {
        accountDao.deleteByKey(draftID)
    }
}
