package com.wali.live.watchsdk.ipc.service;

/**
 * Created by chengsimin on 2016/12/27.
 */

public class MiLiveSdkEvent {
    public static int SUCCESS = 0;
    public static int FAILED = 1;

    public static class LoginResult {
        public int code;

        public LoginResult(int code) {
            this.code = code;
        }
    }

    public static class LogoffResult {
        public int code;

        public LogoffResult(int code) {
            this.code = code;
        }
    }
}
