package com.module.playways.room.prepare.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.rxretrofit.ApiManager.APPLICATION_JSON
import com.common.utils.HandlerTaskTimer
import com.component.busilib.constans.GameModeType
import com.component.busilib.recommend.RA
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.playways.room.msg.event.QJoinActionEvent
import com.module.playways.room.prepare.MatchServerApi
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel
import com.module.playways.room.prepare.model.PrepareData
import com.module.playways.room.prepare.view.IGrabMatchingView
import io.reactivex.annotations.NonNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

// 只处理匹配 请求匹配 取消匹配 和 收到加入游戏通知
class GrabMatchPresenter(@param:NonNull internal var mView: IGrabMatchingView, internal var mPrepareData: PrepareData) : BaseMatchPresenter() {
    internal var mMatchServerApi: MatchServerApi
    internal var mLoopMatchTask: HandlerTaskTimer? = null
    // TODO: 2018/12/12 怎么确定一个push肯定是当前一轮的push？？？
    internal var mJoinActionEvent: QJoinActionEvent? = null

    @Volatile
    internal var mMatchState = MatchState.IDLE

    private var mStartMatchTask: Job? = null

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
        mLoopMatchTask?.dispose()
    }

    /**
     * 开始匹配
     *
     */
    private fun startMatch() {
        mStartMatchTask?.cancel()
        mStartMatchTask = launch {
            mMatchState = MatchState.Matching
            val map = HashMap<String, Any>()
            map["modeID"] = mPrepareData.gameType
            map["platform"] = PLAT_FORM   // 代表是android平台
            map["tagID"] = mPrepareData.tagId
            if (mPrepareData.isNewUser) {
                map["isNewUser"] = true
                map["ageStage"] = MyUserInfoManager.getInstance().ageStage
            }
            map["vars"] = RA.getVars()
            map["testList"] = RA.getTestList()
            val body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map))
            val result = if (mPrepareData.gameType == GameModeType.GAME_MODE_PLAYBOOK) {
                subscribe { mMatchServerApi.startPlaybookMatch(body) }
            } else {
                subscribe { mMatchServerApi.startGrabMatch(body) }
            }
            MyLog.w(TAG, "process" + " result =" + result.errno + " traceId =" + result.traceId)
            if (result.errno == 0) {
                //                    U.getToastUtil().showShort("开始匹配");
            } else if (result.errno == 8344155) {
                //专场已下线
                mView.channelIsOffLine()
            } else {
                MyLog.e("开始匹配失败")
            }
        }
    }

    // 取消匹配,重新回到开始匹配，这里需要判断是因为可以准备了还是因为用户点击了取消
    override fun cancelMatch() {
        MyLog.d(TAG, "cancelMatch")
        disposeLoopMatchTask()
        mStartMatchTask?.cancel()
        mMatchState = MatchState.IDLE
        launch {
            val map = HashMap<String, Any>()
            map["modeID"] = mPrepareData.gameType
            val body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map))
            val result = if (mPrepareData.gameType == GameModeType.GAME_MODE_PLAYBOOK) {
                subscribe { mMatchServerApi.canclePlayBookMatch(body) }
            } else {
                subscribe { mMatchServerApi.cancleGrabMatch(body) }
            }
        }
    }

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(joinActionEvent: QJoinActionEvent?) {
        if (joinActionEvent != null) {
            MyLog.w(TAG, "onEventMainThread JoinActionEvent currentGameId is " + joinActionEvent.gameId
                    + " timeMs = " + joinActionEvent.info.timeMs
            )
            // 是否要对加入通知进行过滤
            if (mMatchState == MatchState.Matching) {
                mMatchState = MatchState.MatchSucess
                this.mJoinActionEvent = joinActionEvent
                disposeLoopMatchTask()
                mStartMatchTask?.cancel()
                joinRongRoom()
            }
        }
    }

    override fun destroy() {
        super.destroy()
        disposeLoopMatchTask()
        mStartMatchTask?.cancel()
        EventBus.getDefault().unregister(this)
        if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
            // 只是加入融云成功但是并没有返回进入准备页面
            ModuleServiceManager.getInstance().msgService.leaveChatRoom(mJoinActionEvent?.gameId.toString())
        }
    }

    /**
     * 加入融云房间，失败的话继续match，这里的失败得统计一下
     */
    private fun joinRongRoom() {
        MyLog.d(TAG, "joinRongRoom gameId " + mJoinActionEvent?.gameId)
        ModuleServiceManager.getInstance().msgService.joinChatRoom(mJoinActionEvent?.gameId.toString(), 10, object : ICallback {
            override fun onSucess(obj: Any?) {
                if (mMatchState == MatchState.MatchSucess) {
                    mMatchState = MatchState.JoinRongYunRoomSuccess
                    joinGrabRoom()
                } else {
                    MyLog.d(TAG, "joinRongRoom 加入房间成功，但是状态不是 MatchSucess， 当前状态是 $mMatchState")
                    startLoopMatchTask()
                }
            }

            override fun onFailed(obj: Any?, errcode: Int, message: String?) {
                //                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask()
            }
        })
    }

    /**
     * 请求进入房间
     */
    private fun joinGrabRoom() {
        launch {
            val map = HashMap<String, Any>()
            //        map.put("modeID", mGameType);
            //        map.put("platform", PLAT_FORM);
            map["roomID"] = mJoinActionEvent?.gameId ?: 0
            map["matchEnter"] = true
            map["vars"] = RA.getVars()
            map["testList"] = RA.getTestList()

            val body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map))
            val result = if (mPrepareData.gameType == GameModeType.GAME_MODE_PLAYBOOK) {
                subscribe {
                    mMatchServerApi.joinGrabPlaybookRoom(body)
                }
            } else {
                subscribe {
                    mMatchServerApi.joinGrabRoom(body)
                }
            }
            MyLog.w(TAG, "sendIntoRoomReq 请求加入房间 result =  " + result.errno + " traceId = " + result.traceId + " ")
            if (result.errno == 0) {
                if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                    mMatchState = MatchState.JoinGameSuccess
                    val grabCurGameStateModel = JSON.parseObject(result.data!!.toString(), JoinGrabRoomRspModel::class.java)
                    grabCurGameStateModel.gameCreateTimeMs = this@GrabMatchPresenter.mJoinActionEvent?.gameCreateMs
                            ?: 0L
                    mView.matchGrabSucess(grabCurGameStateModel)
                } else {
                    MyLog.d(TAG, "joinRongRoom 加入房间成功，但是状态不是 JoinRongYunRoomSuccess， 当前状态是 $mMatchState")
                    startLoopMatchTask()
                }
            } else {
                startLoopMatchTask()
            }
        }

    }

    /**
     * 退出游戏
     */
    fun exitGame(gameId: Int) {
        if (gameId <= 0) {
            MyLog.w(TAG, "exitGame gameId <= 0")
            return
        }

        val map = HashMap<String, Any>()
        // TODO: 2019/2/27  roomId
        map["roomID"] = mJoinActionEvent?.gameId ?: 0

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mMatchServerApi.exitGame(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.w(TAG, "退出房间 resule no is " + result.errno + ", traceid is " + result.traceId)
            }

            override fun onError(e: Throwable) {
                MyLog.w(TAG, "exitGame error,  e=$e")
            }
        }, this@GrabMatchPresenter)
    }

    internal enum class MatchState private constructor(var v: Int) {
        IDLE(1),
        Matching(2),
        MatchSucess(3),
        JoinRongYunRoomSuccess(4),
        JoinGameSuccess(5)
    }

    companion object {
        val PLAT_FORM = 20
    }
}
