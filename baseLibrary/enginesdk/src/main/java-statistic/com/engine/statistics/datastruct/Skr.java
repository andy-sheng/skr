package com.engine.statistics.datastruct;


import com.engine.statistics.SUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Skr
{
    public static class PingInfo implements ILogItem
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

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("timeStampStr", SUtils.transTime(ts));
                jsObj.put("timeStampValue", ts);
                jsObj.put("isPingOk", isPingOk);
                jsObj.put("timeCost",timeCost);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return getClass().getSimpleName();
        }


    }

    public static class NetworkInfo implements ILogItem
    {
        public long ts = 0; //timestamp;

        public int networkType; //no-network, 2G, 3G, 4G
        public String operatorName;//中国移动，中国电信，中国联通.....
        public int externlIP;
        public String geoLocation;//not used now

        public String extraInfo;

        public String toString() {
            String retStr = SUtils.transTime(ts)+" Skr.NetworkInfo: networkType="+SUtils.trans2NetworkTypeStr(networkType)+
                            ", opName="+operatorName +
                            ", externalIP="+SUtils.intToIPStr(externlIP);
            if (null != extraInfo) {
                retStr += (", extraInfo="+extraInfo+"\n");
            }
            else{
                retStr += "\n";
            }

            return retStr;
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {

                jsObj.put("timeStampStr", SUtils.transTime(ts));
                jsObj.put("timeStampValue", ts);

                jsObj.put("networkType", SUtils.trans2NetworkTypeStr(networkType));
                jsObj.put("operatorName", operatorName);
                jsObj.put("externlIP", SUtils.intToIPStr(externlIP));

                if (null != extraInfo) {
                    jsObj.put("extraInfo", extraInfo);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return getClass().getSimpleName();
        }

    }



}