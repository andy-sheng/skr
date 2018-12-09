package com.module.rankingmode.song.view;

import com.module.rankingmode.song.model.SongModel;

import java.util.List;

public interface ISongTagDetailView {
    // 获取曲库剧本的详细条目
    void loadSongsDetailItems(List<SongModel> list);

    void loadSongsDetailItemsFail();
}
