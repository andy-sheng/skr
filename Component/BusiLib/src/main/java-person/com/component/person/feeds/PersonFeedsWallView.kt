package com.component.person.feeds

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.UserInfoModel
import com.common.rxretrofit.ApiManager
import com.component.busilib.R
import com.component.person.view.RequestCallBack
import com.didichuxing.doraemonkit.ui.base.BaseFragment

/**
 * 神曲墙
 */
class PersonFeedsWallView(val fragment: BaseFragment, var infoModel: UserInfoModel, val requestCallBack: RequestCallBack) : RelativeLayout(fragment.context) {

    private val mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    var mFeedsView: RecyclerView

    init {
        View.inflate(context, R.layout.person_feeds_wall_view_layout, this)

        mFeedsView = findViewById(R.id.feeds_view)
        mFeedsView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


    }

}