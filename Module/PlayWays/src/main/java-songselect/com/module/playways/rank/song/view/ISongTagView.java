package com.module.playways.rank.song.view;

import com.module.playways.rank.song.model.TagModel;

import java.util.List;

public interface ISongTagView {

    // 获取曲库剧本标签成功
    void loadSongsTags(List<TagModel> list);

    // 获取曲库剧本标签失败
    void loadSongsTagsFail();
}
