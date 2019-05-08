package com.module.home.model;

import java.io.Serializable;

public class RechargeItemModel implements Serializable {

    /**
     * goodsID : 200002
     * name : 钻石
     * price : 100000
     * quantity : 100
     * thirdProductID :
     * desc : 100钻石1元
     */

    private String goodsID;
    private String name;
    private int price;
    private int quantity;
    private String thirdProductID;
    private String desc;

    public String getGoodsID() {
        return goodsID;
    }

    public void setGoodsID(String goodsID) {
        this.goodsID = goodsID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getThirdProductID() {
        return thirdProductID;
    }

    public void setThirdProductID(String thirdProductID) {
        this.thirdProductID = thirdProductID;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
