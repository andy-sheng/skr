package com.module.playways.grab.room.voicemsg

class VoiceRecordUiController(voiceRecordTextView: VoiceRecordTextView, voiceRecordTipsView: VoiceRecordTipsView) {
    init {
        voiceRecordTextView.mShowTipsListener = {
            if (it) {
                voiceRecordTipsView.show()
            } else {
                voiceRecordTipsView.hide()
            }
        }
        voiceRecordTextView.mChangeVoiceLevelListener = {
            voiceRecordTipsView.changeVoiceLevel(it)
        }
        voiceRecordTextView.mCancelRecordListener = {
            voiceRecordTipsView.cancelRecord()
        }
        voiceRecordTextView.mRemainTimeListener = {
            voiceRecordTipsView.remainTime(it)
        }
        voiceRecordTextView.mTimeLimitListener = {
            voiceRecordTipsView.showLimitTime(it)
        }
    }
}