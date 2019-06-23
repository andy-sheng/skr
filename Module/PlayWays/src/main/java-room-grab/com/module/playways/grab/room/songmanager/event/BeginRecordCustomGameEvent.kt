package com.module.playways.grab.room.songmanager.event

class BeginRecordCustomGameEvent(var begin: Boolean){
    override fun toString(): String {
        return "BeginRecordCustomGameEvent(begin=$begin)"
    }
}
