package com.module;

/**
 * 保存所有ARouter_Path
 * 请按module来分
 */
public interface RouterConstants {
    String ACTIVITY_FLUTTER = "/flutter/FlutterActivity";

    String ACTIVITY_HOME = "/home/HomeActivity";
    String ACTIVITY_UPLOAD = "/home/UploadAccountInfoActivity";
    String ACTIVITY_UPLOAD_SEX_AGE = "/home/UploadSexAndAgeTagActivity";
    String ACTIVITY_EDIT_INFO = "/home/EditInfoActivity";
    String ACTIVITY_EDIT_AGE = "/home/EditAgeTagActivity";

    String ACTIVITY_LOGIN = "/core/LoginActivity";
    String ACTIVITY_SCHEME = "/core/SchemeSdkActivity";

    String ACTIVITY_VOICEROOM = "/rankingmode/VoiceRoomActivity";
    String ACTIVITY_AUDIOROOM = "/rankingmode/AudioRoomActivity";
    String ACTIVITY_BEAUTY_PREVIEW = "/rankingmode/BeautyPreviewActivity";
    String ACTIVITY_PLAY_WAYS = "/rankingmode/PlayWaysActivity";
    String ACTIVITY_RANK_ROOM = "/rankingmode/RankRoomActivity";
    String ACTIVITY_GRAB_CREATE_ROOM = "/rankingmode/GrabCreateRoomActivity";
    String ACTIVITY_GRAB_ROOM = "/rankingmode/GrabRoomActivity";
    String ACTIVITY_AUDITION_ROOM = "/rankingmode/AuditionActivity";
    String ACTIVITY_GRAB_RESULT = "/rankingmode/GrabResultActivity";
    String ACTIVITY_SHARE_WEB = "/rankingmode/ShareWebActivity";
    String ACTIVITY_GRAB_MATCH_ROOM = "/rankingmode/GrabMatchActivity";
    String ACTIVITY_GRAB_SPECIAL = "/rankingmode/GrabSpecialActivity";

    String ACTIVITY_BALANCE = "/home/BalanceActivity";
    String ACTIVITY_WALLET = "/home/DiamondBallanceActivity";
    String ACTIVITY_INCOME = "/home/InComeActivity";
    String ACTIVITY_WITH_DRAW = "/home/WithDrawActivity";
    String ACTIVITY_SMS_AUTH = "/home/SmsAuthActivity";
    String ACTIVITY_SETTING = "/home/SettingActivity";
    String ACTIVITY_ENGINE_SETTING = "/home/EngineSettingActivity";
    String ACTIVITY_RANKED = "/home/RankedActivity";

    String ACTIVITY_LAST_FOLLOW = "/msg/LastFollowActivity";
    String ACTIVITY_COMMENT_LIKE = "/msg/CommentAndLikeActivity";
    String ACTIVITY_SPECIAL_FOLLOW = "/msg/SpecialFollowActivity";
    String ACTIVITY_GIFT_RECORD = "/rankingmode/GiftRecordActivity";

    String ACTIVITY_RELATION = "/busilib/RelationActivity";
    String ACTIVITY_OTHER_PERSON = "/busilib/OtherPersonActivity";
    String ACTIVITY_GUARD_LIST = "/busilib/GuardListActivity";

    String ACTIVITY_PERSON_BUSINESS = "/busilib/PersonBusinessActivity";
    String ACTIVITY_PERSON_PHOTO = "/busilib/PersonPhotoActivity";
    String ACTIVITY_PERSON_WORKS = "/busilib/PersonWorksActivity";
    String ACTIVITY_PERSON_POST = "/post/PersonPostActivity";
    String ACTIVITY_PERSON_FEED = "/feed/PersonFeedActivity";

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

    String ACTIVITY_DOUBLE_HOME = "/rankingmode/DoubleHomeActivity";
    String ACTIVITY_DOUBLE_PLAY = "/rankingmode/DoublePlayActivity";
    String ACTIVITY_DOUBLE_MATCH = "/rankingmode/DoubleMatchActivity";
    String ACTIVITY_DOUBLE_END = "/rankingmode/DoubleEndActivity";

    String ACTIVITY_FEEDS_SECOND_DETAIL = "/feeds/FeedsSecondCommentDetailActivity";
    String ACTIVITY_FEEDS_DETAIL = "/feeds/FeedsDetailActivity";
    String ACTIVITY_FEEDS_MAKE = "/feeds/FeedsMakeActivity";
    String ACTIVITY_FEEDS_LYRIC_MAKE = "/feeds/FeedsLyricMakeActivity";
    String ACTIVITY_FEEDS_EDITOR = "/feeds/FeedsEditorActivity";
    String ACTIVITY_FEEDS_PUBLISH = "/feeds/FeedsPublishActivity";
    String ACTIVITY_FEEDS_SHARE = "/feeds/FeedsShareActivity";
    String ACTIVITY_FEEDS_REPORT = "/feeds/FeedsReportActivity";
    String ACTIVITY_FEEDS_COPY_REPORT = "/feeds/FeedCopyReportActivity";
    String ACTIVITY_FEEDS_RANK = "/feeds/FeedsRankActivity";
    String ACTIVITY_FEEDS_RANK_DETAIL = "/feeds/FeedsRankDetailActivity";
    String ACTIVITY_FEEDS_RANK_SEARCH = "/feeds/FeedsRankSearchActivity";
    String ACTIVITY_FEEDS_SELECT_MODE = "/feeds/FeedsSelectModeActivity";
    String ACTIVITY_FEEDS_SONG_MANAGE = "/feeds/FeedSongManagerActivity";
    String ACTIVITY_FEEDS_SONG_SEARCH = "/feeds/FeedSongSearchActivity";
    String ACTIVITY_FEEDS_TAG = "/feeds/FeedsTagActivity";
    String ACTIVITY_FEEDS_TAG_DETAIL = "/feeds/FeedsTagDetailActivity";


    String ACTIVITY_RACE_ROOM = "/race/RaceRoomActivity";
    String ACTIVITY_RACE_MATCH_ROOM = "/race/RaceMatchActivity";
    String ACTIVITY_RACE_RESULT = "/race/RaceResultActivity";

    //WebViewActivity
    String ACTIVITY_WEB = "/common/ExpendWebActivity";
    String ACTIVITY_DEBUG_CORE_ACTIVITY = "/debug/DebugCoreActivity";
    String KEY_WEB_URL = "url";

    String SERVICE_HOME = "/home/service1";
    String SERVICE_RANKINGMODE = "/rankingmode/service1";
    String SERVICE_MSG = "/rongmsg/service1";
    String SERVICE_FEEDS = "/feeds/service1";
    String SERVICE_POSTS = "/posts/service1";
    String SERVICE_GRAB_SERVICE = "/game/grab_service1";
    String SERVICE_DOUBLE_PLAY = "/game/double_play_service";
    String SERVICE_CLUB = "/club/service1";

    String ACTIVITY_FOR_TEST = "/test/ForTestActivity";

    String ACTIVITY_BATTLE_LIST = "/battle/BattleListActivity";
    String ACTIVITY_BATTLE_RANK = "/battle/BattleRankActivity";

    String ACTIVITY_POSTS_TOPIC = "/posts/PostsTopicActivity";
    String ACTIVITY_POSTS_TOPIC_SELECT = "/posts/PostsTopicSelectActivity";
    String ACTIVITY_POSTS_DETAIL = "/posts/PostsDetailActivity";
    String ACTIVITY_POSTS_COMMENT_DETAIL = "/posts/PostsCommentDetailActivity";
    String ACTIVITY_POSTS_PUBLISH = "/posts/PostsPublishActivity";
    String ACTIVITY_POSTS_REPORT = "/posts/PostsReportActivity";
    String ACTIVITY_POSTS_VOTE_EDIT = "/posts/PostsVoteEditActivity";
    String ACTIVITY_POSTS_RED_PKG_EDIT = "/posts/PostsRedPkgEditActivity";

    String ACTIVITY_VOICE_RECORD = "/posts/VoiceRecordActivity";

    String ACTIVITY_MIC_HOME = "/mic/MicHomeActivity";
    String ACTIVITY_CREATE_MIC_ROOM = "/mic/MicRoomCreateActivity";

    String ACTIVITY_MIC_ROOM = "/mic/MicRoomActivity";

    String ACTIVITY_MIC_MATCH = "/mic/MicMatchActivity";

    String ACTIVITY_INVITE_FRIEND = "/room/InviteFriendActivity";

    String ACTIVITY_MALL_MALL = "/mall/MallActivity";
    String ACTIVITY_MALL_PACKAGE = "/mall/PackageActivity";

    String ACTIVITY_RELAY_HOME = "/relay/RelayHomeActivity";
    String ACTIVITY_RELAY_MATCH = "/relay/RelayMatchActivity";
    String ACTIVITY_RELAY_ROOM = "/relay/RelayRoomActivity";
    String ACTIVITY_RELAY_RESULT = "/relay/RelayResultActivity";

    String ACTIVITY_CREATE_PARTY_ROOM = "/party/PartyRoomCreateActivity";
    String ACTIVITY_PARTY_ROOM = "/party/PartyRoomActivity";
    String ACTIVITY_PARTY_SELECT_GAME = "/party/PartySelectGameActivity";
    String ACTIVITY_PARTY_HOME = "/party/PartyHomeActivity";
    String ACTIVITY_PARTY_SEARCH = "/party/PartyRoomSearchActivity";

    String ACTIVITY_CREATE_CLUB = "/club/CreateClubActivity";
    String ACTIVITY_LIST_CLUB = "/club/ClubListActivity";
    String ACTIVITY_SEARCH_CLUB = "/club/ClubSearchActivity";
    String ACTIVITY_LIST_MEMBER = "/club/ClubMemberListActivity";
    String ACTIVITY_LIST_APPLY_CLUB = "/club/ClubApplyListActivity";
    String ACTIVITY_LIST_CLUB_RANK = "/club/ClubRankListActivity";
    String ACTIVITY_FEEDBACK = "/all/QuickFeedbackActivity";


    /**
     * Flutter 相关页面
     */
    String FLUTTER_PAGE_TEST = "SkrTestPage";
    String FLUTTER_PAGE_RELAY_RESULT = "RelayResultPage";
    String FLUTTER_PAGE_PARTY_BGM_PAGE = "PartyBgMusicManagerPage";
    String FLUTTER_PAGE_SETTING = "SettingManagerPage";
}
