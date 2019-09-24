package com.component.busilib.event

class FeedSongMakeSucessEvent {
    var songId:Int? = 0
    var songName:String? = null
    var localPath:String? = ""
    var duration:Int? = 0

    constructor(songId: Int?,songName:String?, localPath: String?, duration: Int?) {
        this.songId = songId
        this.songName = songName
        this.localPath = localPath
        this.duration = duration
    }
}