package com.common.core.channel;

public class HostChannelManager {
    private static class HostChannelManagerHolder {
        private static final HostChannelManager INSTANCE = new HostChannelManager();
    }

    private HostChannelManager() {

    }

    public static final HostChannelManager getInstance() {
        return HostChannelManagerHolder.INSTANCE;
    }

    public int getChannelId() {
        return 50019;
    }

}
