package com.module.playways.room.song.model;

import com.zq.live.proto.Common.EMiniGamePlayType;
import com.zq.live.proto.Common.MiniGameInfo;

import org.apache.commons.lang3.text.StrBuilder;

import java.io.Serializable;

public class MiniGameInfoModel implements Serializable {

    int gameID;
    String gameName;
    String gameRule;
    int gamePlayType;
    String keyWord;
    String fixedTxt;
    MiniGameSongInfoModel songInfo;

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

    public MiniGameSongInfoModel getSongInfo() {
        return songInfo;
    }

    public void setSongInfo(MiniGameSongInfoModel songInfo) {
        this.songInfo = songInfo;
    }

    /**
     * 展示的游戏规则
     *
     * @return
     */
    public String getDisplayGameRule() {
        StringBuilder result = new StringBuilder();
        result.append(gameRule);
        result.append("\n");
        if (gamePlayType == EMiniGamePlayType.EMGP_KEYWORD.getValue()) {
            result.append(keyWord);
        } else if (gamePlayType == EMiniGamePlayType.EMGP_FIXED_TXT.getValue()) {
            result.append(fixedTxt);
        } else if (gamePlayType == EMiniGamePlayType.EMGP_SONG_DETAIL.getValue()) {
            result.append("《" + songInfo.getSongName() + "》");
        }
        return result.toString();
    }

    public static MiniGameInfoModel parse(MiniGameInfo miniGameInfo) {
        MiniGameInfoModel gameInfoModel = new MiniGameInfoModel();
        gameInfoModel.setGameID(miniGameInfo.getGameID());
        gameInfoModel.setGameName(miniGameInfo.getGameName());
        gameInfoModel.setGameRule(miniGameInfo.getGameRule());
        gameInfoModel.setGamePlayType(miniGameInfo.getGamePlayType().getValue());
        gameInfoModel.setKeyWord(miniGameInfo.getKeyWord());
        gameInfoModel.setFixedTxt(miniGameInfo.getFixedTxt());
        gameInfoModel.setSongInfo(MiniGameSongInfoModel.parse(miniGameInfo.getSongInfo()));
        return gameInfoModel;
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
                ", songInfo=" + songInfo +
                '}';
    }
}
