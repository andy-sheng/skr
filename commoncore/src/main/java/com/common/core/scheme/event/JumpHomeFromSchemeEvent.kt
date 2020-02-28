package com.common.core.scheme.event

class JumpHomeFromSchemeEvent(channel: Int) {
    var channel = 1 // 0主页 1广场 2 消息 3个人主页
    var extra:Any? = null // club 代表家族

    constructor(channel: Int,extra:Any?) : this(channel){
        this.extra = extra
    }

    init {
        this.channel = channel
    }
}
