package com.component.club.view

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.component.busilib.R
import com.component.club.ClubMemberServerApi
import com.component.club.model.ClubMemberInfoModel
import com.module.RouterConstants
import com.module.club.homepage.view.ClubMemberAdapter
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
    var memberCnt: Int = 0
    private val clubServerApi = ApiManager.getInstance().createService(ClubMemberServerApi::class.java)
    private var offset = 0
    private val cnt = 15

    private var showNums = 0

    init {
        View.inflate(context, R.layout.club_member_view_layout, this)

        val array = context.obtainStyledAttributes(attrs, R.styleable.ClubMemberView)
        array?.let {
            showNums = it.getInt(R.styleable.ClubMemberView_showNums, 6)
        }
        array?.recycle()

        recyclerView = this.findViewById(R.id.recycler_view)
        adapter = ClubMemberAdapter(showNums)
        recyclerView.layoutManager = GridLayoutManager(context, showNums)
        recyclerView.adapter = adapter

        adapter.listener = { position, model ->
            model?.userInfoModel?.userId?.let {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", it)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        }
    }

    fun loadData(callback: () -> Unit?) {
        launch {
            val result = subscribe(RequestControl("initData", ControlType.CancelThis)) {
                clubServerApi.getClubMemberList(clubID, 0, cnt)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("items"), ClubMemberInfoModel::class.java)
                adapter.mTotal = memberCnt
                adapter.mDataList.clear()
                if (!list.isNullOrEmpty()) {
                    adapter.mDataList.addAll(list)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun destroy() {
        cancel()
    }
}