package com.wali.live.utils.jvm;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by chengsimin on 16/7/26.
 */
public class VMArguUtils {
    private static long sLastGetTime = 0;
    private static float sLastCpuUsage = 0;

    private static float readCpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            sLastCpuUsage = (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
            sLastGetTime = System.currentTimeMillis();
            return sLastCpuUsage;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    /*耗时行为*/
    public static float getCpuUsage(boolean force) {
        if (force) {
            return readCpuUsage();
        } else {
            if (System.currentTimeMillis() - sLastGetTime > 5000) {
                return readCpuUsage();
            } else {
                return sLastCpuUsage;
            }
        }
    }
}
