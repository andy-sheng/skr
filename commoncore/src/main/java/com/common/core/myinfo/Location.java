package com.common.core.myinfo;

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
}
