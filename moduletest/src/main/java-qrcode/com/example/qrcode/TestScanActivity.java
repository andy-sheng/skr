package com.example.qrcode;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.imagepicker.ResPicker;
import com.imagepicker.fragment.ResPickerFragment;
import com.imagepicker.model.ImageItem;
import com.wali.live.moduletest.R;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cn.bingoogolapple.qrcode.core.BarcodeType;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class TestScanActivity extends BaseActivity implements QRCodeView.Delegate {
    private static final String TAG = TestScanActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;

    private ZXingView mZXingView;

    private CommonTitleBar mCommonTitleBar;

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_test_scan;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(this);
        mCommonTitleBar = findViewById(R.id.titlebar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mZXingView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
//        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别

        mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void destroy() {
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        super.destroy();
    }

    private void vibrate() {
        // 震动一下
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        mCommonTitleBar.getCenterTextView().setText(result);
        vibrate();

        mZXingView.startSpot(); // 延迟0.1秒后开始识别
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        String tipText = mZXingView.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZXingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                mZXingView.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start_preview) {
            mZXingView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别

        } else if (i == R.id.stop_preview) {
            mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框

        } else if (i == R.id.start_spot) {
            mZXingView.startSpot(); // 延迟0.1秒后开始识别

        } else if (i == R.id.stop_spot) {
            mZXingView.stopSpot(); // 停止识别

        } else if (i == R.id.start_spot_showrect) {
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.stop_spot_hiddenrect) {
            mZXingView.stopSpotAndHiddenRect(); // 停止识别，并且隐藏扫描框

        } else if (i == R.id.show_scan_rect) {
            mZXingView.showScanRect(); // 显示扫描框

        } else if (i == R.id.hidden_scan_rect) {
            mZXingView.hiddenScanRect(); // 隐藏扫描框

        } else if (i == R.id.decode_scan_box_area) {
            mZXingView.getScanBoxView().setOnlyDecodeScanBoxArea(true); // 仅识别扫描框中的码

        } else if (i == R.id.decode_full_screen_area) {
            mZXingView.getScanBoxView().setOnlyDecodeScanBoxArea(false); // 识别整个屏幕中的码

        } else if (i == R.id.open_flashlight) {
            mZXingView.openFlashlight(); // 打开闪光灯

        } else if (i == R.id.close_flashlight) {
            mZXingView.closeFlashlight(); // 关闭闪光灯

        } else if (i == R.id.scan_one_dimension) {
            mZXingView.changeToScanBarcodeStyle(); // 切换成扫描条码样式
            mZXingView.setType(BarcodeType.ONE_DIMENSION, null); // 只识别一维条码
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.scan_two_dimension) {
            mZXingView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
            mZXingView.setType(BarcodeType.TWO_DIMENSION, null); // 只识别二维条码
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.scan_qr_code) {
            mZXingView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
            mZXingView.setType(BarcodeType.ONLY_QR_CODE, null); // 只识别 QR_CODE
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.scan_code128) {
            mZXingView.changeToScanBarcodeStyle(); // 切换成扫描条码样式
            mZXingView.setType(BarcodeType.ONLY_CODE_128, null); // 只识别 CODE_128
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.scan_ean13) {
            mZXingView.changeToScanBarcodeStyle(); // 切换成扫描条码样式
            mZXingView.setType(BarcodeType.ONLY_EAN_13, null); // 只识别 EAN_13
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.scan_high_frequency) {
            mZXingView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
            mZXingView.setType(BarcodeType.HIGH_FREQUENCY, null); // 只识别高频率格式，包括 QR_CODE、UPC_A、EAN_13、CODE_128
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.scan_all) {
            mZXingView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式
            mZXingView.setType(BarcodeType.ALL, null); // 识别所有类型的码
            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.scan_custom) {
            mZXingView.changeToScanQRCodeStyle(); // 切换成扫描二维码样式

            Map<DecodeHintType, Object> hintMap = new EnumMap<>(DecodeHintType.class);
            List<BarcodeFormat> formatList = new ArrayList<>();
            formatList.add(BarcodeFormat.QR_CODE);
            formatList.add(BarcodeFormat.UPC_A);
            formatList.add(BarcodeFormat.EAN_13);
            formatList.add(BarcodeFormat.CODE_128);
            hintMap.put(DecodeHintType.POSSIBLE_FORMATS, formatList); // 可能的编码格式
            hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE); // 花更多的时间用于寻找图上的编码，优化准确性，但不优化速度
            hintMap.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 编码字符集
            mZXingView.setType(BarcodeType.CUSTOM, hintMap); // 自定义识别的类型

            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别

        } else if (i == R.id.choose_qrcde_from_gallery) {/*
                从相册选取二维码图片，这里为了方便演示，使用的是
                https://github.com/bingoogolapple/BGAPhotoPicker-Android
                这个库来从图库中选择二维码图片，这个库不是必须的，你也可以通过自己的方式从图库中选择图片
                 */
            ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
                    .setMultiMode(false)
                    .setCrop(false)
                    .build()
            );
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, ResPickerFragment.class)
                    .setFragmentDataListener(new FragmentDataListener() {
                        @Override
                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle,Object object) {
                            ArrayList<ImageItem> list = ResPicker.getInstance().getSelectedResList();
                            mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别
                            mZXingView.decodeQRCode(list.get(0).getPath());
                        }
                    })
                    .build());

        }

    }



}