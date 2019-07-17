package com.module.playways.songmanager.event

class MuteAllVoiceEvent(var begin: Boolean){
    override fun toString(): String {
        return "MuteAllVoiceEvent(begin=$begin)"
    }
}
