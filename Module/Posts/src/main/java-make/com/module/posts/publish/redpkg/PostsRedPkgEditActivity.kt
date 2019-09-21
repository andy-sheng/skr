package com.module.posts.publish.redpkg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.callback.ProgressCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.publish.PostsPublishModel
import com.module.posts.publish.PostsPublishServerApi
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_POSTS_RED_PKG_EDIT)
class PostsRedPkgEditActivity : BaseActivity() {
    companion object {
        const val REQ_CODE_RED_PKG_EDIT = 13
    }

    lateinit var mainActContainer: ConstraintLayout
    lateinit var titleBar: CommonTitleBar
    lateinit var balanceVp: ExConstraintLayout
    lateinit var balanceTv: ExTextView
    lateinit var coinBalanceTv: ExTextView
    lateinit var diamondBalanceTv: ExTextView
    lateinit var mainVp: ExConstraintLayout
    lateinit var valueTipsTv: TextView
    lateinit var valueRv: RecyclerView
    lateinit var ruleTipsTv: TextView
    lateinit var okBtn: ExTextView
    lateinit var postsRedPkgAdapter: PostsRedPkgAdapter

    lateinit var model: PostsPublishModel

    var mGameRuleDialog: DialogPlus? = null

    var mLoadService:LoadService<*>? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_red_pkg_edit_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        model = intent.getSerializableExtra("model") as PostsPublishModel
        mainActContainer = findViewById(R.id.main_act_container)
        titleBar = findViewById(R.id.title_bar)
        balanceVp = findViewById(R.id.balance_vp)
        balanceTv = findViewById(R.id.balance_tv)
        coinBalanceTv = findViewById(R.id.coin_balance_tv)
        diamondBalanceTv = findViewById(R.id.diamond_balance_tv)
        mainVp = findViewById(R.id.main_vp)
        valueTipsTv = findViewById(R.id.value_tips_tv)
        valueRv = findViewById(R.id.value_rv)
        ruleTipsTv = findViewById(R.id.rule_tips_tv)
        okBtn = findViewById(R.id.ok_btn)
        postsRedPkgAdapter = PostsRedPkgAdapter()
        valueRv.layoutManager = GridLayoutManager(this, 4)
        valueRv.adapter = postsRedPkgAdapter

        postsRedPkgAdapter.selectModel = model.redPkg

        titleBar.leftImageButton.setOnClickListener {
            finish()
        }
        titleBar.rightImageButton.setOnClickListener {
            mGameRuleDialog?.dismiss()
            mGameRuleDialog = DialogPlus.newDialog(this@PostsRedPkgEditActivity)
                    .setContentHolder(ViewHolder(R.layout.posts_redpkg_rule_view_layout))
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_50)
                    .setMargin(U.getDisplayUtils().dip2px(16f), -1, U.getDisplayUtils().dip2px(16f), -1)
                    .setExpanded(false)
                    .setGravity(Gravity.CENTER)
                    .create()
            mGameRuleDialog?.show()

        }
        okBtn.setOnClickListener {
            if (postsRedPkgAdapter.selectModel == null) {
                U.getToastUtil().showShort("请选择一个红包类型")
                return@setOnClickListener
            }
            setResult(Activity.RESULT_OK, Intent().apply {
                this.putExtra("redPkg", postsRedPkgAdapter.selectModel)
            })
            finish()
        }

        val mLoadSir = LoadSir.Builder()
                .addCallback(ProgressCallback.Builder().build())
                .build()

        mLoadService = mLoadSir.register(valueRv)
        mLoadService?.showCallback(ProgressCallback::class.java)

        launch {
            val api = ApiManager.getInstance().createService(PostsPublishServerApi::class.java)
            val result = subscribe { api.getRedpacketInfo() }
            mLoadService?.showSuccess()
            if (result.errno == 0) {
                // TODO 余额
                val coinBalanceInt = result.data.getIntValue("coinBalanceInt")
                val zsBalanceFloat = result.data.getFloatValue("zsBalanceFloat")

                coinBalanceTv.text = coinBalanceInt.toString()
                diamondBalanceTv.text = zsBalanceFloat.toString()

                val lstr = result.data.getString("redpackets")
                // 加载有些慢 考虑用sp 或者 Loadsir
                val list = JSON.parseArray(lstr, RedPkgModel::class.java)
                postsRedPkgAdapter.dataList.clear()
                postsRedPkgAdapter.dataList.addAll(list)
                if (postsRedPkgAdapter.selectModel == null) {
                    postsRedPkgAdapter.selectModel = postsRedPkgAdapter.dataList[0]
                }
                postsRedPkgAdapter.notifyDataSetChanged()
            } else {

            }
        }
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
