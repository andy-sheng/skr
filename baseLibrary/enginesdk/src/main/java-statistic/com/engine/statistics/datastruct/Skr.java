package com.engine.statistics.datastruct;


import com.engine.statistics.SUtils;

public class Skr
{
    public static class PingInfo
    {
        public long ts = 0; //timestamp;

        public float timeCost = 0;
        public boolean isPingOk = false;

        public PingInfo() {
            timeCost = -1;
            isPingOk = false;
        }

        public PingInfo(int timeCost, boolean isOK) {
            this.timeCost = timeCost;
            this.isPingOk = isOK;
        }

        public String toString() {
            return SUtils.transTime(ts)+" PingInfo: is_ping_ok="+isPingOk+", time="+timeCost+" ms\n";
        }
    }

    public static class NetworkInfo
    {
        public long ts = 0; //timestamp;

        public int networkType; //no-network, 2G, 3G, 4G
        public String operatorName;//中国移动，中国电信，中国联通.....
        public int externlIP;
        public String geoLocation;


        public String toString() {
            return SUtils.transTime(ts)+" Skr.NetworkInfo: networkType="+SUtils.trans2NetworkTypeStr(networkType)+
                    ", opName="+operatorName +
                    ", externalIP="+SUtils.intToIPStr(externlIP) +
                    "\n";
        }
    }



}