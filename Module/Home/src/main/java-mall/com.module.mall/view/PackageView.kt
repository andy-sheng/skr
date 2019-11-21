package com.module.mall.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.home.R
import com.module.home.loadsir.DqEmptyCallBack
import com.module.mall.MallServerApi
import com.module.mall.adapter.PackageAdapter
import com.module.mall.model.PackageModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

class PackageView : ExConstraintLayout {
    val TAG = "PackageView" + hashCode()
    var recyclerView: RecyclerView
    var refreshLayout: SmartRefreshLayout

    var displayType = -1

    var productAdapter: PackageAdapter? = null

    val rankedServerApi = ApiManager.getInstance().createService(MallServerApi::class.java)

    var offset = 0

    var limit = 30

    var hasMore = true

    internal var mLoadService: LoadService<*>

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.product_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        recyclerView.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        productAdapter = PackageAdapter()
        recyclerView.adapter = productAdapter
        productAdapter?.notifyDataSetChanged()
        productAdapter?.useEffectMethod = {
            useEffect(it)
        }

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(true)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        refreshLayout.setEnableOverScrollDrag(false)
        refreshLayout.setRefreshHeader(ClassicsHeader(context))
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                tryLoad()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        val mLoadSir = LoadSir.Builder()
                .addCallback(DqEmptyCallBack())
                .build()

        mLoadService = mLoadSir.register(refreshLayout) { tryLoad() }
    }

    fun useEffect(packageModel: PackageModel) {
        launch {
            val map = HashMap<String, Any>()
            map["packetItemID"] = packageModel.packetItemID

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe {
                rankedServerApi.useGoods(body)
            }

            if (obj.errno == 0) {
                packageModel.useStatus = 2
                productAdapter?.notifyDataSetChanged()
            } else {
                U.getToastUtil().showShort(obj.errmsg)
            }
        }
    }

    fun selected() {
        if (productAdapter?.dataList?.size == 0) {
            tryLoad()
        }
    }

    private fun tryLoad() {
        if (!hasMore) {
            return
        }

        launch {
            val obj = subscribe(RequestControl("$TAG tryLoad", ControlType.CancelThis)) {
                rankedServerApi.getPacketList(displayType, offset, limit)
            }

            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("list"), PackageModel::class.java)
                if (list != null && list.size > 0) {
                    productAdapter?.insertListLast(list)
                }

                offset = obj.data.getIntValue("offset")
                hasMore = obj.data.getBooleanValue("hasMore")
                mLoadService.showSuccess()
                refreshLayout.setEnableLoadMore(hasMore)
            } else {
                if (productAdapter?.dataList?.size == 0) {
                    mLoadService.showCallback(DqEmptyCallBack::class.java)
                } else {
                    U.getToastUtil().showShort(obj.errmsg)
                }
            }
        }
    }
}