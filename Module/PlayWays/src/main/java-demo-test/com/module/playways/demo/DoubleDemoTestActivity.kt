package com.module.playways.demo

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewStub
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.widget.ManyLyricsView
import com.engine.EngineEvent
import com.engine.Params
import com.module.playways.R
import com.module.playways.room.data.H
import com.zq.mediaengine.kit.ZqEngineKit
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

@Route(path = "/demo/DoubleDemoTestActivity")
class DoubleDemoTestActivity : com.common.base.BaseActivity() {

    fun getTag(): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        return simpleDateFormat.format(Date(System.currentTimeMillis()))
    }

    lateinit var readyBtn: ExTextView
    lateinit var changeBtn: ExTextView
    lateinit var volumeBtn: ExTextView
    lateinit var mManyLyricsView: ManyLyricsView
    val lyricAndAccMatchManager = LyricAndAccMatchManager()

    var volumeAnimation = false

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.double_demo_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        readyBtn = this.findViewById(R.id.ready_btn)
        changeBtn = this.findViewById(R.id.change_btn)
        volumeBtn = this.findViewById(R.id.volume_btn)

        mManyLyricsView = this.findViewById(R.id.many_lyrics_view)
        mManyLyricsView?.initLrcData()
        val p = Params.getFromPref()
        p.scene = Params.Scene.rank
        ZqEngineKit.getInstance().init("demotest", p)
        ZqEngineKit.getInstance().joinRoom("chengsimin", MyUserInfoManager.uid.toInt(), false, "")
        readyBtn.setOnClickListener {
            var size = 0
            for (us in ZqEngineKit.getInstance().mUserStatusMap.values) {
                if (us.isAnchor) {
                    size++
                }
            }
            if (size == 0) {
                U.getToastUtil().showShort("有两个主播后会开始播放伴奏")
            }
            ZqEngineKit.getInstance().setClientRole(true)
            readyBtn.visibility = View.GONE
        }
        volumeBtn.setOnClickListener {
            volumeAnimation = !volumeAnimation
            if (volumeAnimation) {
                volumeBtn.text = "关闭渐弱渐强"
                U.getToastUtil().showShort("已开启")
            } else {
                volumeBtn.text = "开启渐弱渐强"
                U.getToastUtil().showShort("已关闭")
            }
        }

        changeBtn.visibility = View.GONE
        changeBtn.setOnClickListener {
            U.getToastUtil().showShort("对手唱了")
            if (volumeAnimation) {
                changeBtn.visibility = View.GONE
                val animation = ValueAnimator.ofInt(100, 0)
                animation.duration = 1000
                animation.addUpdateListener {
                    var v = it.animatedValue as Int
                    ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(v, false)
                    ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(v, false)
                }
                animation.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        sing(otherId)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        sing(otherId)
                    }
                })
                animation.start()
            } else {
                sing(otherId)
            }
        }
        if (MyLog.isDebugLogOpen()) {
            val viewStub = this.findViewById<ViewStub>(R.id.debug_log_view_stub)
            val debugLogView = DebugLogView(viewStub)
            debugLogView.tryInflate()
        }

        val configParams = LyricAndAccMatchManager.ConfigParams()
        configParams.manyLyricsView = mManyLyricsView
//        configParams.voiceScaleView = mVoiceScaleView
        configParams.lyricUrl = "http://song-static.inframe.mobi/lrc/4ee4ac0711c74d6f333fcac10c113239.zrce"
        configParams.lyricBeginTs = 0
        configParams.lyricEndTs = 4 * 60 * 1000
        configParams.accBeginTs = 0
        configParams.accEndTs = 4 * 60 * 1000
//        configParams.authorName = songModel.uploaderName
        lyricAndAccMatchManager!!.setArgs(configParams)
        lyricAndAccMatchManager!!.start(object : LyricAndAccMatchManager.Listener {

            override fun onLyricParseSuccess(reader: LyricsReader) {
//                mSvlyric?.visibility = View.GONE
            }

            override fun onLyricParseFailed() {
//                playWithNoAcc(songModel)
            }

            override fun onLyricEventPost(lineNum: Int) {
                if (H.isGrabRoom()) {
                    H.grabRoomData?.songLineNum = lineNum
                }
            }
        })
//        ZqEngineKit.getInstance().setRecognizeListener { result, list, targetSongInfo, lineNo -> mLyricAndAccMatchManager!!.onAcrResult(result, list, targetSongInfo, lineNo) }
    }

    /**
     * 引擎相关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        MyLog.d(TAG, "onEvent event = $event")
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
                DebugLogView.println(getTag(), "对手静音 不唱了")
                U.getToastUtil().showShort("轮到你唱了")
                if (volumeAnimation) {
                    ZqEngineKit.getInstance().muteLocalAudioStream(false)
                    val animation = ValueAnimator.ofInt(0, 100)
                    animation.duration = 1000
                    animation.addUpdateListener {
                        var v = it.animatedValue as Int
                        ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(v, false)
                        ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(v, false)
                    }
                    animation.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            sing(MyUserInfoManager.uid.toInt())
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            sing(MyUserInfoManager.uid.toInt())
                        }
                    })
                    animation.start()
                } else {
                    sing(MyUserInfoManager.uid.toInt())
                }
            }
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
            DebugLogView.println(getTag(), "伴奏播放结束")
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            val timeInfo = event.getObj<Any>() as EngineEvent.MixMusicTimeInfo
            //DebugLogView.println(getTag(),"伴奏前进 cur=${timeInfo.current}")
        } else {
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
            DebugLogView.println(getTag(), "tryBegin 两位主播 开始播放伴奏")
            ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), "http://song-static.inframe.mobi/bgm/e3b214d337f1301420dad255230fe085.mp3", null, 0, false, false, 1)
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
                DebugLogView.println(getTag(), "轮到你唱了")
                ZqEngineKit.getInstance().muteLocalAudioStream(false)
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(100, false)
                ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(100, false)
                changeBtn.visibility = View.VISIBLE
            }
            if (singId == otherId) {
                DebugLogView.println(getTag(), "对手唱了")
                ZqEngineKit.getInstance().muteLocalAudioStream(true)
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(0, false)
                ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(0, false)
                changeBtn.visibility = View.GONE
            }
        }
    }

    override fun destroy() {
        super.destroy()
        mManyLyricsView?.release()
        lyricAndAccMatchManager.stop()
        ZqEngineKit.getInstance().destroy("demotest")
    }

    override fun useEventBus(): Boolean {
        return true
    }
}