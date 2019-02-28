package com.module.home.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

public class GameKConfigModel implements Serializable {

    @JSONField(name = "pk-tags")
    private List<String> pktags;

    @JSONField(name = "stand-tags")
    private List<String> standtags;

    public List<String> getPktags() {
        return pktags;
    }

    public void setPktags(List<String> pktags) {
        this.pktags = pktags;
    }

    public List<String> getStandtags() {
        return standtags;
    }

    public void setStandtags(List<String> standtags) {
        this.standtags = standtags;
    }
}
