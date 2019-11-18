package com.module.playways.demo

import android.os.Bundle
import android.view.View
import android.view.ViewStub
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.engine.EngineEvent
import com.engine.Params
import com.module.playways.R
import com.zq.mediaengine.kit.ZqEngineKit
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

@Route(path = "/demo/DoubleDemoTestActivity")
class DoubleDemoTestActivity : com.common.base.BaseActivity() {

    fun getTag():String{
        val simpleDateFormat =  SimpleDateFormat("HH:mm:ss")
        return simpleDateFormat.format( Date(System.currentTimeMillis()))
    }
    lateinit var readyBtn: ExTextView
    lateinit var changeBtn: ExTextView

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.double_demo_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        readyBtn = this.findViewById(R.id.ready_btn)
        changeBtn = this.findViewById(R.id.change_btn)
        ZqEngineKit.getInstance().init("demotest", Params.getFromPref())
        ZqEngineKit.getInstance().joinRoom("chengsimin", MyUserInfoManager.uid.toInt(), false, "")
        readyBtn.setOnClickListener {
            ZqEngineKit.getInstance().setClientRole(true)
            readyBtn.visibility = View.GONE
        }
        changeBtn.setOnClickListener {
            sing(otherId)
        }
        if (MyLog.isDebugLogOpen()) {
            val viewStub = this.findViewById<ViewStub>(R.id.debug_log_view_stub)
            val debugLogView = DebugLogView(viewStub)
            debugLogView.tryInflate()
        }

    }

    /**
     * 引擎相关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
            val roleChangeInfo = event.getObj<EngineEvent.RoleChangeInfo>()
            if (roleChangeInfo.newRole == 1) {
                DebugLogView.println(getTag(), "演唱环节切换主播成功")
                tryBegin()
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_JOIN) {
            tryBegin()
        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_AUDIO) {
            if (event.userStatus.isAudioMute && event.userStatus.userId == otherId) {
                DebugLogView.println(getTag(),"对手静音 不唱了")
                sing(MyUserInfoManager.uid.toInt())
            }
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
            DebugLogView.println(getTag(),"伴奏播放结束")
        } else if(event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER){
            val timeInfo = event.getObj<Any>() as EngineEvent.MixMusicTimeInfo
            DebugLogView.println(getTag(),"伴奏前进 cur=${timeInfo.current}")
        }else {
        }
    }

    var otherId = 0

    var singId = 0

    private fun tryBegin() {
        var size = 0
        for (us in ZqEngineKit.getInstance().mUserStatusMap.values) {
            MyLog.d(TAG, "tryBegin us=${us}")
            if (us.isAnchor) {
                size++
                if (us.userId != MyUserInfoManager.uid.toInt()) {
                    otherId = us.userId
                }
            }
        }
        if (size >= 2) {
            DebugLogView.println(getTag(),"tryBegin 两位主播 开始播放伴奏")
            ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), "http://song-static.inframe.mobi/bgm/48fd50615e0e8fd2da4f7febbbacf49f.mp3", null, 0, false, false, 1)
            if (MyUserInfoManager.uid < otherId) {
                sing(MyUserInfoManager.uid.toInt())
            } else {
                sing(otherId)
            }
        }
    }

    private fun sing(sid: Int) {
        if (sid != singId) {
            singId = sid
            if (singId == MyUserInfoManager.uid.toInt()) {
                U.getToastUtil().showShort("你唱了")
                DebugLogView.println(getTag(),"你唱了")
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(100, false)
                ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(100, false)
            }
            if (singId == otherId) {
                U.getToastUtil().showShort("对手唱了")
                DebugLogView.println(getTag(),"对手唱了")
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(0, false)
                ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(0, false)
                ZqEngineKit.getInstance().muteLocalAudioStream(true)
            }
        }
    }

    override fun destroy() {
        super.destroy()
        ZqEngineKit.getInstance().destroy("demotest")
    }

    override fun useEventBus(): Boolean {
        return true
    }
}