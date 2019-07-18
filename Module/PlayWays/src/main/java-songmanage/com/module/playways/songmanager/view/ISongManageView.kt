package com.module.playways.songmanager.view

import com.module.playways.songmanager.model.RecommendTagModel

interface ISongManageView {
    fun showRoomName(roomName: String?)

    fun showRecommendSong(recommendTagModelList: MutableList<RecommendTagModel>)

    fun showAddSongCnt(cnt : Int)
}
