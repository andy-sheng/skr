package com.module.rankingmode.song.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SongModel implements Parcelable {
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
     * melp : http://sound-huabei-inframe.oss-cn-beijing.aliyuncs.com/mel/smlt.melp
     * zip : http://sound-huabei-inframe.oss-cn-beijing.aliyuncs.com/mel/smlt_zbd.zip
     */

    private int itemID;
    private String itemName;
    private String cover;
    private String owner;
    private String lyric;
    private String ori;
    private String acc;
    private String midi;
    private int totalMs;
    private int beginMs;
    private int endMs;
    private String melp;
    private String zip;


    public SongModel() {
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

    public String getMelp() {
        return melp;
    }

    public void setMelp(String melp) {
        this.melp = melp;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.itemID);
        dest.writeString(this.itemName);
        dest.writeString(this.cover);
        dest.writeString(this.owner);
        dest.writeString(this.lyric);
        dest.writeString(this.ori);
        dest.writeString(this.acc);
        dest.writeString(this.midi);
        dest.writeInt(this.totalMs);
        dest.writeInt(this.beginMs);
        dest.writeInt(this.endMs);
        dest.writeString(this.melp);
        dest.writeString(this.zip);
    }

    protected SongModel(Parcel in) {
        this.itemID = in.readInt();
        this.itemName = in.readString();
        this.cover = in.readString();
        this.owner = in.readString();
        this.lyric = in.readString();
        this.ori = in.readString();
        this.acc = in.readString();
        this.midi = in.readString();
        this.totalMs = in.readInt();
        this.beginMs = in.readInt();
        this.endMs = in.readInt();
        this.melp = in.readString();
        this.zip = in.readString();
    }

    public static final Parcelable.Creator<SongModel> CREATOR = new Parcelable.Creator<SongModel>() {
        @Override
        public SongModel createFromParcel(Parcel source) {
            return new SongModel(source);
        }

        @Override
        public SongModel[] newArray(int size) {
            return new SongModel[size];
        }
    };
}
