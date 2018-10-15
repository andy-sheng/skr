package com.wali.live.utils;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.network.DnsPodUtils;
import com.base.utils.network.Ping;
import com.wali.live.receiver.NetworkReceiver;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by chengsimin on 16/9/23.
 */
public class AppNetworkUtils {
    private static final String TAG = "AppNetworkUtils";

    /**
     * 同步
     */
    public static List<String> hourseTraceSync(final List<String> result) {

        if (result == null || result.size() <= 1) {
            return result;
        }

        List<FutureTask> futureTaskList = new ArrayList<FutureTask>(result.size());
        for (final String item : result) {
            if (!TextUtils.isEmpty(item)) {
                FutureTask<Map<String, Double>> task = new FutureTask<Map<String, Double>>(new Callable<Map<String, Double>>() {
                    @Override
                    public Map<String, Double> call() throws Exception {
                        MyLog.d(TAG,"hourseTraceSync "+hashCode()+" begin, ip:"+item );

                        double time = Ping.doPing(item);
                        HashMap<String, Double> result = new HashMap<String, Double>();
                        result.put(item, time);

                        MyLog.d(TAG,"hourseTraceSync "+hashCode()+" over" );
                        return result;
                    }
                });
                futureTaskList.add(task);
                new Thread(task, "horseRunner").start();
            }
        }

        Map<String, Double> resultMap = new HashMap<>();

        for (FutureTask taskItem : futureTaskList) {
            try {
                Map<String, Double> resultItem = (Map<String, Double>) taskItem.get(Ping.PING_TIME_OUT * 3, TimeUnit.MILLISECONDS);
                if (resultItem != null) {
                    Set<Map.Entry<String, Double>> entrySet = resultItem.entrySet();
                    for (Map.Entry<String, Double> item : entrySet) {
                        resultMap.put(item.getKey(), item.getValue());
                    }
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }


        List<Map.Entry<String, Double>> resultAsList = new ArrayList<Map.Entry<String, Double>>(resultMap.entrySet());

        Collections.sort(resultAsList, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> lhs, Map.Entry<String, Double> rhs) {
                if (lhs.getValue() > rhs.getValue()) {
                    return 1;
                } else if (lhs.getValue() < rhs.getValue()) {
                    return -1;
                }
                return 0;
            }
        });
        MyLog.w("NetWorkHorse resultList" + resultAsList);

        List<String> sortResult = new ArrayList<String>();

        for (Map.Entry<String, Double> entry : resultAsList) {
            sortResult.add(entry.getKey());
        }

        MyLog.w("NetWorkHorseList" + sortResult);
        if (sortResult.size() != result.size()) {
            return result; //保证数据不丢失
        }
        return sortResult;
    }


    public static List<String> getAddressByHost(String host, boolean forceUseDnsPod) {
        List<String> address = new ArrayList<>();
        if (!TextUtils.isEmpty(host)) {
            InetAddress[] addr = null;
            try {
                addr = InetAddress.getAllByName(host);
                if (addr != null && addr.length > 0) {
                    String localDnsStr = "";
                    for (InetAddress item : addr) {
                        address.add(item.getHostAddress());
                        localDnsStr += localDnsStr.isEmpty() ? item.getHostAddress() : ";" + item.getHostAddress();
                    }
                    MyLog.w("LocalDns", "domain=" + host + ",ipsList=" + localDnsStr);
                }
            } catch (Exception e) {
                MyLog.e("getAddressByHost error = " + e);
            }

            //DnsPod
            if (address.size() == 0 || forceUseDnsPod) {
                String dnsPodStr = DnsPodUtils.getAddressByHostDnsPod(host);
                if (!TextUtils.isEmpty(dnsPodStr)) {
                    processDnsPodResult(address, dnsPodStr, host);
                } else {
                    // chenyong1 失败的话重试一次
                    MyLog.w("HttpDns", "dnspod failed retry");
                    dnsPodStr = DnsPodUtils.getAddressByHostDnsPod(host);
                    if (!TextUtils.isEmpty(dnsPodStr)) {
                        processDnsPodResult(address, dnsPodStr, host);
                    } else {
                        MyLog.w("HttpDns", "dnspod failed again");
                    }
                }
            }
        }
        return address;
    }

    private static void processDnsPodResult(List<String> address, String dnsPodStr, String host) {
        dnsPodStr = dnsPodStr.split(",")[0];
        String[] dnsPodStrs = dnsPodStr.split(";");
        MyLog.w("HttpDns", "domain=" + host + ",ipsList=" + dnsPodStr);
        for (int i = dnsPodStrs.length - 1; i >= 0; i--) {
            String ipStr = dnsPodStrs[i];
            if (!address.contains(ipStr)) {
                address.add(0, ipStr);
            }
        }
    }

    public static List<String> getAddressByHost(String host) {
        List<String> address = new ArrayList<>();
        if (!TextUtils.isEmpty(host)) {

            //DnsPod
            String dnsPodStr = DnsPodUtils.getAddressByHostDnsPod(host);
            if (!TextUtils.isEmpty(dnsPodStr)) {
                dnsPodStr = dnsPodStr.split(",")[0];
                String[] dnsPodStrs = dnsPodStr.split(";");
                MyLog.w("HttpDns", "domain=" + host + ",ipsList=" + dnsPodStr);
                for (int i = dnsPodStrs.length - 1; i >= 0; i--) {
                    String ipStr = dnsPodStrs[i];
                    if (!address.contains(ipStr)) {
                        address.add(0, ipStr);
                    }
                }
            }

            InetAddress[] addr = null;
            try {
                addr = InetAddress.getAllByName(host);
                if (addr != null && addr.length > 0) {
                    for (InetAddress item : addr) {
                        String hostAddress = item.getHostAddress();
                        if (!address.contains(hostAddress)) {
                            address.add(hostAddress);
                        }
                    }
                }
            } catch (Exception e) {
                MyLog.e("getAddressByHost error = " + e);
            }

        }
        return address;
    }

    public static boolean is4g() {
        NetworkReceiver.NetState netCode = NetworkReceiver.getCurrentNetStateCode(GlobalData.app());
        if (netCode != NetworkReceiver.NetState.NET_NO) {
            MyLog.w(TAG, "onNetStateChanged netCode = " + netCode);
            if (netCode == NetworkReceiver.NetState.NET_2G ||
                    netCode == NetworkReceiver.NetState.NET_3G ||
                    netCode == NetworkReceiver.NetState.NET_4G) {
                return true;
            }
        }
        return false;
    }
}
