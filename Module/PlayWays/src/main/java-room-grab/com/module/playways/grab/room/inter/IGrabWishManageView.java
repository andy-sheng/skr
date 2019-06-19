package com.module.playways.grab.room.inter;

import com.module.playways.grab.room.songmanager.model.GrabWishSongModel;

import java.util.List;

public interface IGrabWishManageView {
    void addGrabWishSongModels(int fromOffset,List<GrabWishSongModel> grabWishSongModels);

    void deleteWishSong(GrabWishSongModel grabWishSongModel);
}
