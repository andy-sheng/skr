package com.module.feeds.make.editor

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route

import com.common.base.BaseActivity
import com.module.RouterConstants
import com.component.voice.control.VoiceControlPanelView
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.countdown.CircleCountDownView
import com.common.view.titlebar.CommonTitleBar
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.utils.SongResUtils
import com.component.toast.NoImageCommonToastView
import com.engine.EngineEvent
import com.engine.Params
import com.module.feeds.R
import com.module.feeds.watch.model.FeedSongModel
import com.zq.mediaengine.kit.ZqEngineKit
import io.agora.rtc.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RouterConstants.ACTIVITY_FEEDS_EDITOR)
class FeedsEditorActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_editor_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        initEngine()
    }

    private fun initEngine() {
        if (!ZqEngineKit.getInstance().isInit) {
            // 不能每次都初始化,播放伴奏
            val params = Params.getFromPref()
            params.apply {
                scene = Params.Scene.audiotest
                isEnableVideo = false
                isEnableAudio = true
                isUseExternalAudio = true
                isUseExternalAudioRecord = true
            }
            ZqEngineKit.getInstance().init("feeds_editor", params)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ZqEngineKit.getInstance().destroy("feeds_editor")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_START) {
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }
}
