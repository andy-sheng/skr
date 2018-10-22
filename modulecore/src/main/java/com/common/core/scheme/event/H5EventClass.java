package com.common.core.scheme.event;

public class H5EventClass {

    public static class H5FirstPayEvent {
        public int gooid;
        public int gemCnt;
        public int giveGemCnt;
        public int goodPrice;
        public int payType;
        public int channel;

        public H5FirstPayEvent(int gooid, int gemCnt, int giveGemCnt, int goodPrice, int payType, int channel) {
            this.gooid = gooid;
            this.gemCnt = gemCnt;
            this.giveGemCnt = giveGemCnt;
            this.goodPrice = goodPrice;
            this.payType = payType;
            this.channel = channel;
        }
    }

    /**
     * h5页面上, 用户没有登录的事件
     */
    public static class H5UnloginEvent {

    }
}
