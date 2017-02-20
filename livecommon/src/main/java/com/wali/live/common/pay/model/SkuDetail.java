package com.wali.live.common.pay.model;

import java.math.BigDecimal;

/**
 * Created by rongzhisheng on 16-5-20.
 */
public class SkuDetail {
    public static final BigDecimal DIVISOR = new BigDecimal(1000000L);
    /**
     * 商品ID
     */
    private String productId;
    /**
     * 商品类型{inapp,subs}
     */
    private String type;
    /**
     * 商品的格式化价格，包含货比符号，不含税，例如￥0.5
     */
    private String price;
    /**
     * 1个货币单位乘以1000000后的结果，例如0.5人民币的值为500000
     */
    private long priceAmountMicros;
    /**
     * 货币代号，例如人民币是CNY，详见https://en.wikipedia.org/wiki/ISO_4217#Active_codes
     */
    private String priceCurrencyCode;
    /**
     * 商品标题
     */
    private String title;
    /**
     * 商品描述
     */
    private String description;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public long getPriceAmountMicros() {
        return priceAmountMicros;
    }

    public void setPriceAmountMicros(long priceAmountMicros) {
        this.priceAmountMicros = priceAmountMicros;
    }

    public String getPriceCurrencyCode() {
        return priceCurrencyCode;
    }

    public void setPriceCurrencyCode(String priceCurrencyCode) {
        this.priceCurrencyCode = priceCurrencyCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SkuDetail{");
        sb.append("productId='").append(productId).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", price='").append(price).append('\'');
        sb.append(", priceAmountMicros=").append(priceAmountMicros);
        sb.append(", priceCurrencyCode='").append(priceCurrencyCode).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
