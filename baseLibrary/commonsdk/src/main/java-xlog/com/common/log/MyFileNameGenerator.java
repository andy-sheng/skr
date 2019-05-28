package com.common.log;

import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;

/**
 * Created by linjinbin on 2018/3/20.
 */

public class MyFileNameGenerator extends DateFileNameGenerator {

    /**
     * Generate a file name which represent a specific date.
     */
    @Override
    public String generateFileName(int logLevel, long timestamp) {
        return super.generateFileName(logLevel, timestamp) + ".log";
    }
}
