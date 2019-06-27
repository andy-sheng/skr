package com.module.home.game.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.friends.FriendRoomVerticalAdapter
import com.component.busilib.friends.GrabSongApi
import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.component.busilib.recommend.RA
import com.component.busilib.verify.RealNameVerifyUtils
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.home.R
import com.module.home.loadsir.BalanceEmptyCallBack
import com.module.playways.IPlaywaysModeService
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.friend_room_view_layout.view.*

/**
 * 好友房
 */
class FriendRoomGameView : RelativeLayout {

    companion object {
        const val TAG = "FriendRoomGameView"
    }

    private var mFriendRoomVeritAdapter: FriendRoomVerticalAdapter
    private var mDisposable: Disposable? = null
    private var mSkrAudioPermission: SkrAudioPermission
    private var mCameraPermission: SkrCameraPermission
    internal var mRealNameVerifyUtils = RealNameVerifyUtils()

    var mRecommendTimer: HandlerTaskTimer? = null
    var mRecommendInterval: Int = 0

    var mLoadService: LoadService<*>

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    init {
        View.inflate(context, R.layout.friend_room_view_layout, this)

        mSkrAudioPermission = SkrAudioPermission()
        mCameraPermission = SkrCameraPermission()

        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(true)
        refreshLayout.setHeaderMaxDragRate(1.5f)
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                initData()
            }
        })
        recycler_view.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))

        mFriendRoomVeritAdapter = FriendRoomVerticalAdapter(object : RecyclerOnItemClickListener<RecommendModel> {

            override fun onItemClicked(view: View, position: Int, model: RecommendModel?) {
                StatisticsAdapter.recordCountEvent("grab", "room_click4", null)
                if (model != null) {
                    val friendRoomModel = model as RecommendModel?
                    if (friendRoomModel != null && friendRoomModel.roomInfo != null) {
                        if (friendRoomModel.roomInfo.mediaType == SpecialModel.TYPE_VIDEO) {
                            mSkrAudioPermission.ensurePermission({
                                mCameraPermission.ensurePermission({
                                    mRealNameVerifyUtils.checkJoinVideoPermission {
                                        // 进入视频预览
                                        ARouter.getInstance()
                                                .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                                .withInt("mFrom", FROM_FRIEND_RECOMMEND)
                                                .withInt("mRoomId", friendRoomModel.roomInfo.roomID)
                                                .withInt("mInviteType", 0)
                                                .navigation()
                                    }
                                }, true)
                            }, true)
                        } else {
                            mSkrAudioPermission.ensurePermission({
                                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                                iRankingModeService?.tryGoGrabRoom(friendRoomModel.roomInfo.roomID, 0)
                            }, true)
                        }
                    } else {
                        MyLog.w(TAG, "friendRoomModel == null or friendRoomModel.getRoomInfo() == null")
                    }
                } else {
                    MyLog.w(TAG, "onItemClicked view=$view position=$position model=$model")
                }
            }
        })
        recycler_view.adapter = mFriendRoomVeritAdapter

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.tongxunlu_fensikongbaiye, "暂时没有房间了～"))
                .build()
        mLoadService = mLoadSir.register(refreshLayout, Callback.OnReloadListener {
            initData()
        })
    }

    fun initData() {
        if (mRecommendInterval <= 0) {
            mRecommendInterval = 15
        }
        stopTimer()
        mRecommendTimer = HandlerTaskTimer.newBuilder()
                .take(-1)
                .interval((mRecommendInterval * 1000).toLong())
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        loadRecommendData()
                    }
                })

    }

    fun stopTimer() {
        mRecommendTimer?.dispose()
    }

    private fun loadRecommendData() {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        val grabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)
        mDisposable = ApiMethods.subscribe<ApiResult>(grabSongApi.getRecommendRoomList(RA.getVars(),RA.getTestList()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    val list = JSON.parseArray(obj.data!!.getString("rooms"), RecommendModel::class.java)
                    refreshView(list)
                }
            }
        })
    }

    /**
     * 刷新数据
     *
     * @param list
     */
    private fun refreshView(list: List<RecommendModel>?) {
        refreshLayout.finishRefresh()
        if (list != null && list!!.isNotEmpty()) {
            mLoadService.showSuccess()
            mFriendRoomVeritAdapter.dataList.clear()
            mFriendRoomVeritAdapter.dataList.addAll(list)
            mFriendRoomVeritAdapter.notifyDataSetChanged()
        } else {
            mLoadService.showCallback(EmptyCallback::class.java)
            mFriendRoomVeritAdapter.dataList.clear()
            mFriendRoomVeritAdapter.notifyDataSetChanged()
        }
    }


    fun destory() {
        mRecommendTimer?.dispose()
        mDisposable?.dispose()
    }
}
