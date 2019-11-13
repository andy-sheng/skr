package com.module.playways.room.record

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.room.gift.GiftServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

// 礼物记录页面
@Route(path = RouterConstants.ACTIVITY_GIFT_RECORD)
class GiftRecordActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView

    var adapter: GiftRecordAdapter = GiftRecordAdapter()

    var offset: Int = 0
    var hasMore = true
    val mCnt = 20

    lateinit var mLoadService: LoadService<*>

    private val giftServerApi = ApiManager.getInstance().createService(GiftServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.gift_record_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        contentRv = findViewById(R.id.content_rv)

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        refreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getGiftRecordList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        contentRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        contentRv.adapter = adapter

        adapter.onClickAvatarListener = { model, _ ->
            model?.userInfo?.userId?.let {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", it)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        }

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.gift_record_empty_icon, "暂无送礼记录", "#8c3B4E79"))
                .build()
        mLoadService = mLoadSir.register(refreshLayout) { getGiftRecordList(0, true) }

        getGiftRecordList(0, true)
    }

    private fun getGiftRecordList(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getGiftRecordList", ControlType.CancelThis)) {
                giftServerApi.getGiftRecordList(MyUserInfoManager.uid.toInt(), off, mCnt)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("details"), GiftRecordModel::class.java)
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                addList(list, isClean)
            } else {
                refreshLayout.finishRefresh()
                refreshLayout.finishLoadMore()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun addList(list: List<GiftRecordModel>?, isClean: Boolean) {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
        refreshLayout.setEnableLoadMore(hasMore)
        if (isClean) {
            adapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
            }
            adapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
                adapter?.notifyDataSetChanged()
            }
        }

        if (adapter?.mDataList.isNullOrEmpty()) {
            mLoadService.showCallback(EmptyCallback::class.java)
        } else {
            mLoadService.showSuccess()
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}