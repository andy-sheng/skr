package com.module.msg.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.permission.SkrNotificationPermission
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.notification.event.FollowNotifyEvent
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.msg.follow.LastFollowAdapter
import com.module.msg.follow.LastFollowEmptyCallback
import com.module.msg.follow.LastFollowModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

import io.rong.imkit.R
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_LAST_FOLLOW)
class LastFollowActivity : BaseActivity() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mContentRv: RecyclerView

    lateinit var mLastFollowAdapter: LastFollowAdapter
    lateinit var mLoadService: LoadService<*>

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.last_follow_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
        mTitlebar = findViewById(R.id.titlebar)
        mRefreshLayout = findViewById(R.id.refreshLayout)
        mContentRv = findViewById(R.id.content_rv)

        mRefreshLayout.setEnableRefresh(true)
        mRefreshLayout.setEnableLoadMore(false)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setRefreshHeader(ClassicsHeader(this))
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                getLastRelations()
            }
        })

        mLastFollowAdapter = LastFollowAdapter(RecyclerOnItemClickListener { view, position, model ->
            if (view.id == R.id.content) {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", model.userID)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            } else if (view.id == R.id.follow_tv) {
                // todo 和产品想的有点出入,后面优化一下
                if (!model.isIsFollow && !model.isIsFriend) {
                    UserInfoManager.getInstance().mateRelation(model.userID, UserInfoManager.RA_BUILD, model.isIsFriend)
                }
            }
        })

        mContentRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mContentRv.adapter = mLastFollowAdapter

        mTitlebar.leftTextView.setDebounceViewClickListener { finish() }

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.last_follow_activity_empty_icon, "暂无最新关注", "#8c3B4E79"))
                .build()
        mLoadService = mLoadSir.register(mRefreshLayout) { getLastRelations() }

        getLastRelations()
    }

    private fun getLastRelations() {
        val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
        ApiMethods.subscribe(userInfoServerApi.getLatestRelation(100), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val list = JSON.parseArray(result.data!!.getString("users"), LastFollowModel::class.java)
                    showLastRelation(list)
                }
            }
        }, this)
    }

    private fun showLastRelation(list: List<LastFollowModel>?) {
        mRefreshLayout.finishRefresh()
        if (list != null && list.isNotEmpty()) {
            mLoadService.showSuccess()
            mLastFollowAdapter.dataList = list
        } else {
            mLoadService.showCallback(EmptyCallback::class.java)
            MyLog.w(TAG, "showLastRelation list=$list")
        }

    }

    override fun finish() {
        super.finish()
        /**
         * 如果没有通知栏权限，提示一次
         */
        if (U.getPermissionUtils().checkNotification(this)) {
            // 有权限
        } else {
            val lastShowTs = U.getPreferenceUtils().getSettingLong("show_go_notification_page", 0)
            if (System.currentTimeMillis() - lastShowTs > 24 * 60 * 60 * 1000) {
                U.getPreferenceUtils().setSettingLong("show_go_notification_page", System.currentTimeMillis())
                val skrNotificationPermission = SkrNotificationPermission()
                skrNotificationPermission.ensurePermission(U.getActivityUtils().homeActivity, null, true)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun resizeLayoutSelfWhenKeyboardShow(): Boolean {
        return true
    }

    override fun canSlide(): Boolean {
        return false
    }

    /**
     * 别人关注的事件,所有的关系都是从我出发
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FollowNotifyEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        // TODO: 2019/4/24 可以再优化，暂时这么写
        getLastRelations()
    }

    /**
     * 自己主动关注或取关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        MyLog.d(TAG, "RelationChangeEvent" + " event type = " + event.type + " isFriend = " + event.isFriend)
        // TODO: 2019/4/24 可以再优化，暂时这么写
        getLastRelations()
    }
}
