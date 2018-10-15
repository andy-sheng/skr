package com.common.milink.event;

/**
 * Created by chengsimin on 16/7/1.
 */
public class MiLinkEvent {
    private MiLinkEvent() {
    }

    public static class AccountKick {
        public int type;
        public long ts;
        public String info;

        public AccountKick(int type, long ts, String info) {
            this.type = type;
            this.ts = ts;
            this.info = info;
        }
    }

    public static class AccountWantGetToken {
    }

    public static class AccountTokenExpired {
    }

    public static class StatusConnected {
    }

    public static class StatusDisConnected {
    }

    public static class StatusLogined {
    }

    public static class StatusNotLogin {
    }

    public static class RequestUploadLog {
    }

    public static class TouristLoginEvent {
    }
}
