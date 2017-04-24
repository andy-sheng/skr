package com.mi.liveassistant.milink.command;

public class MiLinkCommand {
    /*金山云鉴权*/
    public static final String COMMAND_ZHIBO_KS3AUTH_REQUEST = "zhibo.mfas.auth";
    public static final String COMMAND_ZHIBO_MULTIPART_AUTH = "zhibo.mfas.multipartauth";

    /*直播相关*/
    public static final String COMMAND_LIVE_BEGIN = "zhibo.live.begin";                     //创建一个直播
    public static final String COMMAND_LIVE_END = "zhibo.live.end";                         //结束一个直播
    public static final String COMMAND_LIVE_ENTER = "zhibo.live.enter";
    public static final String COMMAND_LIVE_LEAVE = "zhibo.live.leave";

    public static final String COMMAND_LIVE_HB = "zhibo.live.hb";                           //直播时的心跳
}