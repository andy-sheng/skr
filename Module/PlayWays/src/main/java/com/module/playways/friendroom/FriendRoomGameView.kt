package com.module.playways.friendroom

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.friends.*
import com.component.busilib.recommend.RA
import com.component.busilib.verify.SkrVerifyUtils
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.component.dialog.InviteFriendDialog
import com.module.playways.IFriendRoomView
import com.module.playways.R
import com.module.playways.mic.home.RecommendMicInfoModel
import com.module.playways.mic.home.RecommendUserInfo
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.friend_room_view_layout.view.*

/**
 * 好友房
 */
class FriendRoomGameView : RelativeLayout, IFriendRoomView {

    val mTag = "FriendRoomGameView"
    val playerTag = mTag + hashCode()
    private val playCallback: SinglePlayerCallbackAdapter

    private var mListener: RecyclerView.OnScrollListener? = null
    private var friendRoomAdapter: FriendRoomAdapter? = null
    private var mDisposable: Disposable? = null
    private var mCheckDisposable: Disposable? = null

    private var mSkrAudioPermission: SkrAudioPermission
    private var mCameraPermission: SkrCameraPermission
    private var mOffset: Int = 0
    private var grabSongApi: GrabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)
    private var mRealNameVerifyUtils = SkrVerifyUtils()

    var mRecommendTimer: HandlerTaskTimer? = null
    var mRecommendInterval: Int = 0

    var mLoadService: LoadService<*>
    var mInviteFriendDialog: InviteFriendDialog? = null

    internal var mLastLoadDateTime: Long = 0    //记录上次获取接口的时间

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
                loadRecommendData(mOffset)
                starTimer((mRecommendInterval * 1000).toLong())
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                initData(true)
            }
        })

        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        friendRoomAdapter = FriendRoomAdapter(object : FriendRoomAdapter.FriendRoomClickListener {
            override fun onClickMicRoom(model: RecommendMicInfoModel?, position: Int) {
                // 进入排麦房
            }

            override fun onClickMicVoice(model: RecommendMicInfoModel?, position: Int, userInfoModel: RecommendUserInfo?, childPos: Int) {
                // 点击排麦房的声音
            }

            override fun onClickGrabVoice(position: Int, model: GrabRecommendModel?) {
                // 点击抢唱房的声音标签
                if (friendRoomAdapter?.mCurrPlayModel != null && friendRoomAdapter?.mCurrPlayModel == model) {
                    SinglePlayer.stop(playerTag)
                    // 自动刷新去
                    starTimer((mRecommendInterval * 1000).toLong())
                } else {
                    // 播放中 不能刷新，停止自动刷新
                    stopTimer()
                    model?.voiceInfo?.voiceURL?.let {
                        SinglePlayer.startPlay(playerTag, it)
                    }
                }
                friendRoomAdapter?.startOrPauseAudio(position, model)
            }

            override fun onClickGrabRoom(position: Int, model: GrabRecommendModel?) {
                // 进入抢唱房间
                SinglePlayer.stop(playerTag)
                friendRoomAdapter?.stopPlay()
                if (model != null) {
                    StatisticsAdapter.recordCountEvent("grab", "room_click4", null)

                    if (model.roomInfo != null) {
                        if (model?.category == GrabRecommendModel.TYPE_FOLLOW || model.category == GrabRecommendModel.TYPE_FRIEND) {
                            // 好友或者关注
                            checkUserRoom(model.userInfo?.userId
                                    ?: 0, model, position)
                        } else {
                            model.roomInfo?.let { tryJoinRoom(it) }
                        }
                    } else {
                        MyLog.w(mTag, "friendRoomModel == null or friendRoomModel.getRoomInfo() == null")
                    }
                } else {
                    if (position == 0) {
                        StatisticsAdapter.recordCountEvent("grab", "1.1tab_invite", null)
                        showShareDialog()
                    } else {
                        MyLog.w(mTag, "onClickFriendRoom position=$position model=$model")
                    }
                }
            }
        })

        recycler_view.adapter = friendRoomAdapter

        mListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    if (friendRoomAdapter?.isPlay == false) {
                        // 没有播放时 才让刷新
                        starTimer((mRecommendInterval * 1000).toLong())
                    }
                } else {
                    stopTimer()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        }
        recycler_view.addOnScrollListener(mListener)

        playCallback = object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                friendRoomAdapter?.stopPlay()
                // 自动刷新去
                starTimer((mRecommendInterval * 1000).toLong())
            }

            override fun onPlaytagChange(oldPlayerTag: String?, newPlayerTag: String?) {
                if (newPlayerTag != playerTag) {
                    friendRoomAdapter?.stopPlay()
                    // 自动刷新去
                    starTimer((mRecommendInterval * 1000).toLong())
                }
            }
        }
        SinglePlayer.addCallback(playerTag, playCallback)

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.more_friend_empty_icon, "暂时没有房间了～", "#FFFFFF"))
                .build()
        mLoadService = mLoadSir.register(refreshLayout, Callback.OnReloadListener {
            initData(true)
        })
    }

    fun showShareDialog() {
        if (mInviteFriendDialog == null) {
            mInviteFriendDialog = InviteFriendDialog(context, InviteFriendDialog.INVITE_GRAB_FRIEND, 0, 0, 0, null)
        }
        mInviteFriendDialog?.show()
    }

    /**
     * flag标记是不是要立马更新
     */
    override fun initData(flag: Boolean) {
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

    override fun stopTimer() {
        mRecommendTimer?.dispose()
    }

    override fun stopPlay() {
        SinglePlayer.stop(playerTag)
        friendRoomAdapter?.stopPlay()
    }

    fun tryJoinRoom(roomInfoGrab: GrabSimpleRoomInfo) {
        if (roomInfoGrab.mediaType == SpecialModel.TYPE_VIDEO) {
            mSkrAudioPermission.ensurePermission({
                mCameraPermission.ensurePermission({
                    mRealNameVerifyUtils.checkJoinVideoPermission {
                        // 进入视频预览
                        ARouter.getInstance()
                                .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                .withInt("mFrom", FROM_FRIEND_RECOMMEND)
                                .withInt("mRoomId", roomInfoGrab.roomID)
                                .withInt("mInviteType", 0)
                                .navigation()
                    }
                }, true)
            }, true)
        } else {
            mSkrAudioPermission.ensurePermission({
                mRealNameVerifyUtils.checkJoinAudioPermission(roomInfoGrab.tagID) {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService?.tryGoGrabRoom(roomInfoGrab.roomID, 0)
                }
            }, true)
        }
    }

    fun checkUserRoom(userID: Int, friendRoomModelGrab: GrabRecommendModel, position: Int) {
        if (mCheckDisposable != null && !mCheckDisposable!!.isDisposed) {
            mCheckDisposable?.dispose()
        }

        mCheckDisposable = ApiMethods.subscribe<ApiResult>(grabSongApi.checkUserRoom(userID), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    var roomInfo = JSON.parseObject(obj.data.getString("roomInfo"), GrabSimpleRoomInfo::class.java)
                    if (roomInfo != null) {
                        if (roomInfo.roomID == friendRoomModelGrab.roomInfo?.roomID) {
                            StatisticsAdapter.recordCountEvent("grab", "1.1roomclick_same", null)
                        } else {
                            StatisticsAdapter.recordCountEvent("grab", "1.1roomclick_diff", null)
                            // 更新下本地的数据
                            friendRoomModelGrab.roomInfo = roomInfo
                            friendRoomAdapter?.update(friendRoomModelGrab, position)
                        }
                        if (roomInfo.roomType != 1) {
                            // 不再私密房里面
                            tryJoinRoom(roomInfo)
                        } else {
                            // 在私密房里面
                            friendRoomAdapter?.remove(position)
                            friendRoomAdapter?.notifyDataSetChanged()
                            U.getToastUtil().showShort("好友已离开房间")
                        }
                    } else {
                        // 不在房间里面了
                        friendRoomAdapter?.remove(position)
                        friendRoomAdapter?.notifyDataSetChanged()
                        U.getToastUtil().showShort("好友已离开房间")
                    }
                } else {
                    MyLog.w(mTag, " checkUserRoom error = $obj ")
                    U.getToastUtil().showShort(obj.errmsg)
                }
            }

            override fun onNetworkError(errorType: ErrorType?) {
                super.onNetworkError(errorType)

            }
        })
    }

    private fun loadRecommendData(offset: Int) {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        mDisposable = ApiMethods.subscribe<ApiResult>(grabSongApi.getRecommendRoomList(offset, RA.getTestList(), RA.getVars()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mLastLoadDateTime = System.currentTimeMillis()
                    val list = JSON.parseArray(obj.data.getString("rooms"), GrabRecommendModel::class.java)
                    val newOffset = obj.data!!.getIntValue("offset")
                    if (offset == 0) {
                        refreshView(list, true, newOffset)
                    } else {
                        refreshView(list, false, newOffset)
                    }
                } else {
                    refreshLayout.finishRefresh()
                    refreshLayout.finishLoadMore()
                }
            }

            override fun onNetworkError(errorType: ErrorType?) {
                super.onNetworkError(errorType)
                refreshLayout.finishRefresh()
                refreshLayout.finishLoadMore()
            }
        })
    }

    /**
     * 刷新数据
     *
     * @param list
     */
    private fun refreshView(list: List<GrabRecommendModel>?, clear: Boolean, newOffset: Int) {
        mOffset = newOffset
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()

        if (clear) {
            SinglePlayer.stop(playerTag)
            friendRoomAdapter?.stopPlay()
            friendRoomAdapter?.dataList?.clear()
            if (!list.isNullOrEmpty()) {
                friendRoomAdapter?.dataList?.addAll(list)
            }
            friendRoomAdapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                friendRoomAdapter?.dataList?.addAll(list)
                friendRoomAdapter?.notifyDataSetChanged()
            }
        }

        if (!friendRoomAdapter?.dataList.isNullOrEmpty()) {
            mLoadService.showSuccess()
        } else {
            mLoadService.showCallback(EmptyCallback::class.java)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun destory() {
        SinglePlayer.removeCallback(playerTag)
        SinglePlayer.reset(playerTag)
        mRecommendTimer?.dispose()
        mDisposable?.dispose()
        mCheckDisposable?.dispose()
        mInviteFriendDialog?.dismiss(false)
        recycler_view.removeOnScrollListener(mListener)
        mListener = null
    }
}
