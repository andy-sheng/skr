package com.module.playways.songmanager.view;

import com.module.playways.songmanager.model.GrabRoomSongModel;
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
