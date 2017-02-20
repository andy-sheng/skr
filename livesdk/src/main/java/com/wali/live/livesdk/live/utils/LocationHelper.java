package com.wali.live.livesdk.live.utils;

import android.text.TextUtils;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.base.global.GlobalData;
import com.base.log.MyLog;

/**
 * Created by mi on 2016/2/25.
 *
 * @module 百度定位
 */
public class LocationHelper implements BDLocationListener {

    public static final String LogTag = LocationHelper.class.getName();
    private String addressDes;
    private double mLatitude;
    private double mLongitude;

    AddressCallback mAddressCallback;
    boolean isSDKInitialized = false;
    //LocationClient类必须在主线程中声明。需要Context类型的参数。
    private LocationClient mLocClient;

    private Address mAddr;
    public String address;

    private static LocationHelper sInstance;

    private LocationHelper() {
        if (!isSDKInitialized && GlobalData.app() != null) {
            MyLog.d(LogTag, "LocationHelper()  isSDKInitialized");
            SDKInitializer.initialize(GlobalData.app());

            isSDKInitialized = true;
        }
    }

    public static LocationHelper getInstance() {
        synchronized (LocationHelper.class) {
            if (sInstance == null) {
                sInstance = new LocationHelper();
            }
        }
        return sInstance;
    }

    public void getAddress(AddressCallback mAddressCallback) {
        boolean isUseCache = isCacheValid();
        getAddress(true, isUseCache, mAddressCallback);
    }

    public void getAddress(boolean isUseCache, AddressCallback mAddressCallback) {
        getAddress(true, isUseCache, mAddressCallback);
    }

    public void getAddress(boolean isUseGPS, boolean isUseCache, AddressCallback addressCallback) {
        if (isUseCache && mAddr != null && !TextUtils.isEmpty(mAddr.address)) {
            MyLog.d(LogTag, "getAddress  UseCache");
            if (addressCallback != null) {
                addressCallback.returnAddress(mLatitude, mLongitude, mAddr);
            }
            return;
        }
        if (addressCallback != null) {
            mAddressCallback = addressCallback;
        }
        getBDLocation(isUseGPS);
    }

    public void initLocationClient() {
        if (!isSDKInitialized && GlobalData.app() != null) {
            MyLog.d(LogTag, "initLocationClient  isSDKInitialized");
            SDKInitializer.initialize(GlobalData.app());
            isSDKInitialized = true;
        }
        MyLog.d(LogTag, "initLocationClient ");
        // 定位初始化
        mLocClient = new LocationClient(GlobalData.app());
        mLocClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.disableCache(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    private void getBDLocation(boolean isUseGPS) {
        if (!isSDKInitialized) {
            MyLog.d(LogTag, "getBDLocation  isSDKInitialized");
            SDKInitializer.initialize(GlobalData.app());
            isSDKInitialized = true;
        }
        // 定位初始化
        if (mLocClient == null) {
            mLocClient = new LocationClient(GlobalData.app());
            mLocClient.registerLocationListener(this);
        }

        //LocationClientOption option =  mLocClient.getLocOption();
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);


        if (isUseGPS) {
            option.setOpenGps(true); // 打开gps
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        } else {
            option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        }
        option.disableCache(true);

        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    private void updateLocation(BDLocation location) {

        if (location == null) {
            return;
        }
        MyLog.d(LogTag, "updateLocation");
        MyLog.d(LogTag, "BDLocation LocType == " + location.getLocType());
        mAddr = location.getAddress();
        address = mAddr.address;
        //address = location.getAddrStr();
        addressDes = location.getLocationDescribe();
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        MyLog.d(LogTag, "BDLocation address == " + address);
        MyLog.d(LogTag, "BDLocation mLatitude == " + mLatitude);
        MyLog.d(LogTag, "BDLocation mLongitude == " + mLongitude);
        MyLog.d(LogTag, "BDLocation addressDes == " + addressDes);
        if (mAddressCallback != null) {
            mAddressCallback.returnAddress(mLatitude, mLongitude, mAddr);
            mAddressCallback = null;
        }
        lastGetLocationTime = System.currentTimeMillis();
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        // 销毁定位
        mLocClient.stop();
        if (location == null) {
            MyLog.d(LogTag, "location == null");
            if (mAddressCallback != null) {
                mAddressCallback.returnAddress(0, 0, null);
                mAddressCallback = null;
            }
        } else {
            updateLocation(location);
        }
    }

    public interface AddressCallback {
        void returnAddress(double latitude, double longitude, Address address);
    }

    private final int GET_ADDRESS_INTERVAL = 60 * 1000;
    private long lastGetLocationTime = 0;

    public boolean isCacheValid() {
        return System.currentTimeMillis() - lastGetLocationTime < GET_ADDRESS_INTERVAL;
    }

    public void clear() {
        mAddressCallback = null;
    }
}


