package com.module.playways.songmanager.event

class BeginRecordCustomGameEvent(var begin: Boolean){
    override fun toString(): String {
        return "BeginRecordCustomGameEvent(begin=$begin)"
    }
}
