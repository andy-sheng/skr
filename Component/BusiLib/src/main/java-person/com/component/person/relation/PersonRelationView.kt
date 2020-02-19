package com.component.person.relation

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.component.busilib.R
import com.component.person.model.RelationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

// 个人主页的关系
class PersonRelationView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val relationTitleTv: TextView
    private val titleArrowIv: ImageView
    private val recyclerView: RecyclerView
    private val arrowIv: ImageView
    private val adapter: PersonRelationAdapter

    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
    private var mLastUpdateTime: Long = 0  // 上次刷新时间

    init {
        View.inflate(context, R.layout.person_relation_view_layout, this)

        relationTitleTv = this.findViewById(R.id.relation_title_tv)
        titleArrowIv = this.findViewById(R.id.title_arrow_iv)
        recyclerView = this.findViewById(R.id.recycler_view)
        arrowIv = this.findViewById(R.id.arrow_iv)

        adapter = PersonRelationAdapter(object : PersonRelationAdapter.Listener {
            override fun onClickItem(position: Int, model: RelationModel?) {
                // todo 进个人主页面么？
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    fun initData(userID: Long, flag: Boolean) {
        // 也设一个时间间隔吧
        val now = System.currentTimeMillis()
        if (!flag) {
            if (now - mLastUpdateTime < 60 * 1000) {
                return
            }
        }

        getRelationInfo(userID)
    }

    private fun getRelationInfo(userID: Long) {
        launch {
            val map = mutableMapOf(
                    "userID" to userID
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("getRelationInfo", ControlType.CancelThis)) {
                userInfoServerApi.getAllRelationInfoKt(body)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("relationList"), RelationModel::class.java)
                if (list.isNullOrEmpty()) {
                    recyclerView.visibility = View.GONE
                    arrowIv.visibility = View.GONE
                    titleArrowIv.visibility = View.VISIBLE
                    adapter.mDataList.clear()
                    adapter.notifyDataSetChanged()
                } else {
                    recyclerView.visibility = View.VISIBLE
                    arrowIv.visibility = View.VISIBLE
                    titleArrowIv.visibility = View.GONE
                    adapter.mDataList.clear()
                    adapter.mDataList.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            } else {

            }
        }
    }


    fun destory() {
        cancel()
    }
}