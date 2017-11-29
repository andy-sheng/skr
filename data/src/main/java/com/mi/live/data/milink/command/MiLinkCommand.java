package com.mi.live.data.milink.command;

import com.mi.milink.sdk.data.Const;
import com.wali.live.proto.SignalProto;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by linjinbin on 15/11/2.
 */
public class MiLinkCommand {
    public static final String COMMAND_RECOMMEND_CHANNELLIST = "zhibo.recommend.channellist";
    public static final String COMMAND_LIST_FOLLOWLIVE = "zhibo.list.followlive";

    public static final String COMMAND_VOIP_AUTO_UPLOAD_LOG = "milink.push.uploadlog";
    /*金山云鉴权*/
    public static final String COMMAND_ZHIBO_KS3AUTH_REQUEST = "zhibo.mfas.auth";
    public static final String COMMAND_ZHIBO_MULTIPART_AUTH = "zhibo.mfas.multipartauth";

    /*验证助手合法性*/
    public static final String COMMAND_ACCOUNT_VERIFY_ASSISTANT = "zhibo.account.verifyassistant";

    /*直播相关*/
    public static final String COMMAND_LIVE_GET_ROOM_ID = "zhibo.live.getroomid";                  //创建一个直播
    public static final String COMMAND_LIVE_BEGIN = "zhibo.live.begin";                     //创建一个直播
    public static final String COMMAND_LIVE_END = "zhibo.live.end";                         //结束一个直播
    public static final String COMMAND_LIVE_ENTER = "zhibo.live.enter";
    public static final String COMMAND_LIVE_LEAVE = "zhibo.live.leave";
    public static final String COMMAND_LIVE_BEGININIT = "zhibo.live.begininit";             //查询是否有开启门票直播权限
    public static final String COMMAND_LIVE_ROOM_INFO = "zhibo.live.roominfo";              //查询房间状态
    public static final String COMMAND_LIVE_HISTORY_ROOM_INFO = "zhibo.live.historyroominfo";              //历史房间信息查询、认证
    public static final String COMMAND_LIVE_VIEWER_TOP = "zhibo.live.viewertop";
    public static final String COMMAND_LIVE_HB = "zhibo.live.hb";                           //直播时的心跳
    public static final String COMMAND_LIVE_ZUID_SLEEP = "zhibo.live.zuidSleep";            //主播退到后台运行
    public static final String COMMAND_LIVE_ZUID_ACTIVE = "zhibo.live.zuidActive";          //主播回来
    public static final String COMMAND_LIVE_HISTORY_DELETE = "zhibo.live.historydelete";    //删除回放直播
    public static final String COMMAND_LIVE_VIEWERINFO = "zhibo.live.viewerinfo";           //拉取房间的观众信息
    public static final String COMMAND_LIVE_MYROOM = "zhibo.live.myroom";                   //查询我的房间信息
    public static final String COMMAND_LIVE_ISINROOM = "zhibo.live.isInRoom";                   //查询我的房间信息
    public static final String COMMAND_LIVE_ROOM_INFO_CHANGE = "zhibo.live.roominfochange"; //房间信息更新
    public static final String COMMAND_LIVE_ROOM_TAG = "zhibo.live.getroomtag";             //拉房间标签列表

    public static final String COMMAND_LIVE_PKBEGIN = "zhibo.live.pkbegin";                   // pk开始
    public static final String COMMAND_LIVE_PKEND = "zhibo.live.pkend";                   // pk结束
    public static final String COMMAND_LIVE_PKGETINFO = "zhibo.live.getpkinfo";                   // 查询pk信息

    public static final String COMMAND_LIVE_MIC_BEGIN = "zhibo.live.micbegin";              // 连麦开始
    public static final String COMMAND_LIVE_MIC_END = "zhibo.live.micend";                  // 连麦结束
    /**
     * 私密房间添加邀请人
     */
    public static final String COMMAND_LIVE_ADD_INVITEE = "zhibo.live.addinvitee";
    /**
     * 私密房间删除邀请人
     */
    public static final String COMMAND_LIVE_DELETE_INVITEE = "zhibo.live.deleteinvitee";

    /*直播通知*/
    public static final String COMMAND_LIVE_NOTIFY = "zhibo.live.notify";

    /*直播跳转scheme*/
    public static final String COMMAND_PUSH_JUMP = "zhibo.push.jump";     //服务器跳转scheme命令字

    /*直播列表相关*/
    public static final String COMMAND_LIST_CONCERNS = "zhibo.list.concerns";       //拉取关注的好友直播和回放列表信息
    public static final String COMMAND_LIST_LIVE = "zhibo.list.live";               //拉取某个频道的直播列表信息
    public static final String COMMAND_LIST_TAGLIVE = "zhibo.list.taglive";         //拉取某个频道的直播列表信息,带标签
    public static final String COMMAND_LIST_CHANNEL = "zhibo.list.channel";         //拉取频道列表信息
    public static final String COMMAND_GET_SIGN_STATUS = "zhibo.signin.getsignin";//拉取首页签到按钮是否显示等信息
    public static final String COMMAND_LIST_HISTORY = "zhibo.live.history";         //历史直播信息
    public static final String COMMAND_LIST_DEL_HISTORY = "zhibo.live.historydelete";         //删除历史直播信息
    public static final String COMMAND_UPDATE_MSGRULE = "zhibo.live.updatemsgrule"; //更新房间内发言频率、是否重复

    /*礼物相关*/
    public static final String COMMAND_GIFT_GET_LIST = "zhibo.mall.getGiftList";
    public static final String COMMAND_GIFT_BUY = "zhibo.mall.buyGift";
    public static final String COMMAND_GIFT_BUY_GAME_ROOM = "zhibo.mall.buyGameGift";
    public static final String COMMAND_GIFT_CARD_PUSH = "zhibo.bank.giftCardNotify";
    public static final String COMMAND_MIBI_BALANCE = "zhibo.mall.mibibalance";
    /*抢红包*/
    public static final String COMMAND_GRAP_RED_ENVELOP = "zhibo.bank.grabenvelop";
    /*获取红包列表*/
    public static final String COMMAND_GET_RED_ENVELOP = "zhibo.bank.getenvelop";
    //拉取红包配置信息
    public static final String COMMAND_GET_RED_ENVELOP_SEETTING = "zhibo.bank.getenvelopsetting";
    //发红包
    public static final String COMMAND_CREATE_REDENVELOP = "zhibo.bank.createnvelop";

    //查询关注列表
    public static final String COMMAND_GET_FOLLOWING_LIST = "zhibo.relation.followinglist";
    //查询粉丝列表
    public static final String COMMAND_GET_FOLLOWER_LIST = "zhibo.relation.followerlist";
    //查询黑名单列表
    public static final String COMMAND_GET_BLOCK_LIST = "zhibo.relation.blockerlist";
    //查询可以PK的用户列表
    public static final String COMMAND_GET_PKUSER_LIST = "zhibo.voip.pkuserlist";
    //查询可以连麦的用户列表
    public static final String COMMAND_GET_MICUSER_LIST = "zhibo.voip.micuserlist";
    //关注推送设置
    public static final String COMMAND_SET_PUSH = "zhibo.relation.setpush";
    //关注
    public static final String COMMAND_FOLLOW_REQUEST = "zhibo.relation.follow";
    //取消
    public static final String COMMAND_UNFOLLOW_REQUEST = "zhibo.relation.unfollow";

    //订阅
    public static final String COMMAND_SUBSCRIBE_REQUEST = "zhibo.relation.subscribe";
    //取消订阅
    public static final String COMMAND_GET_SUBSCRIBE_INFO_REQUEST = "zhibo.relation.getsubscribeinfo";

    //拉黑
    public static final String COMMAND_BLOCK_REQUEST = "zhibo.relation.block";
    //取消拉黑
    public static final String COMMAND_UNBLOCK_REQUEST = "zhibo.relation.unblock";
    //获取房间推荐列表
    public static final String COMMAND_RECOMMEND_ROOM = "zhibo.recommend.inliveend";
    //获取商品推荐列表
    public static final String COMMAND_RECOMMEND_GOODS = "zhibo.shopping.select_goods";
    //获取主播商品列表
    public static final String COMMAND_GOODS = "zhibo.shopping.get_goods_list";
    //添加商品
    public static final String COMMAND_ADD_GOODS = "zhibo.shopping.add_goods";
    //添加商品
    //TODO
    public static final String COMMAND_SEARCH_GOODS = "zhibo.shopping.search_goods";

    public static final String COMMAND_ORDER_LIST = "zhibo.shopping.get_sales_orders";
    public static final String COMMAND_ANCHOR_SALES_INFO = "zhibo.shopping.get_sales_info";
    public static final String COMMAND_SHOPPING_ANCHOR_LIST = "zhibo.shopping.sales_list";
    public static final String COMMAND_LIVE_SHOP_ENTER_GOODS_PUSH = "zhibo.shopping.taobao_buying";
    public static final String COMMAND_BUY_TB_GOODS_MSG = "zhibo.shopping.gen_user_order";
    public static final String COMMAND_ENTER_GOODS_DETAIL_TAP = "zhibo.shopping.goods_tap_stat";
    //获取所有商品
    public static final String COMMAND_ALL_GOODS = "zhibo.shopping.all_goods";
    //得到pid
    public static final String COMMAND_ZU_GET_PID = "zhibo.shopping.get_pid";

    public static final String COMMAND_KICK_VIEWER = "zhibo.live.kickviewer";

    /*弹幕相关*/
    public static final String COMMAND_SEND_BARRAGE = "zhibo.send.roommsg";
    public static final String COMMAND_PUSH_BARRAGE = "zhibo.push.roommsg";
    public static final String COMMAND_SYNC_SYSMSG = "zhibo.sync.sysmsg";
    public static final String COMMAND_PUSH_SYSMSG = "zhibo.push.sysmsg";
    public static final String COMMAND_VFANS_PUSHMSG = "zhibo.push.vfansmsg";          //宠爱团的弹幕push
    public static final String COMMAND_REPLAY_BARRAGE = "zhibo.replay.roommsg";
    public static final String COMMAND_FEED_BARRAGE = "zhibo.feed.roommsg";           //拉取feeds 直播弹幕

    /*通知相关*/
    public static final String COMMAND_PUSH_NOTIFY = "zhibo.push.notifymsg";

    /*购买钻石*/
    public static final String COMMAND_PAY_PRECHARGELIST = "zhibo.bank.rechargeList";
    public static final String COMMAND_PAY_PRICE_LIST = "zhibo.bank.getGemPriceListV2";
    public static final String COMMAND_PAY_CREATE_ORDER = "zhibo.bank.createOrder";
    public static final String COMMAND_PAY_CHECK_ORDER = "zhibo.bank.checkOrder";
    public static final String COMMAND_PAY_GET_BALANCE_DETAIL = "zhibo.bank.getBalanceDetail";
    /*充值小红点*/
    public static final String COMMAND_PAY_GET_RED_ICON = "zhibo.redpoint.get";
    public static final String COMMAND_PAY_NOTIFY = "zhibo.bank.payNotify";

    /*私信相关*/
    public static final String COMMAND_SEND_CHAT_MSG = "zhibo.send.chatmsg";
    public static final String COMMAND_SEND_READ_MSG = "zhibo.send.readmsg";
    public static final String COMMAND_SYNC_CHAT_MSG = "zhibo.sync.chatmsg";
    public static final String COMMAND_SYNC_HISTORY = "zhibo.sync.history";
    public static final String COMMAND_PUSH_CHAT_MSG = "zhibo.push.chatmsg";
    public static final String COMMAND_PUSH_READ_MSG = "zhibo.push.readmsg";
    public static final String COMMAND_NOTIFY_CHAT_MSG = "zhibo.notify.chatmsg";

    /*用户信息相关*/
    public static final String COMMAND_GET_USER_INFO_BY_ID = "zhibo.user.getuserinfobyid";
    public static final String COMMAND_GET_USER_LIST_BY_ID = "zhibo.user.mutigetuserinfo";
    public static final String COMMAND_GET_OWN_INFO = "zhibo.user.getowninfo";      //获取自己的信息
    @Deprecated
    public static final String COMMAND_GET_PERRSONAL_DATA = "zhibo.user.getpersonaldata";
    public static final String COMMAND_GET_HOMEPAGE = "zhibo.user.gethomepage";
    public static final String COMMAND_UPLOAD_USER_SETTING = "zhibo.user.uploadusersetting";
    public static final String COMMAND_SEARCH = "zhibo.user.search";
    public static final String COMMAND_SEARCH_ALL = "zhibo.user.search_all";
    public static final String COMMAND_UPLOAD_USER_INFO = "zhibo.user.uploaduserpro";       //更新用户信息
    public static final String COMMAND_UPLOAD_OWN_SETTING = "zhibo.user.uploadusersetting";
    public static final String COMMAND_GET_OWN_SETTING = "zhibo.user.getownsetting";
    public static final String COMMAND_ADMIN_SETTING = "zhibo.user.adminsetting";
    public static final String COMMAND_GET_ADMIN_LIST = "zhibo.user.adminlist";
    public static final String COMMAND_GET_CAPTCHA = "zhibo.user.getcaptcha";
    public static final String COMMAND_VERIFY_CAPTCHA = "zhibo.user.verifycaptcha";
    public static final String COMMAND_APPLY_CERTIFICATION = "zhibo.user.applycertification";
    public static final String COMMAND_GET_COUNTRYCODE = "zhibo.user.getcountrycode";//获取国家编码
    public static final String COMMAND_GET_COUNTRYLIST = "zhibo.user.getcountrylist";

    /*登陆相关*/
    public static final String COMMAND_LOGIN = "zhibo.account.login";
    public static final String COMMAND_GET_SERVICE_TOKEN = "zhibo.account.getservicetoken";
    public static final String COMMAND_EXPLEVEL_UPDATE = "zhibo.explevel.update";
    public static final String COMMAND_EXPLEVEL_GET = "zhibo.explevel.get";         //获取经验值

    public static final String COMMAND_ACCOUNT_GET_CAPTCHA = "zhibo.account.getcaptcha";         //获取验证码
    public static final String COMMAND_ACCOUNT_REGISTER_BY_PHONE = "zhibo.account.registerbyphone";         //注册手机号码
    public static final String COMMAND_ACCOUNT_LOGIN_BY_PHONE = "zhibo.account.loginbyphone";         //手机号码登录
    public static final String COMMAND_ACCOUNT_UPDATE_PWD = "zhibo.account.updatepwd";         //更改密码
    public static final String COMMAND_ACCOUNT_FORGET_PWD = "zhibo.account.forgetpwd";         //忘记密码
    public static final String COMMAND_ACCOUNT_LOGIN_BYUUID = "zhibo.account.loginbyuuid";        //帐号登录
    public static final String COMMAND_ACCOUNT_SETPWD = "zhibo.account.setpwd";                   //设置密码
    public static final String COMMAND_ACCOUNT_UPDATEPWDUUID = "zhibo.account.updatepwduuid";     //更改密码
    public static final String COMMAND_ACCOUNT_GETACCOUNTPWDINFO = "zhibo.account.getaccountpwdinfo";//查询账户密码信息
    public static final String COMMAND_ACCOUNT_FORGETPWD4UUID = "zhibo.account.forgetpwd4uuid";       //忘记密码
    public static final String COMMAND_ACCOUNT_SCAN_QRCODE = "zhibo.account.scanqrcode";       //扫描二维码
    public static final String COMMAND_ACCOUNT_CONFIRM_LOGIN_BY_QRCODE = "zhibo.account.confirmloginbyqrcode"; //扫描二维码后，确认登录
    public static final String COMMAND_ACCOUNT_XIAOMI_SSO_LOGIN = "zhibo.account.missologin";   //小米帐号sso登录
    public static final String COMMAND_ACCOUNT_GET_ACCESS_TOKEN = "zhibo.account.getaccesstoken"; //用于第三方（如小米游戏）接入小米直播
    public static final String COMMAND_ACCOUNT_3PARTSIGNLOGIN = "zhibo.account.3partsignlogin"; //对接第三方账号签名登陆，比如对接真真海淘，直播客户端用户进入直播房间要打通用户对输入参数进行签名

    /*提现相关*/
    public static final String COMMAND_GET_RANK_LIST = "zhibo.rank.list";                           //查询尚票排行榜
    public static final String COMMAND_GET_RANK_LIST_V2 = "zhibo.rank.listv2";                      //查询尚票排行榜
    public static final String COMMAND_GET_RANK_ROOM_LIST = "zhibo.rank.room";                      //查询房间星票排行榜
    public static final String COMMAND_GET_RANK_ROOM_TICKET = "zhibo.rank.roomtotalticket";         //查询房间星票数
    public static final String COMMAND_GET_RANK_ROOM_TEN_MIN_LIST = "zhibo.rank.tenmin";            //查询房间近十分钟星票排行榜
    public static final String COMMAND_GET_EXCHANGE_LIST = "zhibo.bank.getExchangeList";            //查询尚票和钻的兑换列表
    public static final String COMMAND_QUERY_PROFIT = "zhibo.bank.queryProfit";                     //查询用户收益
    public static final String COMMAND_EXCHANGE = "zhibo.bank.exchange";                            //票换钻
    public static final String COMMAND_BAN_SPEAKER = "zhibo.live.banSpeaker";                       //禁言观众
    public static final String COMMAND_CANCEL_BAN_SPEAKER = "zhibo.live.cancelBanSpeaker";          //取消禁言观众
    public static final String COMMAND_GET_BANSPEAKER_LIST = "zhibo.live.getLiveKeyPersonInfo";     //得到禁言列表
    public static final String COMMAND_COMMIT_PAY_INFO = "zhibo.bank.bind";                         //帮顶提现资料
    public static final String COMMAND_COMMIT_PAY_TAGET = "zhibo.bank.withdraw";
    public static final String COMMAND_WITHDRAW_RECORD = "zhibo.bank.withdrawList";
    public static final String COMMAND_BANK_AUTHENTICATION = "zhibo.bank.authentication";
    public static final String COMMAND_BANK_AUTH_AND_WITHDRAW = "zhibo.bank.authAndWithdraw";

    public static final String COMMAND_GET_CONFIG = "zhibo.getconfig";                              //获取配置 如等级的对应关系

    /*卡拉ok相关*/
    public static final String COMMAND_KARA_OK_PARAMS = "zhibo.volume";
    public static final String COMMAND_RECOMMEND_MUSIC = "zhibo.music.recommend";
    public static final String COMMAND_SEARCH_MUSIC = "zhibo.music.search";
    public static final String COMMAND_GET_LYRIC = "zhibo.music.lyric";

    public static final String COMMAND_FACE_BEAUTY_PARAMS = "zhibo.getconfig.camera";

    public static final String COMMAND_STAT_REPORT = "zhibo.stat.report";

    public static final String COMMAND_REPORT = "zhibo.feedback.report";
    public static final String COMMAND_DELAY_REPORT = "zhibo.report.delay";
    public static final String COMMAND_IP_SELECT_QUERY = "zhibo.ipselect.query";

    /*直播购物相关*/
    public static final String COMMAND_SHOPPING_MALL_QUERY = "zhibo.shopping.get_goods_list";
    public static final String COMMAND_MY_ORDER_QUERY = "zhibo.shopping.user_order_info";
    public static final String COMMAND_SHOPPING_TAP_PUSH = "zhibo.shopping.tap_ad_push";

    public static final String COMMAND_HOTSPOT_SET = "zhibo.hotspot.set";
    public static final String COMMAND_HOTSPOT_GET = "zhibo.hotspot.get";
    //微博认证
    public static final String COMMAND_CERTIFICATE = "zhibo.account.certificate";

    //拉取banner
    public static final String COMMAND_BANNER_SYNC = "zhibo.banner.syncbanner";

    //查询排序后的房间用户列表
    public static final String COMMAND_ROOM_VIEWER = "zhibo.room.users";

    //根据uuid查询正在直播的房间信息
    public static final String COMMAND_GET_LIVE_ROOM = "zhibo.live.hisroom";

    //push命令, 设置日志级别
    public static final String COMMAND_PUSH_LOGLEVEL = Const.DATA_LOGLEVEL_CMD;

    //连麦信令
    public static final String COMMAND_LIVE_MICUIDACTIVE = "zhibo.live.micuidactive";
    public static final String COMMAND_LIVE_MICUIDSLEEP = "zhibo.live.micuidsleep";

    //PK通话信令
    public static final String COMMAND_VOIP_SIGNAL = "zhibo.signal";

    public static final String COMMAND_VOIP_SIGNAL_MEMBERISALIVE = "zhibo.signal.memberisalive";

    //网络探测命令字
    public static final String COMMAND_NET_WORK_PRODE = "milink.push.probenet";

    //游戏相关
    public static final String COMMAND_GAME_HOMEPAGE = "zhibo.game.homepage";//首页游戏页签
    public static final String COMMAND_GAME_SUBJECT = "zhibo.game.specialtopic";//游戏专题
    public static final String COMMAND_GAME_CATEGORY = "zhibo.game.category";//游戏分类
    public static final String COMMAND_GAME_HOMEPAGEUP = "zhibo.game.homepageup";//首页游戏页签上拉

    //获取feeds列表
    public static final String COMMAND_GET_FEEDS_LIST = "zhibo.feed.getFeedList";

    //获取单个用户feeds列表
    public static final String COMMAND_GET_USER_FEEDS_LIST = "zhibo.feed.userPageFeedList";

    //点赞
    public static final String COMMAND_FEEDS_LIKE = "zhibo.feeds.like";
    //取消点赞
    public static final String COMMAND_FEEDS_CANCEL_LIKE = "zhibo.feeds.like_delete";
    //创建评论
    public static final String COMMAND_FEEDS_COMMENT_CREATE = "zhibo.feeds.create_comment";
    //删除评论
    public static final String COMMAND_FEEDS_COMMENT_DELETE = "zhibo.feeds.delete_comment";
    //获取一个feed详情
    public static final String COMMAND_FEEDS_GET_FEED_INFO = "zhibo.feed.getFeedInfo";
    //拉取一个feeds 的评论
    public static final String COMMAND_FEEDS_COMMENT_QUERY = "zhibo.feeds.query_comment";
    //小视频和回放的
    public static final String COMMAND_FEEDS_SET_STAT_INFO = "zhibo.feeds.set_stat_info";
    //拉取通知未读数
    public static final String COMMAND_SYNC_NOTIFY_UNREAD_COUNT = "zhibo.feeds.to_me_count";
    //拉取通知
    public static final String COMMAND_SYNC_NOTIFY_MSG = "zhibo.feeds.to_me_list";

    public static final String COMMAND_FEEDS_CREAT = "zhibo.feeds.create";

    //删除feeds
    public static final String COMMAND_FEEDS_DELETE = "zhibo.feeds.delete";

    public static final String COMMAND_SYNC_NEW_FEED = "zhibo.feeds.getnew";

    public static final String COMMAND_FETCH_LIKE_LIST = "zhibo.feeds.like_list";

    public static final String COMMAND_SYNC_NOTIFY_LIST_ACK = "zhibo.feeds.to_me_list_ack";

    public static final String COMMAND_PUSH_GLOBAL_MSG = "zhibo.push.globalmsg";

    public static final String COMMAND_EFFECT_GET = "zhibo.effect.get";

    public static final String COMMAND_ZHIBO_OPEN_API_AUTH = "zhibo.openapi.auth";

    public static final String COMMAND_PULL_ROOM_MESSAGE = "zhibo.pull.roommsg";

    //拉取个人feed 总数
    public static final String COMMAND_FEEDS_GET_FEED_NUM = "zhibo.feeds.feed_num";

    public static final String COMMAND_LIST_TOPIC = "zhibo.list.tpclive";//获取指定话题的直播列表

    public static final String COMMAND_FEEDS_PUSH_TO_ME_COUNT = "zhibo.feeds.push_tome_count"; //push count

    //搜索推荐页数据
    public static final String COMMAND_USER_SEARCH_PAGE = "zhibo.user.search_page";
    // 获取搜索关键字对的联想词
    public static final String COMMAND_SEARCh_RELATION_KEYWORD = "zhibo.user.relation_keyword";

    /*频道相关*/
    public static final String COMMAND_HOT_CHANNEL_LIST = "zhibo.recommend.list";
    public static final String COMMAND_HOT_CHANNEL_SUB_LIST = "zhibo.recommend.sublist";
    public static final String COMMAND_MI_CHANNEL_GET_MJ = "zhibo.michannel.getmj";

    public static final Set<String> CHANNEL_COMMAND = new HashSet();

    static {
        CHANNEL_COMMAND.add(COMMAND_HOT_CHANNEL_LIST);
    }

    public static final String COMMAND_ROOM_ATTACHMENT = "zhibo.room.attachment";
    public static final String COMMAND_ROOM_WIDGET = "zhibo.room.getwidget";
    public static final String COMMAND_ROOM_ATTACHMENT_CLICK = "zhibo.room.widgetclick";
    //话题推荐相关
    public static final String COMMAND_TOPIC_RECOMMEND = "zhibo.recommend.topics";

    //抽奖
    public static final String COMMAND_SIMPLE_LOTTERY_GET = "zhibo.lottery.simple";

    public static final String COMMAND_GIFT_LOTTERY_GET = "zhibo.lottery.gift";

    public static final String COMMAND_GIFT_LOTTERY_INFO_GET = "zhibo.lottery.getgiftinfo";

    public static final String COMMAND_GIFT_LOTTERY_INFO_REPORT = "zhibo.lottery.report";

    public static final String COMMAND_LOTTERY_DETAIL_GET = "zhibo.lottery.getdetail";

    public static final String COMMAND_LOTTERY_LIST_GET = "zhibo.lottery.getlist";

    public static final String COMMAND_LOTTERY_ROUND_GET = "zhibo.lottery.getround";

    public static final String COMMAND_GET_LOTTERY_WHITE_LIST = "zhibo.lottery.isinwhitelist";

    //点击购物 “想要”统计
    public static final String COMMAND_ADD_NUM = "zhibo.shopping.stat_info";

    //主播一键上架
    public static final String COMMAND_TAP_TO_SELL = "zhibo.shopping.tap_to_sell";

    public static String generateCommandByAction(final SignalProto.SignalAction action) {
        if (null != action) {
            switch (action) {
                case INVITE: {
                    return COMMAND_VOIP_SIGNAL + ".invite";
                }
                case ACCEPT: {
                    return COMMAND_VOIP_SIGNAL + ".accept";
                }
                case BUSY: {
                    return COMMAND_VOIP_SIGNAL + ".busy";
                }
                case CANCEL: {
                    return COMMAND_VOIP_SIGNAL + ".cancel";
                }
                case CHECK: {
                    return COMMAND_VOIP_SIGNAL + ".check";
                }
                case RING: {
                    return COMMAND_VOIP_SIGNAL + ".ring";
                }
                case EVENT_NOTIFY: {
                    return COMMAND_VOIP_SIGNAL + ".event_notify";
                }
                case PUSHACK: {
                    return COMMAND_VOIP_SIGNAL + ".push_ack";
                }
            }
        }
        return COMMAND_VOIP_SIGNAL;
    }

    public static final String COMMAND_SHARE_TAG_TAIL = "zhibo.share.tagtail";// 获取分享尾部文案，服务器可配方案

    //先审后发
    public static final String COMMAND_USER_FIRST_ADUIT = "zhibo.user.firstaudit4pro";

    public static final String COMMAND_REPLAY_COUNT = "zhibo.feeds.set_stat_info";

    //创建预告
    public static final String COMMAND_FORNOTICE_CREATE = "zhibo.fornotice.create";

    //取消预告
    public static final String COMMAND_FORNOTICE_CANEL = "zhibo.fornotice.cancel";

    //删除预告
    public static final String COMMAND_FORNOTICE_DELETE = "zhibo.fornotice.delete";

    //查询预告
    public static final String COMMAND_FORNOTICE_GET = "zhibo.fornotice.get";

    //查询混排列表 C2S
    public static final String COMMAND_FORNOTICE_GETLIST = "zhibo.fornotice.getlist";

    //查询预告频道页 C2S
    public static final String COMMAND_FORNOTICE_GETPAGE = "zhibo.fornotice.getpage";

    //检查预告白名单
    public static final String COMMAND_FORNOTICE_CHECK = "zhibo.fornotice.check";

    public static final String COMMAND_ACCOUNT_SIGN = "zhibo.account.sign";
    public static final String COMMAND_ROOM_YIZHIBOINFO = "zhibo.room.yizhiboInfo";

    // 一直播兑换
    public static final String COMMAND_YZB_EXCHANGELIST = "zhibo.mall.yzbExchangeList";
    public static final String COMMAND_YZB_EXCHANGE = "zhibo.mall.yzbExchange";

    //垃圾箱命令字
    public static final String COMMAND_GET_TRASHBIN_LIST = "zhibo.get.trashbinlist";
    public static final String COMMAND_ADD_TRASHBIN_LIST = "zhibo.add.trashbinlist";
    public static final String COMMAND_CLEAR_TRASHBIN_LIST = "zhibo.clear.trashbinlist";
    public static final String COMMAND_REMOVE_TRASHBIN_LIST = "zhibo.remove.trashbinlist";

    //主播排行榜相关
    public static final String COMMAND_RANK_LIST_CONFIG = "zhibo.ranklist.config";
    public static final String COMMAND_RANK_LIST_QUERY = "zhibo.ranklist.query";
    //电视台相关
//    public static final String COMMAND_CURRENT_PROGRAM = "zhibo.currentprogram.get";
//    public static final String COMMAND_DAY_PROGRAM_LIST = "zhibo.dayprogram.get";
//    public static final String COMMAND_SUBSCRIB_PROGRAM = "zhibo.program.subscribe";


    public static final String COMMAND_CURRENT_PROGRAM = "zhibo.roomtv.getcurrentprogram";
    public static final String COMMAND_DAY_PROGRAM_LIST = "zhibo.roomtv.getdayprogram";
    public static final String COMMAND_SUBSCRIB_PROGRAM = "zhibo.roomtv.subscribe";
    public static final String COMMAND_GET_NEW_CHANNELID = "zhibo.roomtv.getnewchannelid";
    //Feed列表为空时，推荐关注人
    public static final String COMMAND_FEED_RECOMUSERS = "zhibo.feed.recomUsers";

    public static final String COMMAND_LIVE_GET_TITLE_LIST = "zhibo.live.gettitlelist";

    public static final String COMMAND_EXCHANGEGAME = "zhibo.bank.exchangeGameTicket";                            //票换钻
    public static final String COMMAND_GET_EXCHANGEGAME_LIST = "zhibo.bank.getExchangeGameTicketList";            //查询尚票和钻的兑换列表

    // 打点统计，复用频道打点
    public static final String COMMAND_STATISTICS_RECOMMEND_TAG = "zhibo.ai.recommendflag";

    // 宠爱团专用
    // 拉取用户加入的宠爱团列表
    public static final String COMMAND_VFANS_GROUP_LIST = "zhibo.vfans.get_grouplist";
    // 宠爱团详情
    public static final String COMMAND_VFANS_GROUP_DETAIL = "zhibo.vfans.group_detail";
    // 列表拉取群成员请求
    public static final String COMMAND_VFANS_MEM_LIST = "zhibo.vfans.member_list";
    // 查看群完成任务
    public static final String COMMAND_VFANS_JOB_LIST = "zhibo.vfans.group_joblist";
    // 创建宠爱团
    public static final String COMMAND_VFANS_CREATE_GROUP = "zhibo.vfans.create_group";
    // 申请宠爱团请求
    public static final String COMMAND_VFANS_APPLY_JOIN_GROUP = "zhibo.vfans.apply_joingroup";
    // 退出宠爱团
    public static final String COMMAND_VFANS_QUIT_GROUP = "zhibo.vfans.quit_group";
    // 完成任务领取宠爱值
    public static final String COMMAND_VFANS_FINISH_GROUP_JOB = "zhibo.vfans.finish_groupjob";
    // 修改群成员请求
    public static final String COMMAND_VFANS_UPDATE_GROUP_MEM = "zhibo.vfans.update_groupmem";
    // 用户完成的最近任务
    public static final String COMMAND_VFAN_GET_RECENT_JOB = "zhibo.vfans.get_recentjob";
    // 处理加群请求
    public static final String COMMAND_VFANS_HANDLE_JOIN_GROUP = "zhibo.vfans.handle_joingroup";
    // 查询宠爱团勋章
    public static final String COMMAND_VFAN_GET_GROUP_MEDAL = "zhibo.vfans.get_groupmedal";
    // 设置宠爱团勋章
    public static final String COMMAND_VFAN_SET_GROUP_MEDAL = "zhibo.vfans.set_groupmedal";
    // 团排行榜
    public static final String COMMAND_VFANS_GROUP_RANK_LIST = "zhibo.vfans.group_ranklist";

    // 粉丝团群通知
    public static final String COMMAND_VFANS_GETNOTIFICATION = "zhibo.vfans.getnotification";
    public static final String COMMAND_VFANS_ACKNOTIFICATION = "zhibo.vfans.acknotification";
    public static final String COMMAND_VFANS_PUSHNOTIFICATION = "zhibo.push.vfansnotification";
}
