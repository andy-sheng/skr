package com.mi.live.data.region;

import com.wali.live.proto.UserProto;

import java.io.Serializable;

/**
 * Created by feary on 17/2/20.
 */

public class Region implements Serializable {
    public String getCountry() {
        return countryName;
    }

    public void setCountry(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    private String countryName;
    private String countryCode;

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    private int sourceType = 1;

    public Region() {

    }

    public void parseFrom(UserProto.Region region) {
        if (region != null) {
            setCountryCode(region.getCountryCode());
            setCountry(region.getCountry());
            setSourceType(region.getSourceType());
        }
    }

    public Region(UserProto.Region region) {
        parseFrom(region);
    }

    public byte[] toByteArray() {
        return UserProto.Region.newBuilder()
                .setCountryCode(countryCode)
                .setCountry(countryName)
                .setSourceType(sourceType).build().toByteArray();
    }
}
