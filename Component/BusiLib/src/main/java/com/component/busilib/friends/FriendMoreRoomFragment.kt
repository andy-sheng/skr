package com.component.busilib.friends

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
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
import com.common.view.DebounceViewClickListener
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.R
import com.component.busilib.verify.SkrVerifyUtils
import com.kingja.loadsir.core.LoadService
import com.component.dialog.InviteFriendDialog
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.recommend.RA
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

class FriendMoreRoomFragment : BaseFragment() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mClassicsHeader: ClassicsHeader
    lateinit var mRecyclerView: RecyclerView

    lateinit var mListener: RecyclerView.OnScrollListener
    lateinit var mFriendRoomVeritAdapter: FriendRoomVerticalAdapter

    private var mSkrAudioPermission: SkrAudioPermission = SkrAudioPermission()
    private var mCameraPermission: SkrCameraPermission = SkrCameraPermission()
    private var mRealNameVerifyUtils = SkrVerifyUtils()

    private var mOffset: Int = 0
    private var grabSongApi: GrabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)

    var mRecommendTimer: HandlerTaskTimer? = null
    var mRecommendInterval: Int = 0

    lateinit var mLoadService: LoadService<*>
    var mInviteFriendDialog: InviteFriendDialog? = null

    internal var mLastLoadDateTime: Long = 0    //记录上次获取接口的时间
    override fun initView(): Int {
        return R.layout.friend_room_more_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = rootView.findViewById(R.id.titlebar)
        mRefreshLayout = rootView.findViewById(R.id.refreshLayout)
        mClassicsHeader = rootView.findViewById(R.id.classics_header)
        mRecyclerView = rootView.findViewById(R.id.recycler_view)

        mTitlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
               activity?.finish()
            }
        })

        mRefreshLayout.setEnableRefresh(true)
        mRefreshLayout.setEnableLoadMore(false)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(true)
        mRefreshLayout.setHeaderMaxDragRate(1.5f)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                loadRecommendData(mOffset)
                starTimer((mRecommendInterval * 1000).toLong())
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                initData(true)
            }
        })

        mRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        mFriendRoomVeritAdapter = FriendRoomVerticalAdapter(object : RecyclerOnItemClickListener<RecommendModel> {

            override fun onItemClicked(view: View, position: Int, model: RecommendModel?) {
                if (model != null) {
                    StatisticsAdapter.recordCountEvent("moreroom", "room_insideclick", null)
                    val friendRoomModel = model as RecommendModel?

                    if (friendRoomModel != null && friendRoomModel.roomInfo != null) {
                        if (friendRoomModel?.category == RecommendModel.TYPE_FOLLOW || friendRoomModel?.category == RecommendModel.TYPE_FRIEND) {
                            // 好友或者关注
                            checkUserRoom(friendRoomModel?.userInfo.userId, friendRoomModel, position)
                        } else {
                            tryJoinRoom(friendRoomModel.roomInfo)
                        }
                    } else {
                        MyLog.w(TAG, "friendRoomModel == null or friendRoomModel.getRoomInfo() == null")
                    }
                } else {
                    if (position == 0) {
                        StatisticsAdapter.recordCountEvent("moreroom", "1.1tab_invite", null)
                        showShareDialog()
                    } else {
                        MyLog.w(TAG, "onItemClicked view=$view position=$position model=$model")
                    }
                }
            }
        })
        mRecyclerView.adapter = mFriendRoomVeritAdapter

        mListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    starTimer((mRecommendInterval * 1000).toLong())
                } else {
                    stopTimer()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        }
        mRecyclerView.addOnScrollListener(mListener)

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.more_friend_empty_icon, "暂时没有房间了～", "#4cffffff"))
                .build()
        mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
            initData(true)
        })
    }

    /**
     * flag标记是不是要立马更新
     */
    fun initData(flag: Boolean) {
        if (mRecommendInterval <= 0) {
            mRecommendInterval = 15
        }

        if (!flag) {
            var now = System.currentTimeMillis();
            if ((now - mLastLoadDateTime) > mRecommendInterval * 1000) {
                starTimer(0)
            } else {
                var delayTime = mRecommendInterval * 1000 - (now - mLastLoadDateTime)
                starTimer(delayTime)
            }
        } else {
            starTimer(0)
        }
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        initData(false)
    }

    fun checkUserRoom(userID: Int, friendRoomModel: RecommendModel, position: Int) {
        ApiMethods.subscribe<ApiResult>(grabSongApi.checkUserRoom(userID), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    var roomInfo = JSON.parseObject(obj.data.getString("roomInfo"), SimpleRoomInfo::class.java)
                    if (roomInfo != null) {
                        if (roomInfo.roomID == friendRoomModel.roomInfo.roomID) {
                            StatisticsAdapter.recordCountEvent("moreroom", "1.1roomclick_same", null)
                        } else {
                            StatisticsAdapter.recordCountEvent("moreroom", "1.1roomclick_diff", null)
                            // 更新下本地的数据
                            friendRoomModel.roomInfo = roomInfo
                            mFriendRoomVeritAdapter.update(friendRoomModel, position)
                        }
                        tryJoinRoom(roomInfo)
                    } else {
                        // 不在房间里面了
                        mFriendRoomVeritAdapter.remove(position)
                        U.getToastUtil().showShort("好友已离开房间")
                    }
                } else {
                    MyLog.w(TAG, " checkUserRoom error = $obj ")
                    U.getToastUtil().showShort(obj.errmsg)
                }
            }

            override fun onNetworkError(errorType: ErrorType?) {
                super.onNetworkError(errorType)

            }
        }, this)
    }

    fun tryJoinRoom(roomInfo: SimpleRoomInfo) {
        if (roomInfo.mediaType == SpecialModel.TYPE_VIDEO) {
            mSkrAudioPermission.ensurePermission({
                mCameraPermission.ensurePermission({
                    mRealNameVerifyUtils.checkJoinVideoPermission {
                        // 进入视频预览
                        ARouter.getInstance()
                                .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                .withInt("mFrom", FROM_FRIEND_RECOMMEND)
                                .withInt("mRoomId", roomInfo.roomID)
                                .withInt("mInviteType", 0)
                                .navigation()
                    }
                }, true)
            }, true)
        } else {
            mSkrAudioPermission.ensurePermission({
                mRealNameVerifyUtils.checkJoinAudioPermission(roomInfo.tagID) {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService?.tryGoGrabRoom(roomInfo.roomID, 0)
                }
            }, true)
        }
    }

    private fun loadRecommendData(offset: Int) {
        ApiMethods.subscribe<ApiResult>(grabSongApi.getRecommendRoomList(offset, RA.getTestList(), RA.getVars()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mLastLoadDateTime = System.currentTimeMillis()
                    val list = JSON.parseArray(obj.data.getString("rooms"), RecommendModel::class.java)
                    val newOffset = obj.data!!.getIntValue("offset")
                    if (offset == 0) {
                        refreshView(list, true, newOffset)
                    } else {
                        refreshView(list, false, newOffset)
                    }
                } else {
                    mRefreshLayout.finishRefresh()
                    mRefreshLayout.finishLoadMore()
                }
            }

            override fun onNetworkError(errorType: ErrorType?) {
                super.onNetworkError(errorType)
                mRefreshLayout.finishRefresh()
                mRefreshLayout.finishLoadMore()
            }
        })
    }

    /**
     * 刷新数据
     *
     * @param list
     */
    private fun refreshView(list: List<RecommendModel>?, clear: Boolean, newOffset: Int) {
        mOffset = newOffset
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()

        if (clear) {
            mFriendRoomVeritAdapter.dataList.clear()
        }

        if (list != null && list.isNotEmpty()) {
            mLoadService.showSuccess()
            mFriendRoomVeritAdapter.dataList.addAll(list)
            mFriendRoomVeritAdapter.notifyDataSetChanged()
        } else {
            if (mFriendRoomVeritAdapter.dataList != null && mFriendRoomVeritAdapter.dataList.size > 0) {

            } else {
                mLoadService.showCallback(EmptyCallback::class.java)
            }
        }
    }


    fun showShareDialog() {
        if (mInviteFriendDialog == null) {
            mInviteFriendDialog = InviteFriendDialog(context, InviteFriendDialog.INVITE_GRAB_FRIEND, 0, 0, 0, null)
        }
        mInviteFriendDialog?.show()
    }

    fun starTimer(delayTimeMill: Long) {
        stopTimer()
        mRecommendTimer = HandlerTaskTimer.newBuilder()
                .delay(delayTimeMill)
                .take(-1)
                .interval((mRecommendInterval * 1000).toLong())
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        loadRecommendData(0)
                    }
                })
    }

    fun stopTimer() {
        mRecommendTimer?.dispose()
    }

    override fun destroy() {
        super.destroy()
        stopTimer()
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
