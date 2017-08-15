package com.mi.live.data.api;

/**
 * Created by lan on 15-12-1.<br/>
 * For latest, visit <a>http://wiki.n.miui.com/pages/viewpage.action?pageId=18995829</a>
 */
public class ErrorCode {
    // 0 ： 任何业务返回0，都代表成功
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ERROR_NORMAL = -1;
    public static final int CODE_TIME_OUT = -2;
    public static final int CODE_EXCEPTION = -3;

    //直播 没有权限进行invite
    public static final int ZHIBO_DENIED_ERROR = 1191;
    //直播 对方版本过低
    public static final int ZHIBO_VERSION_ERROR = 1192;
    //直播 对方机型太老
    public static final int ZHIBO_DEVICE_ERROR = 1193;
    //直播  主叫方机型太老
    public static final int ZHIBO_CALLING_DEVICE_ERROR = 1194;

    //[5000-5500]房间模块
    public static final int CODE_ROOM_NOT_EXIST = 5001;          //房间不存在
    public static final int CODE_USER_IS_LIVING = 5002;         //用户正在直播
    public static final int CODE_USER_ERROR = 5003;             //用户信息错误，upstream中uuid和pb中uuid不等
    public static final int CODE_PARAM_ERROR = 5004;            //参数错误
    public static final int CODE_NO_PREVILEGE = 5005;           //操作人无权限
    public static final int CODE_VIEWER_NOT_EXIST = 5006;       //观众不存在
    public static final int CODE_ROOM_ZUID_SLEEP = 5007;        //主播退到后台
    public static final int CODE_ROOM_NOT_INVITE = 5008;        //私密房间没有权限进入
    public static final int CODE_ROOM_INVITE_CNT_OVER = 5009;   //私密房间邀请人数超过上限
    public static final int CODE_ISOLATE_ROOMID_ERROR = 5010; //隔离房间的roomid与kv此时的live.liveid()不等
    public static final int CODE_SERVER_ERROR = 5011; //服务异常
    public static final int CODE_DB_ERROR = 5012;
    public static final int CODE_USER_NOT_HISTORY = 5013; //此用户没有回放，此错误码不报警
    public static final int CODE_LIVE_BEGIN_NO_PERMISSION = 5014; //此用户被封禁，没有权限发直播
    public static final int CODE_LIVE_BEGIN_ILLEGAL_TITLE = 5016; //直播标题非法
    public static final int CODE_PB_ERROR = 5017; //pb解析失败
    public static final int CODE_ROOM_NOT_END = 5018;//房间还没有结束
    public static final int CODE_USER_IS_PKING = 5019;//此人正在pk
    public static final int CODE_OTHER_USER_IS_PKING = 5020;//对方正在pk
    public static final int CODE_IS_NOT_PKING = 5021; //不在pk
    public static final int CODE_PK_ROOM_NOT_EXIT = 5022; //pk的房间不存在
    public static final int CODE_IS_IN_ROOM_VIEWER_CNT_OVER = 5023; //查询是否在房间超过人数上限
    public static final int CODE_ROOM_PASSWORD_ERROR = 5024; //进入密码房间，验证密码失败
    public static final int CODE_ROOM_IS_MICING = 5025; //开始连麦失败，房间已经在连麦
    public static final int CODE_ROOM_MICUID_NOT_EXIT = 5026; //连麦嘉宾不存在
    public static final int CODE_ZUID_CERTIFY_ERROR = 5028;     //主播认证信息不对（失败或者没认证过），不允许开播
    public static final int CODE_ZUID_NOT_ADULT = 5029; //主播未满18岁，不允许开播
    public static final int CODE_ZUID_CERTIFY_GOING = 5030; //主播实名认证审核中，不允许开播
    public static final int CODE_ZUID_APPINFO_ERROR = 5031; //appinfo 错误
    public static final int CODE_HISTORY_IS_NOT_EXIST = 5036;//回放不存在
    public static final int CODE_PASSWORD_ERROR_OVER_LIMIT = 5037;//直播间密码重试次数超过上限
    public static final int CODE_NOT_SECRET_CAN_NOT_INVITE = 5038; //非私密房间不能添加和取消邀请人
    public static final int CODE_NOT_MEET_BEGIN_LIVE_LEVEL = 5039; //没有达到开播需要的最低用户等级
    public static final int CODE_NO_PERMISSION_BEGIN_TICKET = 5040; //没有权限创建门票直播;相关命令字：zhibo.live.begin
    public static final int CODE_SET_ROMM_TICKET_ERROR = 5041;//创建房间时，设置直播间的门票失败;相关命令字：zhibo.live.begin
    public static final int CODE_QUERY_BUY_TICKET_ERROR = 5042; //进入门票直播间时，查询是否购票信息失败，客户端需重试（这种情况会告诉客户端没买票）
    //相关命令字：zhibo.live.enter ，zhibo.live.roominfo， hibo.live.hisroom ， zhibo.live.historyroominfo


    //[6000-6100]提现模块;

    public static final int CODE_WITHDRAW_ERROR_NULL = 6000;
    public static final int CODE_ACCOUT_FORBIDDEN = 6021;
    public static final int CODE_PAYPAL_HAS_BEEN_BINDED = 11402;
    public static final int CODE_ACCOUNT_HAS_BEEN_BINDED_PAYPAL = 11079;

    //扫码登录
    public static final int CODE_ACCOUT_CONNECTD = 6027;

    //6200-6500 下载模块
    public static final int CODE_DOWNLOAD_ERROR_TYPE_ERROR = 6200;
    public static final int CODE_DOWNLOAD_ERROR_AUDIO_ERROR = 6201;
    public static final int CODE_DOWNLOAD_ERROR_LRC_ERROR = 6202;

    public static final int CODE_PAY_TICKET_ALREADY_USED = 11054;//票据已使用
    //服务端下发失败错误吗
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE = 11080;
    public static final int CODE_SERVER_RESPONSE_ERROR_INFO_NOT_CORRENT = 11082;

    public static final int CODE_SERVER_RESPONSE_ERROR_NOT_RECOMMIT = 11001;
    public static final int CODE_SERVER_RESPONSE_ERROR_USER_HAS_NOT_ENOUGH = 11032;
    public static final int CODE_SERVER_RESPONSE_ERROR_USER_FOBITTON_FOR_COMMUTE = 11070;
    public static final int CODE_SERVER_RESPONSE_ERROR_USER_NOT_ENOUGH = 11087;
    public static final int CODE_SERVER_RESPONSE_ERROR_USER_NOT_BIND_ALI = 11088;
    public static final int CODE_SERVER_RESPONSE_ERROR_USER_ITEM_HANDLER = 11089;
    public static final int CODE_SERVER_RESPONSE_ERROR_MONEY_NOT_ENOUGH = 11090;
    public static final int CODE_SERVER_RESPONSE_ERROR_MORE_THAN_MAX_MONEY_DAY = 11091;
    public static final int CODE_SERVER_RESPONSE_ERROR_WITHDRAW_ERROR = 11092;
    public static final int CODE_SERVER_RESPONSE_ERROR_MORE_THAN_MAX_ONE = 11098;
    public static final int CODE_SERVER_RESPONSE_ERROR_MORE_THAN_MAX_COUNT = 11099;

    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_REBIND_ERROR = 11078;

    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_PHONE_NUM_WRONG = 6019;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_CAPTCHA_ERROR = 6013;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_REGISTED = 6012;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_NOT_REGIST = 6017;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_PWD_ERROR = 6018;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_CHANGE_PWD_ERROR = 6016;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_INVALID_TOKEN = 6020;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_BLOCKED_ACCOUNT = 6021;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_ID_NOT_MATCH = 6023;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_NOT_SETTING_PWD = 6024;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_INVALID_ACCOUNT = 6026;
    public static final int ERR_HAS_ANTISPAM_ERROR = 7021;
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_NO_PERMISSION_TO_ENTER_ROOM = 5033;         // 没有权限进入房间
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_NOT_HAVE_PERMISSION_TO_KICK = 5034;         // 没有权限发起踢人操作
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_NOT_HAVE_PERMISSION_TO_KICK_VIEWER = 5035;  // 没有权限踢掉该观众
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_IDCARD_INVALID_ERROR = 7013;                // 身份证号或护照号为空或不合法
    public static final int CODE_SERVER_RESPONSE_ERROR_CODE_REALNAME_VERIFY_FAIL = 7016;                // 姓名和身份证号检验失败

    public static final int CODE_NAME_SENSITIVE = 7021;       // 包含敏感词

    /* 关系错误码 */
    public static final int CODE_RELATION_YOURSELF = 7505;
    public static final int CODE_RELATION_BLACK = 7506;     // 黑名单
}
