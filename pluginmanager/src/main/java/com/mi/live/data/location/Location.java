package com.mi.live.data.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

@Keep
public class Location implements Parcelable {
    private double lon;         //经度
    private double lat;         //纬度
    private String country;     //国家
    private String province;    //省份
    private String city;        //市
    private int type;           //地址定位的地图类型:0:百度,1:ios原生,2:高德,3:其他,4:google

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

    private Location(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    private void setCountry(String country) {
        this.country = country;
    }

    private void setProvince(String province) {
        this.province = province;
    }

    private void setCity(String city) {
        this.city = city;
    }

    private void setType(int type) {
        this.type = type;
    }

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