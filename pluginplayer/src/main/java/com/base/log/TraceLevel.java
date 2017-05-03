package com.base.log;

/**
 * Created by chenyong on 2017/3/7.
 */

public interface TraceLevel {
    int VERBOSE = 1;
    int DEBUG = 2;
    int INFO = 4;
    int WARN = 8;
    int ERROR = 16;
    int ASSERT = 32;
    int ABOVE_VERBOSE = 62;
    int DEBUG_AND_ABOVE = 62;
    int ABOVE_DEBUG = 60;
    int INFO_AND_ABOVE = 62;
    int ABOVE_INFO = 56;
    int WARN_AND_ABOVE = 56;
    int ABOVE_WARN = 48;
    int ALL = 63;
}
