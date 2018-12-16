package com.module.rankingmode.song.model;

import com.common.log.MyLog;
import com.zq.live.proto.Common.MusicInfo;

import java.io.Serializable;

public class SongModel implements Serializable {
    /**
     * itemID : 10
     * itemName : 沙漠骆驼
     * cover : http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/cover/song/smlt_cover.jpg
     * owner : 展展与罗罗
     * lyric : http://sound-huabei-inframe.oss-cn-beijing.aliyuncs.com/lrc/smlt.zrce
     * ori : http://sound-huabei-inframe.oss-cn-beijing.aliyuncs.com/mp3/smlt_mp3.mp3
     * acc : http://sound-huabei-inframe.oss-cn-beijing.aliyuncs.com/bgm/smlt_sound.mp3
     * midi :
     * totalMs : 338000
     * beginMs : 0
     * endMs : 338000
     * zip : http://sound-huabei-inframe.oss-cn-beijing.aliyuncs.com/mel/smlt_zbd.zip
     */

    private int itemID;
    private String itemName;
    private String cover;
    private String owner;
    //歌词
    private String lyric;
    //原唱
    private String ori;
    //伴奏
    private String acc;
    //midi文件，用来评分
    private String midi;
    private int totalMs;
    private int beginMs;
    private int endMs;
    private String zip;

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getOri() {
        return ori;
    }

    public void setOri(String ori) {
        this.ori = ori;
    }

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public String getMidi() {
        return midi;
    }

    public void setMidi(String midi) {
        this.midi = midi;
    }

    public int getTotalMs() {
        return totalMs;
    }

    public void setTotalMs(int totalMs) {
        this.totalMs = totalMs;
    }

    public int getBeginMs() {
        return beginMs;
    }

    public void setBeginMs(int beginMs) {
        this.beginMs = beginMs;
    }

    public int getEndMs() {
        return endMs;
    }

    public void setEndMs(int endMs) {
        this.endMs = endMs;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void parse(MusicInfo musicInfo) {
        if (musicInfo == null) {
            MyLog.e("SongModel MusicInfo == null");
            return;
        }

        this.setItemID(musicInfo.getItemID());
        this.setItemName(musicInfo.getItemName());
        this.setCover(musicInfo.getCover());
        this.setOwner(musicInfo.getOwner());
        this.setLyric(musicInfo.getLyric());
        this.setOri(musicInfo.getOri());
        this.setAcc(musicInfo.getAcc());
        this.setMidi(musicInfo.getMidi());
        this.setZip(musicInfo.getZip());
        this.setTotalMs(musicInfo.getTotalTimeMs());
        this.setBeginMs(musicInfo.getBeginTimeMs());
        this.setEndMs(musicInfo.getEndTimeMs());
        // todo PB缺两个现在，等服务器完善
    }

    @Override
    public String toString() {
        return "SongModel{" +
                "itemID=" + itemID +
                ", itemName='" + itemName + '\'' +
                ", cover='" + cover + '\'' +
                ", owner='" + owner + '\'' +
                ", lyric='" + lyric + '\'' +
                ", ori='" + ori + '\'' +
                ", acc='" + acc + '\'' +
                ", midi='" + midi + '\'' +
                ", totalMs=" + totalMs +
                ", beginMs=" + beginMs +
                ", endMs=" + endMs +
                ", zip='" + zip + '\'' +
                '}';
    }
}
