package com.module.rankingmode.song.model;

public class TagModel {
    String tagId; //标签id
    String name; //标签名称

    public TagModel(String tagId, String name){
        this.tagId = tagId;
        this.name = name;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
