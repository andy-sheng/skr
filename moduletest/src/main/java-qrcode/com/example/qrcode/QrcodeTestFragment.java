package com.example.qrcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.permission.PermissionUtils;
import com.common.utils.U;
import com.wali.live.moduletest.R;

import java.util.List;

public class QrcodeTestFragment extends BaseFragment {

    TextView mTestScanQrcode;
    TextView mTestGenerateQrcode;

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public int initView() {
        return R.layout.qrcode_test_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTestScanQrcode = (TextView) mRootView.findViewById(R.id.test_scan_qrcode);
        mTestGenerateQrcode = (TextView) mRootView.findViewById(R.id.test_generate_qrcode);
        mTestScanQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (U.getPermissionUtils().checkCamera(getActivity())) {
                    startActivity(new Intent(getContext(), TestScanActivity.class));
                } else {
                    U.getPermissionUtils().requestCamera(new PermissionUtils.RequestPermission() {
                        @Override
                        public void onRequestPermissionSuccess() {

                        }

                        @Override
                        public void onRequestPermissionFailure(List<String> permissions) {

                        }

                        @Override
                        public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                        }
                    }, getActivity());
                }
            }
        });
        mTestGenerateQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TestGeneratectivity.class));
            }
        });
    }
}
