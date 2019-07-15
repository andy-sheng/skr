package com.module.playways.doubleplay.pbLocalModel;

import com.zq.live.proto.Common.GameItemInfo;

import java.io.Serializable;

public class LocalGameItemInfo implements Serializable {


    /**
     * itemID : 1
     * gameType : 1
     * desc : 一起唱隔壁老樊的歌
     * music : {"title":"【一起唱隔壁老樊的歌】","content":"轮流演唱隔壁老樊的歌 看谁会的多","example":"例如：我曾把完整的镜子打碎"}
     * question : null
     */

    private int itemID;
    private int gameType;
    private String desc;
    private MusicBean music;
    private QuestionBean question;

    public LocalGameItemInfo(GameItemInfo gameItemInfo) {
        this.itemID = gameItemInfo.getItemID();
        this.desc = gameItemInfo.getDesc();
        music = new MusicBean();
        music.content = gameItemInfo.getMusic().getContent();
        music.example = gameItemInfo.getMusic().getExample();
        music.title = gameItemInfo.getMusic().getTitle();
        gameType = gameItemInfo.getGameType().getValue();
        question = new QuestionBean();
        question.content = gameItemInfo.getQuestion().getContent();
    }

    public LocalGameItemInfo() {
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public MusicBean getMusic() {
        return music;
    }

    public void setMusic(MusicBean music) {
        this.music = music;
    }

    public QuestionBean getQuestion() {
        return question;
    }

    public void setQuestion(QuestionBean question) {
        this.question = question;
    }

    public static class MusicBean implements Serializable {
        /**
         * title : 【一起唱隔壁老樊的歌】
         * content : 轮流演唱隔壁老樊的歌 看谁会的多
         * example : 例如：我曾把完整的镜子打碎
         */

        private String title;
        private String content;
        private String example;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getExample() {
            return example;
        }

        public void setExample(String example) {
            this.example = example;
        }

        @Override
        public String toString() {
            return "MusicBean{" +
                    "title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", example='" + example + '\'' +
                    '}';
        }
    }

    public static class QuestionBean implements Serializable {
        /**
         * title : 【一起唱隔壁老樊的歌】
         * content : 轮流演唱隔壁老樊的歌 看谁会的多
         * example : 例如：我曾把完整的镜子打碎
         */

        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "QuestionBean{" +
                    "content='" + content + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LocalGameItemInfo{" +
                "itemID=" + itemID +
                ", gameType=" + gameType +
                ", desc='" + desc + '\'' +
                ", music=" + music +
                ", question=" + question +
                '}';
    }
}
