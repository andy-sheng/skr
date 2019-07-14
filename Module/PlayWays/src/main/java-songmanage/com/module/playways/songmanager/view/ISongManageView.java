package com.module.playways.songmanager.view;

import com.module.playways.songmanager.model.RecommendTagModel;

import java.util.List;

public interface ISongManageView {
    void showRoomName(String roomName);

    void showRecommendSong(List<RecommendTagModel> recommendTagModelList);
}
