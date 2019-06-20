package com.module;

/**
 * 保存所有ARouter_Path
 * 请按module来分
 */
public interface RouterConstants {
    String ACTIVITY_HOME = "/home/HomeActivity";
    String ACTIVITY_UPLOAD = "/home/UploadAccountInfoActivity";
    String ACTIVITY_EDIT_INFO = "/home/EditInfoActivity";

    String ACTIVITY_LOGIN = "/core/LoginActivity";
    String ACTIVITY_SCHEME = "/core/SchemeSdkActivity";

    String ACTIVITY_VOICEROOM = "/rankingmode/VoiceRoomActivity";
    String ACTIVITY_AUDIOROOM = "/rankingmode/AudioRoomActivity";
    String ACTIVITY_BEAUTY_PREVIEW = "/rankingmode/BeautyPreviewActivity";
    String ACTIVITY_PLAY_WAYS = "/rankingmode/PlayWaysActivity";
    String ACTIVITY_RANK_ROOM = "/rankingmode/RankRoomActivity";
    String ACTIVITY_GRAB_CREATE_ROOM = "/rankingmode/GrabCreateRoomActivity";
    String ACTIVITY_GRAB_ROOM = "/rankingmode/GrabRoomActivity";
    String ACTIVITY_GRAB_GUIDE = "/rankingmode/GrabGuideActivity";
    String ACTIVITY_GRAB_GUIDE_RESULT = "/rankingmode/GrabGuideResultActivity";
    String ACTIVITY_AUDITION_ROOM = "/rankingmode/AuditionActivity";
    String ACTIVITY_GRAB_RESULT = "/rankingmode/GrabResultActivity";
    String ACTIVITY_SHARE_WEB = "/rankingmode/ShareWebActivity";
    String ACTIVITY_GRAB_MATCH_ROOM = "/rankingmode/GrabMatchActivity";

    String ACTIVITY_WALLET = "/home/WalletActivity";
    String ACTIVITY_BALANCE = "/home/BalanceActivity";
    String ACTIVITY_DIAMOND_BALANCE = "/home/DiamondBallanceActivity";
    String ACTIVITY_INCOME = "/home/InComeActivity";
    String ACTIVITY_WITH_DRAW = "/home/WithDrawActivity";
    String ACTIVITY_SMS_AUTH = "/home/SmsAuthActivity";
    String ACTIVITY_MUSIC_TEST = "/home/MusicTestActivity";
    String ACTIVITY_SETTING = "/home/SettingActivity";

    String ACTIVITY_LAST_FOLLOW = "/msg/LastFollowActivity";

    String ACTIVITY_FRIEND_ROOM = "/busilib/GrabFriendRoomActivity";
    String ACTIVITY_RELATION = "/busilib/RelationActivity";
    String ACTIVITY_OTHER_PERSON = "/busilib/OtherPersonActivity";

    String ACTIVITY_DEVICE_INFO = "/test/DeviceInfoActivity";
    String ACTIVITY_EMOJI = "/test/EmojiActivity";
    String ACTIVITY_TEST = "/test/TestSdkActivity";
    String ACTIVITY_TEST_VIDEO = "/test/BaseCameraActivity";
    String ACTIVITY_TEST_BYTED_EFFECT = "/test/WelcomeActivity";

    String ACTIVITY_WATCH = "/watch/WatchSdkAcitivity";
    //VideoDetailSdkActivity
    String ACTIVITY_VIDEO = "VideoDetailSdkActivity";
    //SubChannelActivity
    String ACTIVITY_SUB_CHANNEL = "SubChannelActivity";
    //HalfWebViewActivity
    String ACTIVITY_HALFWEB = "HalfWebViewActivity";
    //LongTextActivity
    String ACTIVITY_LONGTEXT = "LongTextActivity";
    //RechargeActivity
    String ACTIVITY_RECHARGE = "RechargeActivity";
    String ACTIVITY_CHANNEL_LIST_SDK = "/channel/ChannelListSdkActivity";

    String ACTIVITY_DOUBLE_PLAY = "/rankingmode/DoublePlayActivity";
    String ACTIVITY_DOUBLE_MATCH = "/rankingmode/DoubleMatchActivity";
    String ACTIVITY_DOUBLE_END = "/rankingmode/DoubleEndActivity";

    //WebViewActivity
    String ACTIVITY_WEB = "/common/ExpendWebActivity";
    String ACTIVITY_DEBUG_CORE_ACTIVITY = "/debug/DebugCoreActivity";
    String KEY_WEB_URL = "url";

    String SERVICE_HOME = "/home/service1";
    String SERVICE_RANKINGMODE = "/rankingmode/service1";
    String SERVICE_MSG = "/rongmsg/service1";
    String SERVICE_GRAB_SERVICE = "/game/grab_service1";
    String SERVICE_DOUBLE_PLAY = "/game/double_play_service";


}
