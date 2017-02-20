package com.mi.live.data.user;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by caoxiangyu on 16-10-12.
 */
public class CountryData implements Comparable<CountryData> {

    public String countryName;

    public String countryCode;

    public char index;

    public boolean isHot;

    public ArrayList<Integer> lengths;

    ArrayList<String> prefix;

    public CountryData(String name, String code, boolean isHot) {
        this.isHot = isHot;
        countryName = name;
        countryCode = code;
        if (!TextUtils.isEmpty(countryName)) {
            index = name.charAt(0);
        } else {
            index = '!';
        }
    }

    @Override
    public int compareTo(CountryData another) {
        return countryName.compareTo(another.countryName);
    }
}