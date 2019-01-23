package com.module.home.model;

public class SlideShowModel {

    /**
     * roundSeq : 1
     * coverURL : http://cover-1
     * linkURL : http://link-1
     * desc : desc-1
     */

    private int roundSeq;
    private String coverURL;
    private String linkURL;
    private String desc;

    public int getRoundSeq() {
        return roundSeq;
    }

    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public String getLinkURL() {
        return linkURL;
    }

    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
