package com.module.playways.battle.songlist

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.audition.fragment.PlayRecordFragment
import com.module.playways.battle.BattleServerApi
import com.module.playways.battle.songlist.adapter.BattleListAdapter
import com.module.playways.battle.songlist.model.BattleTagModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_BATTLE_LIST)
class BattleListActivity : BaseActivity() {

    val mTag = "BattleListActivity"

    lateinit var topArea: ImageView
    lateinit var titlebar: CommonTitleBar
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView
    lateinit var maskIv: View

    private var mGameRuleDialog: DialogPlus? = null

    val adapter: BattleListAdapter = BattleListAdapter()

    val battleServerApi = ApiManager.getInstance().createService(BattleServerApi::class.java)
    var offset = 0
    var hasMore = true
    val mCNT = 20

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.battle_list_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        topArea = findViewById(R.id.top_area)
        titlebar = findViewById(R.id.titlebar)
        smartRefresh = findViewById(R.id.smart_refresh)
        recyclerView = findViewById(R.id.recycler_view)
        maskIv = findViewById(R.id.mask_iv)

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
                    getStandTagList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        adapter.onClickListener = { model, _ ->
            model?.let {
                showSongCard(it)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        getStandTagList(0, true)
    }

    private fun getStandTagList(off: Int, isClean: Boolean) {
        if (!hasMore) {
            U.getToastUtil().showShort("没有更多了")
            return
        }

        launch {
            val result = subscribe(RequestControl("getStandTagList", ControlType.CancelThis)) {
                battleServerApi.getStandTagList(MyUserInfoManager.getInstance().uid, off, mCNT)
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")

                val list = JSON.parseArray(result.data.getString("details"), BattleTagModel::class.java)
                addStandTagList(list, isClean)
            } else {
                smartRefresh.finishLoadMore()
                smartRefresh.finishRefresh()
            }
        }
    }

    private fun addStandTagList(list: List<BattleTagModel>?, clean: Boolean) {
        smartRefresh.finishLoadMore()
        smartRefresh.finishRefresh()
        smartRefresh.setEnableLoadMore(hasMore)

        if (clean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        } else {
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }

        //todo 是否需要空页面
    }

    private fun showBattleRule() {
        dismissDialog()
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

    private fun showSongCard(tagModel: BattleTagModel) {
        maskIv.visibility = View.VISIBLE
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, BattleSongListCardFragment::class.java)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .setEnterAnim(R.anim.slide_in_bottom)
                .setExitAnim(R.anim.slide_out_bottom)
                .addDataBeforeAdd(0, tagModel)
                .setFragmentDataListener(object : FragmentDataListener {
                    override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                        maskIv.visibility = View.GONE
                    }
                })
                .build())
    }

    private fun dismissDialog() {
        mGameRuleDialog?.dismiss(false)
    }

    override fun useEventBus(): Boolean {
        return false
    }

}