package com.common.utils;

import android.app.Activity;
import android.text.TextUtils;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.common.log.MyLog;
import com.common.permission.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Location Base Service
 * 地理位置相关服务
 */
public class LbsUtils {
    public final static String TAG = "LbsUtils";

    public static final String PREF_KEY_LOCATION_SYNC_TS = "location_sync_ts";

    public static final String PREF_KEY_LOCATION_INFO = "location_info";

    private LocationClient mLocationClient = null;

    private long mLastSyncTs = 0;

    private Callback mOneTimeCallback;

    private Location mLocation;

    LbsUtils() {
        LocationClientOption option = new LocationClientOption();
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        option.setCoorType("bd09ll");

        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        option.setScanSpan(0);

        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);

        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setLocationNotify(true);

        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.setIgnoreKillProcess(false);

        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false);

        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
        option.setWifiCacheTimeOut(5 * 60 * 1000);

        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        option.setEnableSimulateGps(false);

        //可选，是否需要地址信息，默认为不需要，即参数为false
        option.setIsNeedAddress(true);

        //可选，是否需要位置描述信息，默认为不需要，即参数为false
        option.setIsNeedLocationDescribe(true);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        mLocationClient = new LocationClient(U.app(), option);
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation l2) {
                mLocationClient.stop();
                //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
                //以下只列举部分获取经纬度相关（常用）的结果信息
                //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
                Address address = l2.getAddress();
                if (address != null) {
                    MyLog.w(TAG, "onReceiveLocation" + l2.getLocTypeDescription());
                    mLastSyncTs = System.currentTimeMillis();
                    // 如果是有效的
                    Location l = new Location();
                    l.setCoorType(l2.getCoorType());
                    l.setLatitude(l2.getLatitude());
                    l.setLongitude(l2.getLongitude());
                    l.setCountry(address.country);
                    l.setProvince(address.province);
                    l.setDistrict(address.district);
                    l.setCity(address.city);
                    l.setStreet(address.street);
                    l.setAddressDesc(l2.getAddrStr());
                    l.setLocationDesc(l2.getLocationDescribe());

                    mLocation = l;
                    MyLog.w(TAG, "onReceiveLocation mLocation:" + mLocation);
                    U.getPreferenceUtils().setSettingLong(PREF_KEY_LOCATION_SYNC_TS, mLastSyncTs);
                    U.getPreferenceUtils().setSettingString(PREF_KEY_LOCATION_INFO, mLocation.getJsonStr());
                }
                if (mOneTimeCallback != null) {
                    mOneTimeCallback.onReceive(mLocation);
                    mOneTimeCallback = null;
                }
            }
        });
        mLastSyncTs = U.getPreferenceUtils().getSettingLong(PREF_KEY_LOCATION_SYNC_TS, 0);
        mLocation = Location.parseFromJsonStr(U.getPreferenceUtils().getSettingString(PREF_KEY_LOCATION_INFO, ""));
    }

    public void getLocation(Callback callback) {
        getLocation(false, callback);
    }

    /**
     * 返回可用的地理位置
     * strict  true 为严格模式，每次必定实时去查
     * false 时 如果 间隔5分钟内就不会同步了，用上一次的结果
     *
     * @return
     */
    public void getLocation(boolean strict, Callback callback) {
        if (strict) {
            getLocationInner(callback);
        } else {
            /**
             * 5分钟内 可以拿来用
             */
            if (mLocation != null && mLocation.isValid()) {
                if (System.currentTimeMillis() - mLastSyncTs < 5 * 60 * 1000) {
                    callback.onReceive(mLocation);
                    return;
                }
            }
            // 还是同步查
            getLocationInner(callback);
        }
    }

    /**
     * 返回可用的地理位置
     * strict  true 为严格模式，每次必定实时去查
     * false 时 如果 间隔5分钟内就不会同步了，用上一次的结果
     *
     * @return
     */
    private void getLocationInner(Callback callback) {
        Activity activity = U.getActivityUtils().getTopActivity();
        if(activity == null){
            if (callback != null) {
                callback.onReceive(null);
            }
            return;
        }
        if (!U.getPermissionUtils().checkLocation(activity)) {
            U.getPermissionUtils().requestLocation(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    mOneTimeCallback = callback;
                    mLocationClient.start();
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    callback.onReceive(null);
                    if (MyLog.isDebugLogOpen()) {
                        U.getToastUtil().showShort("定位权限请求失败");
                    }
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    callback.onReceive(null);
                    if (MyLog.isDebugLogOpen()) {
                        U.getToastUtil().showShort("定位权限请求失败,不再询问");
                    }
                }
            }, U.getActivityUtils().getTopActivity());
        } else {
            mOneTimeCallback = callback;
            mLocationClient.start();
        }
    }

    public static class Location {
        String coorType;//获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
        double latitude;//纬度
        double longitude;//经度
        String country;//国家
        String province;//省
        String city;//市
        String district;//区
        String street;//街道
        String addressDesc;// 地址描述 如 中国北京海淀朱房路
        String locationDesc;// 位置描述 如 五彩城附近

        public String getCoorType() {
            return coorType;
        }

        public void setCoorType(String coorType) {
            this.coorType = coorType;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getAddressDesc() {
            return addressDesc;
        }

        public void setAddressDesc(String addressDesc) {
            this.addressDesc = addressDesc;
        }

        public String getLocationDesc() {
            return locationDesc;
        }

        public void setLocationDesc(String locationDesc) {
            this.locationDesc = locationDesc;
        }

        public boolean isValid(){
            return !TextUtils.isEmpty(addressDesc);
        }

        @Override
        public String toString() {
            return "Location{" +
                    "coorType='" + coorType + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", country='" + country + '\'' +
                    ", province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", district='" + district + '\'' +
                    ", street='" + street + '\'' +
                    ", addressDesc='" + addressDesc + '\'' +
                    ", locationDesc='" + locationDesc + '\'' +
                    '}';
        }

        public String getJsonStr() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("coorType", coorType);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("country", country);
                jsonObject.put("province", province);
                jsonObject.put("city", city);
                jsonObject.put("district", district);
                jsonObject.put("street", street);
                jsonObject.put("addressDesc", addressDesc);
                jsonObject.put("locationDesc", locationDesc);
            } catch (JSONException e) {
                MyLog.d(TAG, e);
            }
            return jsonObject.toString();
        }

        public static Location parseFromJsonStr(String str) {
            if (str == null && "".equals(str)){
                return null;
            }

            try {
                JSONObject jsonObject = new JSONObject(str);
                String coorType = jsonObject.optString("coorType");
                double latitude = jsonObject.optDouble("latitude");
                double longitude = jsonObject.optDouble("longitude");
                String country = jsonObject.optString("country");
                String province = jsonObject.optString("province");
                String city = jsonObject.optString("city");
                String district = jsonObject.optString("district");
                String street = jsonObject.optString("street");
                String addressDesc = jsonObject.optString("addressDesc");
                String locationDesc = jsonObject.optString("locationDesc");

                Location location = new Location();
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setCoorType(coorType);
                location.setCountry(country);
                location.setProvince(province);
                location.setDistrict(district);
                location.setCity(city);
                location.setStreet(street);
                location.setAddressDesc(addressDesc);
                location.setLocationDesc(locationDesc);
                return location;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public interface Callback {
        void onReceive(Location location);
    }
}
