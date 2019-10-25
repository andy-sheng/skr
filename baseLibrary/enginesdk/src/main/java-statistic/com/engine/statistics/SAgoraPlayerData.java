package com.engine.statistics;







public class SAgoraPlayerData
{

    private String mLinePrefix = "";

    public SAgoraPlayerData setLinePrefix(String prefix) {
        if (null != prefix && 0 != prefix.length())
            mLinePrefix = prefix;

        return this;
    }

    public String toString() {
        //TODO: to implement
        return "";
    }

    public SAgoraPlayerData flush() {
        //TODO: to implement
        return this;
    }

    public SAgoraPlayerData reset() {

        return this;
        //TODO: to implement
    }
    public boolean need2Flush(){
        return false;
    }

}