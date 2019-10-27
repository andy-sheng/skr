package com.engine.statistics.datastruct;


import com.engine.statistics.SUtils;

public class Skr
{
    public static class PingInfo
    {
        public long ts = 0; //timestamp;

        public float pingTime = 0;
        public boolean pingOk = false;

        public PingInfo() {
            pingTime = -1;
            pingOk = false;
        }

        public PingInfo(int pingTime, boolean isOK) {
            this.pingTime = pingTime;
            this.pingOk = isOK;
        }

        public String toString() {
            return SUtils.transTime(ts)+" PingInfo: is_ping_ok="+pingOk+", pingTime="+pingTime+" ms\n";
        }

    }





}