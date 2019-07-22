package com.module.playways.songmanager.view

import com.module.playways.songmanager.model.GrabWishSongModel

interface IGrabWishManageView {
    fun addGrabWishSongModels(clear: Boolean, newOffset: Long, grabWishSongModels: List<GrabWishSongModel>?)

    fun deleteWishSong(grabWishSongModel: GrabWishSongModel)
}
