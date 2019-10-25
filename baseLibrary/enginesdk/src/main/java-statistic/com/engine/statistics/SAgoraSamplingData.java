package com.engine.statistics;


public class SAgoraSamplingData
{

    private String mLinePrefix = "";

    public SAgoraSamplingData setLinePrefix(String prefix) {
        if (null != prefix && 0 != prefix.length())
            mLinePrefix = prefix;

        return this;
    }

    public String toString() {
        //TODO: to implement
        return "";
    }


    public SAgoraSamplingData flush() {

        return this;
        //TODO: to implement
    }

    public SAgoraSamplingData reset() {

        return this;
        //TODO: to implement
    }

    public boolean need2Flush(){
        return false;
    }
}