package com.module.home.presenter;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.pay.IPayCallBack;
import com.common.core.pay.PayApi;
import com.common.core.pay.ali.AliPayReq;
import com.common.core.pay.wx.WxPayReq;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.home.WalletServerApi;
import com.module.home.inter.IBallanceView;
import com.module.home.model.RechargeItemModel;

import java.util.HashMap;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;

//同时只能有一个订单
public class BallencePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "BallencePresenter";

    PayApi mPayApi;

    IBallanceView mIBallanceView;

    WalletServerApi mWalletServerApi;

    String orderID;

    BaseActivity mBaseActivity;

    public BallencePresenter(Activity activity, IBallanceView iBallanceView) {
        mIBallanceView = iBallanceView;
        mBaseActivity = (BaseActivity) activity;
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
        mPayApi = new PayApi(mBaseActivity, new IPayCallBack() {
            @Override
            public void onFaild() {
                mIBallanceView.rechargeFailed();
            }

            @Override
            public void onSuccess() {
                mIBallanceView.rechargeSuccess();
            }
        });
    }

    public void getGoodsList() {
        //20代表安卓平台
        ApiMethods.subscribe(mWalletServerApi.getGoodsList("20"), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<RechargeItemModel> list = JSON.parseArray(obj.getData().getString("list"), RechargeItemModel.class);
                    mIBallanceView.showRechargeList(list);
                }
            }
        });
    }

    public void getZSBalance() {
        ApiMethods.subscribe(mWalletServerApi.getZSBalance(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    long amount = JSON.parseObject(obj.getData().getString("totalAmount"), Long.class);
                    mIBallanceView.showBalance(amount);
                }
            }
        });
    }

    public void rechargeAliPay(String goodsID) {
        HashMap<String, Object> map = new HashMap<>();
        String ts = System.currentTimeMillis() + "";
        map.put("goodsID", goodsID);
        map.put("timeMs", ts);
        String sign = U.getMD5Utils().MD5_32("skrer|"
                + MyUserInfoManager.getInstance().getUid() + "|"
                + goodsID + "|"
                + "dbf555fe9347eef8c74c5ff6b9f047dd" + "|"
                + ts);
        map.put("sign", sign);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mWalletServerApi.aliOrder(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    orderID = obj.getData().getString("orderID");
                    String orderInfo = obj.getData().getString("paySign");
                    mPayApi.pay(new AliPayReq(orderInfo));
                }
            }
        });
    }

    public void rechargeWxPay(String goodsID) {
        HashMap<String, Object> map = new HashMap<>();
        String ts = System.currentTimeMillis() + "";
        map.put("goodsID", goodsID);
        map.put("timeMs", ts);
        String sign = U.getMD5Utils().MD5_32("skrer|"
                + MyUserInfoManager.getInstance().getUid() + "|"
                + goodsID + "|"
                + "dbf555fe9347eef8c74c5ff6b9f047dd" + "|"
                + ts);
        map.put("sign", sign);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mWalletServerApi.wxOrder(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    orderID = obj.getData().getString("orderID");
                    String prepayID = obj.getData().getString("prepayID");
                    mPayApi.pay(new WxPayReq(prepayID, sign));
                }
            }
        });
    }

    public void checkWxOrder(String orderId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderID", orderId);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mWalletServerApi.wxOrderCheck(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {

                }
            }
        });
    }

    public void checkAliOrder(String goodsID) {
        HashMap<String, Object> map = new HashMap<>();
        String ts = System.currentTimeMillis() + "";
        map.put("orderID", goodsID);
        map.put("tradeNo", ts);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mWalletServerApi.aliOrderCheck(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {

                }
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        mPayApi.release();
    }
}
