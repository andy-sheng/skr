package com.module.home.model;

import java.io.Serializable;
import java.util.List;

public class HomeGoldModel implements Serializable {

    /**
     * title : 第四天
     * timeStamp : 0
     * state : 3
     * bonuses : [{"type":1,"amount":15,"amountDesc":"+15"}]
     */

    private String title;
    private int timeStamp;
    private int state;
    private List<BonusesBean> bonuses;
    private int seq;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<BonusesBean> getBonuses() {
        return bonuses;
    }

    public BonusesBean getCoinBonuses() {
        for (BonusesBean bonusesBean : bonuses) {
            if (bonusesBean.getType() == 1) {
                return bonusesBean;
            }
        }

        return null;
    }

    public void setBonuses(List<BonusesBean> bonuses) {
        this.bonuses = bonuses;
    }

    public static class BonusesBean implements Serializable {
        /**
         * type : 1
         * amount : 15
         * amountDesc : +15
         */

        private int type;
        private int amount;
        private String amountDesc;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getAmountDesc() {
            return amountDesc;
        }

        public void setAmountDesc(String amountDesc) {
            this.amountDesc = amountDesc;
        }
    }
}
