package com.mi.live.data.account.event;

/**
 * @module 地区国际化
 * Created by caoxiangyu on 16-10-18.
 */
public class SiteChangeEvent {
    public String countryName;
    public SiteChangeEvent(String countryName) {
        this.countryName = countryName;
    }
}
