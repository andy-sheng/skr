package com.module.feeds.room.msg.process

import com.common.log.MyLog
import com.module.msg.IPushMsgProcess

class FeedsMsgProcess : IPushMsgProcess {
    val TAG = "FeedsMsgProcess"

    override fun process(messageType: Int, data: ByteArray) {
        MyLog.d(TAG, "process messageType=$messageType data=$data")
        when (messageType) {

        }

    }

    override fun acceptType(): IntArray {
        return intArrayOf()
    }

    // 处理房间消息
    private fun processRoomMsg(data: ByteArray) {

    }

}
