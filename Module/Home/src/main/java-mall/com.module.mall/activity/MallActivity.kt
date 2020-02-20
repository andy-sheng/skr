package com.module.mall.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.SlidingTabLayout
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.IHomeService
import com.module.home.R
import com.module.home.WalletServerApi
import com.module.home.fragment.HalfRechargeFragment
import com.module.mall.MallServerApi
import com.module.mall.event.*
import com.module.mall.model.MallTag
import com.module.mall.view.EffectView
import com.module.mall.view.ProductView
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import useroperate.OperateFriendActivity
import useroperate.inter.AbsRelationOperate
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

@Route(path = RouterConstants.ACTIVITY_MALL_MALL)
class MallActivity : BaseActivity() {

    lateinit var title: CommonTitleBar
    lateinit var btnBack: ImageView
    lateinit var mallTv: ExTextView
    lateinit var effectView: EffectView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: ViewPager
    lateinit var diamondTv: ExTextView
    var diamondCount: Float = 0.0f

    var pagerAdapter: PagerAdapter? = null
    var viewList: ArrayList<ProductView>? = null

    var callWhenResume: (() -> Unit)? = null

    var tipsDialogView: TipsDialogView? = null

    var giveMallEvent: GiveMallEvent? = null

    val rankedServerApi = ApiManager.getInstance().createService(MallServerApi::class.java)
    val mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi::class.java)

    companion object {
        val supportDisplayTypeSet = hashSetOf(MALL_TYPE.BG_TYPE.value, MALL_TYPE.LIGHT.value, MALL_TYPE.COIN.value, MALL_TYPE.CARD.value)

        enum class MALL_TYPE(val type: Int) {
            BG_TYPE(4), LIGHT(5), COIN(6), CARD(7);

            val value: Int
                get() = type
        }

    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.mall_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
        title = findViewById(R.id.title)
        btnBack = findViewById(R.id.btn_back)
        mallTv = findViewById(R.id.mall_tv)
        effectView = findViewById(R.id.effect_view)
        tagTab = findViewById(R.id.tag_tab)
        viewpager = findViewById(R.id.viewpager)
        diamondTv = findViewById(R.id.diamond_tv)

        viewpager.offscreenPageLimit = 100

        tagTab.setCustomTabView(R.layout.mall_pager_tab, R.id.tab_tv)
        tagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        tagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        tagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
        tagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        tagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())

        viewList = ArrayList()

        diamondTv.setDebounceViewClickListener {
            showRechargeDialog()
        }

        mallTv.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_MALL_PACKAGE)
                    .navigation()

            callWhenResume = {
                getZSBalance()
            }
        }

        btnBack.setDebounceViewClickListener {
            finish()
        }

        loadTags()
        getZSBalance()
    }

    override fun onResume() {
        super.onResume()
        callWhenResume?.invoke()
        callWhenResume = null
    }

    fun initAdapter(list: List<MallTag>) {
        for (mallTag in list) {
            viewList?.add(ProductView(this).apply {
                displayType = mallTag.displayType
            })
        }

        pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = viewList?.get(position)
                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getCount(): Int {
                return list.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return list[position].displayTypeDesc
            }
        }

        tagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewList?.get(position)?.selected()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewpager.adapter = pagerAdapter
        tagTab.setViewPager(viewpager)

        val paramTag = intent.getIntExtra("tag", 0)
        if (supportDisplayTypeSet.contains(paramTag)) {
            var exist = false
            list?.forEachIndexed { index, mallTag ->
                if (paramTag == mallTag.displayType) {
                    viewpager.setCurrentItem(index, false)
                    viewList?.get(index)?.selected()
                    exist = true
                    return@forEachIndexed
                }
            }

            if (!exist) {
                viewList?.get(0)?.selected()
            }
        } else {
            viewList?.get(0)?.selected()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MallUseCoinEvent) {
        viewList?.let {
            for (view in it) {
                if (view.displayType == 6) {
                    view.clearAndLoad()
                    break
                }
            }
        }
    }

    fun loadTags() {
        launch {
            val obj = subscribe {
                rankedServerApi.getMallDisplayTags()
            }

            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("tags"), MallTag::class.java)
                if (list != null && list.size > 0) {
                    val supportList = ArrayList<MallTag>()

                    for (item in list) {
                        if (supportDisplayTypeSet.contains(item.displayType)) {
                            supportList.add(item)
                        }
                    }

                    initAdapter(supportList)
                }
            } else {
                U.getToastUtil().showShort(obj.errmsg)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowEffectEvent) {
        when (event.productModel.displayType) {
            MALL_TYPE.BG_TYPE.value -> effectView.showBgEffect(event.productModel)
            MALL_TYPE.LIGHT.value -> effectView.showLightEffect(event.productModel)
            MALL_TYPE.COIN.value -> effectView.showCoinEffect(event.productModel)
            MALL_TYPE.CARD.value -> effectView.showCoinEffect(event.productModel)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GiveMallEvent) {
        if (event.price.realPrice / 1000 > diamondCount!!) {
            showRechargeDialog()
            return
        }

        giveMallEvent = event

//        ARouter.getInstance()
//                .build(RouterConstants.ACTIVITY_RELATION)
//                .withInt("from", 1)
//                .navigation()

        OperateFriendActivity.open(OperateFriendActivity.Companion.Builder()
                .setIsEnableFans(true)
                .setIsEnableFriend(true)
                .setIsEnableFollow(true)
                .setText("赠送")
                .setListener(AbsRelationOperate.ClickListener { weakReference, _, _, userInfoModel, _ ->
                    userInfoModel?.let {
                        showGiveDialog(it, weakReference)
                    }
                }))

        EventBus.getDefault().postSticky(SelectMallStickyEvent(event.productModel, event.price))
    }

    private fun showGiveDialog(userInfoModel: UserInfoModel, weakReference: WeakReference<BaseActivity>?) {
        if (tipsDialogView != null) {
            tipsDialogView?.dismiss(false)
            tipsDialogView = null
        }

        val channelService = ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation() as IHomeService

        weakReference?.get()?.let {
            tipsDialogView = TipsDialogView.Builder(it)
                    .setMessageTip("是否赠送给" + userInfoModel.nickname + channelService.selectedMallName + "?")
                    .setCancelBtnClickListener {
                        if (tipsDialogView != null) {
                            tipsDialogView?.dismiss(false)
                        }
                        tipsDialogView = null
                    }
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener {
                        giveMall(userInfoModel.userId)
                        weakReference?.get()?.finish()
                        tipsDialogView = null
                    }
                    .setConfirmTip("赠送")
                    .build()
            tipsDialogView?.showByDialog()
        }
    }

    fun giveMall(userID: Int) {
        giveMallEvent?.let {
            launch {
                val map = HashMap<String, Any>()
                map["buyType"] = it.price.buyType
                map["count"] = 1
                map["goodsID"] = it.productModel.goodsID
                map["priceType"] = it.price.priceType
                map["receiveUserID"] = userID

                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val obj = subscribe {
                    rankedServerApi.presentGoods(body)
                }

                if (obj.errno == 0) {
                    getZSBalance()

                    tipsDialogView?.dismiss(false)
                    tipsDialogView = TipsDialogView.Builder(this@MallActivity)
                            .setMessageTip("商品已成功赠送给好友，提醒ta查收哦～")
                            .setOkBtnTip("确定")
                            .setOkBtnClickListener {
                                tipsDialogView?.dismiss()
                            }
                            .build()
                    tipsDialogView?.showByDialog()
                } else {
                    if (8428101 == obj.errno) {
                        showRechargeDialog()
                    }
                    U.getToastUtil().showShort(obj.errmsg)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BuyMallEvent) {
        launch {
            val map = HashMap<String, Any>()
            map["buyType"] = event.price.buyType
            map["count"] = 1
            map["goodsID"] = event.productModel.goodsID
            map["priceType"] = event.price.priceType

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe {
                rankedServerApi.buyMall(body)
            }

            if (obj.errno == 0) {
                if (event.productModel.displayType != MALL_TYPE.COIN.value && event.productModel.displayType != MALL_TYPE.CARD.value) {
                    if (event.price.buyType == 1) {
                        event.productModel.buyStatus = 1
                    } else if (event.price.buyType == 2) {
                        event.productModel.buyStatus = 2
                    }
                }

                EventBus.getDefault().post(BuyMallSuccessEvent(event.productModel))
//                U.getToastUtil().showShort("购买成功")
                getZSBalance()

                tipsDialogView?.dismiss(false)
                tipsDialogView = TipsDialogView.Builder(this@MallActivity)
                        .setMessageTip("商品已购买成功，加入你的背包啦～")

                        .setOkBtnTip("确定")
                        .setOkBtnClickListener {
                            tipsDialogView?.dismiss()
                        }
                        .build()
                tipsDialogView?.showByDialog()
            } else {
                if (8428101 == obj.errno) {
                    showRechargeDialog()
                }
                U.getToastUtil().showShort(obj.errmsg)
            }
        }
    }

    fun showRechargeDialog() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, HalfRechargeFragment::class.java)
                        .setEnterAnim(R.anim.slide_in_bottom)
                        .setExitAnim(R.anim.slide_out_bottom)
                        .setAddToBackStack(true)
                        .setFragmentDataListener(object : FragmentDataListener {
                            override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                                //充值成功
                                if (requestCode == 100 && resultCode == 0) {
                                    getZSBalance()
                                }
                            }
                        })
                        .setHasAnimation(true)
                        .build())
    }

    fun getZSBalance() {
        ApiMethods.subscribe(mWalletServerApi.getZSBalance(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                MyLog.w(TAG, "getZSBalance process obj=$obj")
                if (obj.errno == 0) {
                    val amount = obj.data!!.getString("totalAmountStr")
                    diamondTv.text = "$amount"
                    diamondCount = amount.toFloat()
                }
            }
        }, this)
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return true
    }
}