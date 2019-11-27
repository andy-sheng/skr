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
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.home.R
import com.module.home.WalletServerApi
import com.module.mall.MallServerApi
import com.module.mall.event.PackageShowEffectEvent
import com.module.mall.event.ShowDefaultEffectEvent
import com.module.mall.model.MallTag
import com.module.mall.view.EffectView
import com.module.mall.view.PackageView
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_MALL_PACKAGE)
class PackageActivity : BaseActivity() {
    lateinit var title: CommonTitleBar
    lateinit var btnBack: ImageView
    lateinit var effectView: EffectView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: ViewPager
    lateinit var diamondTv: ExTextView

    var pagerAdapter: PagerAdapter? = null
    var viewList: ArrayList<PackageView>? = null

    val rankedServerApi = ApiManager.getInstance().createService(MallServerApi::class.java)
    val mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi::class.java)

    var callWhenResume: (() -> Unit)? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.package_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
        title = findViewById(R.id.title)
        btnBack = findViewById(R.id.btn_back)
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
            ARouter.getInstance().build(RouterConstants.ACTIVITY_BALANCE)
                    .navigation()

            callWhenResume = {
                getZSBalance()
            }
        }

//        mallTv.setDebounceViewClickListener {
//            ARouter.getInstance().build(RouterConstants.ACTIVITY_MALL_MALL)
//                    .navigation()
//        }

        btnBack.setDebounceViewClickListener {
            finish()
        }

        loadTags()
        getZSBalance()
    }

    fun initAdapter(list: List<MallTag>) {
        for (mallTag in list) {
            viewList?.add(PackageView(this).apply {
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
        viewList?.get(0)?.selected()
    }

    override fun onResume() {
        super.onResume()
        callWhenResume?.invoke()
        callWhenResume = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PackageShowEffectEvent) {
        when (event.productModel.displayType) {
            4 -> effectView.showBgEffect(event.productModel)
            5 -> effectView.showLightEffect(event.productModel)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowDefaultEffectEvent) {
        when (event.displayType) {
            4 -> effectView.showDefaultBgEffect()
            5 -> effectView.showDefaultLightEffect()
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
                    initAdapter(list)
                }
            } else {
                U.getToastUtil().showShort(obj.errmsg)
            }
        }
    }

    fun getZSBalance() {
        ApiMethods.subscribe(mWalletServerApi.getZSBalance(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                MyLog.w(TAG, "getZSBalance process obj=$obj")
                if (obj.errno == 0) {
                    val amount = obj.data!!.getString("totalAmountStr")
                    diamondTv.text = "$amount"
                }
            }
        }, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return true
    }
}