package com.component.voice.control

import android.content.Context
import android.support.v7.widget.AppCompatRadioButton
import android.util.AttributeSet
import android.view.View
import android.widget.*

import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.engine.EngineEvent
import com.engine.Params
import com.kyleduo.switchbutton.SwitchButton
import com.zq.mediaengine.kit.ZqEngineKit

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open abstract class VoiceControlPanelView : ScrollView {

    protected var mEarSb: SwitchButton ?= null
    protected var mEarTv:TextView? = null
    protected var mPeopleVoice: ExTextView? =null
    protected var mPeopleVoiceSeekbar: SeekBar? =null
    protected var mAccVoice: ExTextView? =null
    protected var mMusicVoiceSeekbar: SeekBar? =null

    protected var mScenesBtnGroup: RadioGroup? =null
    protected var mDefaultSbtn: AppCompatRadioButton? =null
    protected var mKtvSbtn: AppCompatRadioButton?=null
    protected var mRockSbtn: AppCompatRadioButton?=null
    protected var mDianyinSbtn: AppCompatRadioButton?=null
    protected var mKonglingSbtn: AppCompatRadioButton?=null

    // 记录值用来标记是否改变
    internal var mBeforeMode: Params.AudioEffect? = null
    internal var mBeforePeopleVoice: Int = 0
    internal var mBeforeMusicVoice: Int = 0

    internal var mAfterMode: Params.AudioEffect? = null
    internal var mAfterPeopleVoice: Int = 0
    internal var mAfterMusicVoice: Int = 0

    internal var isShowACC = true  //是否显示伴奏


    protected open abstract fun getLayout():Int

    protected open fun getMarginLeft():Int{
        return U.getDisplayUtils().screenWidth - U.getDisplayUtils().dip2px((30 + 24).toFloat()) - U.getDisplayUtils().dip2px((53 * 5).toFloat())
    }

    val isChange: Boolean
        get() = !(mBeforeMode == mAfterMode && mBeforeMusicVoice == mAfterMusicVoice && mBeforePeopleVoice == mAfterPeopleVoice)

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.VoiceControlPanelView)
        isShowACC = typedArray!!.getBoolean(R.styleable.VoiceControlPanelView_isShowACC, true)
        typedArray?.recycle()
        init(context)
    }

    open fun init(context: Context?) {
        View.inflate(getContext(), getLayout(), this)
        mPeopleVoice = this.findViewById(R.id.people_voice)
        mPeopleVoiceSeekbar = this.findViewById(R.id.people_voice_seekbar)
        mAccVoice = this.findViewById(R.id.acc_voice)
        mMusicVoiceSeekbar = this.findViewById(R.id.music_voice_seekbar)

        mDefaultSbtn = this.findViewById(R.id.default_sbtn)
        mKtvSbtn = this.findViewById(R.id.ktv_sbtn)
        mRockSbtn = this.findViewById(R.id.rock_sbtn)
        mDianyinSbtn = this.findViewById(R.id.liuxing_sbtn)
        mKonglingSbtn = this.findViewById(R.id.kongling_sbtn)

        mEarSb = this.findViewById(R.id.ear_sb)
        mEarTv = this.findViewById(R.id.ear_tv)

        var marginLeft = getMarginLeft()
        marginLeft = marginLeft / 6

        if (!isShowACC) {
            mAccVoice?.visibility = View.GONE
            mMusicVoiceSeekbar?.visibility = View.GONE
        }

        setMarginLeft(mKtvSbtn, marginLeft)
        setMarginLeft(mRockSbtn, marginLeft)
        setMarginLeft(mDianyinSbtn, marginLeft)
        setMarginLeft(mKonglingSbtn, marginLeft)

        mScenesBtnGroup = this.findViewById(R.id.scenes_btn_group)
        setListener()
    }

    protected open fun setListener() {
        mPeopleVoiceSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //                ZqEngineKit.getInstance().adjustPlaybackSignalVolume(progress);
                mAfterPeopleVoice = progress
                ZqEngineKit.getInstance().adjustRecordingSignalVolume(progress)
//                ZqEngineKit.getInstance().adjustPlaybackSignalVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        mMusicVoiceSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mAfterMusicVoice = progress
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(progress)
                ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(progress,true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        mScenesBtnGroup?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.default_sbtn -> {
                    mAfterMode = Params.AudioEffect.none
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.none)
                }
                R.id.ktv_sbtn -> {
                    mAfterMode = Params.AudioEffect.ktv
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.ktv)
                }
                R.id.rock_sbtn -> {
                    mAfterMode = Params.AudioEffect.rock
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.rock)
                }
                R.id.liuxing_sbtn -> {
                    mAfterMode = Params.AudioEffect.liuxing
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.liuxing)
                }
                R.id.kongling_sbtn -> {
                    mAfterMode = Params.AudioEffect.kongling
                    ZqEngineKit.getInstance().setAudioEffectStyle(Params.AudioEffect.kongling)
                }
            }
        }

//        mLowLatencySb?.setOnCheckedChangeListener { buttonView, isChecked ->
//            ZqEngineKit.getInstance().setEnableAudioLowLatency(isChecked)
//        }
        mEarSb?.setOnCheckedChangeListener { buttonView, isChecked ->
            // TODO: 测试用途
            ZqEngineKit.getInstance().enableInEarMonitoring(isChecked)
//            if (isChecked) {
//                mMixSb?.setCheckedNoEvent(false)
//            }
//            ZqEngineKit.getInstance().setEnableAudioPreviewLatencyTest(isChecked)
        }
//        mMixSb?.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                mEarSb?.setCheckedNoEvent(false)
//            }
//            ZqEngineKit.getInstance().setEnableAudioMixLatencyTest(isChecked)
//        }
    }

    private fun setMarginLeft(view: AppCompatRadioButton?, marginLeft: Int) {
        val layoutParams = view?.layoutParams as RadioGroup.LayoutParams?
        layoutParams?.setMargins(marginLeft, 0, 0, 0)
    }

    open fun bindData() {
        var styleEnum: Params.AudioEffect? = null
        if (ZqEngineKit.getInstance().params != null) {
            styleEnum = ZqEngineKit.getInstance().params.styleEnum
        }
        if (styleEnum == Params.AudioEffect.liuxing) {
            mScenesBtnGroup?.check(R.id.liuxing_sbtn)
        } else if (styleEnum == Params.AudioEffect.kongling) {
            mScenesBtnGroup?.check(R.id.kongling_sbtn)
        } else if (styleEnum == Params.AudioEffect.ktv) {
            mScenesBtnGroup?.check(R.id.ktv_sbtn)
        } else if (styleEnum == Params.AudioEffect.rock) {
            mScenesBtnGroup?.check(R.id.rock_sbtn)
        } else {
            mScenesBtnGroup?.check(R.id.default_sbtn)
        }
        mPeopleVoiceSeekbar?.progress = ZqEngineKit.getInstance().params.recordingSignalVolume
        mMusicVoiceSeekbar?.progress = ZqEngineKit.getInstance().params.audioMixingPlayoutVolume

        mBeforeMode = styleEnum
        mBeforePeopleVoice = ZqEngineKit.getInstance().params.recordingSignalVolume
        mBeforeMusicVoice = ZqEngineKit.getInstance().params.audioMixingPlayoutVolume

        mAfterMode = styleEnum
        mAfterPeopleVoice = ZqEngineKit.getInstance().params.recordingSignalVolume
        mAfterMusicVoice = ZqEngineKit.getInstance().params.audioMixingPlayoutVolume

        if(ZqEngineKit.getInstance().params.isUseExternalAudio){
            mEarTv?.visibility = View.VISIBLE
            mEarSb?.visibility = View.VISIBLE
            mEarSb?.setCheckedNoEvent(ZqEngineKit.getInstance().params.isEnableInEarMonitoring)
        }else{
            mEarTv?.visibility = View.GONE
            mEarSb?.visibility = View.GONE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 停止播放混音
        //        ZqEngineKit.getInstance().pauseAudioMixing();
        EventBus.getDefault().unregister(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (EngineEvent.TYPE_USER_SELF_JOIN_SUCCESS == event.getType()) {
            bindData()
        }
    }
}
