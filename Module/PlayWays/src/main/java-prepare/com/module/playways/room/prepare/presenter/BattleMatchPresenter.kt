package com.module.playways.room.prepare.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.utils.HandlerTaskTimer
import com.component.busilib.constans.GameModeType
import com.component.busilib.recommend.RA
import com.module.ModuleServiceManager
import com.module.playways.battle.match.model.JoinBattleRoomRspModel
import com.module.playways.room.msg.event.QJoinActionEvent
import com.module.playways.room.prepare.MatchServerApi
import com.module.playways.room.prepare.model.PrepareData
import com.module.playways.room.prepare.view.IGrabMatchingView
import com.zq.live.proto.BattleRoom.BUserEnterMsg
import io.reactivex.annotations.NonNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BattleMatchPresenter(@param:NonNull internal var mView: IGrabMatchingView, internal var mPrepareData: PrepareData) : BaseMatchPresenter() {
    internal var mMatchServerApi: MatchServerApi
    internal var mLoopMatchTask: HandlerTaskTimer? = null
    // TODO: 2018/12/12 怎么确定一个push肯定是当前一轮的push？？？
    internal var mJoinActionEvent: QJoinActionEvent? = null

    @Volatile
    internal var mMatchState = GrabMatchPresenter.MatchState.IDLE

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
            mMatchState = GrabMatchPresenter.MatchState.Matching
            val map = mapOf(
                    "platform" to GrabMatchPresenter.PLAT_FORM
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { mMatchServerApi.startBattleMatch(body) }
            MyLog.w(TAG, "process" + " result =" + result.errno + " traceId =" + result.traceId)
            if (result.errno == 0) {
                val hasMatchedRoom = result.data.getBooleanValue("hasMatchedRoom")
                if (hasMatchedRoom) {
                    // 进房间了
                    if (mMatchState != GrabMatchPresenter.MatchState.JoinGameSuccess) {
                        mMatchState = GrabMatchPresenter.MatchState.JoinGameSuccess
                        val model = JSON.parseObject(result.data.getString("roomEnterMsg"), JoinBattleRoomRspModel::class.java)
                        mView.matchBattleSuccess(model, "短链接匹配")
                    }
                }
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
        mMatchState = GrabMatchPresenter.MatchState.IDLE
        launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe { mMatchServerApi.cancelBattleMatch(body) }
        }
    }

    // 进入房间(系统消息下发)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(userEnterMsg: BUserEnterMsg) {
        if (mMatchState != GrabMatchPresenter.MatchState.JoinGameSuccess) {
            mMatchState = GrabMatchPresenter.MatchState.JoinGameSuccess
            val model = JoinBattleRoomRspModel.parseFromPB(userEnterMsg)
            mView.matchBattleSuccess(model, "长连接PB")
        }
    }


    override fun destroy() {
        super.destroy()
        disposeLoopMatchTask()
        mStartMatchTask?.cancel()
        EventBus.getDefault().unregister(this)
        if (mMatchState == GrabMatchPresenter.MatchState.JoinRongYunRoomSuccess) {
            // 只是加入融云成功但是并没有返回进入准备页面
            ModuleServiceManager.getInstance().msgService.leaveChatRoom(mJoinActionEvent?.gameId.toString())
        }
    }

}