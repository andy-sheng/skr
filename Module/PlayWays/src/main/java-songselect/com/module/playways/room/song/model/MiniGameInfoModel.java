package com.module.playways.room.song.model;

import com.zq.live.proto.Common.MiniGameInfo;

import java.io.Serializable;

public class MiniGameInfoModel implements Serializable {

    int gameID;
    String gameName;
    String gameRule;
    int gamePlayType;
    String keyWord;
    String fixedTxt;
    String songURL;

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameRule() {
        return gameRule;
    }

    public void setGameRule(String gameRule) {
        this.gameRule = gameRule;
    }

    public int getGamePlayType() {
        return gamePlayType;
    }

    public void setGamePlayType(int gamePlayType) {
        this.gamePlayType = gamePlayType;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getFixedTxt() {
        return fixedTxt;
    }

    public void setFixedTxt(String fixedTxt) {
        this.fixedTxt = fixedTxt;
    }

    public String getSongURL() {
        return songURL;
    }

    public void setSongURL(String songURL) {
        this.songURL = songURL;
    }

    public void parse(MiniGameInfo miniGameInfo) {
        this.setGameID(miniGameInfo.getGameID());
        this.setGameName(miniGameInfo.getGameName());
        this.setGameRule(miniGameInfo.getGameRule());
        this.setGamePlayType(miniGameInfo.getGamePlayType().getValue());
        this.setKeyWord(miniGameInfo.getKeyWord());
        this.setFixedTxt(miniGameInfo.getFixedTxt());
        this.setSongURL(miniGameInfo.getSongURL());
    }


    @Override
    public String toString() {
        return "MiniGameInfoModel{" +
                "gameID=" + gameID +
                ", gameName='" + gameName + '\'' +
                ", gameRule='" + gameRule + '\'' +
                ", gamePlayType=" + gamePlayType +
                ", keyWord='" + keyWord + '\'' +
                ", fixedTxt='" + fixedTxt + '\'' +
                ", songURL='" + songURL + '\'' +
                '}';
    }
}
