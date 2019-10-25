package com.module.playways.mic.room.presenter

import com.common.mvp.RxLifeCyclePresenter
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.ui.IMicRoomView

class MicCorePresenter(var mRoomData: MicRoomData, var roomView: IMicRoomView) : RxLifeCyclePresenter() {


}