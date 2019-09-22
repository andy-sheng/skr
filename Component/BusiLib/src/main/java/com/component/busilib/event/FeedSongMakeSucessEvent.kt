package com.component.busilib.event

class FeedSongMakeSucessEvent {
    var songId:Int? = 0
    var localPath:String? = ""
    var duration:Int? = 0

    constructor(songId: Int?, localPath: String?, duration: Int?) {
        this.songId = songId
        this.localPath = localPath
        this.duration = duration
    }
}