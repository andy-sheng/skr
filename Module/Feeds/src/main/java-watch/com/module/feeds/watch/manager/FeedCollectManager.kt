package com.module.feeds.watch.manager

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsCollectModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.functions.Consumer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FeedCollectManager {

    val TAG = "FeedCollectManager"

    const val PREF_KEY_COLLECT_MARKER_WATER = "collect_marker_water"

    suspend fun getMyCollect(): List<FeedsCollectModel>? {
        return suspendCancellableCoroutine {
            getMyCollect(object : ResponseCallBack<List<FeedsCollectModel>?>() {
                override fun onServerSucess(t: List<FeedsCollectModel>?) {
                    it.resume(t)
                }

                override fun onServerFailed() {
                    it.resumeWithException(Exception("读取收藏数据失败"))
                }
            })
        }
    }

    fun getMyCollect(feedCollectListCallBack: ResponseCallBack<List<FeedsCollectModel>?>?) {
        Observable.create(ObservableOnSubscribe<List<FeedsCollectModel>> {
            val feedWatchServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)
            var collectMarkerWater = U.getPreferenceUtils().getSettingLong(PREF_KEY_COLLECT_MARKER_WATER, -1L)
            if (collectMarkerWater == -1L) {
                // 全量拉
                var offset = 0
                val mCnt = 30
                var baohu = 0
                while (baohu < 100) {
                    baohu++
                    try {
                        val result = feedWatchServerApi.getCollectListByPage(offset, mCnt, MyUserInfoManager.getInstance().uid)
                        val response = result.execute()
                        val obj = response.body()
                        if (obj == null || obj.data == null || obj.errno != 0) {
                            break
                        }
                        if (offset == 0) {
                            // offset为0是记录水位
                            if (obj.data != null) {
                                collectMarkerWater = obj.data.getLongValue("lastIndexID")
                            }
                        }
                        offset = obj.data.getIntValue("offset")
                        val feedCollectsList = JSON.parseArray(obj.data.getString("likes"), FeedsCollectModel::class.java)
                        val hasMore = obj.data.getBooleanValue("hasMore")
                        if (!feedCollectsList.isNullOrEmpty()) {
                            // 存到数据库
                            FeedCollectLocalApi.insertOrUpdate(feedCollectsList)
                        }
                        if (!hasMore) {
                            // 水位持久化
                            U.getPreferenceUtils().setSettingLong(PREF_KEY_COLLECT_MARKER_WATER, collectMarkerWater)
                            break
                        }
                    } catch (e: Exception) {
                        MyLog.e(TAG, "getMyCollect 全量拉取 $e")
                        break
                    }
                }
            } else {
                // 增量拉
                var baohu = 0
                while (baohu < 100) {
                    baohu++
                    try {
                        val result = feedWatchServerApi.getCollectListByIndex(collectMarkerWater, MyUserInfoManager.getInstance().uid)
                        val response = result.execute()
                        val obj = response.body()
                        if (obj != null && !obj.data.isNullOrEmpty() && obj.errno == 0) {
                            val updateList = JSON.parseArray(obj.data.getString(""), FeedsCollectModel::class.java)
                            val delList = JSON.parseArray(obj.data.getString(""), Int::class.java)
                            collectMarkerWater = obj.data.getLongValue("lastIndexID")
                            val hasMore = obj.data.getBooleanValue("hasMore")

                            if (!updateList.isNullOrEmpty()) {
                                // 批量更新
                                FeedCollectLocalApi.insertOrUpdate(updateList)

                            }
                            if (!delList.isNullOrEmpty()) {
                                // 批量删除
                                FeedCollectLocalApi.deleteFeedCollectByFeedIDs(delList)
                            }
                            if (!hasMore) {
                                U.getPreferenceUtils().setSettingLong(PREF_KEY_COLLECT_MARKER_WATER, collectMarkerWater)
                                break
                            }
                        } else {
                            // 请求出错，跳出循环
                            break
                        }
                    } catch (e: Exception) {
                        MyLog.e(TAG, "getMyCollect 增量拉取 $e")
                        break
                    }
                }
            }

            // 操作完数据库，从数据库中读取所有的内容
            val resultList = FeedCollectLocalApi.getFeedCollects()
            feedCollectListCallBack?.onServerSucess(resultList)

            it.onComplete()
        }).subscribeOn(U.getThreadUtils().singleThreadPoll())
                .subscribe(Consumer<List<FeedsCollectModel>> {
                }, Consumer<Throwable> {
                    MyLog.d(TAG, it)
                })
    }


}