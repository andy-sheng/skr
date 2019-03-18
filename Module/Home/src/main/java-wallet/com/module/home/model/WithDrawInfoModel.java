package com.module.home.model;

import java.io.Serializable;
import java.util.List;

public class WithDrawInfoModel implements Serializable {


    /**
     * available : 100.0
     * locked : 10.0
     * availableInt : 10000000
     * lockedInt : 1000000
     * rule : ["1. 这是提现的规则","2. 提现规则2","3.提现规则"]
     * maxWihtrawAmount : 100000000
     * isRealAuth : true
     * cfg : [{"channel":1,"isBind":true}]
     */

    private String available;
    private String locked;
    private long availableInt;
    private long lockedInt;
    private long maxWihtrawAmount;
    private boolean isRealAuth;
    private List<String> rule;
    private List<CfgBean> cfg;

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public long getAvailableInt() {
        return availableInt;
    }

    public void setAvailableInt(long availableInt) {
        this.availableInt = availableInt;
    }

    public long getLockedInt() {
        return lockedInt;
    }

    public void setLockedInt(long lockedInt) {
        this.lockedInt = lockedInt;
    }

    public long getMaxWihtrawAmount() {
        return maxWihtrawAmount;
    }

    public void setMaxWihtrawAmount(long maxWihtrawAmount) {
        this.maxWihtrawAmount = maxWihtrawAmount;
    }

    public boolean isIsRealAuth() {
        return isRealAuth;
    }

    public void setIsRealAuth(boolean isRealAuth) {
        this.isRealAuth = isRealAuth;
    }

    public List<String> getRule() {
        return rule;
    }

    public void setRule(List<String> rule) {
        this.rule = rule;
    }

    public List<CfgBean> getCfg() {
        return cfg;
    }

    public void setCfg(List<CfgBean> cfg) {
        this.cfg = cfg;
    }

    public CfgBean getByChannel(int channel){
        for (WithDrawInfoModel.CfgBean cfgBean :
                getCfg()) {
            if (cfgBean.getChannel() == channel) {
                return cfgBean;
            }
        }

        return null;
    }

    public static class CfgBean implements Serializable {
        /**
         * channel : 1
         * isBind : true
         */

        private int channel;
        private boolean isBind;

        public int getChannel() {
            return channel;
        }

        public void setChannel(int channel) {
            this.channel = channel;
        }

        public boolean isIsBind() {
            return isBind;
        }

        public void setIsBind(boolean isBind) {
            this.isBind = isBind;
        }
    }
}
