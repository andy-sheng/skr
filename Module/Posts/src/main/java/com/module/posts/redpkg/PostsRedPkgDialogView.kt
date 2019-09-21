package com.module.posts.redpkg

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.component.person.utils.StringFromatUtils
import com.module.posts.R
import com.module.posts.watch.PostsWatchServerApi
import com.module.posts.watch.model.PostsRedPkgModel
import com.module.posts.watch.model.PostsWatchModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 红包的弹窗
 */
class PostsRedPkgDialogView(var activity: Activity, var model: PostsRedPkgModel) : ConstraintLayout(activity), CoroutineScope by MainScope() {

    var mDialogPlus: DialogPlus? = null

    val container: ConstraintLayout
    val redpkgNumTv: TextView
    val redpkgDescTv: TextView
    val redpkgStatusTv: TextView
    val recyclerView: RecyclerView

    val adapter = PostsRPUserAdapter()
    val postsWatchServerApi = ApiManager.getInstance().createService(PostsWatchServerApi::class.java)

    init {
        View.inflate(context, R.layout.posts_red_pkg_dialog_view_layout, this)

        container = this.findViewById(R.id.container)
        redpkgNumTv = this.findViewById(R.id.redpkg_num_tv)
        redpkgDescTv = this.findViewById(R.id.redpkg_desc_tv)
        recyclerView = this.findViewById(R.id.recycler_view)
        redpkgStatusTv = this.findViewById(R.id.redpkg_status_tv)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        getRedPacketDetail()

        container.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss()
            }
        })
    }

    private fun getRedPacketDetail() {
        launch {
            val result = subscribe(RequestControl("getRedPacketDetail", ControlType.CancelThis)) {
                postsWatchServerApi.getRedPkgDetail(MyUserInfoManager.getInstance().uid, model.redpacketID)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("records"), PostsRedPkgUserModel::class.java)
                val model = JSON.parseObject(result.data.getString("redpacketInfo"), PostsRedPkgModel::class.java)
                showRedPkgDetail(list, model)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun showRedPkgDetail(list: List<PostsRedPkgUserModel>?, postsRedPkgModel: PostsRedPkgModel?) {
        postsRedPkgModel?.let {
            model = postsRedPkgModel
            redpkgNumTv.text = it.redpacketDesc
            when (it.status) {
                PostsRedPkgModel.RS_UN_AUDIT -> {
                    redpkgDescTv.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    redpkgStatusTv.visibility = View.VISIBLE
                    redpkgStatusTv.text = "审核通过后，开始发放倒计时"
                }
                PostsRedPkgModel.RS_ONGING -> {
                    redpkgDescTv.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    redpkgStatusTv.visibility = View.VISIBLE
                    redpkgStatusTv.text = "倒计时: ${U.getDateTimeUtils().formatVideoTime(it.resTimeMs)}"
                }
                PostsRedPkgModel.RS_GET_PART -> {
                    redpkgDescTv.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    redpkgStatusTv.visibility = View.GONE
                    redpkgDescTv.text = "红包已过期"
                    list?.let { userList ->
                        adapter.mDataList.clear()
                        adapter.mDataList.addAll(userList)
                        adapter.notifyDataSetChanged()
                    }
                }
                PostsRedPkgModel.RS_GET_ALL -> {
                    redpkgDescTv.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    redpkgStatusTv.visibility = View.GONE
                    redpkgDescTv.text = "红包已瓜分完毕"
                    list?.let { userList ->
                        adapter.mDataList.clear()
                        adapter.mDataList.addAll(userList)
                        adapter.notifyDataSetChanged()
                    }
                }
                else -> {
                    // todo donothing
                }
            }
        }
    }

    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(activity)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}