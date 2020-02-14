package com.module.playways;

import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.utils.U;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

// TODO: 该实现在https模式下对时误差增大
public class TimeSync {
    private static final String TAG = "TimeSync";

    private static final String REQUEST_URL = "http://dev.game.inframe.mobi/v1/relaygame/timestamp";

    public static final long INVALID_TS = Long.MIN_VALUE;

    public static long getShiftTs(int roomID) {
        HttpURLConnection connection = null;
        try {
            URL url = genURL(roomID);
            MyLog.d(TAG, "url: " + url);

            long ts0 = System.currentTimeMillis();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(500);
            connection.connect();
            MyLog.d(TAG, "connect success");

            long ts1 = System.currentTimeMillis();
            int code = connection.getResponseCode();
            long ts2 = System.currentTimeMillis();
            MyLog.d(TAG, "getResponseCode: " + code);

            if (code != 200) {
                MyLog.e(TAG, "GET " + url + " failed with code: " + code);
                return INVALID_TS;
            }

            InputStream responseStream = connection.getInputStream();
            String response = drainStream(responseStream);
            MyLog.d(TAG, "response: " + response);
            responseStream.close();

            long serverTs = parseTimestamp(response);
            long shiftTs = (ts1 + ts2) / 2 - serverTs;
            MyLog.i(TAG, "getShiftTs success, shiftTs = " + shiftTs +
                    " connect time: " + (ts1 - ts0) + " response time: " + (ts2 - ts1));
            return shiftTs;
        } catch (Exception e) {
            MyLog.e(TAG, "sync time with server failed!");
            e.printStackTrace();
            return INVALID_TS;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static URL genURL(int roomID) throws MalformedURLException {
        URL url = new URL(REQUEST_URL);
        String protocol = U.getChannelUtils().isStaging() ? "http" : "https";
        String host = url.getHost();
        host = ApiManager.getInstance().findRealHostByChannel(host);
        String file = url.getFile() + "?roomID=" + roomID;
        return new URL(protocol, host, file);
    }

    // Return the contents of an InputStream as a String.
    private static String drainStream(InputStream in) {
        Scanner s = new Scanner(in).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static long parseTimestamp(String response) throws JSONException {
        JSONObject responseJson = new JSONObject(response);
        JSONObject dataJson = responseJson.getJSONObject("data");
        return dataJson.getLong("timestamp");
    }
}
