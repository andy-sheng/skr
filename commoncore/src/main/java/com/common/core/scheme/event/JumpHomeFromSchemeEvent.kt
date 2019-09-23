package com.common.core.scheme.event

class JumpHomeFromSchemeEvent(channel: Int) {
    var channel = 1 // 0主页 1广场 2 消息 3个人主页
    var extra:Any? = null
    init {
        this.channel = channel
    }
}
