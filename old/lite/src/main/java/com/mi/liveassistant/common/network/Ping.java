package com.mi.liveassistant.common.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anping on 16-7-1.
 */
public class Ping {
    private static final String PING_TEMPLATE = "ping -w %d -i 0.2 -c %d %s";

    public static final int PING_TIME_OUT = 5 * 1000;

    private static final int PING_TRY_TIMES = 5;

    /**
     * ping 地址，并且返回 回包 平均值
     */
    public static double doPing(String address) {

        List<Double> resultContainer = new ArrayList<>(3);
        BufferedReader reader = null;
        Process process = null;
        try {
            String pingCmd = String.format(PING_TEMPLATE, PING_TIME_OUT, PING_TRY_TIMES, address);
            process = Runtime.getRuntime().exec(pingCmd);
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("time=")) {
                    String[] lineSplit = line.split("time=");
                    if (lineSplit != null && lineSplit.length > 1 && lineSplit[1].contains("ms")) {
                        String[] timeResult = lineSplit[1].split("ms");
                        resultContainer.add(Double.parseDouble(timeResult[0].trim()));
                    }
                }
                line = reader.readLine();
            }
            process.waitFor();
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
            return Double.MAX_VALUE;
        }
        double result = 0;
        for (double item : resultContainer) {
            result = result + item;
        }
        return result / (double) resultContainer.size();
    }
}
