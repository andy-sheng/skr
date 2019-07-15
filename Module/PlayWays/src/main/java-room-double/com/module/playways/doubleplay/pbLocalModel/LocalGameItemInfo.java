package com.module.playways.doubleplay.pbLocalModel;

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
    private Object question;

    public LocalGameItemInfo(int itemID, String itemDesc) {
        this.itemID = itemID;
        this.desc = itemDesc;
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

    public Object getQuestion() {
        return question;
    }

    public void setQuestion(Object question) {
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
    }
}
