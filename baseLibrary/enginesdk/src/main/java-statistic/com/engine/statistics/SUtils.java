package com.engine.statistics;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.engine.statistics.datastruct.Skr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SUtils
{

    public static final int NETWORK_NONE = 0; // 没有网络连接
    public static final int NETWORK_WIFI = 1; // wifi连接
    public static final int NETWORK_2G = 2; // 2G
    public static final int NETWORK_3G = 3; // 3G
    public static final int NETWORK_4G = 4; // 4G
    public static final int NETWORK_MOBILE_UNKNOW = 5; // 其他未知类型



    private static SimpleDateFormat sFmt = null;
    private static Date sDate = null;





    private SUtils(){

    }

    public static String transTime(long ms) {
        if (null == sFmt) {
            sFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        if (null == sDate) {
            sDate = new Date();
        }

        sDate.setTime(ms);
        String dataMSStr = sFmt.format(sDate);
        return dataMSStr;
    }

    public static String trans2NetworkTypeStr(int type) {
        String retStr = "";
        switch (type) {
            case NETWORK_NONE:
                retStr = "没有网络链接";
                break;
            case NETWORK_WIFI:
                retStr = "Wifi";
                break;
            case NETWORK_2G:
                retStr = "2G";
                break;
            case NETWORK_3G:
                retStr = "3G";
                break;
            case NETWORK_4G:
                retStr = "4G";
                break;
            case NETWORK_MOBILE_UNKNOW:
                retStr = "未知网络类型";
                break;
            default :
                retStr = "Unknow-Type network("+type+")";
                break;
        }
        return retStr;
    }


    public static Skr.NetworkInfo getNetworkInfo(Context ctx) {
        Skr.NetworkInfo nwInfo = new Skr.NetworkInfo();

        nwInfo.networkType = getNetworkType(ctx);
        String opName = getOperatorName(ctx);
        if (null == opName || 0 == opName.length())
            nwInfo.operatorName = "无移动网络";
        else
            nwInfo.operatorName = opName;

        getExternalIPAndLocation(nwInfo);

        return nwInfo;
    }



    public static int getNetworkType(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取网络服务
        if (null == connManager) { // 为空则认为无网络
            return NETWORK_NONE;
        }
        // 获取网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        }
        // 判断是否为WIFI
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_WIFI;
                }
            }
        }
        // 若不是WIFI，则去判断是2G、3G、4G网
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            /*
             GPRS : 2G(2.5) General Packet Radia Service 114kbps
             EDGE : 2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
             UMTS : 3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
             CDMA : 2G 电信 Code Division Multiple Access 码分多址
             EVDO_0 : 3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
             EVDO_A : 3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
             1xRTT : 2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
             HSDPA : 3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
             HSUPA : 3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
             HSPA : 3G (分HSDPA,HSUPA) High Speed Packet Access
             IDEN : 2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
             EVDO_B : 3G EV-DO Rev.B 14.7Mbps 下行 3.5G
             LTE : 4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
             EHRPD : 3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
             HSPAP : 3G HSPAP 比 HSDPA 快些
             */
            // 2G网络
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_2G;
            // 3G网络
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_3G;
            // 4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_4G;
            default:
                return NETWORK_MOBILE_UNKNOW;
        }
    }

    public static String getOperatorName(Context context) {
        /*
         * getSimOperatorName()就可以直接获取到运营商的名字
         * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
         * IMSI相关链接：http://baike.baidu.com/item/imsi
         */
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // getSimOperatorName就可以直接获取到运营商的名字
        return telephonyManager.getSimOperatorName();
    }



    public static void getExternalIPAndLocation(Skr.NetworkInfo nwInfo) {

        if (null == nwInfo) return;

        String line = "";
        URL infoUrl = null;
        InputStream inStream = null;

        nwInfo.externlIP = 0;
        nwInfo.geoLocation = null;

        try {
            String url = "https://pv.sohu.com/cityjson?ie=utf-8";
//                    String url="https://www.baidu.com/";
            infoUrl = new URL(url);
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                // 从反馈的结果中提取出IP地址
                int start = strber.indexOf("{");
                int end = strber.indexOf("}");
                String json = strber.substring(start, end + 1);

                if (json != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String ipStr = jsonObject.optString("cip");
                        String geoLocation = jsonObject.optString("cname");

                        nwInfo.externlIP = ipStrToInt(ipStr);
                        nwInfo.geoLocation = geoLocation;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
//                Message msg = new Message();
//                msg.what = MSG_GET_NET_IP;
//                msg.obj = ngInfo;
//                //向主线程发送消息
//                handler.sendMessage(msg);

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }


    public static String intToIPStr(int i) {

//        int ip = (-1) << 24 | (-1) << 16 | (-1) << 8 | (-1) ;
//        i = ip;

        return  ((i >> 24) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                (i & 0xFF);
    }
    public static int ipStrToInt(String ipStr)
    {
        String[]ip=ipStr.split("\\.");

        int a = (Integer.parseInt(ip[0]) << 24) +
                (Integer.parseInt(ip[1]) << 16) +
                (Integer.parseInt(ip[2]) << 8) +
                (Integer.parseInt(ip[3]));
        return a;
    }

}