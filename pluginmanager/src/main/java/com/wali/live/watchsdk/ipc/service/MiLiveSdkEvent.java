package com.wali.live.watchsdk.ipc.service;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 2016/12/27.
 */
public class MiLiveSdkEvent {
    public static int SUCCESS = 0;
    public static int FAILED = 1;

    public static class LoginResult {
        public int code;

        private LoginResult(int code) {
            this.code = code;
        }
    }

    public static void postLogin(int code) {
        EventBus.getDefault().post(new LoginResult(code));
    }

    public static class LogoffResult {
        public int code;

        private LogoffResult(int code) {
            this.code = code;
        }
    }

    public static void postLogoff(int code) {
        EventBus.getDefault().post(new LogoffResult(code));
    }

    public static class WantLogin {
        private WantLogin() {
        }
    }

    public static void postWantLogin() {
        EventBus.getDefault().post(new WantLogin());
    }

    public static class VerifyFailure {
        public int code;

        private VerifyFailure(int code) {
            this.code = code;
        }
    }

    public static void postVerifyFailure(int code) {
        EventBus.getDefault().post(new VerifyFailure(code));
    }
}
