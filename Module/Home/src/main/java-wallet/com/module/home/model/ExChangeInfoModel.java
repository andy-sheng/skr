package com.module.home.model;

import java.io.Serializable;

public class ExChangeInfoModel implements Serializable {


    /**
     * dqBalance : {"createdAt":-62135596800,"lockAmount":0,"lockAmountStr":"0.00","remark":"","status":2,"totalAmount":660,"totalAmountStr":"0.66"}
     * toDQDesc : 1钻石礼物兑换0.9点券
     * toRMBDesc : 100点券兑换人民币0.6元
     * toRMBRatio : 600
     * toZSDesc : 1点券兑换1钻石
     * toZSRatio : 1
     * zsBalance : {"createdAt":1555656476,"lockAmount":0,"lockAmountStr":"0.00","remark":"","status":2,"totalAmount":1419340,"totalAmountStr":"1419.34"}
     */

    private DqBalanceBean dqBalance;
    private String toDQDesc;
    private String toRMBDesc;
    private int toRMBRatio;
    private String toZSDesc;
    private int toZSRatio;
    private ZsBalanceBean zsBalance;

    public DqBalanceBean getDqBalance() {
        return dqBalance;
    }

    public void setDqBalance(DqBalanceBean dqBalance) {
        this.dqBalance = dqBalance;
    }

    public String getToDQDesc() {
        return toDQDesc;
    }

    public void setToDQDesc(String toDQDesc) {
        this.toDQDesc = toDQDesc;
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

    public ZsBalanceBean getZsBalance() {
        return zsBalance;
    }

    public void setZsBalance(ZsBalanceBean zsBalance) {
        this.zsBalance = zsBalance;
    }

    public static class DqBalanceBean implements Serializable {
        /**
         * createdAt : -62135596800
         * lockAmount : 0
         * lockAmountStr : 0.00
         * remark :
         * status : 2
         * totalAmount : 660
         * totalAmountStr : 0.66
         */

        private long createdAt;
        private int lockAmount;
        private String lockAmountStr;
        private String remark;
        private int status;
        private int totalAmount;
        private String totalAmountStr;

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public int getLockAmount() {
            return lockAmount;
        }

        public void setLockAmount(int lockAmount) {
            this.lockAmount = lockAmount;
        }

        public String getLockAmountStr() {
            return lockAmountStr;
        }

        public void setLockAmountStr(String lockAmountStr) {
            this.lockAmountStr = lockAmountStr;
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

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getTotalAmountStr() {
            return totalAmountStr;
        }

        public void setTotalAmountStr(String totalAmountStr) {
            this.totalAmountStr = totalAmountStr;
        }
    }

    public static class ZsBalanceBean implements Serializable {
        /**
         * createdAt : 1555656476
         * lockAmount : 0
         * lockAmountStr : 0.00
         * remark :
         * status : 2
         * totalAmount : 1419340
         * totalAmountStr : 1419.34
         */

        private int createdAt;
        private int lockAmount;
        private String lockAmountStr;
        private String remark;
        private int status;
        private int totalAmount;
        private String totalAmountStr;

        public int getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(int createdAt) {
            this.createdAt = createdAt;
        }

        public int getLockAmount() {
            return lockAmount;
        }

        public void setLockAmount(int lockAmount) {
            this.lockAmount = lockAmount;
        }

        public String getLockAmountStr() {
            return lockAmountStr;
        }

        public void setLockAmountStr(String lockAmountStr) {
            this.lockAmountStr = lockAmountStr;
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

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getTotalAmountStr() {
            return totalAmountStr;
        }

        public void setTotalAmountStr(String totalAmountStr) {
            this.totalAmountStr = totalAmountStr;
        }
    }
}
