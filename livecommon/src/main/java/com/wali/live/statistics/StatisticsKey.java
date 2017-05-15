package com.wali.live.statistics;

/**
 * Created by yurui on 3/8/16.
 *
 * @module 常量池
 */
public class StatisticsKey {

    public final static String AC_APP = "ml_app";
    public final static String KEY = "key";
    public final static String TIMES = "times";

    private final static String SPLITTER_HYPHEN = "-";

    public static String appendKeyWithArgs(String key, Object... args) {
        StringBuilder stringBuilder = new StringBuilder(key);
        for (Object arg : args) {
            stringBuilder.append(SPLITTER_HYPHEN).append("" + arg);
        }
        return stringBuilder.toString();
    }

    //    1	概况打点	打开app的次数	使用人数/次数	and_20150301	计数值	用户每天打开米聊的人数和次数
    public static final String TYPE_APP_OPEN_COUNT = "and_20150301";
    //    2		米聊的使用时长	使用时长	and_20150302	计算起始时间	米聊app在前台的时间
    public static final String TYPE_APP_FORGROUND_TIME = "and_20150302";

    //成功率关键指标
    public static final String AC_CALL_FACTOR = "call_factor";

    public static final String KEY_SONG_DOWNLOAD = "key_song_download";

    public static final String KEY_NETWORK_PRODE_START = "network_prode_start"; //开始网络探测的打点统计
    public static final String KEY_NETWORK_PRODE_SUCCESS = "network_prode_success"; //网络探测成功的打点统计

    public static final String KEY_BACK_SHOW_SHARE_COUNT = "live_back_show_share_count";
    public static final String KEY_LIVE_SHOWING_SHARE_COUNT = "live_showing_share_count";
    public static final String KEY_LIVE_PRE_SHOW_SHARE_COUNT = "live_pre_show_share_count";
    public static final String KEY_LIVE_AFTER_SHOW_SHARE_COUNT = "live_after_show_share_count";

    public static final String KEY_LIVE_SHOW_CANCEL_MOMENT_SHARE_COUNT = "live_show_cancel_moment_share_count";
    public static final String KEY_LIVE_SHOW_CANCEL_FACEBOOK_SHARE_COUNT = "live_show_cancel_facebook_share_count";

    public static final String KEY_PLATFORM_WINXIN = "weixin_";
    public static final String KEY_PLATFORM_MOMENT = "moment_";
    public static final String KEY_PLATFORM_QQ = "qq_";
    public static final String KEY_PLATFORM_QZONE = "qzone_";
    public static final String KEY_PLATFORM_BLOG = "blog_";
    public static final String KEY_SHARE_PERSON_ANCHOR = "Anchor_";
    public static final String KEY_SHARE_PERSON_AUDIENCE = "Audience_";

    public static final String KEY_JUMP_LIVE_FROM_SHOUYE = "jump_live_from_shouye";
    public static final String KEY_JUMP_PRIVATE_LIVE_FROM_SHOUYE = "jump_private_live_from_shouye";
    public static final String KEY_JUMP_LIVE_FROM_FLOAT_NOTIFICATION = "jump_live_from_float_notification";
    public static final String KEY_JUMP_PRIVATE_LIVE_FROM_FLOAT_NOTIFICATION = "jump_private_live_from_float_notification";


    public static final String KEY_USERINFO_CARD_OPEN = "C-sard";       //个人名片打开次数打点
    public static final String KEY_USERINFO_CARD_NO1 = "C-no1";         //个人名片页贡献第一头像点击次数打点
    public static final String KEY_USERINFO_CARD_FOLLOW = "C-follow";       //个人名片页关注按钮点击次数打点
    public static final String KEY_USERINFO_CARD_REPORT = "C-report";       //个人名片页举报按钮点击次数打点
    public static final String KEY_USERINFO_CARD_HOME = "C-home";           //个人名片页主页按钮点击次数打点
    public static final String KEY_USERINOF_CARD_MESS = "C-mess";           //个人名片页私信按钮点击次数打点
    public static final String KEY_USERINFO_CARD_MANAGER = "C-manager";        //个人名片页设为管理员按钮点击次数打点

    /*私信部分打点key*/
    public final static String KEY_IM_SEND_LIVE = "im_send_live";   // 直播房间内打点
    public final static String KEY_IM_SEND_OFFLINE = "im_send_offline"; //直播房间外打点

    public final static String KEY_IM_SEND_PIC_KS3 = "im_send_live_pic_ks3";   // 发送私信图片

    public final static String KEY_IM_SEND_PIC_COUNT = "im_send_live_pic_count";   // 发送私信图片

    public final static String KEY_IM_SEND_PIC_SUCCESS_COUNT = "im_send_live_pic_success_count";   // 发送私信图片成功打点

    public final static String KEY_IM_SEND_TEXT_COUNT = "im_send_text_count"; //发送私信文本图片打点

    public final static String KEY_IM_SEND_TEXT_SUCCESS_COUNT = "im_send_text_success_count"; //发送私信文本成功打点

    public final static String KEY_IM_SEND_FAIL_INFO = "im_fail_info"; //发送私信文本成功打点

    public final static String KEY_IM_SEND_FROM_PAGE = "im_send-%spage";//从指定的页面发送私信
     /*私信部分打点key结束*/

    //弹幕打点
    public final static String KEY_BARRAGE_CUSTOM_SEND_ALL = "barrage_custom_send_count";
    public final static String KEY_BARRAGE_CUSTOM_SEND_SUCCESS = "barrage_custom_send_success";

    // 厂包首次启动页面打点
    public final static String KEY_FACTORY_FIRSTPAGE_VIEW = "factory-firstpage-view";   // 页面展示打点
    public final static String KEY_FACTORY_FIRSTPAGE_CANCEL = "factory-firstpage-cancel";  // 点击了取消
    public final static String KEY_FACTORY_FIRSTPAGE_AGREE = "factory-firstpage-agree";   // 点击了同意

    /*登录方式*/
    public static final String KEY_LOGIN_TYPE_WX = "wx";//微信
    public static final String KEY_LOGIN_TYPE_QQ = "qq";//QQ
    public static final String KEY_LOGIN_TYPE_WB = "wb";//微博
    public static final String KEY_LOGIN_TYPE_MI = "mi";//小米
    public static final String KEY_LOGIN_TYPE_FACEBOOK = "facebook";
    public static final String KEY_LOGIN_TYPE_GOOGLE = "google";
    public static final String KEY_LOGIN_TYPE_INSTAGRAM = "instagram";
    public static final String KEY_LOGIN_TYPE_PHONE = "phone";//手机号码
    public static final String KEY_LOGIN_TYPE_ID = "id";//直播id
    public static final String KEY_LOGIN_TYPE_MISSO = "misso";

    /*
     * 登陆打点
     */
    //登录成功打点
    public static final String KEY_LOGIN_SUCCESS = "log_%s_success";
    //登录失败打点
    public static final String KEY_LOGIN_FAIL = "log_%s_fail";
    //取消登录
    public static final String KEY_LOGIN_CANCEL = "log_%s_cancel";
    //登录页面展示——log_view
    public static final String KEY_LOGIN_VIEW = "log_view";
    //点击注册——log_reg
    public static final String KEY_LOGIN_RIGISTER = "log_reg";
    //点击微信登录——log_wx
    public static final String KEY_LOGIN_WEIXIN = "log_wx";
    //点击QQ登录——log_qq
    public static final String KEY_LOGIN_QQ = "log_qq";
    //点击更多
    public static final String KEY_LOGIN_MORE = "log_more";
    //点击微博登录——log_wb
    public static final String KEY_LOGIN_WEIBO = "log_wb";
    //点击米聊登录——log_mi
    public static final String KEY_LOGIN_MILIAO = "log_mi";
    //点击google登录
    public static final String KEY_LOGIN_GOOGLE = "log_google";
    //点击facebook登录
    public static final String KEY_LOGIN_FACEBOOK = "log_facebook";
    //点击instagram登录
    public static final String KEY_LOGIN_INSTAGRAM = "log_instagram";
    //点击退出登录
    public static final String KEY_LOGOUT = "log_out";
    //首次手机号码登录展示
    public static final String KEY_PHONE_LOGIN_VIEW = "log_phone_login_view";
    //点击帐号登录
    public static final String KEY_LOGIN_PHONE = "log_phone";
    //显示用小米帐号登录的对话框
    public static final String KEY_SHOW_MI_ACCOUNT_LOGIN_DIALOG = "log_show_mi_account_login_dialog";
    //点击用小米帐号登录的对话框的取消按钮
    public static final String KEY_CLICK_MI_ACCOUNT_LOGIN_DIALOG_CANCEL = "log_click_mi_account_login_dialog_cancel";
    //点击用小米帐号登录的对话框的确定按钮
    public static final String KEY_CLICK_MI_ACCOUNT_LOGIN_DIALOG_CONFIRM = "log_click_mi_account_login_dialog_confirm";
    //小米sso登录获取authtoken成功
    public static final String KEY_LOGIN_MISSO_GETAUTHTOKEN_SUCCESS = "log_misso_get_authtoken_success";
    //小米sso登录获取authtoken失败
    public static final String KEY_LOGIN_MISSO_GETAUTHTOKEN_FAIL = "log_misso_get_authtoken_fail";
    //小米sso登录获取authtoken耗时，单位毫秒
    public static final String KEY_LOGIN_MISSO_GETAUTHTOKEN_TIME = "log_misso_get_authtoken_time";
    //进入小米sso登录
    public static final String KEY_LOGIN_MISSO_LOGIN = "log_misso_login";
    //小米sso登录成功
    public static final String KEY_LOGIN_MISSO_LOGIN_SUCCESS = "log_misso_login_success";
    //小米sso登录失败
    public static final String KEY_LOGIN_MISSO_LOGIN_FAIL = "log_misso_login_fail";
    //小米sso登录耗时
    public static final String KEY_LOGIN_MISSO_LOGIN_TIME = "log_misso_login_time";
    //小米sso登录用户信息不全需要进入到用户信息设置引导页
    public static final String KEY_LOGIN_MISSO_NEED_GUIDE = "log_misso_login_need_guide";

    /*
     * 注册页面打点
     */
    //注册页面展示
    public static final String KEY_REGIST_VIEW = "log_regist_view";
    //点击手机号码注册页面提交按钮
    public static final String KEY_REGIST_SUBMIT = "log_regist_submit";
    //点击注册时发送验证码按钮
    public static final String KEY_REGIST_SEND_CAPTCHA = "log_regist_send_captcha";
    //点击注册时选择国家地区码按钮
    public static final String KEY_REGIST_SELECT_COUNTRY_CODE = "log_regist_select_country_code";
    //注册成功
    public static final String KEY_REGIST_PHONE_SUCCESS = "log_reg_success";
    //注册失败
    public static final String KEY_REGIST_PHONE_FAILED = "log_reg_failed";
    //注册取消,返回
    public static final String KEY_REGIST_PHONE_CANCEL = "log_regist_view_cancel";

    /*
     * 登录页面打点
     */
    //帐号登录页面展示
    public static final String KEY_ID_LOGIN_VIEW = "log_id_login_view";
    //点击手机号码登录按钮
    public static final String KEY_PHONE_LOGIN = "log_phone_login";
    //点击直播帐号登录按钮
    public static final String KEY_ID_LOGIN = "log_id_login";
    //点击登录时选择国家地区码按钮
    public static final String KEY_LOGIN_SELECT_COUNTRY_CODE = "log_login_select_country_code";

    public static final String KEY_RESET_PWD = "reset_password";
    //点击帐号登录页面手机号提交按钮
    public static final String KEY_PHONE_LOGIN_SUBMIT = "log_id_login_submit";
    //点击phone忘记密码按钮
    public static final String KEY_PHONE_RESET_PWD = "log_reset_password";
    //点击帐号登录页面账号ID提交按钮
    public static final String KEY_ID_LOGIN_SUBMIT = "login_id";
    //点击id忘记密码按钮
    public static final String KEY_ID_RESET_PWD = "login_id_reset";

    /*
     * 忘记密码页面打点
     */
    //重置手机号密码页面展示
    public static final String KEY_RESET_PHONE_PASSWORD_VIEW = "log_reset_phone_password_view";
    //重置手机号密码页面取消打点
    public static final String KEY_RESET_PHONE_PASSWORD_CANCEL = "log_reset_phone_password_view_cancel";
    //点击重置密码时选择国家地区码按钮
    public static final String KEY_RESET_SELECT_COUNTRY_CODE = "log_reset_select_country_code";
    //点击重置密码时发送验证码按钮
    public static final String KEY_RESET_SEND_CAPTCHA = "log_reset_send_captcha";
    //点击重置手机号码的密码提交按钮
    public static final String KEY_RESET_PHONE_PWD_SUBMIT = "log_reset_phone_pwd_submit";
    //重置帐号密码页面展现
    public static final String KEY_RESET_ID_PWD_VIEW = "log_reset_id_pwd_view";
    //id
    public static final String KEY_RESET_ID_PWD_CANCEL = "log_reset_id_pwd_view_cancel";
    //点击重置直播帐号密码提交按钮
    public static final String KEY_RESET_ID_PWD_SUBMIT = "log_reset_id_pwd_submit";
    //设置新密码页面展现
    public static final String KEY_SET_NEW_PWD_VIEW = "log_set_new_pwd_view";
    //点击设置新密码提交按钮
    public static final String KEY_SET_NEW_PWD_SUBMIT = "log_set_new_pwd_submit";
    //帐号没有设置密码
    public static final String KEY_ID_NOT_HAVE_PWD = "log_id_not_have_pwd";

    /*
     * 个人信息设置页面打点
     */
    //个人信息设置页面展现
    public static final String KEY_PROFILE_SETTING_VIEW = "log_profile_setting_view";
    //点击个人信息设置提交按钮
    public static final String KEY_PROFILE_SETTING_SUBMIT = "log_profile_setting_submit";
    //
    public static final String KEY_UPLOAD_USERINFO_CANCEL = "log_profile_setting_view_cancel";


    public static final String KEY_LINK_MIC_INVITE = "link_mic_invite";            // 主播发起连麦邀请
    public static final String KEY_LINK_MIC_INVITE_RING = "link_mic_invite_ring";       // 嘉宾端收到请
    public static final String KEY_LINK_MIC_ACCEPT = "link_mic_accept";            // 观众接受连麦
    public static final String KEY_LINK_MIC_REJECT = "link_mic_reject";            // 观众拒绝连麦
    public static final String KEY_LINK_MIC_SPEAKING = "link_mic_speaking";          // 主播收到连麦，连麦成功
    public static final String KEY_LINK_MIC_SPEAKING_DURATION = "link_mic_speaking_duration"; // 连麦的通话时长
    public static final String KEY_LINK_MIC_SEND_GUEST_LEAVE = "link_mic_send_guest_leave";  // 通知切到后台
    public static final String KEY_LINK_MIC_RECV_GUEST_LEAVE = "link_mic_recv_guest_leave";  // 接收切换后台
    public static final String KEY_LINK_MIC_SEND_GUEST_REJOIN = "link_mic_send_guest_rejoin"; // 通知切换前台
    public static final String KEY_LINK_MIC_RECV_GUEST_REJOIN = "link_mic_recv_guest_rejoin"; // 接收切换前台

    /***
     * 推拉流打点
     */
    public static final String KEY_MONITOR_STREAM_STARTED_SUCCESS = "monitor_stream_started_success";
    public static final String KEY_MONITOR_STREAM_STARTED_FAILED = "monitor_stream_started_failed";
    public static final String KEY_PLAYER_STARTED_SUCCESS = "monitor_player_started_success";// 拉流成功
    public static final String KEY_PLAYER_STARTED_FAILED = "monitor_player_started_failed";// 拉流失败
    public static final String KEY_LIVE_LANDSCAPE = "key_live_landscape"; //Live横屏
    public static final String KEY_WATCH_LANDSCAPE = "key_watch_landscape";//Watch横屏

    // 回放打点
    public static final String KEY_REPLAY = "replay-%s-%d";

    /**
     * 搜索页面打点
     */
    public static final String KEY_SEARCH_RESULT_OPERATION_QUERY_PAGE_CLASSIFICATION_ID = "search_result-%s-%s-%s-%s-%s";

    //搜索框下拉列表打点
    public static final String KEY_SEARCH_CLICK_REMIND_WORD = "search_remind_%s-%d-%s";

    /**
     * 在搜索页面点击搜索结果，查看个人名片 searchPeople_Base64.encode(搜索关键词)_用户点击查看资料的用户ID
     */
    public static final String KEY_SEARCH_PEOPLE_LOOK_PERSON_INFO = "searchPeople_%s_%d";
    /**
     * 在搜索页面点击关注 searchPeople_Base64.encode(搜索关键词)_用户点击关注的用户ID_{0,1} 1表示点了关注，0表示没有点
     */
    public static final String KEY_SEARCH_PEOPLE_FOLLOW = "searchPeople_%s_%d_%d";

    public static final String KEY_END_LIVE_SHOW = "endlive_show_%s_%s_%s_%s_%s_%s";
    public static final String KEY_END_LIVE_FOLLOW = "endlive_follow_%s_%s";
    public static final String KEY_END_LIVE_CHAT = "endlive_chat_%s_%s";
    public static final String KEY_END_LIVE_CLOSE = "endlive_close_%s_%s";
    public static final String KEY_END_LIVE_AVATAR = "endlive_head_click_%s";
    public static final String KEY_END_LIVE_HOMEPAGE = "endlive_tohomepage_click-%s";
    public static final String KEY_END_LIVE_RECOMMEND = "endlive_recommend-%s-%d-%s-%s";

    public static final String KEY_TV_LIVE_TIME = "channel_tvlive_looking_%s-%s-%s-%s";
    public static final String KEY_TV_ENTER_TIME = "channel_tvlive_click_%s-%s-%s-%s";
    public static final String KEY_TV_FOLLOW = "tvchannel-chatpage-follow";
    public static final String KEY_TV_ENLARGE = "tvchannel-chatpage-enlarge";
    public static final String KEY_TV_BARRAGE = "tvchannel-chatpage-barrage-%s";
    public static final String KEY_TV_CLICK_PROGRAM = "tvchannel-chatpage-%s-%s";
    public static final String KEY_TV_CLICK_PAGE = "tvchannel-personalpage-view";
    public static final String KEY_TV_PERSONAL_FOLLOW = "tvchannel-personalpage-follow";
    public static final String KEY_TV_PERSONAL_MESSAGE = "tvchannel-personalpage-message";
    public static final String KEY_TV_PERSONAL_LIVE = "tvchannel-personalpage-live";
    public static final String KEY_TV_WARN = "tvchannel-live-warn";
    public static final String KEY_TV_REPLAY = "tvchannel-replay-click";
    public static final String KEY_TV_REPLAY_TIME = "tvchannel-replay-seetimes-%s";
    public static final String KEY_TV_CLICK_BARRAGE = "tvchannel-live-barrage-%s-%s";

    public static final String KEY_WIDGET_CLICK = "live_widget_%s_%s_%s";

    public static final String KEY_RANKING_CLICK_TOTAL = "key_live_click_total_vanscoin";
    public static final String KEY_RANKING_CLICK_CURRENT = "key_live_click_room_vanscoin";
    public static final String KEY_RANKING_SHOW = "key_rank_show_room_coinrank";

    //feeds的打点
    public static final String KEY_FEEDS_LIST_PULL_NEWSET = "feeds_list_pull_newest";           //feeds列表拉取最新
    public static final String KEY_FEEDS_LIST_PULL_OLDER = "feeds_list_pull_older";             //feeds列表拉取旧数据
    public static final String KEY_FEEDS_DELETE = "feeds_delete";           //feeds删除
    public static final String KEY_FEEDS_LIKE = "feeds_like";               //feeds点赞
    public static final String KEY_FEEDS_COMMENT_SEND = "feeds_comment_send";        //feeds评论发送
    public static final String KEY_FEEDS_COMMENT_TIMES = "feeds_comment_times";           //feeds评论次数
    public static final String KEY_FEEDS_COMMENT_DELETE = "feeds_comment_delete";               //feeds评论删除
    public static final String KEY_FEEDS_GET_DETAIL = "feeds_get_detail";                   //feeds拉取详情
    public static final String KEY_FEEDS_PAGE_STAY_SECONDS = "feeds_page_stay_seconds";             //feeds页面停留的时间（以秒为单位）
    public static final String KEY_FEEDS_SHARE_TIMES = "feeds_share_times";          //分享次数
    public static final String KEY_FEEDS_LIKE_TIMES = "feeds_like_times";           //点赞次数
    public static final String KEY_FEEDS_CLICK_PIC_MAIN = "feeds_click_picture_main";       //图片点击在动态页
    public static final String KEY_FEEDS_CLICK_PIC_HOME = "feeds_click_picture_home";       //图片点击在个人主页
    public static final String KEY_FEEDS_CLICK_VIDEO_MAIN = "feeds_click_video_main";       //视频点击在动态页
    public static final String KEY_FEEDS_CLICK_VIDEO_HOME = "feeds_click_video_home";       //视频点击在个人主页
    public static final String KEY_FEEDS_CLICK_BACKSHOW_MAIN = "feeds_click_backshow_main";     //回放点击在动态页
    public static final String KEY_FEEDS_CLICL_BACKSHOW_HOME = "feeds_click_backshow_home";     //回放点击在个人主页

    //分享打点的类型
    public static final String KEY_FEEDS_SHARE_CONTENT_TYPE_LIVESHOW = "liveshow";
    public static final String KEY_FEEDS_SHARE_CONTENT_TYPE_BACKSSHOW = "backshow";
    public static final String KEY_FEEDS_SHARE_CONTENT_TYPE_BACKSET = "backset";
    public static final String KEY_FEEDS_SHARE_CONTENT_TYPE_VIDEO = "video";
    public static final String KEY_FEEDS_SHARE_CONTENT_TYPE_PIC = "picture";
    public static final String KEY_FEEDS_SHARE_CONTENT_TYPE_JOURNAL = "journal";

    //分享平台的类型
    public static final String KEY_FEEDS_SHARE_PLAT_TYPE_QQ = "QQ";
    public static final String KEY_FEEDS_SHARE_PLAT_TYPE_WEIBO = "weibo";
    public static final String KEY_FEEDS_SHARE_PLAT_TYPE_QZONE = "QZone";
    public static final String KEY_FEEDS_SHARE_PLAT_TYPE_WECHAT = "wechat";         //分享到微信
    public static final String KEY_FEEDS_SHARE_PLAT_TYPE_TWITTER = "twitter";
    public static final String KEY_FEEDS_SHARE_PLAT_TYPE_FACEBOOK = "facebook";
    public static final String KEY_FEEDS_SHARE_PLAT_TYPE_WECHAT_TIMELINE = "wechatTimeline";        //分享到朋友圈

    public static final String KEY_FEEDS_SHARE_RESULT_SUCCESS = "succeed";
    public static final String KEY_FEEDS_SHARE_RESULT_FAIL = "fail";

    public static final String KEY_FEEDS_SHARE = "feeds_share_%s_%s_%s";           //feeds分享的打点, 第一个%s表示类型, 第二个表示分享的平台. 第三个%s表示成功或者失败, 比如: feeds_share_video_weibo_succeed

    //金山云的打点
    public static final String KEY_KS3_UPLOAD_FILE = "zhibo.ks3.%s.uploadfile";
    public static final String KEY_KS3_SLICE_FILE = "zhibo.ks3.%s.slicefile";
    public static final String KEY_KS3_UPLOAD_SCLICE = "zhibo.ks3.%s.uploadslice";
    public static final String KEY_KS3_DOWNLOAD = "zhibo.ks3.%s.download";

    //preLiveFragment
    //mi5的美颜等级打点
    public static final String KEY_PRE_LIVE_BEAUTY_CLOSE = "key_pre_live_beauty_close";
    public static final String KEY_PRE_LIVE_BEAUTY_LOW = "key_pre_live_beauty_low";
    public static final String KEY_PRE_LIVE_BEAUTY_MIDDLE = "key_pre_live_beauty_middle";
    public static final String KEY_PRE_LIVE_BEAUTY_HIGH = "key_pre_live_beauty_high";
    //普通机的美颜打点
    public static final String KEY_PRE_LIVE_BEAUTY = "key_pre_live_beauty";
    public static final String KEY_PRE_LIVE_CAMERA = "key_pre_live_camera";

    //LiveActivity
    //mi5的美颜等级打点
    public static final String KEY_LIVING_BEAUTY_CLOSE = "key_live_beauty_close";
    public static final String KEY_LIVING_BEAUTY_LOW = "key_live_beauty_low";
    public static final String KEY_LIVING_BEAUTY_MIDDLE = "key_live_beauty_middle";
    public static final String KEY_LIVING_BEAUTY_HIGH = "key_live_beauty_high";
    //普通机的美颜打点
    public static final String KEY_LIVING_BEAUTY = "key_live_beauty";
    public static final String KEY_LIVING_CAMERA = "key_live_camera";            //前后摄像头切换
    public static final String KEY_LIVING_SELF_MIRROR = "key_live_self_mirror";   //自拍镜像
    public static final String KEY_LIVING_PHOTO_FLASH = "key_live_photo_flash";   //闪光灯

    public static final String KEY_LIVING_SONG_ADJUST = "key_live_song_adjust";//调节音乐声音
    public static final String KEY_LIVING_SONG_MUTE = "key_live_song_mute";   //音乐静音
    public static final String KEY_LIVING_SONG_HIGHEST = "key_live_song_highest"; //音乐声最大

    public static final String KEY_LIVING_VOICE_ADJUST = "key_live_voice_adjust"; //调节人声
    public static final String KEY_LIVING_VOICE_MUTE = "key_live_voice_mute";   //人声静音
    public static final String KEY_LIVING_VOICE_HIGHEST = "key_live_voice_highest"; //人声最大

    public static final String KEY_LIVING_SOUND_EFFECT = "key_live_sound_effect";   //音效=氛围
    public static final String KEY_LIVING_SOUND_EFFECT_LAUGH = "key_live_sound_effect_baoxiao";   //音效 爆笑
    public static final String KEY_LIVING_SOUND_EFFECT_SMILE = "key_live_sound_effect_daxiao";   //音效 大笑
    public static final String KEY_LIVING_SOUND_EFFECT_CHEER_UP = "key_live_sound_effect_cheer_up";   //音效 欢呼
    public static final String KEY_LIVING_SOUND_EFFECT_CLAP = "key_live_sound_effect_clap";   //音效 鼓掌
    public static final String KEY_LIVING_SOUND_EFFECT_FUNNY = "key_live_sound_effect_funny";   //音效 搞笑

    public static final String KEY_LIVING_SONG = "key_live_song";    //音乐
    public static final String KEY_LIVING_REVERBERATION = "key_live_reverberation";    //混响
    public static final String KEY_LIVING_REVERBERATION_ORIGIN = "key_live_reverberation_origin";    //混响-原声
    public static final String KEY_LIVING_REVERBERATION_RECORD_STUDIO = "key_live_reverberation_record_studio";    //混响-录音棚
    public static final String KEY_LIVING_REVERBERATION_KTV = "key_live_reverberation_ktv";    //混响-KTV
    public static final String KEY_LIVING_REVERBERATION_CONCERT = "key_live_reverberation_concert";    //混响-演唱会
    public static final String KEY_LIVING_SONG_HIFI = "key_live_song_hi_fi";    //高保真High-Fidelity

    //音乐
    public static final String KEY_LIVING_SONG_PLAY = "key_live_song_play";           //播放歌曲
    public static final String KEY_LIVING_SONG_DOWNLOAD = "key_live_song_download";   //下载歌曲
    public static final String KEY_LIVE_SONG_SEARCH = "key_live_song_search";         //搜索到的歌曲
    public static final String KEY_LIVE_SONG_SEARCH_NOT = "key_live_song_search_not"; //搜索不到的歌曲

    public static final String KEY_SETTING_SONGLIST = "key_setting_songlist";         //"我的曲库"点击
    public static final String KEY_SONGLIST_PRACTICING = "key_songlist_practicing";   //练唱点击

    public static final String KEY_EXT_RSPCODE = "recharge_ext_rspcode";
    // 充值相关打点
    public static final String KEY_RECHARGE = "recharge";
    public static final String KEY_RECHARGE_EXT_PAY_WAY = "recharge_ext_pay_way";
    public static final String KEY_RECHARGE_EXT_ORDER_ID = "recharge_ext_order_id";
    public static final String KEY_RECHARGE_ACTION_PULL_LIST = "recharge_action_pull_list";
    public static final String KEY_RECHARGE_ACTION_CONSUME_GOOGLEPLAY_PRODUCT = "recharge_action_consume_googelplay_product";
    public static final String KEY_RECHARGE_ACTION_CREATE_ORDER = "recharge_action_create_order";
    public static final String KEY_RECHARGE_ACTION_PAY_BY_WEIXIN = "recharge_action_pay_by_weixin";
    public static final String KEY_RECHARGE_ACTION_PAY_BY_ALIPAY = "recharge_action_pay_by_alipay";
    public static final String KEY_RECHARGE_ACTION_PAY_BY_MIWALLET = "recharge_action_pay_by_miwallet";
    public static final String KEY_RECHARGE_ACTION_PAY_BY_GOOGLEWALLET = "recharge_action_pay_by_googlewallet";
    public static final String KEY_RECHARGE_ACTION_PAY_BY_PAYPAL = "recharge_action_pay_by_paypal";
    public static final String KEY_RECHARGE_ACTION_SYNC_ORDER = "recharge_action_sync_order";

    // 频道中点击更多
    public static final String KEY_CHANNEL_CLICK_MORE = "michannel_%s_%s";
    // 频道搜索框点击预告
    public static final String KEY_CHANNEL_SCHEDULE_CLICK = "michannel_schedule_btn_click";

    public static final String KEY_ENTER_ROOM_ADMIN_PAGE = "room_admin_page"; //进入到房间管理页面

    public static final String KEY_ROOM_ADMIN_TAB_ADMIN = "room_admin_tab_admin_onclick"; //点击”管理员tab“事件

    public static final String KEY_ROOM_ADMIN_TAB_BANSPEAK = "room_admin_tab_banspeak_onclick";   //点击“禁言列表tab”事件

    public static final String KEY_ROOM_ADMIN_TAB_ROOMSETTING = "room_admin_tab_roomsetting_onclick"; //点击“房间设定tab”事件

    public static final String KEY_ROOM_ADMIN_TAB_INVITE = "room_admin_tab_invite_onclick"; //点击“邀请好友tab”事件

    public static final String KEY_ROOMSETTING_SPEAK_FREQUENCY_CONTROL = "roomsetting_speak_frequency_onclick";   //点击“不能重复发言”事件

    public static final String KEY_ROOMSETTING_SPEAK_SPACING = "roomsetting_speak_spacing_%d";    //点击“发言频率控制”事件

    //搜索
    public static final String KEY_SEARCH_HOMEPAGE_CLICK_TIMES_DESC_KEYWORD = "search_homepage_click_times-%s-%s";         //主页搜索框点击
    public static final String KEY_SEARCH_TAG_USER = "search_tag_user_%d";      //热门搜索之用户
    public static final String KEY_SEARCH_TAG_TOPIC = "search_tag_topic_%s";               //热门搜索之话题
    public static final String KEY_SEARCH_HOTUSER = "search_hotuser_%d";                   //推荐主播
    public static final String KEY_SEARCH_REPLAY = "search_replay_%s";          //回放
    public static final String KEY_SEARCH_TAG_KEYWORD = "search_tag_resultpage_%s"; // 关键词类型的tag
    public static final String KEY_SEARCH_TAG_URL = "search_tag_url_%s_%s"; // 跳转url的tag

    public static final String KEY_FOLLOW_FLOATING_WINDOW = "floating-window-%s";   //显示关注引导弹层
    public static final String KEY_FLOATING_FOLLOW = "floating-follow-%s";    //点击关注
    public static final String KEY_FLOATING_NAME_FOLLOW = "floating-namefollow-%s";   //点击名片关注
    public static final String KEY_FLOATING_WINDOWFOLLOW = "floating-windowfollow-%s";  //点击关注引导弹层

    public static final String KEY_LIVE_ROOM_TOP_FOLLOW_BUTTON = "live_room_top_follow_button_";    //+uuid
    public static final String KEY_LIVE_ROOM_FLOAT_FOLLOW_BUTTON = "live_room_float_follow_button_";//+uuid
    public static final String KEY_SEARCH_FOLLOW_BUTTON = "search_follow_button_";                  //+uuid
    public static final String KEY_PERSON_INFO_FOLLOW_BUTTON = "personinfo_follow_button_";         //+uuid

    public static final String KEY_SEARCH_RECOMMEND_PLAYBACK_CLICK = "search_recommend_playback_click_%s-%s";   //+cid,+liveid
    public static final String KEY_SEARCH_RECOMMEND_PLAYBACK_LOOKING = "search_recommend_playback_looking_%s-%s";   //+cid,+liveid

    public static final String KEY_SEARCH_RESULT_PLAYBACK_CLICK = "search_result_playback_click_%s";   //+liveid
    public static final String KEY_SEARCH_RESULT_PLAYBACK_LOOKING = "search_result_playback_looking_%s";   //+liveid

    //观众房间 主播多久被关注
    public static final String STATISTICS_FOLLOW_ANCHOR_AC = "ac_room_follow";                      //ac的key
    public static final String STATISTICS_FOLLOW_ANCHOR_ICON_KEY = "room_follow_anchor_icon";       //“key”的value
    public static final String STATISTICS_FOLLOW_ANCHOR_CARD_KEY = "room_follow_anchor_card";       //”key“的value
    public static final String STATISTICS_FOLLOW_ANCHOR_USERID = "anchor_userid";                   //主播id的key
    public static final String STATISTICS_FOLLOW_ANCHOR_LIVEID = "anchor_liveid";                   //房间id的key
    public static final String STATISTICS_FOLLOW_ANCHOR_DURATION = "anchor_duration";               //时长id的key

    public static final String KEY_CHANNEL_CLICK = "channel_click_%s";                                          //频道点击 %cId
    public static final String KEY_CHANNEL_LOOKING = "channel_looking_%s";                                      //频道浏览时长 %cId
    public static final String KEY_CHANNEL_SUB_CLICK = "channel_sub_click_%s-%s";                               //频道 %cId_%sublistId
    public static final String KEY_CHANNEL_SUB_LOOKING = "channel_sub_looking_%s-%s";                           //频道 %cId_%sublistId

    public static final String KEY_CHANNEL_FEEDS_DETAIL_CLICK = "channel_feeds_detail_click_%s-%s-%s-%s";       //频道 %cId_%sublistId_%sectionId_%feedsId
    public static final String KEY_CHANNEL_FEEDS_DETAIL_LOOKING = "channel_feeds_detail_looking_%s-%s-%s-%s";   //频道 %cId_%sublistId_%sectionId_%feedsId
    public static final String KEY_CHANNEL_FEEDS_VIDEO_CLICK = "channel_feeds_video_click_%s-%s-%s-%s";         //频道 %cId_%sublistId_%sectionId_%feedsId
    public static final String KEY_CHANNEL_FEEDS_VIDEO_PLAYING = "channel_feeds_video_looking_%s-%s-%s-%s";     //频道 %cId_%sublistId_%sectionId_%feedsId

    public static final String KEY_CHANNEL_LIVE_CLICK = "channel_live_click_%s-%s-%s-%s";                       //频道 %cId_%sublistId_%sectionId_%liveId
    public static final String KEY_CHANNEL_LIVE_LOOKING = "channel_live_looking_%s-%s-%s-%s";                   //频道 %cId_%sublistId_%sectionId_%liveId
    public static final String KEY_CHANNEL_PLAYBACK_CLICK = "channel_playback_click_%s-%s-%s-%s";               //频道 %cId_%sublistId_%sectionId_%liveId
    public static final String KEY_CHANNEL_PLAYBACK_LOOKING = "channel_playback_looking_%s-%s-%s-%s";           //频道 %cId_%sublistId_%sectionId_%liveId

    public static final String KEY_CHANNEL_H5_CLICK = "channel_h5_click_%s-%s-%s-%s";

    public static final String KEY_TOURIST_CHANNEL_CLICK = "tourist-visit-anon-view-%s";                //游客模式下频道点击 %cId
    public static final String KEY_TOURIST_CHANNEL_LOOKING = "tourist-time-anon-view-%s";               //游客模式下频道浏览时长 %cId
    public static final String KEY_TOURIST_SHOW_LOGIN_VIEW = "tourist-jump-anon-view-%s";         //游客模式下显示登录弹层的次数 %cid-%sectionId-%tab-%scheme-%others
    public static final String KEY_TOURIST_HIDE_LOGIN_VIEW = "tourist-jump-anon-cancel";                //游客模式下隐藏登录弹层的次数
    public static final String KEY_TOURIST_LOG_MISSOAUTHTOKEN_SUCCESS = "tourist-log-missoauthtoken-success"; //小米sso登录授权成功量
    public static final String KEY_TOURIST_LOG_MISSOAUTHTOKEN_FAIL = "tourist-log-missoauthtoken-fail"; //小米sso登录授权成功量
    public static final String KEY_TOURIST_TIME_MISSOAUTHTOKEN = "tourist-time-missoauthtoken-view";    //小米sso登录授权时长
    public static final String KEY_TOURIST_TIME_MISSO_VIEW = "tourist-time-misso-view"; //小米sso登录时长

    public static final String KEY_CHANNEL_CLICK_NOTICE = "channel_click_notice_%s_%s";

    public static final String KEY_INTO_ROOM_ANIM_COUNT = "intoroom_anim_count";//进入房间提示动画打点
    public static final String KEY_INTO_ROOM_CLICK_COUNT = "intoroom_anim_click_count"; //点击进入房间提示动画打点

    //用户召回打点数据上报
    public static final String USER_RECALL_AC = "ac_push";
    //推送的时间戳（秒）
    public static final String USER_RECALL_PUSH_TIME = "user_recall_push_time";
    //推送的id
    public static final String USER_RECALL_PUSH_ID = "user_recall_push_ID";
    /**
     * 和服务器协商的数据类型
     * 针对运营push可能有不同的投放策略
     * 主要以固定时间的热门活动push或者针对用户的个性化的push
     * 因此push的策略会有几种，目前暂定以下3种
     * <p>
     * 0 : 固定时间
     * 1 : 兴趣主播开播时间
     * 2 : 新用户安装时间
     */
    public static final String USER_RECALL_PUSH_CATEGORY = "user_recall_push_category";
    //标识用户是否点击过，点击过为1,未点击（notification被划掉）为0
    public static final String USER_RECALL_PUSH_CLICK_COUNT = "user_recall_push_click_count";
    //用户通过点击notification进入直播间后观看的时间长度
    public static final String USER_RECALL_PUSH_WATCH_TIME = "user_recall_push_watch_time";
    public static final String TS = "ts";

    public static final String KEY_EXPOSE_LIVE_ID = "livepv_%s";
    public static final String KEY_CLICK_LIVE_ID = "liveclick_%s";

    //直播+打点
    public static final String KEY_PLUS_IMAGE_SHARE = "plus_image_share";
    public static final String KEY_PLUS_VIDEO_SHARE = "plus_video_share";
    public static final String KEY_PLUS_VIDEO_TIME = "plus_video_time";
    public static final String PLUS_VIDEO_PLAYTIME = "plus_video_playtime";

    //////////////////
    ////口令直播打点////
    //////////////////
    private static final String KEY_PASSWORD_LIVE = "password_live";
    /**
     * 主播在开始直播时分享
     */
    public static final String KEY_PASSWORD_LIVE_BEGIN_SHARE = KEY_PASSWORD_LIVE + "_begin_share";
    /**
     * 主播/观众在直播间的分享
     */
    public static final String KEY_PASSWORD_LIVE_ROOM_SHARE = KEY_PASSWORD_LIVE + "_room_share";
    /**
     * 直播结束页主播分享
     */
    public static final String KEY_PASSWORD_LIVE_END_SHARE = KEY_PASSWORD_LIVE + "_end_share";
    /**
     * 主播动态页分享
     */
    public static final String KEY_PASSWORD_LIVE_FEED_SHARE = KEY_PASSWORD_LIVE + "_feed_share";
    /***/
    public static final String KEY_PASSWORD_LIVE_REPLAY_SHARE = KEY_PASSWORD_LIVE + "_replay_share";

    public static final String KEY_DOWNLOAD_IMG = "download_img";//fresco 下载图片的回调

    //抽奖打点
    //最小化按钮
    public static final String KEY_LOTTERY_VIEW_GROUP_HIDE_BUTTON = "20160817_minimize";
    //刷礼物抽取按钮
    public static final String KEY_START_GIFT_LOTTERY_BUTTON = "20160817_take_gift";
    //观众抽取按钮
    public static final String KEY_START_SIMPLE_LOTTERY_BUTTON = "20160817_take_simple";
    //刷屏抽取抽取按钮1
    public static final String KEY_START_BARRAGE1_LOTTERY_BUTTON = "20160817_take_barrage1";
    //刷屏抽取抽取按钮2
    public static final String KEY_START_BARRAGE2_LOTTERY_BUTTON = "20160817_take_barrage2";
    //刷屏抽取抽取按钮3
    public static final String KEY_START_BARRAGE3_LOTTERY_BUTTON = "20160817_take_barrage3";
    //中奖名单按钮
    public static final String KEY_LOTTERY_LIST_BUTTON = "20160817_winnerlist";
    //中奖管理按钮
    public static final String KEY_LOTTERY_LIST_MANAGER_BUTTON = "20160817_winners";
    //私信按钮
    public static final String KEY_LOTTERY_LIST_CHAT_BUTTON = "20160817_winnerlist_mes";
    //刷屏抽奖参与人数
    public static final String KEY_LOTTERY_BARRAGE_CHANCE_NUM = "20160817_barrage_chance-";
    //话题推荐请求打点
    public static final String KEY_TOPIC_REQUEST_SUCCESS = "topic_request_success";
    public static final String KEY_TOPIC_REQUEST_SUM = "topic_request_sum";
    //连接外设打点
    public static final String KEY_LINK_DEVICE_CONTINUE = "dev_con_clicked";
    public static final String KEY_LINK_DEVICE_SHARE = "dev_share_and_continue";
    public static final String KEY_LINK_DEVICE_SUCCESS = "dev_con_suc";
    public static final String KEY_LINK_DEVICE_WIFI_STATE = "dev_con_wifi_info";
    public static final String KEY_LINK_DEVICE_EN = "dev_con_enterprise_network";

    public final static String KEY_PROFILE_PLAYBACK_CLICK = "profile_playback_click_%s";//profile_playback_click_%liveid
    public final static String KEY_PROFILE_PLAYBACK_LOOKING = "profile_playback_looking_%s";//profile_playback_looking_%liveid
    public final static String KEY_FEEDS_PLAYBACK_CLICK = "feeds_playback_click_%s";//feeds_playback_click_%liveid
    public final static String KEY_FEEDS_PLAYBACK_LOOKING = "feeds_playback_looking_%s";//feeds_playback_looking_%liveid
    public final static String KEY_TOPIC_PLAYBACK_CLICK = "topic_playback_click_%s";//topic_playback_click_%liveid
    public final static String KEY_TOPIC_PLAYBACK_LOOKING = "topic_playback_looking_%s";//topic_playback_looking_%liveid

    // 关于私信以及直播开播提醒push设置打点
    public static final String KEY_LIVE_PUSH_CONTROL = "live_push_control-";
    public static final String KEY_LIVE_PUSH_CONTROL_TIME = "live_push_control_time";
    public static final String KEY_DISTURB = "disturb_control-";
    public static final String KEY_DISTURB_CONTROL_TIME = "disturb_control_time";
    public static final String KEY_SOUND_CONTROL = "sound_control-";
    public static final String KEY_SOUND_CONTROL_TIME = "sound_control_time";
    public static final String KEY_VIBRATION_CONTROL = "vibration_control-";
    public static final String KEY_VIBRATION_CONTROL_TIME = "vibration_control_time";
    public static final String KEY_DETAILS_CONTROL = "details_control-";
    public static final String KEY_DETAILS_CONTROL_TIME = "details_control_time";

    public static final String KEY_UPLOAD_USER_INFO_SUCCESS = "upload_user_info_success";
    public static final String KEY_UPLOAD_USER_INFO_FAIL = "upload_user_info_fail";


    //运营活动打点
    public static final String KEY_OPERATIVE_ACTIVITY_SHARE = "operation-%s-%s-share"; // 运营轰动分享次数
    public static final String KEY_OPERATIVE_CLICK = "operation-%s-%s-click"; //运营活动点击次数

    //连麦视频质量打点
    public static final String DATA_ENGINE_CONNECT_DETECT_TIME = "engine_connect_detect_time";//引擎跑马时间
    public static final String DATA_ENGINE_BYTE_RATE = "engine_byte_rate";
    public static final String DATA_ENGINE_AVG_RTT = "engine_avg_rtt";
    public static final String DATA_ENGINE_LOST_RATE = "engine_lost_rate";
    public static final String DATA_ENGINE_LOST_RATE_LIST = "engine_lost_rate_list";
    public static final String DATA_ENGINE_AVG_RTT_LIST = "engine_avg_rtt_list";
    public static final String DATA_ENGINE_AVG_FRAME_LIST = "engine_avg_frame_list";

    //商城打点
    public static final String KEY_USER_CLICK_SCAN_MALL = "storemall_scan-click_%s_%s-%s-%s-%s-%s-%s-%s"; // 观众在列表页点击商品
    public static final String KEY_ANCHOR_ADD_MALL = "storemall_add-click_%s-%s-%s-%s-%s-%s"; //主播添加商品
    public static final String KEY_USER_BUY_TB_MALL = "storemall_bought-click_%s_%s-%s-%s-%s-%s-%s-%s"; //观众购买商品成功，淘宝回调，有时候不回调
    public static final String KEY_USER_CLICK_OPERATION = "product-list_url_click_%s_%s_%s_%s_%s"; //商品了表运营位观众点击打点
    public static final String KEY_USER_CLICK_SHOP = "shopicon_click_%s-%s-%s"; //商品了表运营位观众点击打点

    //预告feeds部分打点
    public static final String KEY_FEEDS_CLICK_SHOW_DETAIL = "feeds-%s-%s-click-%s"; //动态阅读数, feeds-from-t1-click-feedid 例：tx代表类型, 如t1是图片
    public static final String KET_FEEDS_DETAIL_COMMENT = "feeds-%s-comment-%s"; //动态评论数, feeds-t1-comment-feedid, tx代表类型, 如t1是图片
    public static final String KEY_FEEDS_DETAIL_SHARE = "feeds-%s-%s-share-%s"; //动态分享数, feeds-t1-wechat-share-feedid: tx代表类型, 如t1是图片
    public static final String KEY_FEEDS_DETAIL_LIKE = "feeds-%s-praise-%s";//动态点赞数, feeds-t1-praise-feedid:tx代表类型, 如t1是图片
    public static final String KET_FEEDS_DETAIL_TIMES = "feeds-%s-%s-times-%s";//动态详情页停留时长, feeds-from-t1-times-feedid, tx代表类型, 如t1是图片
    public static final String KEY_ENTER_FEEDS_FROM_PERSON_PAGE = "f1"; //从个人主页进入
    public static final String KET_ENTER_FEEDS_FROM_FEEDS_LIST = "f2";  //从动态列表页进入
    public static final String KEY_ENTER_FEEDS_FROM_CHANNEL = "f3";  //从频道页进入
    public static final String KEY_TYPE_FEEDS_PIC = "t1";//图片
    public static final String KET_TYPE_FEEDS_VIDEO = "t2";//小视频
    public static final String KEY_TYPE_FEEDS_PLAYBACK = "t3";//回放
    public static final String KEY_TYPE_FEEDS_JOURNAL = "t4";       //feeds日志, 多图多视频

    public static final String KEY_NOTICE_FOLLOW_UID_TITLE = "notice-follow-%d-%d-%s";
    //分享打点
    //liveshare_click_uid-被分享主播id-分享者id-内容id（直播live%iveid, 回放pb%liveid,详情页feeds%feedid）-被分享的位置来源（feeds 1,个人主页 2，频道 3、搜索框 4)
    //-频道id-直播状态（前1，中2，后3）-分享渠道（wx,moment,qq,qzone,weibo,facebook,twitter）-被分享文案的小尾巴id
    //(不存在的用k表示）
    //liveshare_click_uid-hostid-sharerid-contentid-locationid-channelid-livestateid-sourceid-copyid
    public static final String KEY_LIVE_SHARE_CLICK = "liveshare_click_%s-%s-%s-%s-%s-%s-%s-%s-%s";
    //滤镜打点
    public static final String KEY_FILTER_CLICK = "filter_%s_click";

    //电视台打点
    public static final String KEY_TV_CHANNEL = "tvchannel-%s-%s-%s-%s";

    public static final String KEY_OPEN_LIVE_SOURCE = "open_live_source-%s";

    public static final String KEY_HIDE_WIN = "hide_win";
    public static final String KEY_AUDIO_CON = "audio_con";
    public static final String KEY_VIDEO_CON = "video_con";

    // 当app的系统通知权限被关闭时，上报点
    public static final String KEY_REMOTE_PUSH_DISENABLE = "remote_push_disenable";

    //　loading 页相关打点
    public static final String KEY_LOADING_GET_PICTURE_SUCCESS = "loadingpage-%s-%s-%d-%s";
    public static final String KEY_LOADING_SHOW_DEFAULT = "loadingpage-default-%s";
    public static final String KEY_LOADING_CLICK_SKIP_BTN = "loadingpage-skip-click-%s-%s-%d";
    public static final String KEY_LOADING_SKIP_AUTO = "loadingpage-skip-%s-%s-%d";
    public static final String KEY_LOADING_CLICK_PICTURE = "loadingpage-pic-%s-%s-%d-%s-%s";

    //获取美颜,滤镜参数成功与否打点
    public static final String KEY_FETCH_BEAUTY_FILTER_SUCCESS = "fetch_beauty_filter_success";
    public static final String KEY_FETCH_BEAUTY_FILTER_FAILED = "fetch_beauty_filter_failed";
    public static final String KEY_FETCH_KARAOK_PARAMS_SUCCESS = "fetch_karaok_params_success";
    public static final String KEY_FETCH_KARAOK_PARAMS_FAILED = "fetch_karaok_params_failed";

    //门票直播打点 http://jira.n.xiaomi.com/browse/LIVEAND-6657
    public static final String KEY_ICKETLIVE_ENTRANCE_CLICK = "ticketlive-entrance-click-%d";//门票直播入口的点击
    public static final String KEY_TICKETLIVE_MONEY_CLICK = "ticketlive-money-%d-click-%d-%d";//门票直播设置金额和试看
    public static final String KEY_TICKETLIVE_FREE_TOBUY = "ticketlive-free-tobuy-click-%d-%s";//直播试看条中购买门票按钮的点击
    public static final String KEY_TICKETLIVE_RECHARGE_CLICK = "ticketlive-%s-recharge-click-%d-%s";//直播试看的支付页面充值的点击(包括回放)
    public static final String KEY_TICKETLIVE_FREEOVER_RECHARGE_CLICK = "ticketlive-freeover-recharge-click-%d-%s";//直播试看结束后购票蒙层中充值的点击
    public static final String KEY_TICKETLIVE_FREEOVER_TOBUY_CLICK = "ticketlive-freeover-tobuy-click-%d-%s";//直播试看结束购票蒙层中购票进场按钮的点击
    public static final String KEY_TICKETLIVE_FREE_PAGE_TOBUY_CLICK = "ticketlive-free-page-tobuy-click-%d-%s";//直播试看支付页面中购票进场按钮的点击
    public static final String KEY_TICKETLIVE_SEETIMES = "ticketlive-seetimes-%d-%s";//直播观看时长
    public static final String KEY_TICKETLIVE_REPLAY_SEETIMES = "ticketlive-replay-seetimes-%d-%s";//回放观看时长
    public static final String KEY_TICKETLIVE_CLICK = "ticketlive-click-%d-%s";//直播观看次数
    public static final String KEY_TICKETLIVE_REPLAY_CLICK = "ticketlive-replay-click-%d-%s";//回放观看次数
    public static final String KEY_TICKETLIVE_REPLAY_FREE_TOBUY = "ticketlive-replay-free-tobuy-%d-%s";//回放试看条中购买门票按钮的点击
    public static final String KEY_TICKETLIVE_REPLAY_FREEOVER_RECHARGE_CLICK = "ticketlive-replay-freeover-recharge-click-%d-%s";//回放试看结束后蒙层中充值的点击
    public static final String KEY_TICKETLIVE_REPLAY_FREEOVER_TOBUY_CLICK = "ticketlive-replay-freeover-tobuy-click-%d-%s";//回放试看结束后蒙层中购票进场按钮的点击
    public static final String KEY_TICKETLIVE_REPLAY_PAGE_TOBUY_CLICK = "ticketlive-replay-page-tobuy-click-%d-%s";//回放试看支付页面中购票进场按钮的点击

    /**
     * <a href="http://jira.n.xiaomi.com/browse/LIVEAND-7125">充值数据打点</a>
     */
    public interface Recharge {
        public interface PayWay {
            String WeiXin = "wx";
            String Alipay = "zfb";
            String MiWallet = "xm";
            String GoogleWallet = "gp";
            String PayPal = "pa";
            String Mibi = "mibi";
            String CodaIdr = "codaidr";
            String CodaAtm = "codaatm";

        }

        String RECHARGE_FROM = "recharge_from";

        int FROM_OTHER = 0;
        int FROM_ROOM = 1;
        int FROM_SETTING = 2;


        String VISIT = "gem_pay-view";//钻石充值页面的pv
        String PRICE_LIST = "gem_pay-%s";//各支付渠道列表的展示次数
        String CLICK_PAY_BTN = "gem_pay-%s-topay";//各支付渠道点击立即付款的次数
        String EXCEED_SINGLE_DEAL_QUOTA = "gem_pay-%s-exceed";//微信、小米钱包不支持3000的支付页面pv
        String EXCEED_SINGLE_DEAL_QUOTA_ADJUST = "gem_pay-%s-exceed-adjust-%s";//微信、小米钱包不支持3000的支付页面点击调整金额
        String EXCEED_SINGLE_DEAL_QUOTA_TO_ZFB = "gem_pay-%s-exceed-tozfb";//微信、小米钱包不支持3000的支付页面点击去支付宝
        String CANCEL = "gem_pay-%s-cancel";//各支付页面取消付款的次数(支付页面点击左上角返回 & 物理键返回)
        String APP_NOT_INSTALL = "gem_pay-%s-no";//微信、支付宝未安装app 的PV
        String PAY_ERROR_CODE = "gem_pay-%s-error-%s";//支付失败errorcode统计
        String SUCCESS = "gem_pay-%s-paysucc";//支付成功
    }

    // 匿名登录打点--产品
    public static final String KEY_TOURIST_LOGIN_SUCCESS = "tourist-log-%s-success";
    public static final String KEY_TOURIST_LOGIN_FAILED = "tourist-log-%s-fail";
    public static final String KEY_TOURIST_LOGIN_CANCEL = "tourist-log-%s-cancel";
    public static final String KEY_TOURIST_LOGIN_WX = "tourist-log-wx-click";
    public static final String KEY_TOURIST_LOGIN_QQ = "tourist-log-qq-click";
    public static final String KEY_TOURIST_LOGIN_MILIAO = "tourist-log-mi-click";
    public static final String KEY_TOURIST_LOGIN_WEIBO = "tourist-log-wb-click";
    public static final String KEY_TOURIST_LOGIN_FACEBOOK = "tourist-log-fb-click";
    public static final String KEY_TOURIST_LOGIN_GOOGLE = "tourist-log-go-click";
    public static final String KEY_TOURIST_LOGIN_INSTAGRAM = "tourist-log-instagram-click";
    public static final String KEY_TOURIST_LOGIN_PHONEID = "tourist-log-phoneid-click";
    public static final String KEY_TOURIST_VISIT_PHONE_VIEW = "tourist-visit-phone-view";
    public static final String KEY_TOURIST_LOGIN_PHONE_CLICK = "tourist-log-phone-click"; //对应submit那个点
    public static final String KEY_TOURIST_LOGIN_ID_CLICK = "tourist-log-id-click";
    public static final String KEY_TOURIST_REG_PHONE_CLICK = "tourist-reg-phone-click";
    public static final String KEY_TOURIST_REG_VISIT_VIEW = "tourist-visit-regist-view";
    public static final String KEY_TOURIST_REG_PHONE_SUBMIT = "tourist-reg-phone-submit";
    public static final String KEY_TOURIST_REG_PHONE_SUCCESS = "tourist-reg-phone-success";
    public static final String KEY_TOURIST_REG_PHONE_FAILED = "tourist-reg-phone-fail";
    public static final String KEY_TOURIST_REG_PHONE_CANCEL = "tourist-reg-phone-cancel";
    public static final String KEY_TOURIST_REG_SELECT_COUNTRY_CODE = "tourist_reg_select_country_code";
    public static final String KEY_TOURIST_CHANGE_ID_VIEW = "tourist-change-id-view";
    public static final String KEY_TOURIST_CHANGE_PHONE_VIEW = "tourist-change-phone-view";
    public static final String KEY_TOURIST_PASSWORD_PHONE_CLICK = "tourist-password-phone-click";
    public static final String KEY_TOURIST_PASSWORD_PHONE_VIEW = "tourist-password-phone-view";
    public static final String KEY_TOURIST_PASSWORD_PHONE_CANCEL = "tourist-password-phone-cancel";
    public static final String KEY_TOURIST_PASSWORD_ID_CLICK = "tourist-password-id-click";
    public static final String KEY_TOURIST_PASSWORD_ID_VIEW = "tourist-password-id-view";
    public static final String KEY_TOURIST_PASSWORD_ID_CANCEL = "tourist-password-id-cancel";
    public static final String KEY_TOURIST_SNED_REG_CLICK = "tourist-send-regist-click";
    public static final String KEY_TOURIST_VISIT_USERINFO_VIEW = "tourist-visit-userinfo-view";
    public static final String KEY_TOURIST_LOGIN_USERINFO_CLICK = "tourist-log-userinfo-click";
    public static final String KEY_TOURIST_UPLOAD_USERINFO_SUCCESS = "tourist-upload-userinfo-success";
    public static final String KEY_TOURIST_UPLOAD_USERINFO_CANCEL = "tourist-upload-userinfo-cancel";
    public static final String KEY_TOURIST_UPLOAD_USERINFO_FAIL = "tourist-upload-userinfo-fail";

    //匿名登录点--研发分析点
    public static final String KEY_TOURIST_RESET_PWD = "tourist-reset-password";   //整体的忘记密码量
    //帐号没有设置密码
    public static final String KEY_TOURIST_ID_NOT_HAVE_PWD = "tourist-log-id-not-have-pwd";
    //点击重置直播帐号密码提交按钮
    public static final String KEY_TOURIST_RESET_ID_PWD_SUBMIT = "tourist-log-reset-id-pwd-submit";
    //登錄頁面匿名打點
    //点击登录时选择国家地区码按钮
    public static final String KEY_TOURIST_LOGIN_SELECT_COUNTRY_CODE = "tourist-log-select-country-code";
    //点击重置手机号码的密码提交按钮
    public static final String KEY_TOURIST_RESET_PHONE_PWD_SUBMIT = "tourist-log-reset-phone-pwd-submit";
    //点击重置密码时选择国家地区码按钮
    public static final String KEY_TOURIST_RESET_SELECT_COUNTRY_CODE = "tourist-log-reset-select-country-code";
    //点击重置密码时发送验证码按钮-找回密码findpasswordFragment
    public static final String KEY_TOURIST_RESET_SEND_CAPTCHA = "tourist-log-reset-send-captcha";

    //意见反馈的成功率
    public static final String FEED_BACK_RESULT = "feed_back_result";

    //观看记录打点
    public static final String KEY_LOOK_RECORD_CLICK = "look_record-click";
    public static final String KEY_LOOK_RECORD_ZUID_SORT = "look_record-%s-%s";
    public static final String KEY_LOOK_RECORD_FOLLOW_USERID_SORT = "look_record-follow-%s-%s";

    //排行榜打点
    public static final String KEY_ENTER_RANK = "name_list-view";  // 进入排行榜
    public static final String KEY_CLICK_RANK_PAGE_ID_SUBID = "name_list-%d-%d-view";  // 点击排行榜具体页面

    public static final String KEY_FEEDS_TAB_POP = "feeds_tab_pop-%d-%d-%d-%s";                     //动态列表页弹层出现的次数
    public static final String KEY_FEEDS_TAB_POP_FOLLOW = "feeds_tab_pop_follow-%d-%s";             //关注推荐窗中的点击关注的次数
    public static final String KEY_FEEDS_TAB_POP_CLICK = "feeds_tab_pop_click-%d-%s";               //关注推荐窗中的点击的次数
    public static final String KEY_FEEDS_TAB_POP_MORE = "feeds_tab_pop_more-%s";                    //关注推荐窗中点击更多选项的次数
    public static final String KEY_FEEDS_TAB_POP_FOLLOW_COUNT = "feeds_tab_pop_follow_count-%s";    //关注推荐窗中的点击关注的数量
    public static final String KEY_FEEDS_TAB_POP_CLOSE = "feeds_tab_pop_close-%s";                  //关注推荐窗中点击叉的次数

    public static final String KEY_FEEDS_TAB_EMPTY = "feeds_tab_empty-%d-%d-%d-%d-%s";              //空页面的展示次数
    public static final String KEY_FEEDS_TAB_EMPTY_AVATAR = "feeds_tab_empty_avatar-%d-%s";         //空页面点击图标
    public static final String KEY_FEEDS_TAB_EMPTY_BTN = "feeds_tab_empty_btn-%s";                  //空页面点击去推荐列表按钮

    public static final String KEY_FEEDS_TAB_RECOMMENT_CLICK = "feeds_tab_recommend_click-%d-%s";   //推荐列表的点击
    public static final String KEY_FEEDS_TAB_RECOMMENT_FOLLOW = "feeds_tab_recommend_follow-%d-%s"; //推荐主播页关注的次数
    public static final String KEY_FEEDS_TAB_RECOMMENT_FOLLOW_COUNT = "feeds_tab_recommend_follow_count-%s";//推荐列表关注的数量

    //livesdk打点
    public static final int KEY_LIVESDK_MSG_GIFT = 1;
    public static final int KEY_LIVESDK_MSG_SYSTEM = 2;
    public static final int KEY_LIVESDK_MSG_CHAT = 3;
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_SILENT = "plug_flow-click-silent-%s";   //静音按钮点击来源
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_GODESK = "plug_flow-click-godesk-%s";   //回到桌面按钮点击
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_CLOSE = "plug_flow-click-close-%s";   //关闭直播按钮点击
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_SET = "plug_flow-click-set-%s";   //设置按钮点击
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_SET_SPECIFIC = "plug_flow-click-set-%d-%s";   //设置按钮点击（也要分别统计礼物、系统消息、聊天内容点击）
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_SHARE = "plug_flow-click-share-%s";   //分享按钮点击（还要统计到具体的分享项）
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_SCREEN = "plug_flow-click-screen-%s";   //截屏按钮点击
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_CONTROLDESK = "plug_flow-click-controldesk-%s";   //回到控制台按钮点击
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_SENDMESSAGE = "plug_flow-click-sendmessage-%s";   //主播发送消息的次数
    public static final int KEY_LIVESDK_FREEZE_FIXED = 0;
    public static final int KEY_LIVESDK_FREEZE_UNFIXED = 1;
    public static final String KEY_LIVESDK_PLUG_FLOW_CLICK_FREEZE = "plug_flow-click-freeze-%d-%s";//固定悬浮窗打点  点击一次打一次  (固定住%d=0；可移动%d=1)

    // 游戏推荐打点
    public static final String KEY_GAME_ICON_SHOW = "gameSDK-show-%s-%s";
    public static final String KEY_GAME_ICON_CLICK = "gameSDK-click-%s-%s";
    public static final String KEY_GAME_DOWNLOAD_CLICK = "gameSDK-download-%s-%s";

    // 游戏标签时长
    public static final String KEY_GAME_TAG_TIME = "tag-sdk-%s-times";
}
