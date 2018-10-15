package com.mi.liveassistant;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {

    public static final int DB_VERSION = 1;

    public static final String PACKAGE_DAO_NAME = "com.mi.liveassistant.dao";

    public static final String Relation_TABLE_NAME = "Relation";
    public static final String OWNUSERINFO_TABLE_NAME = "OwnUserInfo";


    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(DB_VERSION, PACKAGE_DAO_NAME);
        schema.enableKeepSectionsByDefault();

        // 账号信息
        addAccount(schema);

        //关注列表
        addRelation(schema);

        addOwnUserInfo(schema);

        new DaoGenerator().generateAll(schema, "lite/src/main/java-gen");
    }   

    private static void addAccount(final Schema schema) {
        Entity account = schema.addEntity("UserAccount");
        account.addStringProperty("uuid").primaryKey();

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
     * 增加　个人信息 表
     *
     * @param schema
     */
    public static void addOwnUserInfo(final Schema schema) {
        Entity ownUserInfo = schema.addEntity(OWNUSERINFO_TABLE_NAME);
        ownUserInfo.implementsSerializable();
        ownUserInfo.addIdProperty().autoincrement().primaryKey();

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

}
