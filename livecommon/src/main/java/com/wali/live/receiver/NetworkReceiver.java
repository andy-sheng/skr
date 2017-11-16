package com.wali.live.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.base.log.MyLog;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.dns.PreDnsManager;
import com.wali.live.event.EventClass;
import com.wali.live.network.ImageUrlDNSManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 16/3/7.
 *
 * @module 检测系统网络状态的变化
 */
public class NetworkReceiver extends BroadcastReceiver {
    private final static String TAG = NetworkReceiver.class.getSimpleName();

    /**
     * 枚举网络状态
     * NET_NO：没有网络 ,  NET_2G:2g网络 , NET_3G：3g网络 ,NET_4G：4g网络 ,NET_WIFI：wifi , NET_UNKNOWN：未知网络
     */
    public enum NetState {
        NET_NO, NET_2G, NET_3G, NET_4G, NET_WIFI, NET_UNKNOWN
    }

    private static long mLastClearTimestamp;
    private static NetState mLastNetState;

    public static String getNetworkID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager == null ? null : wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            return String.valueOf(wifiInfo.getBSSID());
        }
        return null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.w("NetworkReceiver", "network changed, NetworkReceiver action=" + intent.getAction());
        NetState netState = getCurrentNetStateCode(context);
        if (null != netState) {
            if (netState == NetState.NET_2G
                    || netState == NetState.NET_3G
                    || netState == NetState.NET_4G
                    || netState == NetState.NET_WIFI) {
                if (mLastNetState != netState && System.currentTimeMillis() - mLastClearTimestamp > 5 * 1000) {
                    mLastClearTimestamp = System.currentTimeMillis();
                    ImageUrlDNSManager.reFetchIpByNetWorkChange();
                }
            }
            MyLog.w(TAG, netState + "");
            String networkId = netState == NetState.NET_WIFI ? getNetworkID(context) : null;
            EventClass.NetWorkChangeEvent event = new EventClass.NetWorkChangeEvent(netState, networkId);
            if (netState != NetState.NET_NO) {
                PreDnsManager.INSTANCE.onNetworkConnected(event.getNetworkId());
                GiftRepository.onChangeNetState();
            }
            EventBus.getDefault().post(event);
        }
        mLastNetState = netState;
    }


    public static NetState getCurrentNetStateCode(Context context) {
        NetState stateCode = NetState.NET_NO;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            switch (ni.getType()) {
                //wifi
                case ConnectivityManager.TYPE_WIFI:
                    stateCode = NetState.NET_WIFI;
                    break;
                //mobile 网络
                case ConnectivityManager.TYPE_MOBILE:
                    switch (ni.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: //联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: //电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: //移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            stateCode = NetState.NET_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: //电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            stateCode = NetState.NET_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE://4g
                        case 19: //4g+
                            stateCode = NetState.NET_4G;
                            break;
                        //未知,一般不会出现
                        default:
                            stateCode = NetState.NET_UNKNOWN;
                    }
                    break;
                default:
                    stateCode = NetState.NET_UNKNOWN;
            }
        }

        return stateCode;
    }

    public static NetworkReceiver registerReceiver(Activity activity) {
        NetworkReceiver networkReceiver = new NetworkReceiver();
        activity.registerReceiver(networkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        return networkReceiver;
    }

    public static void unRegisterReceiver(Activity activity, NetworkReceiver networkReceiver) {
        if (activity != null && networkReceiver != null) {
            activity.unregisterReceiver(networkReceiver);
        }
    }
}
