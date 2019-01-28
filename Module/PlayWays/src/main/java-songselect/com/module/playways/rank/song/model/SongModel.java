package com.module.playways.rank.song.model;

import com.common.log.MyLog;
import com.common.utils.SongResUtils;
import com.zq.live.proto.Common.MusicInfo;

import java.io.File;
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
    private int beginMs;         //开始毫秒
    private int endMs;           //结束毫秒
    private int totalMs;         //共计多少毫秒
    private int rankLrcBeginT;   //匹配玩法第一句歌词开始时间,毫秒
    private String StandIntro;         //一唱到底的导唱
    private int StandIntroBeginT;      //一唱到底导唱的开始毫秒
    private int StandIntroEndT;        //一唱到底导唱的结束毫秒
    private int standLrcBeginT;        //一唱到底第一句歌词的开始毫秒
    private int standLrcEndT;          //一唱到底歌词的结束毫秒
    private boolean isblank = false;   //一唱到底是否是白板item
    private String standLrc = "";   //一唱到底是否是白板item
    private String rankUserVoice;   //排位进入游戏前的背景音乐

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

    public String getStandLrc() {
        return standLrc;
    }

    public void setStandLrc(String standLrc) {
        this.standLrc = standLrc;
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

    public int getStandLrcBeginT() {
        return standLrcBeginT;
    }

    public void setStandLrcBeginT(int standLrcBeginT) {
        this.standLrcBeginT = standLrcBeginT;
    }

    public int getStandLrcEndT() {
        return standLrcEndT;
    }

    public void setStandLrcEndT(int standLrcEndT) {
        this.standLrcEndT = standLrcEndT;
    }

    public int getTotalMs() {
        return totalMs;
    }

    public void setTotalMs(int totalMs) {
        this.totalMs = totalMs;
    }

    public boolean isIsblank() {
        return isblank;
    }

    public void setIsblank(boolean isblank) {
        this.isblank = isblank;
    }

    public String getRankUserVoice() {
        return rankUserVoice;
    }

    public void setRankUserVoice(String rankUserVoice) {
        this.rankUserVoice = rankUserVoice;
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
        this.setRankLrcBeginT(musicInfo.getRankLrcBeginT());

        this.setStandIntro(musicInfo.getStandIntro());
        this.setStandIntroBeginT(musicInfo.getStandIntroBeginT());
        this.setStandIntroEndT(musicInfo.getStandIntroEndT());
        this.setStandLrcBeginT(musicInfo.getStandLrcBeginT());
        this.setStandLrcEndT(musicInfo.getStandLrcEndT());
        this.setIsblank(musicInfo.getIsBlank());
        this.setStandLrc(musicInfo.getStandLrc());
        this.setRankUserVoice(musicInfo.getRankUserVoice());
    }

    public boolean isAllResExist() {
        File lyricFile = SongResUtils.getZRCELyricFileByUrl(getLyric());

        if (lyricFile == null || !lyricFile.exists()) {
            return false;
        }

        File acc = SongResUtils.getAccFileByUrl(getAcc());

        if (acc == null || !acc.exists()) {
            return false;
        }

        File ori = SongResUtils.getORIFileByUrl(getOri());

        if (ori == null || !ori.exists()) {
            return false;
        }

        File midi = SongResUtils.getMIDIFileByUrl(getMidi());

        if (midi == null || !midi.exists()) {
            return false;
        }

        return true;
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
