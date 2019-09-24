package com.module.posts.publish.redpkg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.event.RechargeSuccessEvent
import com.dialog.view.TipsDialogView
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
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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

    var mLoadService: LoadService<*>? = null

    var coinBalanceInt = 0
    var zsBalanceFloat = 0f
    var tipsDialogView: TipsDialogView? = null

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
            if (postsRedPkgAdapter?.selectModel?.redpacketType == 1) {
                if (coinBalanceInt < (postsRedPkgAdapter?.selectModel?.redpacketNum ?: 0)) {
                    U.getToastUtil().showShort("金币不足，可在一唱到底获得")
                    return@setOnClickListener
                }
            } else if (postsRedPkgAdapter?.selectModel?.redpacketType == 2) {
                if (zsBalanceFloat < (postsRedPkgAdapter?.selectModel?.redpacketNum ?: 0)) {
                    //如果已经录入语音
                    tipsDialogView = TipsDialogView.Builder(this)
                            .setMessageTip("钻石不足，是否充值")
                            .setConfirmTip("充值")
                            .setCancelTip("取消")
                            .setCancelBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View?) {
                                    tipsDialogView?.dismiss()
                                }
                            })
                            .setConfirmBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View?) {
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_BALANCE)
                                            .navigation()
                                    tipsDialogView?.dismiss(false)

                                }
                            })
                            .build()
                    tipsDialogView?.showByDialog()
                    return@setOnClickListener
                }
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

        getData()
    }

    private fun getData() {
        launch {
            val api = ApiManager.getInstance().createService(PostsPublishServerApi::class.java)
            val result = subscribe { api.getRedpacketInfo() }
            mLoadService?.showSuccess()
            if (result.errno == 0) {
                // TODO 余额
                coinBalanceInt = result.data.getIntValue("coinBalanceInt")
                zsBalanceFloat = result.data.getFloatValue("zsBalanceFloat")
                coinBalanceTv.text = coinBalanceInt.toString()
                diamondBalanceTv.text = zsBalanceFloat.toString()

                val lstr = result.data.getString("redpackets")
                // 加载有些慢 考虑用sp 或者 Loadsir
                val list = JSON.parseArray(lstr, RedPkgModel::class.java)
                postsRedPkgAdapter.dataList.clear()
                if (!list.isNullOrEmpty()) {
                    postsRedPkgAdapter.dataList.addAll(list)
                } else {
                    MyLog.e(TAG, "getData 为什么没有数据 文佳胜")
                }
                if (postsRedPkgAdapter.selectModel == null) {
                    if (!postsRedPkgAdapter.dataList.isNullOrEmpty()) {
                        postsRedPkgAdapter.selectModel = postsRedPkgAdapter.dataList[0]
                    } else {
                        MyLog.e(TAG, "getData 啥玩意 为什么没有数据 文佳胜")
                    }
                }
                postsRedPkgAdapter.notifyDataSetChanged()
            } else {

            }
        }
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RechargeSuccessEvent) {
        // 收到一条礼物消息,进入生产者队列
        getData()
    }

    override fun useEventBus(): Boolean {
        return true
    }
}
