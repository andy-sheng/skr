package com.module.playways.grab.room.songmanager.view;

import com.module.playways.grab.room.songmanager.model.RecommendTagModel;

import java.util.List;

public interface IOwnerManageView {
    void showRoomName(String roomName);

    void showRecommendSong(List<RecommendTagModel> recommendTagModelList);
}
