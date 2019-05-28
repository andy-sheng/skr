package com.common.core.myinfo;

import android.text.TextUtils;

import java.io.Serializable;

public class Location implements Serializable {

    /**
     * province : 北京1
     * city : 北京
     * district : 昌平区
     */

    private String province;
    private String city;
    private String district;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDesc() {
        StringBuilder sb = new StringBuilder();
        String province = getProvince();
        if (!TextUtils.isEmpty(province)) {
            sb.append(province).append("-");
        }
        String city = getCity();

        if (!TextUtils.isEmpty(city)) {
            if (!city.equals(province)) {
                sb.append(city).append("-");
            }else{
                // 说明是直辖市，例如北京
            }
        }
        String district = getDistrict();
        if (!TextUtils.isEmpty(district)) {
            sb.append(district);
        }
        return sb.toString();
    }

}
