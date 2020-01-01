package com.module.club.manage.list

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.userinfo.model.ClubInfo
import com.common.rxretrofit.ApiManager
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.IClubListView
import com.module.club.IClubModuleService
import com.module.club.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class ClubListView(context: Context) : ConstraintLayout(context), IClubListView, CoroutineScope by MainScope() {

    private val refreshLayout: SmartRefreshLayout
    private val recyclerView: RecyclerView

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private val adapter: ClubListAdapter

    init {
        View.inflate(context, R.layout.club_list_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        adapter = ClubListAdapter(object : ClubListAdapter.Listener {
            override fun onClickItem(position: Int, model: ClubInfo?) {
                model?.let {
                    val clubServices = ARouter.getInstance().build(RouterConstants.SERVICE_CLUB).navigation() as IClubModuleService
                    clubServices.tryGoClubHomePage(it.clubID)
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    override fun initData(flag: Boolean) {

    }

    override fun stopTimer() {

    }

    override fun destory() {

    }
}