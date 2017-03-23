package com.wali.live.common.pay.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.network.NetworkUtils;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.common.pay.constant.RechargeConfig;
import com.wali.live.common.pay.model.Diamond;
import com.wali.live.common.pay.model.SkuDetail;
import com.wali.live.common.pay.presenter.RechargePresenter;
import com.wali.live.event.EventClass;
import com.wali.live.proto.PayProto;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import rx.Observable;
import rx.Subscriber;

import static com.wali.live.common.pay.fragment.RechargeFragment.getCurrentPayWay;

/**
 * @module 充值
 * Created by chengsimin on 16/2/22.
 * Google Play部分由rongzhisheng修改 on 16/5/20
 */
public class PayManager {
    public static final int GOODS_NOT_EXIST = 11040;

    public static String TAG = PayManager.class.getSimpleName();

    private static List<Diamond> mDiamondCache = new Vector<>();
    private static int mExchangeableDiamondCnt;
    private static int mWillExpireDiamondCnt;
    private static int mWillExpireGiftCardCnt;
    private static int sWeiXinTodayAmount;//微信当日已支付金额，单位：分
    private static int sMiWalletTodayAmount;//小米钱包当日已支付金额，单位：分
    private static WeakReference<RechargePresenter> rechargePresenter;

    public static void setRechargePresenter(RechargePresenter rechargePresenter) {
        PayManager.rechargePresenter = new WeakReference<RechargePresenter>(rechargePresenter);
    }

    /**
     * 异步拉取价格列表
     */
    public static void pullPriceListAsync() {
        PayProto.GetGemPriceRequest req = PayProto.GetGemPriceRequest.newBuilder()
                .setPlatform(PayProto.Platform.ANDROID)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setChannel(getChannelByPayWay())
                .build();
        MyLog.v(TAG, "pullPriceListSync request:" + req.toString());

        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
            if (rechargePresenter != null && rechargePresenter.get() != null
                    && !rechargePresenter.get().isNotPullRechargeList()
                    && rechargePresenter.get().canShowErrorToast()) {
                ToastUtils.showToast(R.string.network_unavailable);
                // 空列表
                replace(getDiamondListCache());
            }
            MyLog.w(TAG, "no network connect, request:" + req.toString());
            return;
        }

        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_PAY_PRICE_LIST);
        data.setData(req.toByteArray());
        MiLinkClientAdapter.getsInstance().sendAsync(data);
    }

    private static PayProto.RChannel getChannelByPayWay() {
        switch (getCurrentPayWay()) {
//            case GOOGLEWALLET:
//                return PayProto.RChannel.GO_PLAY_CH;
//            case PAYPAL:
//                return PayProto.RChannel.PAYPAL_CH;
            default:
                return PayProto.RChannel.AND_CH;
        }
    }

    /**
     * 同步创建订单
     *
     * @param goods
     * @param type
     * @return
     */
    public static PayProto.CreateOrderResponse createOrderSync(Diamond goods, PayProto.PayType type) {
        PayProto.CreateOrderRequest req = PayProto.CreateOrderRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .setGoodsId(goods.getId())
                .setGemCnt(goods.getCount())
                .setPrice(goods.getPrice())
                .setPayType(type)
                .setChannel(getChannelByPayType(type))
                .setGiveGemCnt(goods.getExtraGive())
                .setAppChannel(String.valueOf(HostChannelManager.getInstance().getChannelId()))
                .setAppType(PayProto.AppType.ZHIBO_ZHUSHOU)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_PAY_CREATE_ORDER);
        packetData.setData(req.toByteArray());
        MyLog.v(TAG, "createOrder request:" + req.toString());
        PacketData res = MiLinkClientAdapter.getsInstance().sendSync(packetData, 15 * 1000);
        PayProto.CreateOrderResponse response = null;
        try {
            if (res != null) {
                response = PayProto.CreateOrderResponse.parseFrom(res.getData());
            }
        } catch (Exception e) {
        }
        MyLog.d(TAG, "createOrder response:" + response);
        return response;
    }

    @NonNull
    private static PayProto.RChannel getChannelByPayType(PayProto.PayType type) {
        switch (type) {
            case GO_PLAY:
                return PayProto.RChannel.GO_PLAY_CH;
            case PAYPAL:
                return PayProto.RChannel.PAYPAL_CH;
            default:
                return PayProto.RChannel.AND_CH;
        }
    }

    /**
     * 同步查询订单
     *
     * @param orderId
     * @param payId
     * @return
     */
    public static PayProto.CheckOrderResponse checkOrderSync(String orderId, String payId, String receipt, String transactionId) {
        PayProto.CheckOrderRequest.Builder reqBuilder = PayProto.CheckOrderRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .setOrderId(orderId);
        if (!TextUtils.isEmpty(payId)) {
            reqBuilder.setPuid(payId);
        }
        if (!TextUtils.isEmpty(receipt)) {
            reqBuilder.setReceipt(receipt);
        }
        if (!TextUtils.isEmpty(transactionId)) {
            reqBuilder.setTransactionId(transactionId);
        }
        PayProto.CheckOrderRequest req = reqBuilder.build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_PAY_CHECK_ORDER);
        packetData.setData(req.toByteArray());
        MyLog.v(TAG, "checkOrder request:" + req.toString());
        PacketData res = MiLinkClientAdapter.getsInstance().sendSync(packetData, 15 * 1000);
        PayProto.CheckOrderResponse response = null;
        try {
            if (res != null) {
                response = PayProto.CheckOrderResponse.parseFrom(res.getData());
            }
        } catch (Exception e) {
        }
        MyLog.d(TAG, "checkOrder response:" + response);
        return response;
    }

    public static List<Diamond> getDiamondListCache() {
        return mDiamondCache;
    }

    public static int getExchangeableDiamondCnt() {
        return mExchangeableDiamondCnt;
    }

    public static int getWillExpireDiamondCnt() {
        return mWillExpireDiamondCnt;
    }

    public static int getWillExpireGiftCardCnt() {
        return mWillExpireGiftCardCnt;
    }

    public static int getWeiXinTodayAmount() {
        return sWeiXinTodayAmount;
    }

    public static int getMiWalletTodayAmount() {
        return sMiWalletTodayAmount;
    }

    private synchronized static void replace(List<Diamond> newData) {
        if (newData != null && !newData.isEmpty()) {// 拉取充值列表前需要clear
            MyLog.d(TAG, "update diamond cache:" + newData);
            mDiamondCache.clear();
            mDiamondCache.addAll(newData);
        }
        EventBus.getDefault().post(new EventClass.PayEvent(EventClass.PayEvent.EVENT_TYPE_PAY_DIAMOND_CACHE_CHANGE));
    }

    /**
     * 应用内语言变化时，清掉缓存
     */
    public synchronized static void clearCache() {
        mDiamondCache.clear();
    }

    /**
     * 顺序处理MiLink异步请求的返回包,实际上是阻塞的,不要进行耗时操作
     */
    public static void process(PayProto.GetGemPriceResponse response) {
        List<Diamond> result = new ArrayList<>();
        if (response == null) {
            MyLog.e(TAG, "GetGemPriceResponse is null");
            replace(result);
            return;
        }
        MyLog.d(TAG, "GetGemPriceResponse: " + response.toString());
        mExchangeableDiamondCnt = response.getExchangeableGemCnt();
        mWillExpireDiamondCnt = response.getExpireVirtualGemCnt();
        mWillExpireGiftCardCnt = response.getExpireGiftCardCnt();
        if (response.hasAmount()) {
            sWeiXinTodayAmount = response.getAmount().getWxpayAmount();
            sMiWalletTodayAmount = response.getAmount().getMiwalletAmount();
        }
        MyLog.d(TAG, "mExchangeableDiamondCnt = " + mExchangeableDiamondCnt
                + ", mWillExpireDiamondCnt = " + mWillExpireDiamondCnt
                + ", mWillExpireGiftCardCnt = " + mWillExpireGiftCardCnt
                + ", sWeiXinTodayAmount = " + sWeiXinTodayAmount
                + ", sMiWalletTodayAmount = " + sMiWalletTodayAmount
        );
        if (rechargePresenter != null && rechargePresenter.get() != null) {
            if (rechargePresenter.get().isNotPullRechargeList()) {
                MyLog.d(TAG, "is Only Get Exchangeable Diamond");
                EventBus.getDefault().post(new EventClass.PayEvent(EventClass.PayEvent.EVENT_TYPE_PAY_EXCHANGEABLE_DIAMOND_CHANGE));
                return;
            }
        }

        if (RechargeConfig.isServerDiamondInfoCanDirectlyUse(RechargeConfig.getRechargeListType(getCurrentPayWay()))) {
            for (PayProto.GemGoods goods : response.getGemGoodsListList()) {
                result.add(toDiamond(goods));
            }
        }
//        else if (getCurrentPayWay() == PayWay.PAYPAL) {
//            processPayPalRechargeList(response, result);
//        } else if (getCurrentPayWay() == PayWay.GOOGLEWALLET) {
//            processGoogleWalletRechargeList(response, result);
//        }
        replace(result);
    }

    private static void processPayPalRechargeList(PayProto.GetGemPriceResponse response, List<Diamond> result) {
        for (PayProto.GemGoods goods : response.getGemGoodsListList()) {
            Diamond diamond = toDiamond(goods);
            SkuDetail skuDetail = new SkuDetail();
            skuDetail.setProductId(String.valueOf(diamond.getId()));
            skuDetail.setPriceCurrencyCode("USD");
            skuDetail.setPriceAmountMicros(10000L * diamond.getPrice());// price是分
            skuDetail.setPrice(GlobalData.app().getResources().getString(R.string.recharge_paypal_price_format,
                    new BigDecimal(skuDetail.getPriceAmountMicros()).divide(SkuDetail.DIVISOR).toString(), skuDetail.getPriceCurrencyCode()));
            skuDetail.setTitle(getPayPalProductTitle(diamond));// 商品标题
            diamond.setSkuDetail(skuDetail);
            result.add(diamond);
        }
    }

    private static void processGoogleWalletRechargeList(PayProto.GetGemPriceResponse response, List<Diamond> result) {
        ArrayList<String> productIdList = new ArrayList<>();
        for (PayProto.GemGoods goods : response.getGemGoodsListList()) {
            productIdList.add(String.valueOf(goods.getGoodsId()));// TODO 目前服务器上的充值代码和GooglePlay上的productId保持一致
        }
        Map<String, SkuDetail> idSkuDetailMap = null;
        if (rechargePresenter != null && rechargePresenter.get() != null) {
            idSkuDetailMap = rechargePresenter.get().querySkuInfo(productIdList, true);
        }
        if (idSkuDetailMap != null) {
            for (PayProto.GemGoods goods : response.getGemGoodsListList()) {
                SkuDetail skuDetail = null;
                if ((skuDetail = idSkuDetailMap.get(String.valueOf(goods.getGoodsId()))) != null) {
                    Diamond diamond = toDiamond(goods);
                    diamond.setSkuDetail(skuDetail);
                    result.add(diamond);
                }
            }
        }
    }

    /**
     * 返回形如“1200 Diamonds Package + 60 （Given）”的字符串
     *
     * @param diamond
     * @return
     */
    private static String getPayPalProductTitle(Diamond diamond) {
        String main = GlobalData.app().getResources().getQuantityString(R.plurals.gold_diamond, diamond.getCount(), diamond.getCount());
        if (diamond.getExtraGive() > 0) {
            main += " " + GlobalData.app().getString(R.string.given_diamond, diamond.getExtraGive());
        }
        return main;
    }

    private static Diamond toDiamond(PayProto.GemGoods goods) {
        Diamond diamond = new Diamond();
        diamond.setId(goods.getGoodsId());
        diamond.setCount(goods.getGemCnt());
        diamond.setExtraGive(goods.getGiveGemCnt());
        diamond.setMaxBuyTimes(goods.getMaxBuyTimes());
        diamond.setPrice(goods.getPrice());
        diamond.setSubTitle(goods.getSubtitle());
        diamond.setIconUrl(goods.getIcon());
        return diamond;
    }


    /* 充值活动小红点 */
    public static void pullRedPointAsync() {
        PayProto.GetRedPointConfigRequest.Builder reqBuilder = PayProto.GetRedPointConfigRequest.newBuilder()
                .setType("recharge");
        RedPointConfig config = getRedPointFromPreference();
        if (!TextUtils.isEmpty(config.uniq)) {
            reqBuilder.setUniq(config.uniq);
        }
        PayProto.GetRedPointConfigRequest req = reqBuilder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_PAY_GET_RED_ICON);
        data.setData(req.toByteArray());
        MiLinkClientAdapter.getsInstance().sendAsync(data);
        MyLog.v(TAG, "pullRedPointAsync request:" + req.toString());
    }

    public static void updateRedPointInPreference(RedPointConfig r) {
        SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                PREF_RECHARGE_RED_POINT_CONFIG, Context.MODE_PRIVATE);
        PreferenceUtils.setSettingLong(settingPreferences, KEY_START_TIME, r.startTime);
        PreferenceUtils.setSettingLong(settingPreferences, KEY_END_TIME, r.endTime);
        PreferenceUtils.setSettingString(settingPreferences, KEY_UNIQ, r.uniq);
        PreferenceUtils.setSettingBoolean(settingPreferences, KEY_HAS_READ, r.isRead);
    }

    public static RedPointConfig getRedPointFromPreference() {
        SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                PREF_RECHARGE_RED_POINT_CONFIG, Context.MODE_PRIVATE);
        RedPointConfig r = new RedPointConfig();
        r.startTime = settingPreferences.getLong(KEY_START_TIME, 0);
        r.endTime = settingPreferences.getLong(KEY_END_TIME, 0);
        r.uniq = settingPreferences.getString(KEY_UNIQ, "");
        r.isRead = settingPreferences.getBoolean(KEY_HAS_READ, false);
        return r;
    }

    public static void processRedPoint(PayProto.GetRedPointConfigResponse response) {
        if (response == null) {
            return;
        }
        if (response != null) {
            MyLog.d(TAG, "response:" + response);
            RedPointConfig config = RedPointConfig.toRedPointConfig(response.getConfig());
            compareWithCurrent(config);
        }
    }

    private static void compareWithCurrent(RedPointConfig newConfig) {
        RedPointConfig curConfig = getRedPointFromPreference();

        if (curConfig.uniq.equals(newConfig.uniq)
                && curConfig.startTime == newConfig.startTime
                && curConfig.endTime == newConfig.endTime) {
            // 和上次拉取的配置一样，则不更新
            MyLog.d(TAG, "red point not change");
        } else {
            MyLog.d(TAG, "new config");
            // 来了新的配置了,写入新的配置
            updateRedPointInPreference(newConfig);
            long now = System.currentTimeMillis();
            if (now > newConfig.startTime && now < newConfig.endTime) {
                // 活动已经开始且未结束,显示小红点
                EventBus.getDefault().post(new EventClass.ShowRechargeRedPoint());
            }
        }
    }

    public static void setHasReadRedPoint() {
        RedPointConfig r = getRedPointFromPreference();
        r.isRead = true;
        updateRedPointInPreference(r);
    }

    public static boolean isNeedShowRedPoint() {
        long now = System.currentTimeMillis();
        RedPointConfig r = getRedPointFromPreference();
        return now > r.startTime && now < r.endTime && !r.isRead;
    }

    public static String PREF_RECHARGE_RED_POINT_CONFIG = "pref_recharge_red_point_config";
    public static String KEY_START_TIME = "key_start_time";
    public static String KEY_END_TIME = "key_end_time";
    public static String KEY_UNIQ = "key_uniq";
    public static String KEY_HAS_READ = "key_has_read";


    public static class RedPointConfig {
        public long startTime;
        public long endTime;
        public String uniq;
        public boolean isRead;

        public static RedPointConfig toRedPointConfig(PayProto.RedPointConfig config) {
            RedPointConfig r = new RedPointConfig();
            if (config != null) {
                r.startTime = config.getStartTime();
                r.endTime = config.getEndTime();
                r.uniq = config.getUniq();
            }
            return r;
        }
    }

    public interface PullRechargeListIface {
        boolean isNotPullRechargeList();
    }

    // 余额详情
    public static Observable<PayProto.QueryBalanceDetailResponse> getBalanceDetailRsp() {
        return Observable.create(new Observable.OnSubscribe<PayProto.QueryBalanceDetailResponse>() {
            @Override
            public void call(Subscriber<? super PayProto.QueryBalanceDetailResponse> subscriber) {
                PayProto.QueryBalanceDetailRequest req = PayProto.QueryBalanceDetailRequest.newBuilder()
                        .setUuid(MyUserInfoManager.getInstance().getUser().getUid())
                        .setPlatform(PayProto.Platform.ANDROID)
                        .build();

                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_PAY_CREATE_ORDER);
                packetData.setData(req.toByteArray());
                MyLog.v(TAG, "createOrder request:" + req.toString());
                PacketData res = MiLinkClientAdapter.getsInstance().sendSync(packetData, 15 * 1000);
                try {
                    if (res != null) {
                        PayProto.QueryBalanceDetailResponse response = PayProto.QueryBalanceDetailResponse.parseFrom(res.getData());
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

}
