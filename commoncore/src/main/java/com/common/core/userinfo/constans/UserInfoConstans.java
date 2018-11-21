package com.common.core;

public interface UserInfoConstans {
    int GENDER_MAN = 1;
    int GENDER_WOMAN = 2;

    int USER_TYPE_NORMAL = 0;//普通用户
    int USER_TYPE_SHOP = 1;  //商铺
    int USER_TYPE_TV = 2;  //电视台

    int CERTIFICATION_NOT = 0;
    int CERTIFICATION_WEIBO = 1; //微博
    int CERTIFICATION_OFFICIAL = 2;
    int CERTIFICATION_RECOMMEND = 3;
    int CERTIFICATION_XIAOMI = 4; //官微认证
    int CERT_TYPE_PGC = 5;      //pgc 用户
    int CERTIFICATION_TV = 6; //电视台认证

    int WAITING_CERTIFICATION_XIAOMI = 4;
    int WAITING_CERTIFICATION_REALNAME = 5;

    int REALNAME_STATUS_WAITING = 1;
    int REALNAME_STATUS_FAILURE = 3;
    int REALNAME_STATUS_SUCCESS = 2;

    //贵族特权等级类型
    int NOBLE_LEVEL_TOP = 500;//王者
    int NOBLE_LEVEL_SECOND = 400;//公爵
    int NOBLE_LEVEL_THIRD = 300;//侯爵
    int NOBLE_LEVEL_FOURTH = 200;//伯爵
    int NOBLE_LEVEL_FIFTH = 100;//子爵
}
