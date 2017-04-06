package com.mi.live.data.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.wali.live.proto.CommonProto;

/**
 * Created by lan on 16/2/26.
 */
public class Location implements Parcelable {
    public static final int TYPE_BAIDU = 0;
    public static final int TYPE_GPS = 1;

    private double lon;         //经度
    private double lat;         //纬度
    private String country;     //国家
    private String province;    //省份
    private String city;        //市
    private int type;           //地址定位的地图类型:0:百度,1:ios原生,2:高德,3:其他

    public Location() {
    }

    public Location(CommonProto.Location protoLocation) {
        parse(protoLocation);
    }

    private Location(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
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
                .setCountry(!TextUtils.isEmpty(country) ? country : "")
                .setProvince(!TextUtils.isEmpty(province) ? province : "")
                .setCity(!TextUtils.isEmpty(city) ? city : "")
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.lon);
        dest.writeDouble(this.lat);
        dest.writeString(this.country);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeInt(this.type);
    }

    protected Location(Parcel in) {
        this.lon = in.readDouble();
        this.lat = in.readDouble();
        this.country = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.type = in.readInt();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Keep
    public static class Builder {
        private Location location;

        private Builder(Location location) {
            this.location = location;
        }

        public Builder setCountry(String country) {
            location.setCountry(country);
            return this;
        }

        public Builder setProvince(String province) {
            location.setProvince(province);
            return this;
        }

        public Builder setCity(String city) {
            location.setCity(city);
            return this;
        }

        public Builder setType(int type) {
            location.setType(type);
            return this;
        }

        public Location build() {
            return location;
        }

        public static Builder newInstance(double lon, double lat) {
            return new Builder(new Location(lon, lat));
        }
    }
}
