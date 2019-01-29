package com.module.playways.rank.song.model;

import java.util.ArrayList;
import java.util.List;

public class SongCardModel {

    // list 最大限制即SongSelectFragment中DEFAULT_COUNT
    List<SongModel> list;

    public SongCardModel() {
        list = new ArrayList<>();
    }

    public List<SongModel> getList() {
        return list;
    }

    public void setList(List<SongModel> list) {
        this.list = list;
    }

}
