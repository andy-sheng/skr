package com.module.playways.grab.room.voicemsg

import com.module.playways.room.room.comment.CommentView

class VoiceRecordUiController(voiceRecordTextView: VoiceRecordTextView, voiceRecordTipsView: VoiceRecordTipsView,commentView: CommentView) {
    init {
        voiceRecordTextView.mShowTipsListener = {
            if (it) {
                voiceRecordTipsView.show()
                commentView.tryStopPlay()
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