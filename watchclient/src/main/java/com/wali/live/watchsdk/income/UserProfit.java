package com.wali.live.watchsdk.income;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.utils.CommonUtils;
import com.wali.live.proto.PayProto;

/**
 * Created by liuyanyan on 16/3/1.
 */
public class UserProfit {
    public static final String BUNDLE_PAY_TYPE = "bundle_pay_type";
    public static final String BUNDLE_OATH_CODE = "bundle_oath_code";
    public static final String BUNDLE_ACCOUNT = "bundle_account";
    public static final String KEY_EXCHANGE_MIN_CASH = "exchange_min_cash_onetime";
    public static final String KEY_EXCHANGE_MAX_CASH = "exchange_max_cash_onetime";

    public static final int TYPE_CNT = 0;//表明是总数
    public static final int TYPE_SHOW = 1;//表明是娱乐星票
    public static final int TYPE_GAME = 2;//表明是游戏星票

    public static final int TYPE_ALI = 1;
    public static final int TYPE_WX = 2;
    public static final int TYPE_PAYPAL = 3;

    public static final int ACCOUNT_STATUS_AVAILABLE = 0;
    public static final int ACCOUNT_STATUS_UNAVAILABLE = 1;

    public static final int SIGN_STATUS_SIGNED = 1;

    private int ticketCount;//用户娱乐星票数
    private int curExchangeCount;//总可兑换现金数，单位元

    public int getCurGameExchangeCount() {
        return curGameExchangeCount;
    }

    private int curGameExchangeCount;//游戏星票可兑换现金数，单位元
    private int usdGameExchangeCount;//游戏星票可兑换现金数，单位美元
    private Double todayExchangeCount;//用户今日可使用票数：提现或兑钻
    private int accountStatus;//0-正常， 1-账户结算中不可提现和兑换
    private int maxCashCountTimes;
    private int exchangeUsdcash;//paypal可提现的美金
    private Double exchangeUsdcashToday;

    private AliPayAccount aliPayAccount;
    private WeixinPayAccount weixinPayAccount;
    private PaypalPay paypalPay;
    private int exchangeMinCashCntOnetime;//每次最小提现金额
    private int exchangeMinUsdCashCntOnetime;//每次最小提现金额(美分)
    private int exchangeMaxCashCntOnetime;//每次最大提现金额
    private int exchangeMaxUsdCashCntOnetime;//每次最大提现金额(美分)
    private int signStatus;//0,普通用户,1,签约用户
    private String redirectUrl;//点击签约tip跳转的url
    private int frozenUsableTicketCount;//签约主播冻结的可用星票数
    private int usableMibiTicketCount;//（未签约时：未兑换的米币星票数。签约后：签约主播冻结的可用米币星票数）
    private int clearMibiTicketCount;//签约前未兑换的米币星票数
    private int showMoney;//娱乐星票可提现钱数

    public int getShowUsdMoney() {
        return showUsdMoney;
    }

    public void setShowUsdMoney(int showUsdMoney) {
        this.showUsdMoney = showUsdMoney;
    }

    public int getShowMoney() {
        return showMoney;
    }

    public void setShowMoney(int showMoney) {
        this.showMoney = showMoney;
    }

    private int showUsdMoney;//娱乐星票可提现美金数

    public int getClearMibiTicketCount() {
        return clearMibiTicketCount;
    }

    public void setClearMibiTicketCount(int clearMibiTicketCount) {
        this.clearMibiTicketCount = clearMibiTicketCount;
    }

    public int getUsableMibiTicketCount() {
        return usableMibiTicketCount;
    }

    public void setUsableMibiTicketCount(int usableMibiTicketCount) {
        this.usableMibiTicketCount = usableMibiTicketCount;
    }

    public int getFrozenUsableTicketCount() {
        return frozenUsableTicketCount;
    }

    public void setFrozenUsableTicketCount(int frozenUsableTicketCount) {
        this.frozenUsableTicketCount = frozenUsableTicketCount;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirect_url) {
        this.redirectUrl = redirect_url;
    }

    public UserProfit() {
    }

    public UserProfit(int ticketCount, int curExchangeCount, Double todayExchangeCount) {
        this.ticketCount = ticketCount;
        this.curExchangeCount = curExchangeCount;
        this.todayExchangeCount = todayExchangeCount;
    }

    public UserProfit(PayProto.QueryProfitResponse rsp) {
        parse(rsp);
    }

    /**
     * 获取首选提现方式
     *
     * @return
     */
    public static int getPreferWithDrawType() {
        if (CommonUtils.isChinese()) {
            return TYPE_WX;
        }
        return TYPE_PAYPAL;
    }

    /**
     * 单位：美分
     *
     * @return
     */
    public int getExchangeUsdcash() {
        return exchangeUsdcash;
    }

    /**
     * 单位：美分
     *
     * @param exchangeUsdcash
     */
    public void setExchangeUsdcash(int exchangeUsdcash) {
        this.exchangeUsdcash = exchangeUsdcash;
    }

    public Double getExchangeUsdcashToday() {
        return exchangeUsdcashToday;
    }

    public void setExchangeUsdcashToday(Double exchangeUsdcashToday) {
        this.exchangeUsdcashToday = exchangeUsdcashToday;
    }

    public PaypalPay getPaypalPay() {
        return paypalPay;
    }

    public void setPaypalPay(PaypalPay paypalPay) {
        this.paypalPay = paypalPay;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    /**
     * 单位：分
     *
     * @return
     */
    public int getCurExchangeCount() {
        return curExchangeCount;
    }

    /**
     * 单位：分
     *
     * @param curExchangeCount
     */
    public void setCurExchangeCount(int curExchangeCount) {
        this.curExchangeCount = curExchangeCount;
    }

    /**
     * 今日可兑换现金数剩余额度，单位：元
     *
     * @return
     */
    public Double getTodayExchangeCount() {
        return todayExchangeCount;
    }

    /**
     * 今日可兑换现金数剩余额度，单位：元
     *
     * @param todayExchangeCount
     */
    public void setTodayExchangeCount(Double todayExchangeCount) {
        this.todayExchangeCount = todayExchangeCount;
    }

    public void setCurGameExchangeCount(int curGameExchangeCount) {
        this.curGameExchangeCount = curGameExchangeCount;
    }

    public int getUsdGameExchangeCount() {
        return usdGameExchangeCount;
    }

    public void setUsdGameExchangeCount(int usdGameExchangeCount) {
        this.usdGameExchangeCount = usdGameExchangeCount;
    }

    public int getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(int accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getMaxCashCountTimes() {
        return maxCashCountTimes;
    }

    public void setMaxCashCountTimes(int maxCashCountTimes) {
        this.maxCashCountTimes = maxCashCountTimes;
    }

    public AliPayAccount getAliPayAccount() {
        return aliPayAccount;
    }

    public void setAliPayAccount(AliPayAccount aliPayAccount) {
        this.aliPayAccount = aliPayAccount;
    }

    @Nullable
    public WeixinPayAccount getWeixinPayAccount() {
        return weixinPayAccount;
    }

    public void setWeixinPayAccount(WeixinPayAccount weixinPayAccount) {
        this.weixinPayAccount = weixinPayAccount;
    }

    public int getExchangeMinCashCntOnetime() {
        return exchangeMinCashCntOnetime;
    }

    public void setExchangeMinCashCntOnetime(int exchangeMinCashCntOnetime) {
        this.exchangeMinCashCntOnetime = exchangeMinCashCntOnetime;
    }

    public int getExchangeMinUsdCashCntOnetime() {
        return exchangeMinUsdCashCntOnetime;
    }

    public void setExchangeMinUsdCashCntOnetime(int exchangeMinUsdCashCntOnetime) {
        this.exchangeMinUsdCashCntOnetime = exchangeMinUsdCashCntOnetime;
    }

    public int getExchangeMaxCashCntOnetime() {
        return exchangeMaxCashCntOnetime;
    }

    public void setExchangeMaxCashCntOnetime(int exchangeMaxCashCntOnetime) {
        this.exchangeMaxCashCntOnetime = exchangeMaxCashCntOnetime;
    }

    public int getExchangeMaxUsdCashCntOnetime() {
        return exchangeMaxUsdCashCntOnetime;
    }

    public void setExchangeMaxUsdCashCntOnetime(int exchangeMaxUsdCashCntOnetime) {
        this.exchangeMaxUsdCashCntOnetime = exchangeMaxUsdCashCntOnetime;
    }

    public int getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(int signStatus) {
        this.signStatus = signStatus;
    }

    public static class AliPayAccount {
        public String account;
        public String name;

        public AliPayAccount(PayProto.AliPay aliPay) {
            account = aliPay.getAccount();
            name = aliPay.getRealName();
        }
    }

    public static class WeixinPayAccount {
        public static final int NOT_REAL_NAME = 0;
        public static final int REAL_NAME_NOT_VERIFIED = 1;
        public static final int REAL_NAME_FAIL = 2;
        public static final int ALREADY_REAL_NAME = 3;

        public String name;
        public String headImgUrl;
        public int verification;

        public WeixinPayAccount(PayProto.WeixinPay account) {
            name = account.getUserName();
            headImgUrl = account.getHeadimgurl();
            verification = account.getVertification();
        }

        public boolean isNeedBind() {
            return verification != ALREADY_REAL_NAME;
        }

        public static boolean inNeedReBind(int bindStatus) {
            return bindStatus == REAL_NAME_NOT_VERIFIED || bindStatus == REAL_NAME_FAIL;
        }
    }

    public static class PaypalPay {

        public static final int REAL_NAME_NOT_VERIFIED = 2;

        public static final int ALREADY_REAL_NAME = 3;

        private String account;
        private String firstName;
        private String lastName;
        private int vertification;

        public PaypalPay(PayProto.PaypalPay paypalPay) {
            account = paypalPay.getAccount();
            firstName = paypalPay.getFirstname();
            lastName = paypalPay.getLastname();
            vertification = paypalPay.getVertification();
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public int getVertification() {
            return vertification;
        }

        public void setVertification(int vertification) {
            this.vertification = vertification;
        }
    }

    public void parse(PayProto.QueryProfitResponse rsp) {
        setCurExchangeCount(rsp.getExchangeCashCnt());
        setTodayExchangeCount(rsp.getExchangeCashTodayCnt() / 100.0);
        setTicketCount(rsp.getUsableTicketCnt());
        setAccountStatus(rsp.getAccountStatus());
        setMaxCashCountTimes(rsp.getMaxCashCntTimes());
        setExchangeUsdcash(rsp.getExchangeUsdCashCnt());
        setExchangeUsdcashToday(rsp.getExchangeUsdCashTodayCnt() / 100.0);

        if (null != rsp.getAlipay() && !TextUtils.isEmpty(rsp.getAlipay().getAccount()) && !TextUtils.isEmpty(rsp.getAlipay().getRealName())) {
            AliPayAccount account = new AliPayAccount(rsp.getAlipay());
            setAliPayAccount(account);
        }
        if (null != rsp.getWxpay() && !TextUtils.isEmpty(rsp.getWxpay().getUserName())) {
            WeixinPayAccount account = new WeixinPayAccount(rsp.getWxpay());
            setWeixinPayAccount(account);
        }
        if (null != rsp.getPaypal() && !TextUtils.isEmpty(rsp.getPaypal().getAccount())) {
            PaypalPay account = new PaypalPay(rsp.getPaypal());
            setPaypalPay(account);
        }
        setExchangeMinCashCntOnetime(rsp.getExchangeMinCashCntOnetime());
        setExchangeMinUsdCashCntOnetime(rsp.getExchangeMinUsdCashCntOnetime());
        setExchangeMaxCashCntOnetime(rsp.getExchangeMaxCashCntOnetime());
        setExchangeMaxUsdCashCntOnetime(rsp.getExchangeMaxUsdCashCntOnetime());
        setSignStatus(rsp.getSignStatus());
        setRedirectUrl(rsp.getRedirectUrl());
        setFrozenUsableTicketCount(rsp.getFrozenUsableTicketCnt());
        setUsableMibiTicketCount(rsp.getUsableMibiTicketCnt());
        setClearMibiTicketCount(rsp.getClearMibiTicketCnt());
        setCurGameExchangeCount(rsp.getGameTicketCashCnt());
        setUsdGameExchangeCount(rsp.getGameTicketUsdCashCnt());
        setShowMoney(rsp.getTicketCashCnt());
        setShowUsdMoney(rsp.getTicketUsdCashCnt());
    }


}