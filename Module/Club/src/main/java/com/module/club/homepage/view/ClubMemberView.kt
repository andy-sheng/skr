package com.module.club.homepage.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.UserInfoModel
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.module.club.ClubServerApi
import com.module.club.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ClubMemberView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val recyclerView: RecyclerView
    private val adapter: ClubMemberAdapter

    var clubID: Int = 0
    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private val cnt = 15

    init {
        View.inflate(context, R.layout.club_member_view_layout, this)

        recyclerView = this.findViewById(R.id.recycler_view)
        adapter = ClubMemberAdapter()
        recyclerView.layoutManager = GridLayoutManager(context, 6)
        recyclerView.adapter = adapter
    }

    fun initData() {
        launch {
            val result = subscribe(RequestControl("initData", ControlType.CancelThis)) {
                clubServerApi.getClubMemberList(clubID, 0, cnt)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("items"), UserInfoModel::class.java)
                adapter.mDataList.clear()
                if (!list.isNullOrEmpty()) {
                    adapter.mDataList.addAll(list)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}