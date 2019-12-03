package com.module.playways.relay.match.view

import com.kingja.loadsir.callback.Callback
import com.module.playways.R

class RelayEmptyRoomCallback : Callback() {
    override fun onCreateView(): Int {
        return R.layout.relay_room_empty_layout
    }
}