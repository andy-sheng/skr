package com.module.playways.room.song.model;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.log.MyLog;
import com.zq.live.proto.Common.StandPlayType;
import com.zq.lyrics.utils.SongResUtils;
import com.zq.live.proto.Common.MusicInfo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private int rankLrcEndT;   //排位进入游戏前的背景音乐
    private boolean challengeAvailable;// 挑战是否可用
    private int playType;// 玩法类型 普通 合唱 pk
    @JSONField(name = "SPKMusic")
    private List<SongModel> pkMusicList;

    public boolean isChallengeAvailable() {
        return challengeAvailable;
    }

    public void setChallengeAvailable(boolean challengeAvailable) {
        this.challengeAvailable = challengeAvailable;
    }

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

    public int getRankLrcEndT() {
        return rankLrcEndT;
    }

    public void setRankLrcEndT(int rankLrcEndT) {
        this.rankLrcEndT = rankLrcEndT;
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
        if (totalMs <= 0) {
            MyLog.d("SongModel", "totalMs<=0 容错，返回30*1000");
            int t = standLrcEndT - standLrcBeginT;
            if (t <= 0) {
                return 30 * 1000;
            } else {
                return t;
            }
        }
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

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
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
        this.setTotalMs(musicInfo.getTotalMs());
        this.setBeginMs(musicInfo.getBeginMs());
        this.setEndMs(musicInfo.getEndMs());
        this.setRankLrcBeginT(musicInfo.getRankLrcBeginT());

        this.setStandIntro(musicInfo.getStandIntro());
        this.setStandIntroBeginT(musicInfo.getStandIntroBeginT());
        this.setStandIntroEndT(musicInfo.getStandIntroEndT());
        this.setStandLrcBeginT(musicInfo.getStandLrcBeginT());
        this.setStandLrcEndT(musicInfo.getStandLrcEndT());
        this.setIsblank(musicInfo.getIsBlank());
        this.setStandLrc(musicInfo.getStandLrc());
        this.setRankUserVoice(musicInfo.getRankUserVoice());
        this.setRankLrcEndT(musicInfo.getRankLrcEndT());
        this.setChallengeAvailable(musicInfo.getChallengeAvailable());
        this.setPlayType(musicInfo.getPlayType().getValue());
        List<MusicInfo> list = musicInfo.getSPKMusicList();
        if (list != null) {
            this.pkMusicList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                SongModel songModel = new SongModel();
                songModel.parse(list.get(i));
                this.pkMusicList.add(songModel);
            }
        }
    }

    public SongModel getPkMusic() {
        if (pkMusicList.isEmpty()) {
            return null;
        }
        return pkMusicList.get(0);
    }

    public List<SongModel> getPkMusicList() {
        return pkMusicList;
    }

    public void setPkMusicList(List<SongModel> pkMusicList) {
        this.pkMusicList = pkMusicList;
    }


    public boolean isAllResExist() {
        File lyricFile = SongResUtils.getLyricFileByUrl(getLyric());

        if (lyricFile == null || !lyricFile.exists()) {
            return false;
        }

        File acc = SongResUtils.getAccFileByUrl(getAcc());

        if (acc == null || !acc.exists()) {
            return false;
        }

//        File ori = SongResUtils.getORIFileByUrl(getOri());
//
//        if (ori == null || !ori.exists()) {
//            return false;
//        }

        File midi = SongResUtils.getMIDIFileByUrl(getMidi());

        if (midi == null || !midi.exists()) {
            return false;
        }

        return true;
    }

    public String getDisplaySongName() {
        if (playType == StandPlayType.PT_SPK_TYPE.getValue()) {
            if (!TextUtils.isEmpty(itemName) && itemName.contains("（PK版）")) {
                return itemName.substring(0, itemName.length() - 5);
            }
        } else if (playType == StandPlayType.PT_CHO_TYPE.getValue()) {
            if (!TextUtils.isEmpty(itemName) && itemName.contains("（合唱版）")) {
                return itemName.substring(0, itemName.length() - 5);
            }
        }
        return itemName;
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
                ", totalMs=" + totalMs +
                ", rankLrcBeginT=" + rankLrcBeginT +
                ", StandIntro='" + StandIntro + '\'' +
                ", StandIntroBeginT=" + StandIntroBeginT +
                ", StandIntroEndT=" + StandIntroEndT +
                ", standLrcBeginT=" + standLrcBeginT +
                ", standLrcEndT=" + standLrcEndT +
                ", isblank=" + isblank +
                ", standLrc='" + standLrc + '\'' +
                ", rankUserVoice='" + rankUserVoice + '\'' +
                ", rankLrcEndT=" + rankLrcEndT +
                ", challengeAvailable=" + challengeAvailable +
                ", playType=" + playType +
                ", pkMusicList=" + pkMusicList +
                '}';
    }

    public String toSimpleString() {
        return "SongModel{" +
                "itemID=" + itemID +
                ", itemName='" + itemName + '\'' +
//                ", totalMs=" + totalMs +
//                ", StandIntro='" + StandIntro + '\'' +
//                ", StandIntroBeginT=" + StandIntroBeginT +
//                ", StandIntroEndT=" + StandIntroEndT +
//                ", isblank=" + isblank +
//                ", standLrc='" + standLrc + '\'' +
                '}';
    }
}
