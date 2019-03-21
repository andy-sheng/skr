package com.module.playways.grab.room.inter;

import com.module.playways.grab.room.model.GrabRoomSongModel;
import com.module.playways.grab.songselect.model.SpecialModel;

import java.util.List;

public interface IGrabSongManageView {
    void showTagList(List<SpecialModel> specialModelList);

    void updateSongList(List<GrabRoomSongModel> grabRoomSongModelsList);

    void hasMoreSongList(boolean hasMore);

    void changeTagSuccess(SpecialModel specialModel);
}
