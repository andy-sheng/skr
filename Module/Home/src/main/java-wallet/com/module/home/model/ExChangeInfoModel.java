package com.module.home.model;

import java.io.Serializable;

public class ExChangeInfoModel implements Serializable {

    /**
     * toZSDesc : 1点券兑换1钻石
     * toZSRatio : 100000
     * toRMBDesc : 100点券兑换人民币0.6元
     * toRMBRatio : 60000
     * toDQDesc : 1钻石礼物兑换0.9点券
     * zsBalance : {"createdAt":1555656476,"totalAmount":1400000,"lockAmount":0,"remark":"","status":2}
     * dqBalance : {"createdAt":-62135596800,"totalAmount":20000,"lockAmount":0,"remark":"","status":2}
     */

    private String toZSDesc;
    private int toZSRatio;
    private String toRMBDesc;
    private int toRMBRatio;
    private String toDQDesc;
    private ZsBalanceBean zsBalance;
    private DqBalanceBean dqBalance;

    public String getToZSDesc() {
        return toZSDesc;
    }

    public void setToZSDesc(String toZSDesc) {
        this.toZSDesc = toZSDesc;
    }

    public int getToZSRatio() {
        return toZSRatio;
    }

    public void setToZSRatio(int toZSRatio) {
        this.toZSRatio = toZSRatio;
    }

    public String getToRMBDesc() {
        return toRMBDesc;
    }

    public void setToRMBDesc(String toRMBDesc) {
        this.toRMBDesc = toRMBDesc;
    }

    public int getToRMBRatio() {
        return toRMBRatio;
    }

    public void setToRMBRatio(int toRMBRatio) {
        this.toRMBRatio = toRMBRatio;
    }

    public String getToDQDesc() {
        return toDQDesc;
    }

    public void setToDQDesc(String toDQDesc) {
        this.toDQDesc = toDQDesc;
    }

    public ZsBalanceBean getZsBalance() {
        return zsBalance;
    }

    public void setZsBalance(ZsBalanceBean zsBalance) {
        this.zsBalance = zsBalance;
    }

    public DqBalanceBean getDqBalance() {
        return dqBalance;
    }

    public void setDqBalance(DqBalanceBean dqBalance) {
        this.dqBalance = dqBalance;
    }

    public static class ZsBalanceBean implements Serializable {
        /**
         * createdAt : 1555656476
         * totalAmount : 1400000
         * lockAmount : 0
         * remark :
         * status : 2
         */

        private int createdAt;
        private int totalAmount;
        private int lockAmount;
        private String remark;
        private int status;

        public int getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(int createdAt) {
            this.createdAt = createdAt;
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public int getLockAmount() {
            return lockAmount;
        }

        public void setLockAmount(int lockAmount) {
            this.lockAmount = lockAmount;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public static class DqBalanceBean implements Serializable {
        /**
         * createdAt : -62135596800
         * totalAmount : 20000
         * lockAmount : 0
         * remark :
         * status : 2
         */

        private long createdAt;
        private int totalAmount;
        private int lockAmount;
        private String remark;
        private int status;

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public int getLockAmount() {
            return lockAmount;
        }

        public void setLockAmount(int lockAmount) {
            this.lockAmount = lockAmount;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
