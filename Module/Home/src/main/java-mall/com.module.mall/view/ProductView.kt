package com.module.mall.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.home.R
import com.module.mall.MallServerApi
import com.module.mall.adapter.ProductAdapter
import com.module.mall.event.BuyMallSuccessEvent
import com.module.mall.event.ShowEffectEvent
import com.module.mall.loadsir.MallEmptyCallBack
import com.module.mall.model.ProductModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ProductView : ExConstraintLayout {
    val TAG = "ProductView" + hashCode()
    var recyclerView: RecyclerView
    var refreshLayout: SmartRefreshLayout

    var displayType = -1

    var productAdapter: ProductAdapter? = null

    val rankedServerApi = ApiManager.getInstance().createService(MallServerApi::class.java)

    var offset = 0

    var limit = 30

    var hasMore = true

    internal var mLoadService: LoadService<*>

    var buyEffectDialogView: BuyEffectDialogView? = null

    var hasPostProductModel: ProductModel? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    var selectedIndex = -1

    init {
        View.inflate(context, R.layout.product_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        recyclerView.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        productAdapter = ProductAdapter {
            selectedIndex
        }

        recyclerView.adapter = productAdapter
        productAdapter?.notifyDataSetChanged()
        productAdapter?.clickItemMethod = {
            if (buyEffectDialogView == null) {
                buyEffectDialogView = BuyEffectDialogView(context)
            }
            buyEffectDialogView?.showByDialog(true, it)
        }

        productAdapter?.selectItemMethod = { productModel, i ->
            if (selectedIndex != i) {
                val pre = selectedIndex
                selectedIndex = i
                if (pre > -1) {
                    productAdapter?.notifyItemChanged(pre, 1)
                }

                productAdapter?.notifyItemChanged(selectedIndex, 1)
                EventBus.getDefault().post(ShowEffectEvent(productModel))
                hasPostProductModel = productModel
            }
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
                .addCallback(MallEmptyCallBack())
                .build()

        mLoadService = mLoadSir.register(refreshLayout) { tryLoad() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BuyMallSuccessEvent) {
        if (event.productModel.displayType == displayType) {
            for (i in 0 until ((productAdapter?.dataList?.size) ?: 0)) {
                if (productAdapter?.dataList?.get(i)?.goodsID == event.productModel.goodsID) {
                    productAdapter?.dataList?.get(i)?.isBuy = true
                    productAdapter?.notifyItemChanged(i, 1)
                    break
                }
            }
        }
    }

    fun clearAndLoad() {
        post {
            hasMore = true
            productAdapter?.dataList?.clear()
            offset = 0
            productAdapter?.notifyDataSetChanged()
            tryLoad()
        }
    }

    fun selected() {
        if (productAdapter?.dataList?.size == 0) {
            tryLoad()
        } else {
            hasPostProductModel?.let {
                EventBus.getDefault().post(ShowEffectEvent(it))
            }
        }
    }

    private fun tryLoad() {
        MyLog.d(TAG, "tryLoad " + TAG)
        if (!hasMore) {
            return
        }

        launch {
            val obj = subscribe(RequestControl("$TAG tryLoad", ControlType.CancelThis)) {
                rankedServerApi.getProductList(displayType, offset, limit)
            }

            refreshLayout.finishLoadMore()
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("list"), ProductModel::class.java)
                if (list != null && list.size > 0) {
                    productAdapter?.insertListLast(list)
                    if (offset == 0) {
                        selectedIndex = 0
                        productAdapter?.notifyItemChanged(0, 1)
                        EventBus.getDefault().post(ShowEffectEvent(productAdapter!!.dataList[0]!!))
                        hasPostProductModel = productAdapter!!.dataList[0]!!
                    }
                    mLoadService.showSuccess()
                } else {
                    if (productAdapter?.dataList?.size == 0) {
                        mLoadService.showCallback(MallEmptyCallBack::class.java)
                    }
                }

                offset = obj.data.getIntValue("offset")
                hasMore = obj.data.getBooleanValue("hasMore")
                refreshLayout.setEnableLoadMore(hasMore)
            } else {
                if (productAdapter?.dataList?.size == 0) {
                    mLoadService.showCallback(MallEmptyCallBack::class.java)
                } else {
                    U.getToastUtil().showShort(obj.errmsg)
                }
            }
        }
    }
}