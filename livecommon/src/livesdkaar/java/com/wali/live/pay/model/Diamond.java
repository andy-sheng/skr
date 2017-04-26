package com.wali.live.pay.model;

import android.support.annotation.NonNull;

import com.wali.live.proto.PayProto;

/**
 * Created by chengsimin on 16/2/27.
 */
public class Diamond {
    private int id;
    private int count;// 钻石数量
    private int extraGive;// 额外数量
    private int price;//价格，单位：分
    private int maxBuyTimes;//最大购买次数
    private String subTitle;//副标题
    private String iconUrl;
    private SkuDetail skuDetail;// GooglePlay、PayPal上的商品信息
    private int expireVirtualGemCnt;// 即将过期的虚拟钻数量
    private int expireGiftCardCnt;// 即将过期的礼物卡数量

    public Diamond() {

    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public int getPrice() {
        return price;
    }

    public int getExtraGive() {
        return extraGive;
    }

    public int getMaxBuyTimes() {
        return maxBuyTimes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setExtraGive(int extraGive) {
        this.extraGive = extraGive;
    }

    public void setMaxBuyTimes(int maxBuyTimes) {
        this.maxBuyTimes = maxBuyTimes;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public SkuDetail getSkuDetail() {
        return skuDetail;
    }

    public void setSkuDetail(SkuDetail skuDetail) {
        this.skuDetail = skuDetail;
    }

    public int getExpireGiftCardCnt() {
        return expireGiftCardCnt;
    }

    public void setExpireGiftCardCnt(int expireGiftCardCnt) {
        this.expireGiftCardCnt = expireGiftCardCnt;
    }

    public int getExpireVirtualGemCnt() {
        return expireVirtualGemCnt;
    }

    public void setExpireVirtualGemCnt(int expireVirtualGemCnt) {
        this.expireVirtualGemCnt = expireVirtualGemCnt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Diamond{");
        sb.append("count=").append(count);
        sb.append(", id=").append(id);
        sb.append(", extraGive=").append(extraGive);
        sb.append(", price=").append(price);
        sb.append(", maxBuyTimes=").append(maxBuyTimes);
        sb.append(", subTitle='").append(subTitle).append('\'');
        sb.append(", iconUrl='").append(iconUrl).append('\'');
        sb.append(", skuDetail=").append(skuDetail);
        sb.append(", expireVirtualGemCnt=").append(expireVirtualGemCnt);
        sb.append(", expireGiftCardCnt=").append(expireGiftCardCnt);
        sb.append('}');
        return sb.toString();
    }

    public static Diamond parse(@NonNull PayProto.GemGoods goods) {
        Diamond diamond = new Diamond();
        diamond.setId(goods.getGoodsId());
        diamond.setCount(goods.getGemCnt());
        diamond.setExtraGive(goods.getGiveGemCnt());
        diamond.setMaxBuyTimes(goods.getMaxBuyTimes());
        diamond.setPrice(goods.getPrice());
        diamond.setSubTitle(goods.getSubtitle());
        diamond.setIconUrl(goods.getIcon());
        return diamond;
    }
}
