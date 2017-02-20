package com.mi.live.data.location;

import android.text.TextUtils;

import com.wali.live.proto.CommonProto;

/**
 * Created by lan on 16/2/26.
 */
public class Location {
    public static final int TYPE_BAIDU = 0;
    public static final int TYPE_GPS = 1;

    private double lon;         //经度
    private double lat;         //纬度
    private String country;     //国家
    private String province;    //省份
    private String city;        //市
    private int type;           //地址定位的地图类型:0:百度,1:ios原生,2:高德,3:其他

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    private Address address;

    public Location() {
    }

    public Location(CommonProto.Location protoLocation) {
        parse(protoLocation);
    }

    public void parse(CommonProto.Location protoLocation) {
        this.lon = protoLocation.getLon();
        this.lat = protoLocation.getLat();
        this.country = protoLocation.getCountry();
        this.province = protoLocation.getProvince();
        this.city = protoLocation.getCity();
    }

    /**
     * 使用本地定位初始化
     */
    public Location(double latitude, double longitude, Address address) {
        this.lon = longitude;
        this.lat = latitude;
        setAddress(address);

        // 防止为空,否则下面的build会有空指针
        if (address != null) {
            this.country = TextUtils.isEmpty(address.country) ? "" : address.country;
            this.province = TextUtils.isEmpty(address.province) ? "" : address.province;
            this.city = TextUtils.isEmpty(address.city) ? "" : address.city;
        } else {
            this.country = "";
            this.province = "";
            this.city = "";
        }
    }

    public CommonProto.Location build() {
        CommonProto.Location protoLocation = CommonProto.Location.newBuilder()
                .setLon(lon)
                .setLat(lat)
                .setCountry(country)
                .setProvince(province)
                .setCity(city)
                .build();
        return protoLocation;
    }

    public String getCity() {
        if (TextUtils.isEmpty(city) || city.equals("null")) {
            city = "";
        }
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
