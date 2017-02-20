
package com.wali.live.network;

import com.base.utils.Constants;

public class XMConstants {

    public static final String ACCOUNT_DOMAIN_P = "https://account.xiaomi.com/";

    public static final String ACCOUNT_DOMAIN_T = "http://account.preview.n.xiaomi.net/";

    public static final String ACCOUNT_DOMAIN = Constants.isTestBuild ? ACCOUNT_DOMAIN_T
            : ACCOUNT_DOMAIN_P;

    // 用passtoken方式登录
    public static final String LOGIN_PASSTOKEN_API = ACCOUNT_DOMAIN
            + "pass/serviceLogin";

    public static final String LOGIN_VIA_ACCESS_TOKEN = "https://account.xiaomi.com/pass/sns/login/accessToken";

    public static final String LOGIN_PASSWORD_API_V2 = ACCOUNT_DOMAIN
            + "pass/serviceLoginAuth2";

    public static final String LOGIN_PASSWORD_STEP2_API_V2 = ACCOUNT_DOMAIN
            + "pass/loginStep2";


    public static final String XIAOMI_WEBSERVICE_HOST_P = "api.chat.xiaomi.net";

    // public static final String XIAOMI_WEBSERVICE_HOST_T = "211.103.219.166";
    public static final String XIAOMI_WEBSERVICE_HOST_T = Constants.isTestBuild ? "staging.api.chat.xiaomi.net"
            : "api.preview.n.miliao.com";

    public static final String XIAOMI_WEBSERVICE_HOST = Constants.isTestBuild ? XIAOMI_WEBSERVICE_HOST_T
            : XIAOMI_WEBSERVICE_HOST_P;

    public static final String XIAOMI_SUPPORT_WEBSERVICE_BASE_URL_V2_S = String
            .format("https://%s/v2", XIAOMI_WEBSERVICE_HOST);

    public static final String LOGIN_BY_SERVICE_TOKEN_SECURITY = XIAOMI_SUPPORT_WEBSERVICE_BASE_URL_V2_S
            + "/miliaosts/user/%1$s/miliaotoken";

    public static final String XIAOMI_MILIAO_STS = Constants.isTestBuild ? "api.preview.n.miliao.com"
            : XIAOMI_WEBSERVICE_HOST_P;

    public static final String XIAOMI_ACRONYM = "xm";

    /* imei号 */
    public static final String ANDROID_ACRONYM = "an";

    // 发送注册手机验证码API
    public static final String PASSPORT_VERIFY_PHONE = ACCOUNT_DOMAIN
            + "pass/sendPhoneTicket";

    // 获取短信验证码剩余次数
    public static final String PASSPORT_GET_QUOTA = ACCOUNT_DOMAIN
            + "pass/sms/quota";

    public static final String ACTION_RECEIVE_NORMAL_SMS = "android.provider.Telephony.SMS_RECEIVED";

    // 检验手机验证码API
    public static final String PASSPORT_VERIFY_PHONE_TICKET = ACCOUNT_DOMAIN
            + "pass/verifyPhoneRegTicket";

    // 输入手机号注册（新接口）
    public static final String PASSPORT_REGISTER_NEW = ACCOUNT_DOMAIN
            + "pass/register";


    // 拉取启动广告的接口地址
    public static final String ADVERTISE_IMAGES_INFO = Constants.isTestBuild ? "http://migc.wali.com/micossapi/coss.htm"
            : "http://app.migc.wali.com/migcoss/coss.htm";

}
