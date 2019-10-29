package com.module.posts.statistics

import android.util.ArrayMap
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.component.busilib.recommend.RA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

// 帖子的数据上报(曝光和点击)
object PostsStatistics {

    val EST_Exposure = 1  //曝光
    val EST_Click = 2  //点击

    private val infoMap = ArrayMap<Int, Item>()
    private var mapNum = 0
    private var lastUploadTs = System.currentTimeMillis()

    /**
     * 帖子曝光
     */
    fun addCurExpose(postsId: Int) {
        StatisticsAdapter.recordCountEvent("posts", "content_expose", null)
        add2Map(postsId, EST_Exposure)
    }

    /**
     * 有效点击
     */
    fun addCurClick(postsId: Int) {
        StatisticsAdapter.recordCountEvent("posts", "content_click", null)
        add2Map(postsId, EST_Click)
    }

    private fun add2Map(postsId: Int, statisticsType: Int) {
        var l = infoMap[postsId]
        if (l == null) {
            l = Item(0, 0)
            infoMap[postsId] = l
        }
        if (statisticsType == EST_Exposure) {
            l.exposureCnt = l.exposureCnt + 1
            mapNum++
            tryUpload(false)
        } else if (statisticsType == EST_Click) {
            l.clickCnt = l.clickCnt + 1
            mapNum++
            tryUpload(false)
        }
    }

    fun tryUpload(force: Boolean) {
        if (infoMap.size <= 0) {
            return
        }
        val now = System.currentTimeMillis()
        if (!force) {
            if (mapNum < 25 && (now - lastUploadTs < 2 * 60 * 1000)) {
                return
            }
        }
        val l1 = ArrayList<JSONObject>()
        infoMap.keys.forEach {
            val v = infoMap[it]
            val jo = JSONObject()
            jo["postsID"] = it
            val jarrays = JSONArray()
            // 曝光
            val exposure = JSONObject()
            exposure["cnt"] = v?.exposureCnt
            exposure["statisticsType"] = EST_Exposure
            jarrays.add(exposure)
            // 点击
            val click = JSONObject()
            click["cnt"] = v?.clickCnt
            click["statisticsType"] = EST_Click
            jarrays.add(click)
            jo["statisticsVals"] = jarrays
            l1.add(jo)
        }
        infoMap.clear()
        mapNum = 0
        lastUploadTs = now

        val mutableSet1 = mapOf(
                "stats" to l1,
                "userID" to MyUserInfoManager.uid,
                "platform" to 20,
                "vars" to RA.getVars(),
                "testList" to RA.getTestList()
        )

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        GlobalScope.launch {
            val r = subscribe(RequestControl("uploadPostssStatistics", ControlType.CancelThis)) {
                postsStatisticsServerApi.uploadPostsStatistics(body)
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

    private val postsStatisticsServerApi = ApiManager.getInstance().createService(PostsStatisticsServerApi::class.java)
}

private interface PostsStatisticsServerApi {
    @PUT("/v1/posts/statistics")
    fun uploadPostsStatistics(@Body requestBody: RequestBody): Call<ApiResult>
}

// 包括曝光次数和点击次数
private class Item(var exposureCnt: Int, var clickCnt: Int)