package com.module.playways.battle.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.widget.SeekBar
import com.common.utils.U
import com.component.voice.control.VoiceControlPanelView
import com.kyleduo.switchbutton.SwitchButton
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.mediaengine.kit.ZqEngineKit

class BattleVoiceControlPanelView(val cxt: Context) : VoiceControlPanelView(cxt) {

    val TAG = "BattleVoiceControlPanelView"

    private var mLlSwitchContainer: ConstraintLayout? = null

    // 清唱与伴奏
    private var mAccSb: SwitchButton? = null

    internal var roomData: BattleRoomData? = null

    private var mDialogPlus: DialogPlus? = null

    protected override fun getLayout(): Int {
        return R.layout.relay_voice_control_panel_layout
    }

    protected override fun getMarginLeft(): Int {
        return U.getDisplayUtils().screenWidth - U.getDisplayUtils().dip2px((30 + 24).toFloat()) - U.getDisplayUtils().dip2px((44 * 5).toFloat())
    }

    override fun init(context: Context?) {
        super.init(context)
        mPeopleVoiceSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (roomData?.isMute == true) {
                    ZqEngineKit.getInstance().params.recordingSignalVolume = progress
                } else {
                    ZqEngineKit.getInstance().adjustRecordingSignalVolume(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        mMusicVoiceSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(progress)
//                ZqEngineKit.getInstance().adjustAudioMixingPublishVolume(progress,true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun bindData() {
        super.bindData()
    }

    fun setRoomData(raceRoomData: BattleRoomData) {
        roomData = raceRoomData
    }

    /**
     * 以后tips dialog 不要在外部单独写 dialog 了。
     * 可以不
     */
    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        bindData()
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.transparent)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }
}