package com.wali.live.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {
    //account 添加 miid字段 版本号+1 56变为57
    //礼物添加buyType 版本变为56
    //特权礼物改版,版本好修改为55
    //添加观看记录表，版本号+1, 修改为54
    //观众试看门票直播记录，版本号改为53，2016年11月05日10:26:05
    //特权礼物，版本号修改为52
    //新增国家列表数据库创建改为51
    //米币礼物，版本号修改为50
    //增加banner 版本号为49
    //道如果加列表数据库加48
    //礼物橱窗角标以及提示国际化根据服务器变更变为46
    // //新增礼物橱窗提示国际化,版本变为45
    //新增礼物橱窗角标国际化,版本变为44
    //新增虚拟钻，版本号变为43
    //新增红名，版本号变为42
    //新增聊天模块用户微博认证，版本号变为40
    public static final int DB_VERSION = 57;

    public static final String PACKAGE_DAO_NAME = "com.wali.live.dao";

    public static final String CONVERSATION_TABLE_NAME = "Conversation";
    public static final String SIXIN_MESSAGE_TABLE_NAME = "SixinMessage";
    public static final String GIFT_TABLE_NAME = "Gift";
    public static final String Relation_TABLE_NAME = "Relation";
    public static final String OWNUSERINFO_TABLE_NAME = "OwnUserInfo";
    public static final String LIVE_TOKEN = "LiveToken";
    public static final String REGION_CN = "RegionCn";
    public static final String REGION_EN = "RegionEn";
    public static final String REGION_TW = "RegionTw";
    public static final String LOADING_BANNER = "LoadingBanner";
    public static final String ROOM_GLANCE_TABLE_NAME = "RoomGlance";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(DB_VERSION, PACKAGE_DAO_NAME);
        schema.enableKeepSectionsByDefault();

        // 账号信息
        addAccount(schema);

        // 个人资料
        addOwnUserInfo(schema);

        // 商城礼物
        addGift(schema);

        //关注列表
        addRelation(schema);

        //国家列表
        addCountryCN(schema);
        addCountryEN(schema);
        addCountryTW(schema);
        addLoadingBanner(schema);
        new DaoGenerator().generateAll(schema, "data/src/main/java-gen");
    }

    private static void addAccount(final Schema schema) {
        Entity account = schema.addEntity("UserAccount");

        account.addIntProperty("channelid"); // channelid 调用方的渠道
        account.addStringProperty("uuid");
        account.addStringProperty("serviceToken");
        account.addStringProperty("securityKey");
        account.addStringProperty("passToken");
        account.addStringProperty("nickName");
        account.addStringProperty("imgUrl");
        account.addIntProperty("sex");

        //保留以前userAccount信息
        account.addStringProperty("slogan");
        account.addStringProperty("userName");
        account.addStringProperty("password");
        account.addStringProperty("oldPwd");
        account.addStringProperty("deviceId");
        account.addStringProperty("pSecurity");
        account.addStringProperty("sSecurity");
//        account.addStringProperty("sid");
        account.addIntProperty("isReset");
        account.addIntProperty("isNew");
        account.addBooleanProperty("needEditUserInfo");
        //账号退出和登录中的标志位
        account.addBooleanProperty("isLogOff");
        account.addLongProperty("miid");   //sso 添加miid存儲

    }

    /**
     * 增加　个人信息 表
     *
     * @param schema
     */
    public static void addOwnUserInfo(final Schema schema) {
        Entity ownUserInfo = schema.addEntity(OWNUSERINFO_TABLE_NAME);


        ownUserInfo.addIntProperty("channelid");// channelid 调用方的渠道
        ownUserInfo.addLongProperty("uid");

        ownUserInfo.addLongProperty("avatar");
        ownUserInfo.addStringProperty("nickname");
        ownUserInfo.addStringProperty("sign");
        ownUserInfo.addIntProperty("gender");
        ownUserInfo.addIntProperty("level");
        ownUserInfo.addIntProperty("badge");
        ownUserInfo.addStringProperty("certification");
        ownUserInfo.addIntProperty("certificationType");
        ownUserInfo.addIntProperty("waitingCertificationType");
        ownUserInfo.addBooleanProperty("isInspector");
        ownUserInfo.addIntProperty("liveTicketNum");
        ownUserInfo.addIntProperty("fansNum");
        ownUserInfo.addIntProperty("followNum");
        ownUserInfo.addIntProperty("sendDiamondNum");
        ownUserInfo.addIntProperty("sendVirtualDiamondNum");
        ownUserInfo.addIntProperty("vodNum");
        ownUserInfo.addIntProperty("earnNum");
        ownUserInfo.addIntProperty("diamondNum");
        ownUserInfo.addIntProperty("virtualDiamondNum");
        ownUserInfo.addStringProperty("coverPhotoJson");
        ownUserInfo.addBooleanProperty("firstAudit");
        ownUserInfo.addBooleanProperty("redName");
        ownUserInfo.addByteArrayProperty("region");
    }

    public static void addConversation(final Schema schema) {
        Entity conversationEntity = schema.addEntity(CONVERSATION_TABLE_NAME);
        conversationEntity.addIdProperty().autoincrement().primaryKey();
        conversationEntity.addLongProperty("target").index().notNull();
        conversationEntity.addIntProperty("unreadCount");
        conversationEntity.addLongProperty("sendTime");
        conversationEntity.addLongProperty("receivedTime");
        conversationEntity.addStringProperty("content");
        conversationEntity.addLongProperty("lastMsgSeq");
        conversationEntity.addStringProperty("targetName");
        conversationEntity.addLongProperty("msgId");
        conversationEntity.addIntProperty("msgType");
        conversationEntity.addIntProperty("ignoreStatus");
        conversationEntity.addLongProperty("locaLUserId").index().notNull();
        conversationEntity.addBooleanProperty("isNotFocus").index().notNull();
        conversationEntity.addStringProperty("ext");
        conversationEntity.addIntProperty("certificationType"); // 用来标识用户头像右下角的角标
    }


    public static void addChatMessage(final Schema schema) {
        Entity chatMessageEntity = schema.addEntity(SIXIN_MESSAGE_TABLE_NAME);
        chatMessageEntity.addIdProperty().autoincrement().primaryKey();
        chatMessageEntity.addLongProperty("target").index().notNull();
        chatMessageEntity.addStringProperty("targetName");
        chatMessageEntity.addLongProperty("sender");
        chatMessageEntity.addIntProperty("msgTyppe");
        chatMessageEntity.addLongProperty("msgSeq");
        chatMessageEntity.addLongProperty("senderMsgId");
        chatMessageEntity.addLongProperty("sentTime");
        chatMessageEntity.addLongProperty("receivedTime");
        chatMessageEntity.addBooleanProperty("isInbound");
        chatMessageEntity.addIntProperty("msgStatus");
        chatMessageEntity.addIntProperty("outboundStatus");
        chatMessageEntity.addStringProperty("body");
        chatMessageEntity.addStringProperty("ext");
        chatMessageEntity.addLongProperty("locaLUserId").index().notNull();
        chatMessageEntity.addIntProperty("certificationType"); // 用来标识用户头像右下角的角标
    }

    public static void addGift(final Schema schema) {
        Entity giftMessageEntity = schema.addEntity(GIFT_TABLE_NAME);
        giftMessageEntity.addIdProperty().autoincrement().primaryKey();
        giftMessageEntity.addIntProperty("giftId").index().notNull();
        giftMessageEntity.addIntProperty("sortId");
        giftMessageEntity.addStringProperty("name");
        giftMessageEntity.addIntProperty("price");
        giftMessageEntity.addIntProperty("empiricValue");
        giftMessageEntity.addStringProperty("picture");
        giftMessageEntity.addBooleanProperty("canContinuous");
        giftMessageEntity.addStringProperty("languageStr");
        giftMessageEntity.addStringProperty("resourceUrl");
        giftMessageEntity.addBooleanProperty("canSale");
        giftMessageEntity.addIntProperty("catagory");
        giftMessageEntity.addStringProperty("isAllowActivitySet");
        giftMessageEntity.addIntProperty("originalPrice");
        giftMessageEntity.addStringProperty("icon");
        giftMessageEntity.addStringProperty("comment");
        giftMessageEntity.addStringProperty("gifUrl");
        giftMessageEntity.addIntProperty("lowerLimitLevel");
        giftMessageEntity.addIntProperty("originGiftType");
        giftMessageEntity.addIntProperty("buyType");

    }


//    public long userId;
//    public long avatar;
//    public String userNickname;          // 昵称
//    public String signature;             // 签名
//    public int gender;                   // 性别
//    public int level;                    // 等级
//    public int mTicketNum;               // 星票
//    public int certificationType;        //认证类型
//    public boolean mIsPking;             //是否pk中
//    public boolean mIsShowing;           //是否在直播
//    public int mViewerNum;               //pk观众数
//    public boolean isFollowing;          // 是否关注 [仅在查询别人的粉丝、关注列表时需要]
//    public boolean isPushable;           // 是否推送 [仅在查询关注列表时需要]
//    public boolean isBothway;            // 是否双向关注 [判断双向关注]

    public static void addRelation(final Schema schema) {

        Entity relationEntity = schema.addEntity(Relation_TABLE_NAME);
        relationEntity.implementsSerializable();
        //relationEntity.addIdProperty().autoincrement().primaryKey();
        relationEntity.addLongProperty("userId").primaryKey();
        relationEntity.addLongProperty("avatar");
        relationEntity.addStringProperty("userNickname");
        relationEntity.addStringProperty("signature");

        relationEntity.addIntProperty("gender");
        relationEntity.addIntProperty("level");
        relationEntity.addIntProperty("mTicketNum");
        relationEntity.addIntProperty("certificationType");
        relationEntity.addBooleanProperty("isFollowing");
        relationEntity.addBooleanProperty("isBothway");

        //relationEntity.addLongProperty("lastUpdateTime").notNull();
    }


    /**
     * 增加口令直播间密码记录
     *
     * @param schema
     */
    public static void addLiveToken(final Schema schema) {
        Entity liveToken = schema.addEntity(LIVE_TOKEN);
        liveToken.implementsSerializable();
        liveToken.addIdProperty().autoincrement().primaryKey();
        liveToken.addLongProperty("uuid").index();
        liveToken.addStringProperty("roomId").index();
        liveToken.addIntProperty("retryTimes");
        liveToken.addStringProperty("token");
    }

    public static void addCountryCN(final Schema schema) {
        Entity regionCn = schema.addEntity(REGION_CN);
        regionCn.implementsSerializable();
        regionCn.addBooleanProperty("isHot");
        regionCn.addStringProperty("Country");
        regionCn.addStringProperty("CountryCode");
    }

    public static void addCountryEN(final Schema schema) {
        Entity regionCn = schema.addEntity(REGION_EN);
        regionCn.implementsSerializable();
        regionCn.addBooleanProperty("isHot");
        regionCn.addStringProperty("Country");
        regionCn.addStringProperty("CountryCode");
    }

    public static void addCountryTW(final Schema schema) {
        Entity regionCn = schema.addEntity(REGION_TW);
        regionCn.implementsSerializable();
        regionCn.addBooleanProperty("isHot");
        regionCn.addStringProperty("Country");
        regionCn.addStringProperty("CountryCode");
    }

    public static void addLoadingBanner(final Schema schema) {
        Entity loadingBaner = schema.addEntity(LOADING_BANNER);
        loadingBaner.implementsSerializable();
        loadingBaner.addLongProperty("bannerId").primaryKey();
        loadingBaner.addStringProperty("picUrl");
        loadingBaner.addStringProperty("skipUrl");
        loadingBaner.addLongProperty("lastUpdateTs");
        loadingBaner.addIntProperty("startTime");
        loadingBaner.addIntProperty("endTime");
        loadingBaner.addStringProperty("shareIconUrl");
        loadingBaner.addStringProperty("shareTitle");
        loadingBaner.addStringProperty("shareDesc");
        loadingBaner.addLongProperty("lastShowTime");
        loadingBaner.addStringProperty("localPath");
    }

    private static void addRoomGlance(Schema schema) {
        Entity roomGlance = schema.addEntity(ROOM_GLANCE_TABLE_NAME);
        roomGlance.implementsSerializable();
        roomGlance.addIdProperty().autoincrement().primaryKey();
        roomGlance.addLongProperty("uuid").index();
        roomGlance.addStringProperty("roomId").index();
    }
}
