package com.module.home.activity

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.home.R
import com.module.home.WalletServerApi
import com.module.home.fragment.DiamondBallanceFragment
import com.module.home.fragment.InComeFragment
import com.module.home.model.ExChangeInfoModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder


@Route(path = RouterConstants.ACTIVITY_WALLET)
class WalletActivity : BaseActivity() {
    lateinit var mainActContainer: ConstraintLayout
    lateinit var titlebar: CommonTitleBar
    lateinit var slidingTab: SlidingTabLayout
    lateinit var viewPager: NestViewPager
    private var tabPagerAdapter: FragmentPagerAdapter? = null

    internal var mDqRuleDialogPlus: DialogPlus? = null

    internal var mExChangeInfoModel: ExChangeInfoModel? = null

    val mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return com.module.home.R.layout.wallet_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mainActContainer = findViewById(com.module.home.R.id.main_act_container)
        titlebar = findViewById(com.module.home.R.id.titlebar)
        slidingTab = findViewById(com.module.home.R.id.slidingTab)
        viewPager = findViewById(com.module.home.R.id.viewPager)

        slidingTab?.apply {
            setCustomTabView(com.module.home.R.layout.ranked_tab_view, com.module.home.R.id.tab_tv)
            setSelectedIndicatorColors(Color.parseColor("#596CCC"))
            setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
            setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
            setIndicatorWidth(U.getDisplayUtils().dip2px(56f))
            setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f))
            setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
            setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        }

        titlebar?.leftTextView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                finish()
            }
        })

        titlebar?.rightTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mDqRuleDialogPlus == null) {
                    mDqRuleDialogPlus = DialogPlus.newDialog(this@WalletActivity)
                            .setContentHolder(ViewHolder(R.layout.dq_rule_layout))
                            .setGravity(Gravity.CENTER)
                            .setContentBackgroundResource(com.module.home.R.color.transparent)
                            .setOverlayBackgroundResource(com.module.home.R.color.black_trans_80)
                            .setExpanded(false)
                            .setCancelable(true)
                            .create()
                }

                mDqRuleDialogPlus?.show()
                getRule()
            }
        })

        tabPagerAdapter = object : FragmentPagerAdapter(getSupportFragmentManager()) {
            override fun getItem(position: Int): Fragment {
                MyLog.d(TAG, "getItem position=$position")
                if (position == 0) {
                    return DiamondBallanceFragment()
                } else if (position == 1) {
                    return InComeFragment()
                }
                return InComeFragment()
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "余额"
                    1 -> "收益"
                    else -> super.getPageTitle(position)
                }
            }
        }

        slidingTab?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                slidingTab?.notifyDataChange()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewPager?.adapter = tabPagerAdapter
        slidingTab?.setViewPager(viewPager)
        tabPagerAdapter?.notifyDataSetChanged()
        viewPager?.setCurrentItem(0, false)
    }

    private fun getRule() {
        if (mExChangeInfoModel != null) {
            showRule(mExChangeInfoModel!!)
            return
        }

        ApiMethods.subscribe(mWalletServerApi.getExChangeInfo(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mExChangeInfoModel = JSON.parseObject(obj.data!!.toString(), ExChangeInfoModel::class.java)
                    mExChangeInfoModel?.let {
                        showRule(it)
                    }
                }
            }
        })
    }

    fun showRule(exChangeInfoModel: ExChangeInfoModel) {
        MyLog.d(TAG, "showRule exChangeInfoModel=$exChangeInfoModel")
        val ruleOneArea = mDqRuleDialogPlus?.findViewById(R.id.rule_one_area) as LinearLayout
        val toHZDescTv = mDqRuleDialogPlus?.findViewById(R.id.toHZDescTv) as ExTextView
        val ruleTwoArea = mDqRuleDialogPlus?.findViewById(R.id.rule_two_area) as LinearLayout
        val toZSDescTv = mDqRuleDialogPlus?.findViewById(R.id.toZSDescTv) as ExTextView
        val ruleThreeArea = mDqRuleDialogPlus?.findViewById(R.id.rule_three_area) as LinearLayout
        val toCashDescTv = mDqRuleDialogPlus?.findViewById(R.id.toCashDescTv) as ExTextView
        val ruleFourArea = mDqRuleDialogPlus?.findViewById(R.id.rule_four_area) as LinearLayout
        val ruleFour = mDqRuleDialogPlus?.findViewById(R.id.rule_four) as ExTextView

        val linearLayouts = arrayOf<LinearLayout>(ruleOneArea, ruleTwoArea, ruleThreeArea, ruleFourArea)
        val exTextViews = arrayOf<ExTextView>(toHZDescTv, toZSDescTv, toCashDescTv, ruleFour)

        for (i in 0 until if (exChangeInfoModel.rule.size > 4) 4 else exChangeInfoModel.rule.size) {
            linearLayouts[i].visibility = View.VISIBLE
            exTextViews[i].text = exChangeInfoModel.rule[i]
        }
    }

    override fun canSlide(): Boolean {
        return false
    }


    override fun useEventBus(): Boolean {
        return false
    }
}
