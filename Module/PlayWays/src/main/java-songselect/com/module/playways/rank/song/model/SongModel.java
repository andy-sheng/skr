package com.module.playways.rank.song.model;

import com.common.log.MyLog;
import com.zq.live.proto.Common.MusicInfo;

import java.io.Serializable;

public class SongModel implements Serializable {
    /**
     * itemID : 44
     * itemName : 过火
     * cover : http://online-sound-bja.oss-cn-beijing.aliyuncs.com/cover/7b61e42c07746017d7b1c87b216ea797.jpg
     * owner : 张信哲
     * lyric : http://online-sound-bja.oss-cn-beijing.aliyuncs.com/lrc/bff5ca83193e760153b616cfdacaceb7.zrce
     * ori : http://online-sound-bja.oss-cn-beijing.aliyuncs.com/mp3/60e41e6266d57555e075bde1671b009e.mp3
     * acc : http://online-sound-bja.oss-cn-beijing.aliyuncs.com/bgm/ce09989528fb7e717e8d17462cd95737.mp3
     * midi :
     * zip :
     * rankBgm : http://online-sound-bja.oss-cn-beijing.aliyuncs.com/bgm/ce09989528fb7e717e8d17462cd95737.mp3
     * beginMs : 23000
     * endMs : 121000
     * StandIntro :
     * StandIntroBeginT : 0
     * StandIntroEndT : 0
     * totalMs : 98000
     */

    private int itemID;
    private String itemName;
    private String cover;
    private String owner;
    private String lyric;   //歌词
    private String ori;     //原唱
    private String acc;     //伴奏
    private String midi;    //midi文件
    private String zip;
    private String rankBgm;
    private int beginMs;
    private int endMs;
    private String StandIntro;
    private int StandIntroBeginT;
    private int StandIntroEndT;
    private int totalMs;
    private int rankLrcBeginT;

    public int getRankLrcBeginT() {
        return rankLrcBeginT;
    }

    public void setRankLrcBeginT(int rankLrcBeginT) {
        this.rankLrcBeginT = rankLrcBeginT;
    }

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

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getRankBgm() {
        return rankBgm;
    }

    public void setRankBgm(String rankBgm) {
        this.rankBgm = rankBgm;
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

    public String getStandIntro() {
        return StandIntro;
    }

    public void setStandIntro(String StandIntro) {
        this.StandIntro = StandIntro;
    }

    public int getStandIntroBeginT() {
        return StandIntroBeginT;
    }

    public void setStandIntroBeginT(int StandIntroBeginT) {
        this.StandIntroBeginT = StandIntroBeginT;
    }

    public int getStandIntroEndT() {
        return StandIntroEndT;
    }

    public void setStandIntroEndT(int StandIntroEndT) {
        this.StandIntroEndT = StandIntroEndT;
    }

    public int getTotalMs() {
        return totalMs;
    }

    public void setTotalMs(int totalMs) {
        this.totalMs = totalMs;
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
                ", zip='" + zip + '\'' +
                ", rankBgm='" + rankBgm + '\'' +
                ", beginMs=" + beginMs +
                ", endMs=" + endMs +
                ", StandIntro='" + StandIntro + '\'' +
                ", StandIntroBeginT=" + StandIntroBeginT +
                ", StandIntroEndT=" + StandIntroEndT +
                ", totalMs=" + totalMs +
                ", rankLrcBeginT=" + rankLrcBeginT +
                '}';
    }
}
