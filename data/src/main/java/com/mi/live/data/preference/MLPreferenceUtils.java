
package com.mi.live.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.R;

import java.util.Vector;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * @module preference
 * <p>
 * Created by MK on 15/8/12.
 */
public abstract class MLPreferenceUtils extends PreferenceUtils {
    private static final String TAG = MLPreferenceUtils.class.getSimpleName();

    //通知栏是否聚合显示
    public static final String PREF_NOTIFY_SETTING_FOLD = "notify_fold_detail";

    public static final String PREF_KEY_ATTACHMENT_BASE_ID = "pref_key_attachment_base_id"; // 生成attachment_id

    public static final String KEY_LAST_WALL_TS = "last_wall_ts";

    public static final String KEY_LAST_RELATION_TS = "last_relation_ts";

    public static final String KEY_LAST_GLOBAL_USER_TS = "last_global_user_ts";

    public static final String KEY_LAST_SUBSCRIPTION_TS = "last_subscription_ts";

    public static final String KEY_LAST_SETTING_TS = "last_setting_ts";

    public static final String KEY_LAST_SIXIN_TS = "last_sixin_ts";

    public static final String KEY_LAST_NOTIFICATION_CENTER = "last_notification_center";

    public static final String KEY_LAST_MUC_TS = "last_muc_ts";

    public static final String STATUS = "status";

    public static final String KEY_PHONE_ID = "phone_id";

    public static final String KEY_PHONE_NUMBER = "phone_num";

    private static final String KEY_PING_STARTED = "ping_started";

    private static final String KEY_USER_LOGOFF = "pref_user_logoff";

    private static final String KEY_WRONG_PASSWORD = "pref_wrong_password";

    public static final String KEY_WALL_NOTIFICATION = "pref_broadcast_notification";

    public static final String KEY_MSG_NOTIFICATION = "pref_conversion_notification";

    public static final String KEY_SIXIN_NOTIFICATION = "sixin_remind_setting";

    public static final String KEY_MUC_NOTIFICATION = "pref_muc_notification";

    public static final String KEY_MUC_LAST_NOTIFY_TIME = "pref_muc_last_notify_time";

    public static final String KEY_NEW_NOTI_MSG_NOTIFICATION = "pref_new_noti_msg_notification";

    public static final String KEY_REC_MESSAGE_ENABLE = "rec_msg_enable"; // 推荐小助手开关

    public static final String KEY_REC_ALERT_ENABLE = "rec_alert_enable"; // 推荐小助手提醒开关

    public static final String KEY_PHONE_REGISTERRED = "pref_phone_register";

    public static final String KEY_GROUP_TIPS_IN_COMP = "pref_gp_tips_comp";

    public static final String KEY_GROUP_TIPS_IN_MANAGER = "pref_gp_tips_gpm";

    public static final String KEY_GROUP_TIPS_IN_GRAFFITI = "pref_grf_tips";

    public static final String KEY_RECOMMEND_FRIEND_TIPS_IN_CONVERSATION_LIST = "pref_conv_list_rf_tips";

    public static final String KEY_RELEASE_CHANNEL = "pref_channel";

    public static final String KEY_CREATE_REMIND_SHORTCUT = "pref_create_remind_shortcut";

    public static final String KEY_CREATE_TOPIC_SHORTCUT = "pref_create_topic_shortcut";

    public static final String KEY_CREATE_LBS_SHORTCUT = "pref_create_lbs_shortcut";

    public static final String KEY_CREATE_GIF_SHORTCUT = "pref_create_gif_shortcut";

    public static final String KEY_CREATE_BIRTHDAYWALL_SHORTCUT = "pref_create_birthdaywall_shortcut";

    public static final String KEY_CREATE_AVATAR_SHORTCUT = "pref_create_avatar_shortcut";

    public static final String KEY_CREATE_WISHTREE_SHORTCUT = "pref_create_wishtree_shortcut";

    public static final String KEY_CREATE_FINDLUCK_SHORTCUT = "pref_create_findluck_shortcut";

    public static final String KEY_CREATE_CONSTELLATION_SHORTCUT = "pref_create_constellation_shortcut";

    public static final String KEY_CREATE_PK_SHORTCUT = "pref_create_pk_shortcut";

    public static final String KEY_CREATE_MUSIC_SHORTCUT = "pref_create_music_shortcut";

    public static final String KEY_CREATE_CARDS_SHORTCUT = "pref_create_cards_shortcut";

    public static final String KEY_CREATE_LUCK_TEST_SHORTCUT = "pref_create_luck_test_shortcut";

    public static final int PHONE_REGISTERED = -1;

    public static final int PHONE_REGISTER_TRY_THRESHOLD = 3;

    public static final String KEY_SYNC_FAILED_KEY = "pref_failed_key";

    public static final int NOTIFICATION_NONE = 0;

    public static final int NOTIFICATION_SOUND = 1;

    public static final int NOTIFICATION_VIBRATE = 2;

    public static final int NOTIFICATION_SOUND_VIBRATE = 3;

    public static final int NOTIFICATION_BUBBLE = 4;

    public static final String PREF_MSG_WIFI_TO_SIM = "pref_msg_wifi_to_sim";

    public static final String PREF_SIM_MSG_WITH_MILIAO_SUFFIX = "pref_sim_msg_with_miliao_suffix";

    public static final String PREF_NOTIFICATION_ENABLED = "pref_key_enable_notifications";

    public static final String PREF_NOTIFICATION_COLOR = "pref_key_notification_color";

    public static final String PREF_NOTIFICATION_VIBRATE = "pref_key_vibrate";

    public static final String PREF_NOTIFICATION_VIBRATE_WHEN = "pref_key_vibrateWhen";

    public static final String PREF_NOTIFICATION_RINGTONE = "pref_key_ringtone";

    public static final String PREF_QUIT_CONFIRM = "pref_quit_confirm";

    public static final String PREF_ENTER_SEND_MSG = "pref_enter_send_msg";

    public static final String PREF_USER_EARPIECE = "pref_use_earpiece";

    public static final String PREF_QUIT_LOGOFF = "pref_quit_logoff"; // true
    // 退出不接受消息；
    // false
    // 退出接受消息

    public static final String PREF_QUIT_MILIAO_TIME = "pref_quit_miliao_time";

    public static final String PREF_DOWNLOAD_WALL_LATEST_TIME = "pref_download_wall_latest_time";

    public static final String PREF_DOWNLOAD_MUC_VOTE_LATEST_TIME = "pref_download_muc_vote_latest_time";

    public static final String PREF_DOWNLOAD_MUSIC_LATEST_TIME = "pref_download_music_latest_time";

    public static final String PREF_CLEAR_AVATAR_CACHE = "pref_clear_avatar";

    public static final String PREF_CLEAR_SDCARD = "pref_clean_up_sdcard";

    public static final String PREF_SYNC_CONTACT = "pref_sync_contact";

    public static final String PREF_CONNECTION_MODE = "pref_connection_mode";

    public static final String PREF_FRIEND_SETTING = "friend_settings";

    public static final String PREF_SYNC_FRINED = "pref_sync_friend";

    public static final String PREF_SYNC_SUBSCRIBE = "pref_sync_subscribe";

    public static final String PREF_SYNC_MUC = "pref_sync_muc";

    public static final String PREF_ENABLE_MANUAL_MODE = "pref_enable_manual_mode";

    @Deprecated
    public static final String PREF_UNREAD_NOTIFICATION_IDS = "pref_unread_notification_ids";

    public static final String PREF_UNREAD_FRIEND_REQUEST_NOTIFICATION_IDS = "pref_unread_friend_request_notification_ids";

    public static final String PREF_UNREAD_OTHER_NOTIFICATION_IDS = "pref_unread_other_notification_ids";

    @Deprecated
    public static final String KEY_NIGHT_MODE = "pref_night_mode";

    public static final String KEY_FIND_UNI_CLASSMATES = "find_uni_classmates";

    public static final String KEY_FIND_SENIOR_CLASSMATES = "find_senior_classmates";

    public static final String KEY_FIND_WORKER = "find_worker";

    public static final String PREF_SNS_INVITE_CONFIRM = "pref_sns_invite_confirm";

    public static final String KEY_SMILEY_LAST_TAB = "smiley_last_tab";

    // 是否是非匿名帐号。默认值为false。
    public static final String PREF_NON_ANONYMOUS_ACCOUNT = "pref_non_anonymous_account";

    public static final String KEY_VERIFICATION_INITED = "pref_verify_inited";

    public static final String KEY_VERIFICATION_NEEDED = "pref_validation";

    public static final String KEY_FRIEND_VERIFY = "pref_fri_varify";

    public static final String KEY_SEARCH_TYPE = "pref_search_type";

    public static final String KEY_SUGGESTABLE = "pref_suggestable";

    public static final String KEY_MATCH_CONTACT = "pref_match_contacts";

    public static final String KEY_FIRST_START_SYNC_CONTACT = "pref_first_start_sync_contacts";

    public static final String KEY_JOINED_GROUP = "pref_joined_group";// 加入群对所有人可见

    public static final String KEY_INVITED_GROUP = "pref_invited_group";// 好友可以直接把我拉入群

    public static final String KEY_IS_MIUI_USER = "is_miui_user";

    public static final String KEY_PUBLIC_ALBUM_SET = "pref_public_album_set";

    public static final String KEY_VISIBLE_FOR_NEARBY = "pref_visible_for_nearby";

    // 指示是否对米聊的软件的安装进行了打点
    public static final String KEY_IS_MILIAO_INSTALL_LOG = "is_miliao_install_log";

    public static final String KEY_DISCOVERY_STATUS = "discovery_status";

    /**
     * 活动三个标志位，一个是是否参加过了活动 KEY_SPREAD_HAS_JIONED，
     */
    public static final String KEY_SPREAD_HAS_JIONED = "spread_has_joined";

    /**
     * 是否需要在ConversationList显示Notice
     */
    public static final String KEY_SPREAD_NOTICE_NEED_SHOW = "spread_notice_need_show";

    /**
     * 是否已经在米聊小秘书中加入了两条活动相关的信息
     */
    public static final String KEY_SPREAD_SECRETY_MES_INSERTED = "spread_sectory_mes";

    /**
     * 是否现在是活动期间, 现在换成KEY_SPREAD_IN_SESSION_B
     */
    @Deprecated
    public static final String KEY_SPREAD_IN_SESSION = "spread_in_session";

    public static final String KEY_SPREAD_IN_SESSION_B = "spread_in_session_b";

    /**
     * 当前的活动宣传语
     */
    public static final String KEY_SPREAD_TEXT = "spread_main_text";

    public static final String KEY_SPREAD_TRIGGERED = "spread_triggered";

    public static final String KEY_SPREAD_JOINED_ID = "spread_joined_id";

    public static final String KEY_LAST_IP_TS = "discovery_ts_ip_";

    public static final String KEY_LAST_MAC_TS = "discovery_ts_mac_";

    public static final String KEY_LAST_GLOBAL_TS = "last_global_ts";

    public static final String KEY_LAST_ADS_TS = "last ads_ts";

    public static final String KEY_LAST_OPEN_TS = "last_open_ts";

    public static final String KEY_LAST_OPEN_NOTI_TS = "last_open_noti_ts";

    public static final String KEY_LAST_ADS_READED = "last_ads_readed";

    public static final String KEY_LAST_PPL_TS = "last_ppl_ts";

    public static final String KEY_LAST_MIDIAN_RULE_TS = "last_midian_rule_ts";

    public static final String KEY_TEMP_MIDIAN_RULE_TS = "temp_midian_rule_ts";

    public static final String KEY_IS_SHOW_LOTTERY_RULES = "lottery_rule";

    public static final String KEY_ADVERTISEMENT = "advertisement";

    public static final String KEY_LAST_GET_IPS = "last_get_ips";

    @Deprecated
    public static final String KEY_LAST_NEW_NOTIFICATION_COUNT = "last_new_notification_count";

    public static final String KEY_LAST_NEW_FR_NOTIFICATION_COUNT = "last_new_fr_notification_count";

    public static final String KEY_LAST_NEW_OTHER_NOTIFICATION_COUNT = "last_new_other_notification_count";

    public static final String KEY_LAST_NEW_OTHER_NOTIFICATION_TIMESTAMP = "last_new_other_notification_timestamp";

    public static final String KEY_NEW_MESSAGE_ACCOUNT_NAMES = "key_new_message_account_names"; // 记录新消息的发送者
    public static final String KEY_NEW_MESSAGE_ACCOUNT = "key_new_message_account";

    public static final String KEY_NEW_REALTION_LAST_UPDATE = "pref_new_relation_last_update";

    public static final String KEY_VOIP_MESSAGE_ACCOUNT_NAMES = "key_voip_message_account_names"; // 记录voip消息的发送者
    public static final String KEY_VOIP_MESSAGE_ACCOUNT = "key_voip_message_account";

    public static final String PREF_KEY_COUNTRY = "country";

    public static final String PREF_KEY_NO_MESSAGE = "pref_no_msg";

    public static final String PREF_KEY_NO_MESSAGE_ENABLE = "pref_no_message_enable";

    public static final String PREF_KEY_NO_MESSAGE_START = "pref_no_message_start";

    public static final String PREF_KEY_NO_MESSAGE_END = "pref_no_message_end";

    public static final String PREF_KEY_POP_MSG_ENABLED = "pop_message_enabled";

    public static final String PREF_KEY_PLAY_MODE = "play_mode";

    public static final String PREF_KEY_PLAY_MODE_NEW = "play_mode_new";

    public static final String PREF_KEY_LOG_OFF_SYSTEM_ACCOUNT = "pref_logoff_system_account";

    public static final String PREF_KEY_NEARBY = "pref_nearby";

    public static final String PREF_KEY_TIMESPAN = "pref_timespan";

    public static final String PREF_KEY_LBS_VISIBLE = "pref_key_lbs_visible";

    public static final String PREF_KEY_PULL_GROUP_FAILED = "pref_gp_failed";

    public static final String PREF_KEY_BIND_ACCOUNT = "bind_account";

    public static final String PREF_UPLOAD_ADDRESS_BOOK = "pref_upload_address_books";

    public static final String PREF_MYCARD_TIP_SHOWN = "pref_mycard_tip_shown";

    public static final String PREF_KEY_ACCEPT_REMIND = "pref_accept_remind";

    public static final String PREF_KEY_ACCEPT_SINGLE_REMIND_ENABLE = "pref_accept_single_remind_enable";

    public static final String PREF_KEY_ACCEPT_GROUP_REMIND_ENABLE = "pref_accept_group_remind_enable";

    public static final String PREF_KEY_AVATARS_FIXED = "pref_key_avatars_fixed";

    public static final String PREF_LBS_TOGGLE_RECOMMEND_AREA = "pref_lbs_toggle_recommend_area";

    public static final String PREF_ROBOTS_ADDED = "pref_robots_added";

    @Deprecated
    public static final String PREF_COMPOSE_BKG = "pref_compose_bkg";

    public static final String LAST_MILIAO_SCORES = "Last_Miliao_Scores";

    public static final String CURRENT_MILIAO_SCORES = "Current_Miliao_Scores";

    public static final String NEW_LOTTERY_GAME_DIALOG_SHOW_TIMES = "New_Lottery_Game_Show_Times";

    public static final String PREF_KEY_UNREAD_MSG_COUNT = "pref_unread_msg_count";

    public static final String PREF_KEY_LAUNCHER_SHOW_MSG_COUNT = "launcher_unread_count_enabled";

    public static final String PREF_KEY_LAST_GET_TOKEN = "pref_last_get_token";

    public static final String PREF_KEY_LOTTEY_VOICE = "pref_key_lottery_voice";

    public static final String PREF_KEY_GROUP_LIST_WATER = "pref_key_group_list_water";

    public static final String PREF_KEY_SIXIN_PRIVATE = "sixin_private_setting";

    public static final String PREF_KEY_HAS_CLICK_AT_ITEM = "pref_key_has_click_at_item";// 是否点击过了@的按钮，用于判断是否显示new

    public static final String PREF_KEY_HAS_CLICK_SEND_CARD_ITEM = "pref_key_has_click_send_card_item";// 是否点击过了发送名片的按钮，用于判断是否显示new

    public static final String PREF_KEY_HAS_CLICK_MI_WORLD_NEW = "pref_key_has_click_mi_world_new";

    public static final String PREF_KEY_HAS_INSTALLED_MIBA = "pref_key_has_miba_installed";

    public static final String PREF_KEY_HAS_INSTALLED_REMIND = "pref_key_has_remind_installed";

    public static final String PREF_KEY_HAS_INSTALLED_LOTTERY = "pref_key_has_lottery_installed";

    @Deprecated
    public static final String PREF_KEY_VIDEO_RECORDING_IS_FRONT_CAMERA = "pref_key_video_recording_is_front_camera";

    @Deprecated
    public static final String PREF_KEY_IS_RECORDING_VIDEO_MODE = "pref_key_is_recording_video_mode"; // 当前是否是录制视频模式。

    public static final String PREF_KEY_FIRST_CONNECTION_TIME_IN_DAYTIME = "pref_key_first_connection_time_in_daytime"; // 7：00~22：00米聊首次连接网络

    public static final String KEY_VIDEO_BITRATE_CHOICES = "pref_video_bitrate_choices";

    public static final String PREF_KEY_MAX_SEQ = "pref_max_seq";

    public static final String PREF_KEY_LAST_TIME_CHECK_SD_CARD_SPACE = "pref_key_last_time_check_sd_card_space"; // 上次检查SD卡空间的时间

    public static final String PREF_KEY_DISABLE_VIDEO_RECORDING = "pref_key_disable_video_recording"; // 上次检查SD卡空间的时间

    public static final String PREF_KEY_HAS_PROCESS_OLD_MUC_SMS = "key_has_process_old_muc_sms";// 是否已经处理732版本之前的老消息

    public static final String PREF_KEY_WEBVIEW_FONT_SIZE = "pref_key_webview_font_size"; // webview字体大小

    public static final String PREF_KEY_IS_NEW_SEND_VIDEO = "pref_key_is_new_send_video"; // 是否第一次发送视频对讲

    // 提示总开关
    public static final String PREF_NOTIFY_SETTINGS_NEW_MESSAGE = "notify_settings_new_messgae";

    public static final String PREF_NOTIFY_CHAT_ENABLE = "pref_notify_chat_enable";// 收到对话消息时是否提醒

    public static final String PREF_NOTIFY_MUC_GROUP_ENABLE = "pref_notify_muc_group_enable";// 收到群（大群小群）消息时是否提醒

    public static final String PREF_NOTIFY_SUBCRIBE_ENABLE = "pref_notify_subcribe_enable";// 收到订阅账号消息时是否提醒

    public static final String PREF_NOTIFY_NOTIFICATION_CENTER_ENABLE = "pref_notify_notification_center_enable";// 收到通知中心消息时是否提醒

    public static final String PREF_NOTIFY_WALL_ENABLE = "pref_notify_wall_enable";// 收到广播消息时是否提醒

    public static final String PREF_NOTIFY_SAY_HELLO_ENABLE = "pref_notify_say_hello_enable";// 收到打招呼消息时是否提醒

    public static final String PREF_NOTIFY_PPL_ENABLE = "pref_notify_ppl_enable";// 收到碰碰聊消息时是否提醒

    // 通知栏开关
    public static final String PREF_BAR_ENABLE = "pref_bar_enable";

    public static final String PREF_BAR_CHAT_ENABLE = "pref_bar_chat_enable";// 收到对话消息时是否提醒

    public static final String PREF_BAR_MUC_GROUP_ENABLE = "pref_bar_muc_group_enable";// 收到群（大群小群）消息时是否提醒

    public static final String PREF_BAR_SUBCRIBE_ENABLE = "pref_bar_subcribe_enable";// 收到订阅账号消息时是否提醒

    public static final String PREF_BAR_NOTIFICATION_CENTER_ENABLE = "pref_bar_notification_center_enable";// 收到通知中心消息时是否提醒

    public static final String PREF_BAR_WALL_ENABLE = "pref_bar_wall_enable";// 收到广播消息时是否提醒

    public static final String PREF_BAR_SAY_HELLO_ENABLE = "pref_bar_say_hello_enable";// 收到打招呼消息时是否提醒

    public static final String PREF_BAR_NEW_FRIEND_ENABLE = "pref_bar_new_friend_enable";// 收到新好友通知时是否提醒

    public static final String PREF_BAR_PPL_ENABLE = "pref_bar_ppl_enable";// 收到碰碰聊消息时是否提醒

    public static final String PREF_BAR_GROUP_TALK_ENABLE = "pref_bar_group_talk_enable";// 收到多人群聊消息时是否提醒

    // 提示音设置
    public static final String PREF_SOUND = "pref_sound";// 声音,list

    public static final String PREF_SOUND_TITLE = "pref_sound_title";// 声音,list

    public static final String PREF_SOUND_ENABLE = "pref_sound_enable";// 声音开关,bool

    public static final String PREF_SOUND_CHAT_ENABLE = "pref_sound_chat_enable";// 收到对话消息时声音是否

    public static final String PREF_SOUND_MUC_GROUP_ENABLE = "pref_sound_muc_group_enable";// 收到群（大群小群）消息时声音是否

    public static final String PREF_SOUND_GROUP_TALK_ENABLE = "pref_sound_group_talk_enable";// 收到多人群聊消息时声音是否

    public static final String PREF_SOUND_SUBCRIBE_ENABLE = "pref_sound_subcribe_enable";// 收到订阅账号消息时声音是否

    public static final String PREF_SOUND_NOTIFICATION_CENTER_ENABLE = "pref_sound_notification_center_enable";// 收到通知中心消息时声音是否

    public static final String PREF_SOUND_WALL_ENABLE = "pref_sound_wall_enable";// 收到广播消息时声音是否

    public static final String PREF_SOUND_SAY_HELLO_ENABLE = "pref_sound_say_hello_enable";// 收到打招呼消息时声音是否

    public static final String PREF_SOUND_NEW_FRIEND_ENABLE = "pref_sound_new_friend_enable";// 收到新好友通知时是否响铃

    public static final String PREF_SOUND_PPL_ENABLE = "pref_sound_ppl_enable";// 收到碰碰聊消息时声音是否

    // 振动设置
    public static final String PREF_VIBRATE_ENABLE = "pref_vibrate_enable";// 振动开关,bool

    public static final String PREF_VIBRATE_CHAT_ENABLE = "pref_vibrate_chat_enable";// 收到对话消息时是否振动

    public static final String PREF_VIBRATE_MUC_GROUP_ENABLE = "pref_vibrate_muc_group_enable";

    public static final String PREF_VIBRATE_GROUP_TALK_ENABLE = "pref_vibrate_group_talk_enable";// 收到多人群聊消息时声音是否

    public static final String PREF_VIBRATE_SUBCRIBE_ENABLE = "pref_vibrate_subcribe_enable";// 收到订阅账号消息时是否振动

    public static final String PREF_VIBRATE_NOTIFICATION_CENTER_ENABLE = "pref_vibrate_notification_center_enable";// 收到通知中心消息时是否振动

    public static final String PREF_VIBRATE_WALL_ENABLE = "pref_vibrate_wall_enable";// 收到广播消息时是否振动

    public static final String PREF_VIBRATE_SAY_HELLO_ENABLE = "pref_vibrate_say_hello_enable";// 收到打招呼消息时是否振动

    public static final String PREF_VIBRATE_NEW_FRIEND_ENABLE = "pref_vibrate_new_friend_enable";// 收到新好友通知时是否振动

    public static final String PREF_VIBRATE_PPL_ENABLE = "pref_vibrate_ppl_enable";// 收到碰碰聊消息时是否振动

    // LED设置
    public static final String PREF_LED_COLOR = "pref_led_color";// LED颜色,list

    public static final String PREF_LED_ENABLE = "pref_led_enable";// LED开关,bool

    public static final String PREF_LED_CHAT_ENABLE = "pref_led_chat_enable";// 收到对话消息时LED是否闪

    public static final String PREF_LED_MUC_GROUP_ENABLE = "pref_led_muc_group_enable";

    public static final String PREF_LED_GROUP_TALK_ENABLE = "pref_led_group_talk_enable";

    public static final String PREF_LED_SUBCRIBE_ENABLE = "pref_led_subcribe_enable";// 收到订阅账号消息时LED是否闪

    public static final String PREF_LED_NOTIFICATION_CENTER_ENABLE = "pref_led_notification_center_enable";// 收到通知中心消息时LED是否闪

    public static final String PREF_LED_WALL_ENABLE = "pref_led_wall_enable";// 收到广播消息时LED是否闪

    public static final String PREF_LED_SAY_HELLO_ENABLE = "pref_led_say_hello_enable";// 收到打招呼消息时LED是否闪

    public static final String PREF_LED_NEW_FRIEND_ENABLE = "pref_led_new_friend_enable";// 收到新好友通知时LED是否闪

    public static final String PREF_LED_PPL_ENABLE = "pref_led_ppl_enable";// 收到碰碰聊消息时LED是否闪

    public static final String PREF_MUC_CATEGORY_UPDATE_TIME = "pref_muc_category_update_time";// 保存更新群分类数据的时间

    public static final String PREF_ARCHIVED_QUERY_TIME = "pref_archived_query_time";

    public static final String PREF_MUC_ADV_MODIFYTIME = "pref_muc_adv_modifytime";// 群的广告更新时间

    public static final String PREF_LAST_GET_MUC_ADV_TIME = "pref_last_get_muc_adv_time";// 上一次调用拉取广告api的时间

    /**
     * 最开始用于人人网api的开关，现在用于人人网，开心网，FB的开关。
     */
    public static final String REN_REN_API_SWITCH = "rrApiSwitch";

    public static final String PREF_OPEN_MATCH_PHONE_SWITCH = "pref_open_match_phone_switch";// 提醒用户打开通讯录匹配设置

    public static final String PREF_VIEW_DOMAINS = "pref_view_domains";

    public static final String PREF_MILIAO_MODE = "pref_miliao_mode";

    public static final String PREF_MILIAO_IS_COMPLETE_INFO = "pref_miliao_is_complete_info";

    /**
     * 新手进入我的资料的引导
     */
    public static final String PREF_KEY_PROFILE_GUIDE_INFO = "pref_profile_guide_info";

    public static final String PREF_KEY_PROFILE_GUIDE_BIRTHDYA = "pref_profile_guide_birthday";

    public static final String PREF_KEY_PROFILE_GUIDE_INDUSTRY = "pref_profile_guide_industry";

    // 阅后即焚的图片的销毁时间
    public static final String PREF_BURN_IMAGE_TTL = "pref_burn_image_ttl";

    public static final String PREF_RESOTRED_TAB_INDEX = "pref_resotred_tab_index_new";

    public static final String PREF_SHOW_BURN_MSG_TIP = "pref_show_burn_msg_tip";

    // 升级完善资料引导
    public static final String PREF_KEY_UPDATE_COMPLETE_USER_INFO = "pref_update_complete_user_info";

    // 手机内置安装需要提醒联网和定位
    public static final String PREF_KEY_NOTIFY_NETWORK = "pref_notify_network";

    public static final String PREF_KEY_NOTIFY_LOCATION = "pref_notify_location";

    public static final String PREF_MSG_BUBBLE_TEXT_SIZE = "pref_msg_bubble_text_size";
    public static final String PREF_MIUI_TEXT_SIZE = "pref_miui_text_size";
    // 隐私设置绑定
    public static final String PREF_SETTINGS_BIND_PHONE = "pref_bind_phone";

    public static final String PREF_SETTINGS_BIND_EMAIL = "pref_bind_email";

    @Deprecated
    // 原来在登录时设置，现在由于在注册时就设置过了，不用了。
    public static final String KEY_SETTED_LOCALE = "locale_setted";

    private static final Vector<PrefObserver> sPrefObs = new Vector<PrefObserver>();

    public static final String PREF_KEY_TRIM_DISK_TIME = "pref_key_trim_disk_time";

    // 小米社区 字体大小
    public static final String PREF_KEY_BBS_FONTSIZE_DIP = "pref_bbs_font_size_dip";

    // 强制绑定手机次数
    public static final String PREF_KEY_REQUIRE_BIND_PHONE = "pref_key_require_bind_phone";

    // 进入名片播放主题曲
    public static final String PREF_KEY_PLAY_MUSIC_IN_USER_CARD = "pref_key_play_music_in_user_card";

    public static final String PREF_KEY_RESET_TIME_VIPS_SCOPE = "pref_key_reset_time_vips_scope"; // 重新设置vip号段范围的时间

    public static final String PREF_KEY_VIPS_SCOPE = "pref_key_vips_scope"; // vip号段范围

    public static final String PREF_KEY_SHOW_ADOPT_XIAOICE_TIP = "pref_key_show_adopt_xiaoice_tip";

    public static final String PREF_KEY_SECOND_DEX_MD5 = "pref_key_second_dex_md5";

    public static final String PREF_KEY_SHOW_UPGRADE_RED_ME_SET = "pref_key_show_upgrade_red_me_set";//我-设置 红点提示

    public static final String PREF_KEY_SHOW_UPGRADE_NEW_TIP = "pref_key_show_upgrade_new_tip";//我-设置-检查更新 new提示

    // 更新快捷反馈的时间
    public static final String PREF_KEY_UPDATE_FEEDBACK_FAQ = "pref_key_update_feedback_faq";

    public static final String PREF_KEY_CUSTOM_BACKGROUND = "pref_key_custom_background";

    public static final String PREF_KEY_CUSTOM_MUSIC_BACKGROUND = "pref_key_custom_music_background";

    public static final String KEY_IS_HIDE_BIRTHDAY_YEAR = "is_hide_birthday_year";

    public static final boolean KEY_MATCH_CONTACT_DEFAULT_VALUE = true;

    public static final String PREF_KEY_SHOW_RED_ME = "pref_key_show_red_me";//我 红点提示

    //账号互踢
    //private static final String KEY_IS_USER_KICK_OFF = "pref_user_isKickOff";
    //private static final String KEY_IS_PASSWORD_VERIFYED = "pref_user_isPasswordVerifyed";


    public static final String PREF_KEY_SHOW_JOIN_ROOM_VARIABLE = "pref_key_show_join_room_variable";
    public static final String PREF_KEY_REPORT_ITEM_DATA = "pref_key_report_item_data";
    public static final String REPORT_ITEM_DATA_SPLIT = "_";

    // 竖屏键盘高度
    private static final String PREF_KEY_KEBOARD_HEIGHT = "pref_s_key_keboard_height";

    //横屏键盘高度
    private static final String PREF_KEY_CROSS_KEBOARD_HEIGHT = "pref_s_key_CROSS_keboard_height";

    public interface PrefObserver {
        void notifyPrefChange(String key, Object value);
    }

    public static void addPrefObserver(final PrefObserver ob) {
        sPrefObs.add(ob);
    }

    public static void removePrefObserver(final PrefObserver obToRm) {
        for (final PrefObserver ob : sPrefObs) {
            if (ob == obToRm) {
                sPrefObs.remove(ob);
                break;
            }
        }
    }

    public static void notifyPrefChange(final String key, final Object value) {
        for (final PrefObserver ob : sPrefObs) {
            ob.notifyPrefChange(key, value);
        }
    }

    public static String getCachedStatus(final Context c) {
        return getSettingString(c, STATUS, "");
    }

    public static void cacheStatus(final String status, final Context c) {
        setSettingString(c, STATUS, status);
    }

    public static String getCachedPhoneNumber(final Context c, final String thisPhoneId) {
        final String cachedPhoneId = getSettingString(c, KEY_PHONE_ID, "");
        if (cachedPhoneId.equals(thisPhoneId)) {
            return getSettingString(c, KEY_PHONE_NUMBER, null);
        }
        return null;
    }

    public static void cachePhoneNumber(final String phoneNum, final String thisPhoneId,
                                        final Context c) {
        setSettingString(c, KEY_PHONE_ID, thisPhoneId);
        setSettingString(c, KEY_PHONE_NUMBER, phoneNum);
    }

    public static boolean getIsPingStarted(final Context c) {
        return getSettingBoolean(c, KEY_PING_STARTED, false);
    }

    public static void setIsPingStarted(final Context c) {
        setSettingBoolean(c, KEY_PING_STARTED, true);
    }

    public static boolean getIsLogOff(final Context c) {
        return getSettingBoolean(c, KEY_USER_LOGOFF, false);
    }

    public static boolean getIsWrongPassword(final Context c) {
        return getSettingBoolean(c, KEY_WRONG_PASSWORD, false);
    }

    @Deprecated
    public static boolean getIsInNightMode(final Context c) {
        return getSettingBoolean(c, KEY_NIGHT_MODE, false);
    }

    public static void setIsLogOff(final Context c, final boolean isLogOff) {
        setSettingBoolean(c, KEY_USER_LOGOFF, isLogOff);
    }

    public static void setWrongPassword(final Context c, final boolean isWrongPassword) {
        if (isWrongPassword) {
            MyLog.e("setWrongPassword true");
        }
        setSettingBoolean(c, KEY_WRONG_PASSWORD, isWrongPassword);
    }

    @Deprecated
    public static void setIsInNightMode(final Context c, final boolean isNightMode) {
        setSettingBoolean(c, KEY_NIGHT_MODE, isNightMode);
    }

    // public static long getTimeSpan() {
    // return PreferenceUtils.getSettingLong(GlobalData.app(),
    // PREF_KEY_TIMESPAN,
    // NearbySettingActivity.TIMESPAN_SEVENDAYS);
    // }
    public static void setTimeSpan(final long timeSpan) {
        setSettingLong(GlobalData.app(), PREF_KEY_TIMESPAN, timeSpan);
    }

    public static boolean getLbsVisible() {
        return getSettingBoolean(GlobalData.app(), PREF_KEY_LBS_VISIBLE, true);
    }

    public static void setLbsVisible(final boolean isVisible) {
        setSettingBoolean(GlobalData.app(), PREF_KEY_LBS_VISIBLE, isVisible);
    }

//    public static int getNearbyStatus() {
//        return PreferenceUtils.getSettingInt(GlobalData.app(), PREF_KEY_NEARBY, UserSettings.LBS_OFF_SERVER);
//    }
//
//    public static void setNearbyStatus(final int value) {
//        PreferenceUtils.setSettingInt(GlobalData.app(), PREF_KEY_NEARBY, value);
//    }

    public static int getMsgNotificationMode(final Context c) {
        final String msgNotifString = getSettingString(c, KEY_MSG_NOTIFICATION,
                "SOUND");
        if (msgNotifString.equals("SOUND_VIBRATE")) {
            return NOTIFICATION_SOUND_VIBRATE;
        } else if (msgNotifString.equals("SOUND")) {
            return NOTIFICATION_SOUND;
        } else if (msgNotifString.equals("VIBRATE")) {
            return NOTIFICATION_VIBRATE;
        } else if (msgNotifString.equals("BUBBLE")) {
            return NOTIFICATION_BUBBLE;
        } else {
            return NOTIFICATION_NONE;
        }
    }

    public static int getSixinNotificationMode(final Context c) {
        final String msgNotifString = getSettingString(c, KEY_SIXIN_NOTIFICATION,
                "SOUND");
        if (msgNotifString.equals("SOUND_VIBRATE")) {
            return NOTIFICATION_SOUND_VIBRATE;
        } else if (msgNotifString.equals("SOUND")) {
            return NOTIFICATION_SOUND;
        } else if (msgNotifString.equals("VIBRATE")) {
            return NOTIFICATION_VIBRATE;
        } else if (msgNotifString.equals("BUBBLE")) {
            return NOTIFICATION_BUBBLE;
        } else {
            return NOTIFICATION_NONE;
        }
    }

//    public static int getFriVarify(final String value) {
//        if (value.equals("NO_VERIFY")) {
//            return UserSettings.CONFIRMED_DISABLED;
//        } else if (value.equals("NO_ADD")) {
//            return UserSettings.CONFIRMED_NOT_ADDABLE;
//        } else {
//            return UserSettings.CONFIRMED_ENABLED;
//        }
//    }
//
//    public static String getFriVarufyStr(final int value) {
//        if (value == UserSettings.CONFIRMED_DISABLED) {
//            return "NO_VERIFY";
//        } else if (value == UserSettings.CONFIRMED_NOT_ADDABLE) {
//            return "NO_ADD";
//        } else {
//            return "VERIFY";
//        }
//    }
//
//    public static int getFriVarify(final Context c) {
//        final String verifyStr = PreferenceUtils.getSettingString(c, KEY_FRIEND_VERIFY, "VARIFY");
//        return getFriVarify(verifyStr);
//    }

    public static int getWallNotificationMode(final Context c) {
        final String wallNotifString = getSettingString(c, KEY_WALL_NOTIFICATION,
                "NONE");
        if (wallNotifString.equals("SOUND_VIBRATE")) {
            return NOTIFICATION_SOUND_VIBRATE;
        } else if (wallNotifString.equals("SOUND")) {
            return NOTIFICATION_SOUND;
        } else if (wallNotifString.equals("VIBRATE")) {
            return NOTIFICATION_VIBRATE;
        } else if (wallNotifString.equals("BUBBLE")) {
            return NOTIFICATION_BUBBLE;
        } else {
            return NOTIFICATION_NONE;
        }
    }

    public static int getNewNotiMsgNotificationMode(final Context c) {
        final String newNotifString = getSettingString(c,
                KEY_NEW_NOTI_MSG_NOTIFICATION, "SOUND");
        if (newNotifString.equals("SOUND_VIBRATE")) {
            return NOTIFICATION_SOUND_VIBRATE;
        } else if (newNotifString.equals("SOUND")) {
            return NOTIFICATION_SOUND;
        } else if (newNotifString.equals("VIBRATE")) {
            return NOTIFICATION_VIBRATE;
        } else if (newNotifString.equals("BUBBLE")) {
            return NOTIFICATION_BUBBLE;
        } else {
            return NOTIFICATION_NONE;
        }
    }

    public static boolean isMucMsgNotify(final Context context) {
        final String mucNotifySettingStr = getSettingString(context,
                KEY_MUC_NOTIFICATION, "NOTIFY");
        return !"NOT NOTIFY".equals(mucNotifySettingStr);
    }

    /**
     * 特殊的打点，将append的内容添加到key原有值的后面
     *
     * @param sp
     * @param key
     * @param append
     */
    public static void appendSettingString(final SharedPreferences sp, final String key,
                                           final String append) {
        String oldValue = sp.getString(key, "");
        sp.edit().putString(key, oldValue + append).apply();
    }


    public static int getKeyboardHeight() {
        int screenWidth = DisplayUtils.getScreenWidth();
        int screenHeight = DisplayUtils.getScreenHeight();

        if (screenHeight > screenWidth) { //认为是竖屏，返回竖屏的值
            return MLPreferenceUtils.getSettingInt(GlobalData.app(), PREF_KEY_KEBOARD_HEIGHT, GlobalData.app().getResources().getDimensionPixelSize(R.dimen.keyboard_default_height));
        } else { //认为是横屏,返回横屏的值
            return MLPreferenceUtils.getSettingInt(GlobalData.app(), PREF_KEY_CROSS_KEBOARD_HEIGHT, GlobalData.app().getResources().getDimensionPixelSize(R.dimen.keyboard_default_cross_height));
        }
    }

    public static void setKeyboardHeight(final int height) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                int screenWidth = DisplayUtils.getScreenWidth();
                int screenHeight = DisplayUtils.getScreenHeight();
                if (screenHeight > screenWidth) { //认为是竖屏，返回竖屏的值
                    PreferenceUtils.setSettingInt(GlobalData.app(), PREF_KEY_KEBOARD_HEIGHT, height);
                } else { //认为是横屏,返回横屏的值
                    PreferenceUtils.setSettingInt(GlobalData.app(), PREF_KEY_CROSS_KEBOARD_HEIGHT, height);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).subscribe();

    }

    public static int getKeyboardHeight(boolean isisPortrait) {

        if (isisPortrait) { //认为是竖屏，返回竖屏的值
            return MLPreferenceUtils.getSettingInt(GlobalData.app(), PREF_KEY_KEBOARD_HEIGHT, GlobalData.app().getResources().getDimensionPixelSize(R.dimen.keyboard_default_height));
        } else { //认为是横屏,返回横屏的值
            return MLPreferenceUtils.getSettingInt(GlobalData.app(), PREF_KEY_CROSS_KEBOARD_HEIGHT, GlobalData.app().getResources().getDimensionPixelSize(R.dimen.keyboard_default_cross_height));
        }
    }

    public static void setKeyboardHeight(final int height, final boolean isisPortrait) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                if (isisPortrait) { //认为是竖屏，返回竖屏的值
                    PreferenceUtils.setSettingInt(GlobalData.app(), PREF_KEY_KEBOARD_HEIGHT, height);
                } else { //认为是横屏,返回横屏的值
                    PreferenceUtils.setSettingInt(GlobalData.app(), PREF_KEY_CROSS_KEBOARD_HEIGHT, height);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).subscribe();

    }

    private static final String PREF_KEY_SAVE_YZB_ACCOUNT_TS = "pref_key_save_yzb_account_ts";

    public static void setYzbSaveMemberTs(long ts) {
        MLPreferenceUtils.setSettingLong(com.base.global.GlobalData.app(), MLPreferenceUtils.PREF_KEY_SAVE_YZB_ACCOUNT_TS, ts);
    }

    public static long getYzbSaveMemberTs() {
        return MLPreferenceUtils.getSettingLong(com.base.global.GlobalData.app(), MLPreferenceUtils.PREF_KEY_SAVE_YZB_ACCOUNT_TS, 0);
    }

//    public static void setIsKickOff(final Context c, final boolean isKickOff) {
//        PreferenceUtils.setSettingBoolean(c, MLAccountHelper.KEY_IS_USER_KICK_OFF, isKickOff);
//    }
//
//    public static boolean getIsKickOff(final Context c, final boolean isKickOff) {
//        return PreferenceUtils.getSettingBoolean(c, MLAccountHelper.KEY_IS_USER_KICK_OFF, isKickOff);
//    }
//
//    public static void setIsPasswordVerifyed(final Context c, final boolean isPasswordVerifyed) {
//        PreferenceUtils.setSettingBoolean(c, MLAccountHelper.KEY_IS_PASSWORD_VERIFYED, isPasswordVerifyed);
//    }
//
//    public static boolean getIsPasswordVerifyed(final Context c, final boolean isPasswordVerifyed) {
//        return PreferenceUtils.getSettingBoolean(c, MLAccountHelper.KEY_IS_PASSWORD_VERIFYED, isPasswordVerifyed);
//    }

//    public static String getFriVarufyStr(final int value) {
//        if (value == UserSettings.CONFIRMED_DISABLED) {
//            return "NO_VERIFY";
//        } else if (value == UserSettings.CONFIRMED_NOT_ADDABLE) {
//            return "NO_ADD";
//        } else {
//            return "VERIFY";
//        }
//    }
//
//    public static void setNearbyStatus(final int value) {
//        PreferenceUtils.setSettingInt(GlobalData.app(), PREF_KEY_NEARBY, value);
//    }
//
//    public static int getNearbyStatus() {
//        return PreferenceUtils.getSettingInt(GlobalData.app(), PREF_KEY_NEARBY,
//                UserSettings.LBS_OFF_SERVER);
//    }
}
