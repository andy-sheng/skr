package com.mi.live.data.milink.event;

import com.mi.live.data.event.BaseEventClass;

/**
 * Created by chengsimin on 16/7/1.
 */
public class MiLinkEvent {
    private MiLinkEvent() {
    }

    public static class Account extends BaseEventClass {
        public static final int KICK = 1;
        public static final int GET_SERVICE_TOKEN = 2;
        public static final int SERVICE_TOKEN_EXPIRED = 3;

        public Account(int op, Object obj1, Object obj2) {
            super(op, obj1, obj2);
        }
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

}
