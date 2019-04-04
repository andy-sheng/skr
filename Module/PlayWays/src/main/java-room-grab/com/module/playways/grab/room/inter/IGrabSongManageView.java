package com.module.playways.grab.room.inter;

import com.module.playways.grab.room.songmanager.GrabRoomSongModel;
import com.component.busilib.friends.SpecialModel;

import java.util.List;

public interface IGrabSongManageView {
    void showTagList(List<SpecialModel> specialModelList);

    void updateSongList(List<GrabRoomSongModel> grabRoomSongModelsList);

    void hasMoreSongList(boolean hasMore);

    void changeTagSuccess(SpecialModel specialModel);

    void showNum(int num);

    void deleteSong(GrabRoomSongModel grabRoomSongModel);
}
