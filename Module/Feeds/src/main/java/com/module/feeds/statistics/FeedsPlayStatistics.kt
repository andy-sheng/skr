package com.module.feeds.statistics

import android.util.ArrayMap
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.utils.U
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

object FeedsPlayStatistics {

    private val exposeMap = ArrayMap<Int, Item>()
    private val completeMap = ArrayMap<Int, Item>()

    fun addExpose(feedID: Int?) {
        if (feedID == null) {
            return
        }
        var v = exposeMap[feedID]
        if (v == null) {
            v = Item(feedID, 0, System.currentTimeMillis())
            exposeMap.put(feedID, v)
        }
        v.cnt = v.cnt + 1
        v.ts = System.currentTimeMillis()
//        tryUpload(false)
    }

    fun addComplete(feedID: Int?) {
        if (feedID == null) {
            return
        }
        var ev = exposeMap[feedID]
        if (ev == null) {
            // 曝光点都没有，完成点不合法
            return
        }
        var v = completeMap[feedID]
        if (v == null) {
            v = Item(feedID, 1, ev.ts)
            completeMap.put(feedID, v)
            tryUpload(false)
            return
        }
        if (v.ts < ev.ts) {
            // 完成点的ts 小于 曝光点的 ts 合法，可以继续打点
            v.cnt = v.cnt + 1
            v.ts = ev.ts
            tryUpload(false)
        }
    }

    fun tryUpload(force: Boolean) {
        if (exposeMap.size + completeMap.size <= 0) {
            return
        }
        if (!force) {
            if (exposeMap.size + completeMap.size < 20) {
                return
            }
        }
        val l1 = ArrayList<JSONObject>()
        exposeMap.keys.forEach {
            val v = exposeMap[it]
            val jo = JSONObject()
            jo.put("feedID", it)
            jo.put("cnt", v?.cnt)
            l1.add(jo)
        }

        val l2 = ArrayList<JSONObject>()

        completeMap.keys.forEach {
            val v = completeMap[it]
            val jo = JSONObject()
            jo.put("feedID", it)
            jo.put("cnt", v?.cnt)
            l2.add(jo)
        }

        exposeMap.clear()
        completeMap.clear()
        val mutableSet1 = mapOf(
                "exposureFeeds" to l1,
                "userID" to MyUserInfoManager.getInstance().uid,
                "wholePlayedFeeds" to l2
        )

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        GlobalScope.launch {
            val r = subscribe(RequestControl("uploadFeedsStatistics", ControlType.CancelThis)) {
                fedsStatisticsServerApi.uploadFeedsStatistics(body)
            }
            launch(Dispatchers.Main) {
                if (r?.errno == 0) {
                    if (MyLog.isDebugLogOpen()) {
                        U.getToastUtil().showShort("打点上报成功")
                    }

                }
            }
        }
    }

    private val fedsStatisticsServerApi = ApiManager.getInstance().createService(FeedsStatisticsServerApi::class.java)
}

private interface FeedsStatisticsServerApi {
    @PUT("/v1/feed/statistics")
    fun uploadFeedsStatistics(@Body requestBody: RequestBody): Call<ApiResult>
}

private class Item(var feedId: Int, var cnt: Int, var ts: Long)