package com.module.playways.grab.room.model;

import java.io.Serializable;
import java.util.List;

public class NewChorusLyricModel implements Serializable {

    /**
     * ver : 1
     */

    private int ver;
    private List<ItemsBean> items;

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }

    public static class ItemsBean implements Serializable {
        /**
         * turn : 1
         * words : 你哭着对我说 童话里都是骗人的
         */

        private int turn;
        private String words;

        public int getTurn() {
            return turn;
        }

        public void setTurn(int turn) {
            this.turn = turn;
        }

        public String getWords() {
            return words;
        }

        public void setWords(String words) {
            this.words = words;
        }
    }
}
