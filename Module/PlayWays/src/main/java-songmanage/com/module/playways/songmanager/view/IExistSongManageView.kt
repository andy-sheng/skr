package com.module.playways.songmanager.view

import com.module.playways.songmanager.model.GrabRoomSongModel
import com.component.busilib.friends.SpecialModel

interface IExistSongManageView {
    fun showTagList(specialModelList: List<SpecialModel>)

    fun updateSongList(grabRoomSongModelsList: List<GrabRoomSongModel>)

    fun hasMoreSongList(hasMore: Boolean)

    fun changeTagSuccess(specialModel: SpecialModel)

    fun showNum(num: Int)

    fun deleteSong(grabRoomSongModel: GrabRoomSongModel)
}
