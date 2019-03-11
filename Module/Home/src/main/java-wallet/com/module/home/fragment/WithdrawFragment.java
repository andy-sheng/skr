package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;
import com.module.home.inter.IWithDrawView;

public class WithdrawFragment extends BaseFragment implements IWithDrawView {

    CommonTitleBar mTitlebar;

    TextView mTvCertification;

    @Override
    public int initView() {
        return R.layout.withdraw_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTvCertification = (TextView)mRootView.findViewById(R.id.tv_certification);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mTvCertification.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
//                AlipayClient alipayClient = null;
////                        new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2");
//                ZhimaCustomerCertificationInitializeRequest request = new ZhimaCustomerCertificationInitializeRequest();
//
//                String bizContent = "{"
//                        + "\"transaction_id\":\"ZGYD201610252323000001234\","
//                        + "\"product_code\":\"w1010100000000002978\","
//                        + "\"biz_code\":\"FACE\","
//                        + "\"identity_param\":\"{\\\"identity_type\\\":\\\"CERT_INFO\\\",\\\"cert_type\\\":\\\"IDENTITY_CARD\\\",\\\"cert_name\\\":\\\"张三\\\",\\\"cert_no\\\":\\\"260104197909275964\\\"}\","
//                        + "\"ext_biz_param\":\"{}\"" + "  }";
//                request.setBizContent(bizContent);
//
//                ZhimaCustomerCertificationInitializeResponse response = null;
//
//                try {
//                    alipayClient.execute(request);
//                } catch (AlipayApiException e) {
//                    e.printStackTrace();
//                }
//
//                if (response != null && response.isSuccess()) {
//                    response.getBizNo();
//                    System.out.println("调用成功");
//                } else {
//                    System.out.println("调用失败");
//                }
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
