package com.module.playways.rank.song.model;

import java.util.ArrayList;
import java.util.List;

public class SongCardModel {

    List<SongModel> list;

    public static int MAX_COUNT = 6; // 每张卡片可容纳最多SongModer

    public SongCardModel() {
        list = new ArrayList<>(MAX_COUNT);
    }

    public List<SongModel> getList() {
        return list;
    }

    public void setList(List<SongModel> list) {
        this.list = list;
    }

}
