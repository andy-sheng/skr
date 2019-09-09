package com.module.playways.grab.prepare.presenter

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.HandlerTaskTimer
import com.component.busilib.constans.GameModeType
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.playways.room.msg.event.JoinActionEvent
import com.module.playways.room.msg.event.JoinNoticeEvent
import com.module.playways.room.prepare.MatchServerApi
import com.module.playways.room.prepare.model.GameInfoModel
import com.module.playways.room.prepare.model.PrepareData
import com.module.playways.room.prepare.presenter.BaseMatchPresenter
import com.module.playways.room.prepare.view.IRankMatchingView

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.HashMap

import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.RequestBody

import com.common.rxretrofit.ApiManager.APPLICATION_JSON

// 只处理匹配 请求匹配 取消匹配 和 收到加入游戏通知
class RankMatchPresenter(@param:NonNull internal var mView: IRankMatchingView, internal var mPrepareData: PrepareData) : BaseMatchPresenter() {
    internal var mMatchServerApi: MatchServerApi
    internal var mStartMatchTask: Disposable? = null
    internal var mLoopMatchTask: HandlerTaskTimer? = null
    internal var mCheckJoinStateTask: HandlerTaskTimer? = null

    // TODO: 2018/12/12 怎么确定一个push肯定是当前一轮的push？？？
    internal var mJoinActionEvent: JoinActionEvent? = null

    //    int mCurrentGameId; // 游戏标识
    //    long mGameCreateTime;
    //    private List<SongModel> mSongModelList;

    internal var mJsonGameInfo: GameInfoModel? = null

    @Volatile
    internal var mMatchState = MatchState.IDLE

    init {
        mMatchServerApi = ApiManager.getInstance().createService(MatchServerApi::class.java)
        addToLifeCycle()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun startLoopMatchTask() {
        MyLog.d(TAG, "startLoopMatchTask")
        disposeLoopMatchTask()
        mLoopMatchTask = HandlerTaskTimer.newBuilder()
                .interval(10000)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        MyLog.d(TAG, "startLoopMatchTask onNext")
                        startMatch()
                    }
                })
    }

    private fun disposeLoopMatchTask() {
        if (mLoopMatchTask != null) {
            mLoopMatchTask!!.dispose()
        }
    }

    fun disposeMatchTask() {
        if (mStartMatchTask != null) {
            mStartMatchTask!!.dispose()
        }
    }

    /**
     * 开始匹配
     *
     */
    private fun startMatch() {
        disposeMatchTask()
        mMatchState = MatchState.Matching

        val map = HashMap<String, Any>()
        map["mode"] = mPrepareData.gameType
        map["playbookItemID"] = mPrepareData.songModel.itemID
        map["platform"] = 20   // 代表是android平台
        val body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map))
        mStartMatchTask = ApiMethods.subscribe(mMatchServerApi.startMatch(body).retryWhen(RxRetryAssist(1, 5, false)), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.w(TAG, "process" + " result =" + result.errno + " traceId =" + result.traceId)
                if (result.errno == 0) {
                    //                    U.getToastUtil().showShort("开始匹配");
                } else {
                    onError(Throwable("开始匹配失败"))
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
                // 不能这么弄，会导致死循环
                //                startMatch(mCurrentMusicId, mGameType);
            }
        }, this)
    }

    // 取消匹配,重新回到开始匹配，这里需要判断是因为可以准备了还是因为用户点击了取消
    override fun cancelMatch() {
        MyLog.d(TAG, "cancelMatch")
        disposeLoopMatchTask()
        disposeMatchTask()

        mMatchState = MatchState.IDLE
        val map = HashMap<String, Any>()
        map["mode"] = mPrepareData.gameType

        val body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mMatchServerApi.cancleMatch(body).retry(3), null)
    }

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(joinActionEvent: JoinActionEvent?) {
        if (joinActionEvent != null) {
            MyLog.w(TAG, "onEventMainThread JoinActionEvent currentGameId is " + joinActionEvent.gameId
                    + " timeMs = " + joinActionEvent.info.timeMs
                    + " songSize = " + joinActionEvent.songModelList.size
            )
            // 是否要对加入通知进行过滤
            if (mMatchState == MatchState.Matching) {
                mMatchState = MatchState.MatchSucess
                this.mJoinActionEvent = joinActionEvent
                disposeLoopMatchTask()
                disposeMatchTask()
                joinRoom()
            }
        }
    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(joinNoticeEvent: JoinNoticeEvent?) {
        if (joinNoticeEvent != null && joinNoticeEvent.jsonGameInfo != null) {
            MyLog.w(TAG, " onEventMainThread JoinNoticeEvent timeMs = " + joinNoticeEvent.info.timeMs + ", joinNoticeEvent.jsonGameInfo.getReadyClockResMs() " + joinNoticeEvent.jsonGameInfo.readyClockResMs)
            // 需要去更新GameInfo
            if (joinNoticeEvent.jsonGameInfo.readyClockResMs != 0) {
                if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                    mMatchState = MatchState.JoinGameSuccess
                    if (mCheckJoinStateTask != null) {
                        mCheckJoinStateTask!!.dispose()
                    }
                    mView.matchRankSucess(mJoinActionEvent)
                }
            }
        }
    }

    override fun destroy() {
        super.destroy()
        disposeLoopMatchTask()
        disposeMatchTask()
        cancelCheckTask()
        EventBus.getDefault().unregister(this)
        if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
            // 只是加入融云成功但是并没有返回进入准备页面
            ModuleServiceManager.getInstance().msgService.leaveChatRoom(mJoinActionEvent?.gameId.toString())
        }
    }

    /**
     * 加入融云房间，失败的话继续match，这里的失败得统计一下
     */
    private fun joinRoom() {
        MyLog.d(TAG, "joinRoom gameId " + mJoinActionEvent?.gameId)
        ModuleServiceManager.getInstance().msgService.joinChatRoom(mJoinActionEvent?.gameId.toString(), 10, object : ICallback {
            override fun onSucess(obj: Any?) {
                if (mMatchState == MatchState.MatchSucess) {
                    mMatchState = MatchState.JoinRongYunRoomSuccess
                    joinGame()
                }
            }

            override fun onFailed(obj: Any?, errcode: Int, message: String?) {
                //                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask()
            }
        })
    }

    /**
     * 加入我们自己的房间，失败的话继续match，这里的失败得统计一下
     */
    private fun joinGame() {
        MyLog.d(TAG, "joinGame gameId ")
        val map = HashMap<String, Any>()
        map["gameID"] = mJoinActionEvent?.gameId ?: 0

        val body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mMatchServerApi.joinGame(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.w(TAG, "加入房间 result =  " + result.errno + " traceId = " + result.traceId)
                if (result.errno == 0) {
                    updateUserListState()
                } else {
                    startLoopMatchTask()
                }
            }

            override fun onError(e: Throwable) {
                //                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask()
            }
        }, this)

        checkCurrentGameData()
    }

    /**
     * 加入完我们服务器三秒钟后检查房间的情况，
     * 如果三个人都已经加入房间了或者还没到三秒就已经有push告诉客户端已经三个人都加入房间了
     * 就可以跳转到准备界面
     */
    fun checkCurrentGameData() {
        MyLog.d(TAG, "checkCurrentGameData")
        cancelCheckTask()
        mCheckJoinStateTask = HandlerTaskTimer.newBuilder()
                .take(CHECK_NUM)
                .delay(1000)
                .interval(1000)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        MyLog.d(TAG, "checkCurrentGameData onNext try num is " + integer!!)
                        mMatchServerApi = ApiManager.getInstance().createService(MatchServerApi::class.java)
                        ApiMethods.subscribe(mMatchServerApi.getCurrentGameData(mJoinActionEvent?.gameId
                                ?: 0), object : ApiObserver<ApiResult>() {
                            override fun process(result: ApiResult) {
                                MyLog.w(TAG, "checkCurrentGameData result = " + result.errno + " traceId = " + result.traceId)
                                if (result.errno == 0) {
                                    val jsonGameInfo = JSON.parseObject(result.data!!.toString(), GameInfoModel::class.java)
                                    if (jsonGameInfo.readyClockResMs != 0) {
                                        if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                                            mMatchState = MatchState.JoinGameSuccess
                                            mJsonGameInfo = jsonGameInfo
                                            mView.matchRankSucess(mJoinActionEvent)
                                            cancelCheckTask()
                                        } else {
                                            MyLog.w(TAG, "拉信息回来发现当前状态不是 JoinRongYunRoomSuccess")
                                            //跟下面的更新唯一的区别就是三秒钟之后人还不全就从新match
                                            startLoopMatchTask()
                                            exitGame(mJoinActionEvent?.gameId)
                                            cancelCheckTask()
                                        }
                                    } else {
                                        MyLog.w(TAG, "拉完房间信息人数不够3个，try num is $integer")
                                        if (integer >= CHECK_NUM - 1) {
                                            cancelCheckTask()
                                            startLoopMatchTask()
                                            exitGame(mJoinActionEvent?.gameId)
                                        }
                                    }
                                } else {
                                    MyLog.w(TAG, "拉信息返回的resule error code不是 0,是" + result.errno)
                                    cancelCheckTask()
                                    startLoopMatchTask()
                                    exitGame(mJoinActionEvent?.gameId)
                                }
                            }

                            override fun onError(e: Throwable) {
                                MyLog.d(TAG, "checkCurrentGameData2 process e=$e")
                                startLoopMatchTask()
                                exitGame(mJoinActionEvent?.gameId)
                            }
                        }, this@RankMatchPresenter)
                    }
                })
    }

    private fun cancelCheckTask() {
        if (mCheckJoinStateTask != null) {
            mCheckJoinStateTask!!.dispose()
        }
    }

    /**
     * 退出游戏
     */
    fun exitGame(gameId: Int?) {
        gameId?.let {
            val map = HashMap<String, Any>()
            map["gameID"] = gameId

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            ApiMethods.subscribe(mMatchServerApi.exitGame(body), object : ApiObserver<ApiResult>() {
                override fun process(result: ApiResult) {
                    MyLog.w(TAG, "退出房间 resule no is " + result.errno + ", traceid is " + result.traceId)
                }

                override fun onError(e: Throwable) {
                    MyLog.w(TAG, "exitGame error,  e=$e")
                }
            }, this@RankMatchPresenter)
        }
    }

    /**
     * 由于涉及到返回时序问题，都从服务器
     */
    private fun updateUserListState() {
        MyLog.d(TAG, "updateUserListState")
        ApiMethods.subscribe(mMatchServerApi.getCurrentGameData(mJoinActionEvent?.gameId
                ?: 0), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.w(TAG, "updateUserListState result = " + result.errno + " traceId = " + result.traceId)
                if (result.errno == 0) {
                    val jsonGameInfo = JSON.parseObject(result.data!!.toString(), GameInfoModel::class.java)
                    if (jsonGameInfo.readyClockResMs != 0) {
                        if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                            mMatchState = MatchState.JoinGameSuccess
                            mJsonGameInfo = jsonGameInfo
                            mView.matchRankSucess(mJoinActionEvent)
                        }
                    } else {
                        MyLog.d(TAG, "process updateUserListState else")
                    }
                } else {
                    MyLog.d(TAG, "process updateUserListState 2 result=$result")
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this@RankMatchPresenter)
    }

    internal enum class MatchState private constructor(var v: Int) {
        IDLE(1),
        Matching(2),
        MatchSucess(3),
        JoinRongYunRoomSuccess(4),
        JoinGameSuccess(5)
    }

    companion object {
        val CHECK_NUM = 10
    }
}
