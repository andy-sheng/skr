package com.module.playways.grab.room.inter;

import com.module.playways.songmanager.model.GrabWishSongModel;

import java.util.List;

public interface IGrabWishManageView {
    void addGrabWishSongModels(boolean clear, long newOffset, List<GrabWishSongModel> grabWishSongModels);

    void deleteWishSong(GrabWishSongModel grabWishSongModel);
}
