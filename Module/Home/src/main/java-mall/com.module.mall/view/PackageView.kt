package com.module.mall.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.userinfo.model.UserInfoModel
import com.common.rxretrofit.*
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.dialog.view.TipsDialogView
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.ModuleServiceManager
import com.module.home.R
import com.module.mall.MallServerApi
import com.module.mall.RelationCardUtils
import com.module.mall.activity.MallActivity
import com.module.mall.adapter.PackageAdapter
import com.module.mall.event.MallUseCoinEvent
import com.module.mall.event.PackageShowEffectEvent
import com.module.mall.event.ShowDefaultEffectEvent
import com.module.mall.loadsir.MallEmptyCallBack
import com.module.mall.model.PackageModel
import com.module.mall.model.ProductModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import useroperate.OperateFriendActivity
import useroperate.inter.AbsRelationOperate
import java.lang.ref.WeakReference
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

    var selectedIndex = -1
    var selectedPackageItemId = ""

    var hasPostProductModel: ProductModel? = null
    //点击去变成某种关系
    var toRelationCardModel: PackageModel? = null

    internal var tipsDialogView: TipsDialogView? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.product_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        recyclerView.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)

        productAdapter = PackageAdapter({
            selectedPackageItemId
        }, { packetItemID, index ->
            selectedPackageItemId = packetItemID
            selectedIndex = index
        })

        recyclerView.adapter = productAdapter
        productAdapter?.notifyDataSetChanged()
        productAdapter?.useEffectMethod = {
            if (it.goodsInfo?.displayType == MallActivity.Companion.MALL_TYPE.CARD.value) {
                toRelationCardModel = it

                OperateFriendActivity.open(OperateFriendActivity.Companion.Builder()
                        .setIsEnableFriend(true)
                        .setText("邀请")
                        .setListener(AbsRelationOperate.ClickListener { weakReference, _, _, userInfoModel, _ ->
                            userInfoModel?.let {
                                checkRelation(userInfoModel, weakReference)
                            }
                        }))
            } else {
                useEffect(it)
            }
        }

        productAdapter?.cancelUseEffectMethod = {
            cancelUseEffect(it)
        }

        productAdapter?.selectItemMethod = { productModel, index, packageItemID ->
            if (selectedIndex != index) {
                val pre = selectedIndex
                selectedIndex = index
                selectedPackageItemId = packageItemID
                if (pre > -1) {
                    productAdapter?.notifyItemChanged(pre, 1)
                }

                productAdapter?.notifyItemChanged(selectedIndex, 1)
                EventBus.getDefault().post(PackageShowEffectEvent(productModel))
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

    private fun checkRelation(userInfoModel: UserInfoModel, weakReference: WeakReference<BaseActivity>?) {
        val map = mutableMapOf("goodsID" to toRelationCardModel?.goodsInfo?.goodsID, "otherUserID" to userInfoModel.userId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(rankedServerApi.checkCardRelation(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val msg = result.data!!.getString("noticeMsg")
                    showInviteCardDialog(userInfoModel, msg, weakReference)
                } else {
                    //ErrAlreadyHasRelation           = 8428114; //对方已是你的闺蜜，不能再发送邀请哦～
                    //ErrApplyAfter24Hour             = 8428115; //24小时之后才能再次发送关系申请哦～
                    //ErrAlreadyHasOtherRelation      = 8428116; //对方已是你的闺蜜，对方接受邀请将自动解除你们原来的关系哦～
                    if (8428114 == result.errno) {
                        U.getToastUtil().showShort(result.errmsg)
                    } else if (8428115 == result.errno) {
                        U.getToastUtil().showShort(result.errmsg)
                    } else if (8428116 == result.errno) {
                        showInviteCardDialog(userInfoModel, result.errmsg, weakReference)
                    }
                }
            }
        }, RequestControl("checkCardRelation", ControlType.CancelLast))
    }

    /**
     * 邀请好友发生关系
     *
     * @param userInfoModel
     */
    private fun showInviteCardDialog(userInfoModel: UserInfoModel, msg: String, weakReference: WeakReference<BaseActivity>?) {
        if (tipsDialogView != null) {
            tipsDialogView?.dismiss(false)
            tipsDialogView = null
        }

        weakReference?.get()?.let {
            tipsDialogView = TipsDialogView.Builder(it)
                    .setMessageTip(msg)
                    .setCancelBtnClickListener {
                        if (tipsDialogView != null) {
                            tipsDialogView?.dismiss(false)
                        }
                        tipsDialogView = null
                    }
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener {
                        weakReference?.get()?.finish()
                        tipsDialogView = null
                        inviteToRelation(userInfoModel.userId, toRelationCardModel!!)
                    }
                    .setConfirmTip("邀请")
                    .build()
            tipsDialogView?.showByDialog()
        }

    }

    //亲故发生某种关系
    private fun inviteToRelation(userID: Int, packageModel: PackageModel) {
        launch {
            val map = HashMap<String, Any>()
            map["packetItemID"] = packageModel.packetItemID
            map["toUserID"] = userID

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe {
                rankedServerApi.useGoods(body)
            }

            if (obj.errno == 0) {
                offset--
                productAdapter?.dataList?.remove(packageModel)
                productAdapter?.notifyDataSetChanged()

                if (productAdapter?.dataList?.size == 0) {
                    mLoadService.showCallback(MallEmptyCallBack::class.java)
                }

                U.getToastUtil().showShort("邀请成功")
                // 生成一条邀请IM消息
                var msgService = ModuleServiceManager.getInstance().msgService
                msgService?.sendRelationInviteMsg(userID.toString(), obj.data.getString("uniqID"), obj.data.getString("msgContent"))
            } else {
                U.getToastUtil().showShort(obj.errmsg)
            }
        }
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
                if (displayType == 6) {
                    offset--
                    productAdapter?.dataList?.remove(packageModel)
                    productAdapter?.notifyDataSetChanged()

                    if (productAdapter?.dataList?.size == 0) {
                        mLoadService.showCallback(MallEmptyCallBack::class.java)
                    }

                    EventBus.getDefault().post(MallUseCoinEvent())
                    U.getToastUtil().showShort("金币到账成功")
                } else {
                    for (i in 0 until ((productAdapter?.dataList?.size) ?: 0)) {
                        productAdapter?.dataList?.get(i)?.let {
                            if (it.packetItemID == packageModel.packetItemID) {
                                it.useStatus = 2
                                selectedPackageItemId = it.packetItemID
                                selectedIndex = i
                                EventBus.getDefault().post(PackageShowEffectEvent(it.goodsInfo!!))
                            } else {
                                it.useStatus = 1
                            }
                        }
                    }
                    productAdapter?.notifyDataSetChanged()
                }
            } else {
                U.getToastUtil().showShort(obj.errmsg)
            }
        }
    }

    fun cancelUseEffect(packageModel: PackageModel) {
        launch {
            val map = HashMap<String, Any>()
            map["packetItemID"] = packageModel.packetItemID

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe {
                rankedServerApi.cancelUseGoods(body)
            }

            if (obj.errno == 0) {
                for (i in 0 until ((productAdapter?.dataList?.size) ?: 0)) {
                    if (productAdapter?.dataList?.get(i)?.packetItemID == packageModel.packetItemID) {
                        productAdapter?.dataList?.get(i)?.useStatus = 1
                        hasPostProductModel = null
                        selectedPackageItemId = ""
                        selectedIndex = -1
                        EventBus.getDefault().post(ShowDefaultEffectEvent(displayType))
                        productAdapter?.notifyItemChanged(i, 1)
                        break
                    }
                }
            } else {
                U.getToastUtil().showShort(obj.errmsg)
            }
        }
    }

    fun selected() {
        if (productAdapter?.dataList?.size == 0) {
            if (hasMore) {
                tryLoad()
            } else {
                EventBus.getDefault().post(ShowDefaultEffectEvent(displayType))
            }
        } else {
            if (hasPostProductModel == null) {
                EventBus.getDefault().post(ShowDefaultEffectEvent(displayType))
            } else {
                EventBus.getDefault().post(PackageShowEffectEvent(hasPostProductModel!!))
            }
        }
    }

    private fun showSelectedEffect(list: List<ProductModel>?) {
        if (list != null && list.size > 0) {
            EventBus.getDefault().post(PackageShowEffectEvent(list[0]))
            hasPostProductModel = list[0]
        } else {
            EventBus.getDefault().post(ShowDefaultEffectEvent(displayType))
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

            refreshLayout.finishLoadMore()
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("list"), PackageModel::class.java)
                if (list != null && list.size > 0) {
                    productAdapter?.insertListLast(list)
                    if (offset == 0) {
                        val list = JSON.parseArray(obj.data.getString("useList"), ProductModel::class.java)
                        showSelectedEffect(list)
                    }
                    mLoadService.showSuccess()
                } else {
                    if (productAdapter?.dataList?.size == 0) {
                        mLoadService.showCallback(MallEmptyCallBack::class.java)
                    }

                    if (offset == 0) {
                        EventBus.getDefault().post(ShowDefaultEffectEvent(displayType))
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