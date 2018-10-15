package com.wali.live.watchsdk.income;

import com.wali.live.proto.PayProto;

/**
 * Created by liuyanyan on 16/2/29.
 */
public class Exchange {
    private int id;//列表ID
    private int diamondNum;//兑换钻数量
    private int ticketNum;//需要尚票数量
    private int extraDiamondNum;//赠送钻数量
    private int goldGemCnt;
    private int silverGemCnt;

    public Exchange(int id, int diamondNum, int ticketNum, int extraDiamondNum) {
        this.id = id;
        this.diamondNum = diamondNum;
        this.ticketNum = ticketNum;
        this.extraDiamondNum = extraDiamondNum;
    }

    public Exchange(PayProto.GemExchange gemExchange) {
        this.id = gemExchange.getExchangeId();
        this.diamondNum = gemExchange.getGemCnt();
        this.ticketNum = gemExchange.getTicketCnt();
        this.extraDiamondNum = gemExchange.getGiveGemCnt();
        this.goldGemCnt = gemExchange.getGoldGemCnt();
        this.silverGemCnt = gemExchange.getVirtualGemCnt();
    }

    /**
     * exchangeId
     *
     * @return
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 兑换目标数量，钻石或米币
     *
     * @return
     */
    public int getDiamondNum() {
        return diamondNum;
    }

    public void setDiamondNum(int diamondNum) {
        this.diamondNum = diamondNum;
    }

    /**
     * 消耗星票数量，普通星票或米币星票
     *
     * @return
     */
    public int getTicketNum() {
        return ticketNum;
    }

    public void setTicketNum(int ticketNum) {
        this.ticketNum = ticketNum;
    }

    /**
     * 额外赠送的目标数量，钻石或米币
     *
     * @return
     */
    public int getExtraDiamondNum() {
        return extraDiamondNum;
    }

    public void setExtraDiamondNum(int extraDiamondNum) {
        this.extraDiamondNum = extraDiamondNum;
    }

    public int getGoldGemCnt() {
        return goldGemCnt;
    }

    public void setGoldGemCnt(int goldGemCnt) {
        this.goldGemCnt = goldGemCnt;
    }

    public int getSilverGemCnt() {
        return silverGemCnt;
    }

    public void setSilverGemCnt(int silverGemCnt) {
        this.silverGemCnt = silverGemCnt;
    }
}
