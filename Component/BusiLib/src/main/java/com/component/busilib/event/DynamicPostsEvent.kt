package com.component.busilib.event

class DynamicPostsEvent {

    companion object {
        const val EVENT_POST = 1
        const val EVENT_WORK = 2
    }

    var type: Int = 0;

    constructor(type: Int) {
        this.type = type
    }
}