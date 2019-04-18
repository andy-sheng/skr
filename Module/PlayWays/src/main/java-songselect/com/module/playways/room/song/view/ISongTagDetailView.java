package com.module.playways.room.song.view;

import com.module.playways.room.song.model.SongModel;

import java.util.List;

public interface ISongTagDetailView {
    // 获取曲库剧本的详细条目
    void loadSongsDetailItems(List<SongModel> list, int offset, boolean hasMore);

    void loadSongsDetailItemsFail();
}
