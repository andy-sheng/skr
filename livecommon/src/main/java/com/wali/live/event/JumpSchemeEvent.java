package com.wali.live.event;

import com.wali.live.michannel.ChannelParam;

import java.util.List;

/**
 * scheme跳转
 */
public class JumpSchemeEvent {
    public String uri;
    public List list;
    public ChannelParam channelParam;
    public String from;

    public JumpSchemeEvent(String uri) {
        this.uri = uri;
    }

    public JumpSchemeEvent(String uri, ChannelParam channelParam) {
        this.uri = uri;
        this.channelParam = channelParam;
    }

    public JumpSchemeEvent(String uri, List list, ChannelParam channelParam) {
        this.uri = uri;
        this.list = list;
        this.channelParam = channelParam;
    }

}