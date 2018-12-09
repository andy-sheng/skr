package com.module.rankingmode.song.model;

public class SongModel {
    String mediaID;
    String coverUrl;
    String songName;
    String owner;
    String lyricUrl;
    String mediaUrl;

    public SongModel(String mediaID, String coverUrl, String songName, String owner, String lyricUrl, String mediaUrl) {
        this.mediaID = mediaID;
        this.coverUrl = coverUrl;
        this.songName = songName;
        this.owner = owner;
        this.lyricUrl = lyricUrl;
        this.mediaUrl = mediaUrl;
    }

    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) {
        this.mediaID = mediaID;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLyricUrl() {
        return lyricUrl;
    }

    public void setLyricUrl(String lyricUrl) {
        this.lyricUrl = lyricUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
