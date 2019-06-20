package com.module.home.ranked.fragment

import android.graphics.Color
import android.os.Bundle

import com.common.base.BaseFragment
import com.module.home.R
import com.module.home.ranked.model.RankHomeCardModel
import com.module.home.ranked.model.RankHomeCardModel.Companion.DUAN_RANK_TYPE
import com.module.home.ranked.model.RankHomeCardModel.Companion.POPULAR_RANK_TYPE
import com.module.home.ranked.model.RankHomeCardModel.Companion.REWARD_RANK_TYPE
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import org.greenrobot.greendao.query.WhereCondition

class RankedDetailFragment : BaseFragment() {

    private var mContainer: ConstraintLayout? = null
    private var mTopImage: ImageView? = null
    private var mIvBack: ImageView? = null
    private var mIvRule: ImageView? = null

    private var mModel: RankHomeCardModel? = null
    private var mDialogPlus: DialogPlus? = null

    override fun initView(): Int {
        return R.layout.ranked_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mContainer = mRootView.findViewById<View>(R.id.container) as ConstraintLayout
        mTopImage = mRootView.findViewById<View>(R.id.top_image) as ImageView
        mIvBack = mRootView.findViewById<View>(R.id.iv_back) as ImageView
        mIvRule = mRootView.findViewById<View>(R.id.iv_rule) as ImageView


        when (mModel?.rankType) {
            POPULAR_RANK_TYPE -> {
                mContainer?.setBackgroundColor(Color.parseColor("#FAA100"))
                mTopImage?.setBackgroundResource(R.drawable.renqi_bj)
            }
            DUAN_RANK_TYPE -> {
                mContainer?.setBackgroundColor(Color.parseColor("#576FE3"))
                mTopImage?.setBackgroundResource(R.drawable.duanwei_bj)
            }
            REWARD_RANK_TYPE -> {
                mContainer?.setBackgroundColor(Color.parseColor("#67BC65"))
                mTopImage?.setBackgroundResource(R.drawable.dashang_bj)
            }
            else -> {
                MyLog.w(TAG, "unknown rankType")
            }
        }

        mIvBack?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                U.getFragmentUtils().popFragment(this@RankedDetailFragment)
            }

        })

        mIvRule?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                showRankedRule()
            }
        })
    }

    private fun showRankedRule() {
        var viewResourceId: Int
        when (mModel?.rankType) {
            POPULAR_RANK_TYPE -> {
                viewResourceId = R.layout.ranked_rule_renqi_view_layout
            }
            DUAN_RANK_TYPE -> {
                viewResourceId = R.layout.ranked_rule_duan_view_layout
            }
            else -> {
                viewResourceId = R.layout.ranked_rule_reward_view_layout
            }
        }
        mDialogPlus = (DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(viewResourceId))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false))
                .setGravity(Gravity.CENTER)
                .create()
        mDialogPlus?.show()
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            mModel = data as RankHomeCardModel?
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
