package com.common.playcontrol

class RemoteControlEvent(var from:Int){
     // 1来自摇一摇 2 来自线控
    companion object{
         val FROM_SHAKE = 1
         val FROM_HEADSET = 2
     }
}