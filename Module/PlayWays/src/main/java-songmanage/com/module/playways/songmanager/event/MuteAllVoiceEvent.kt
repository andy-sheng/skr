package com.module.playways.songmanager.event

class MuteAllVoiceEvent(var begin: Boolean){
    override fun toString(): String {
        return "BeginRecordCustomGameEvent(begin=$begin)"
    }
}
