package com.module.playways.grab.room.model;

import java.io.Serializable;

public class GrabRedPkgTaskModel implements Serializable {

    /**
     * taskID : 1
     * name : 体验一唱到底15秒，获得新手红包奖励
     * beginT : 0
     * endT : 0
     * done : false
     * deepLink : inframesker://game/grabmatch?from=server&mode=5
     * redbagExtra : {"cash":"18.8"}
     */

    private String taskID;
    private String name;
    private int beginT;
    private int endT;
    private boolean done;
    private String deepLink;
    private RedbagExtraBean redbagExtra;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBeginT() {
        return beginT;
    }

    public void setBeginT(int beginT) {
        this.beginT = beginT;
    }

    public int getEndT() {
        return endT;
    }

    public void setEndT(int endT) {
        this.endT = endT;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public RedbagExtraBean getRedbagExtra() {
        return redbagExtra;
    }

    public void setRedbagExtra(RedbagExtraBean redbagExtra) {
        this.redbagExtra = redbagExtra;
    }

    public static class RedbagExtraBean {
        /**
         * cash : 18.8
         */

        private String cash;

        public String getCash() {
            return cash;
        }

        public void setCash(String cash) {
            this.cash = cash;
        }
    }
}
