package com.zq.engine.avstatistics.datastruct;


import com.zq.engine.avstatistics.SUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Skr
{
    public static class PingInfo implements ILogItem
    {
        public long ts = 0; //timestamp;

        public long timeCost = 0;
        public boolean isPingOk = false;

        public PingInfo() {
            timeCost = -1;
            isPingOk = false;
        }

        public PingInfo(long timeCost, boolean isOK) {
            this.timeCost = timeCost;
            this.isPingOk = isOK;
        }

        public String toString() {
            return SUtils.transTime(ts)+" PingInfo: isOK="+isPingOk+", tc="+timeCost+" ms\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(ts));
                jsObj.put("tsValue", ts);
                jsObj.put("isOk", isPingOk);
                jsObj.put("TC",timeCost);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "PingInfo";
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
            String retStr = SUtils.transTime(ts)+" NetworkInfo: type="+SUtils.trans2NetworkTypeStr(networkType)+
                            ", opName="+operatorName +
                            ", IP="+SUtils.intToIPStr(externlIP);
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

                jsObj.put("tsStr", SUtils.transTime(ts));
                jsObj.put("tsValue", ts);

                jsObj.put("type", SUtils.trans2NetworkTypeStr(networkType));
                jsObj.put("opName", operatorName);
                jsObj.put("IP", SUtils.intToIPStr(externlIP));

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
            return "NetworkInfo";
        }

    }



}