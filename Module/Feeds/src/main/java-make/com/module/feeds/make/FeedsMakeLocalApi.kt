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
                .where(FeedsDraftDBDao.Properties.From.eq(FROM_CHALLENGE))
                .orderDesc(FeedsDraftDBDao.Properties.UpdateTs).list()
        listDB.forEach {
            val makeModel = JSON.parseObject(it.feedsMakeModelJson, FeedsMakeModel::class.java)
            if(makeModel.challengeType == 0 ){
                makeModel.challengeType = CHALLENGE_TYPE_CHANGE_SONG
            }
            list.add(makeModel)
        }
        return list
    }

    fun loadDraftFromQuickSing(): ArrayList<FeedsMakeModel> {
        val list = ArrayList<FeedsMakeModel>()
        val listDB = draftDBDao.queryBuilder()
                .where(FeedsDraftDBDao.Properties.From.eq(FROM_QUICK_SING))
                .orderDesc(FeedsDraftDBDao.Properties.UpdateTs).list()
        listDB.forEach {
            val makeModel = JSON.parseObject(it.feedsMakeModelJson, FeedsMakeModel::class.java)
            if(makeModel.challengeType == 0 ){
                makeModel.challengeType = CHALLENGE_TYPE_QUICK_SONG
            }
            list.add(makeModel)
        }
        return list
    }

    fun loadDraftFromChangeSing(): ArrayList<FeedsMakeModel> {
        val list = ArrayList<FeedsMakeModel>()
        val listDB = draftDBDao.queryBuilder()
                .where(FeedsDraftDBDao.Properties.From.eq(FROM_CHANGE_SING))
                .orderDesc(FeedsDraftDBDao.Properties.UpdateTs).list()
        listDB.forEach {
            val makeModel = JSON.parseObject(it.feedsMakeModelJson, FeedsMakeModel::class.java)
            if(makeModel.challengeType == 0 ){
                makeModel.challengeType = CHALLENGE_TYPE_CHANGE_SONG
            }
            list.add(makeModel)
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
        if(feedsMakeModel?.challengeID==0L){
            if(feedsMakeModel?.challengeType== CHALLENGE_TYPE_QUICK_SONG){
                draftDb.from = FROM_QUICK_SING
            }else{
                draftDb.from = FROM_CHANGE_SING
            }
        }else{
            draftDb.from = FROM_CHALLENGE
        }
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