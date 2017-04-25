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
    /*登录相关*/
    public static final String COMMAND_LOGIN = "zhibo.account.login";
    public static final String COMMAND_GET_SERVICE_TOKEN = "zhibo.account.getservicetoken";
    public static final String COMMAND_ACCOUNT_XIAOMI_SSO_LOGIN = "zhibo.account.missologin";   //小米帐号sso登录
    public static final String COMMAND_ACCOUNT_GET_ACCESS_TOKEN = "zhibo.account.getaccesstoken"; //用于第三方（如小米游戏）接入小米直播
    public static final String COMMAND_ACCOUNT_3PARTSIGNLOGIN="zhibo.account.3partsignlogin"; //对接第三方账号签名登陆，比如对接真真海淘，直播客户端用户进入直播房间要打通用户对输入参数进行签名
    public static final String COMMAND_GET_OWN_INFO = "zhibo.user.getowninfo";      //获取自己的信息
}