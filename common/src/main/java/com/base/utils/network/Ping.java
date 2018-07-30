package com.base.utils.network;

import android.os.Handler;
import android.os.Message;

import com.base.log.MyLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anping on 16-7-1.
 */
public class Ping {
    public final static String TAG = "Ping";

    private static final String PING_TEMPLATE = "ping -w %d -i 0.2 -c %d %s";

    public static final int PING_TIME_OUT = 5 * 1000;

    private static final int PING_TRY_TIMES = 5;

    static Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof BufferedReader) {
                BufferedReader bufferedReader = (BufferedReader) msg.obj;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    /**
     * 　ping 地址，并且返回 回包 平均值
     *
     * @param address
     * @return
     */
    public static double doPing(String address) {
//        address = "221.206.205.62";
        List<Double> resultContainer = new ArrayList<>(3);
        BufferedReader reader = null;
        Process process = null;
        try {
            String pingCmd = String.format(PING_TEMPLATE, PING_TIME_OUT, PING_TRY_TIMES, address);
            MyLog.d(TAG, "doPing" + " address=" + address + " cmd:" + pingCmd);

            process = Runtime.getRuntime().exec(pingCmd);
            MyLog.d(TAG, "doPing 1");
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));

            int flag = (int) System.currentTimeMillis();
            Message msg = Message.obtain();
            msg.obj = reader;
            msg.what =  flag;
            mHandler.sendMessageDelayed(msg,5*1000);

            MyLog.d(TAG, "doPing 2");

            reader.close();
            String line = reader.readLine();
            MyLog.d(TAG, "doPing 3");
            while (line != null) {
                MyLog.d(TAG, "doPing 4");
                if (line.contains("time=")) {
                    String[] lineSplit = line.split("time=");
                    if (lineSplit != null && lineSplit.length > 1 && lineSplit[1].contains("ms")) {
                        String[] timeResult = lineSplit[1].split("ms");
                        resultContainer.add(Double.parseDouble(timeResult[0].trim()));
                    }
                }
                line = reader.readLine();
            }
            mHandler.removeMessages(flag);
            MyLog.d(TAG, "doPing" + " address=" + address + " waitFor begin");
            process.waitFor();
            MyLog.d(TAG, "doPing" + " address=" + address + " waitFor end");
        } catch (IOException e) {
        } catch (Exception e) {
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // ignore;
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (resultContainer.size() == 0) {
            MyLog.d(TAG, "resultContainer.size() == 0");
            return Double.MAX_VALUE;
        }
        double result = 0;
        for (double item : resultContainer) {
            result = result + item;
        }
        double r = result / (double) resultContainer.size();
        MyLog.d(TAG, "doPing return" + " address=" + address + " r:" + r);
        return r;
    }
}
