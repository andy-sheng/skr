package com.component.busilib

class GrabJoinRoomFailEvent constructor(var roomID: Int, var type: Int) {

    companion object {
        const val TYPE_FULL_ROOM: Int = 1
        const val TYPE_DISSOLVE_ROOM: Int = 2
    }

}


