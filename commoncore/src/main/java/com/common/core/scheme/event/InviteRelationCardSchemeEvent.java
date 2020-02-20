package com.common.core.scheme.event;

public class InviteRelationCardSchemeEvent {
    int goodsID = 0;
    String packetID = "";

    public InviteRelationCardSchemeEvent(int goodsID, String packetID) {
        this.goodsID = goodsID;
        this.packetID = packetID;
    }

    public int getGoodsID() {
        return goodsID;
    }

    public String getPacketID() {
        return packetID;
    }
}
