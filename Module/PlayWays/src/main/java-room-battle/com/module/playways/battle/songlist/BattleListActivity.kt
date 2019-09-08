package com.module.playways.battle.songlist

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.playways.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

@Route(path = RouterConstants.ACTIVITY_BATTLE_LIST)
class BattleListActivity : BaseActivity() {

    val mTag = "BattleListActivity"

    lateinit var topArea: ImageView
    lateinit var titlebar: CommonTitleBar
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView

    private var mGameRuleDialog: DialogPlus? = null

    val adapter: BattleListAdapter = BattleListAdapter()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.battle_list_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        topArea = findViewById(R.id.top_area)
        titlebar = findViewById(R.id.titlebar)
        smartRefresh = findViewById(R.id.smart_refresh)
        recyclerView = findViewById(R.id.recycler_view)

        titlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        titlebar.rightTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                showBattleRule()
            }
        })

        smartRefresh.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    // 加载更多
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        adapter.onClickListener = { model, _ ->
            model?.let {
                // 打开预览也么
            }
        }
    }

    private fun showBattleRule() {
        mGameRuleDialog = DialogPlus.newDialog(this@BattleListActivity)
                .setContentHolder(ViewHolder(R.layout.battle_game_rule_view_layout))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setMargin(U.getDisplayUtils().dip2px(16f), -1, U.getDisplayUtils().dip2px(16f), -1)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .create()
        mGameRuleDialog?.show()
    }

    override fun useEventBus(): Boolean {
        return false
    }

}