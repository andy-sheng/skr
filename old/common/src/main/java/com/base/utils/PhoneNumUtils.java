
package com.base.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.base.common.R;
import com.base.global.GlobalData;
import com.base.log.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PhoneNumUtils {

    public static final int MOBILE_PHONE_LENGTH = 11; // Mobile phone # length

    private static final String TAG = PhoneNumUtils.class.getSimpleName();

    public static final int FORMATTED_TELEPHONE_NUMBER_LENGTH = 13;

    public static final String DEFAULT_COUNTRY_CODE = "+86";

    private static String patternMobile = "(\\+)?\\d{11,20}";

    private static String pattern = "(\\+)?\\d{1,20}";

    private static Pattern regexMoblie = Pattern.compile(patternMobile);

    private static Pattern regex = Pattern.compile(pattern);

    private static HashMap<String, CountryPhoneNumData> sMapCountryPhoneData;

    private static Pattern regexFormattedNumber = Pattern.compile("1\\d{2}\\s\\d{4}\\s\\d{4}");

    public static synchronized void ensureDataLoaded() {
        if (sMapCountryPhoneData == null) {
            sMapCountryPhoneData = new HashMap<String, CountryPhoneNumData>();
            String strCountries = "";
            InputStream is = null;

            try {
                is = GlobalData.app().getResources().openRawResource(R.raw.countries);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[512];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
                strCountries = baos.toString();
                baos.close();
                JSONObject obj = new JSONObject(strCountries);
                JSONArray ary = obj.getJSONArray("countries");
                for (int i = 0; i < ary.length(); i++) {
                    JSONObject oneObj = ary.getJSONObject(i);

                    String name = oneObj.getString("cn");
                    String code = oneObj.getString("ic");
                    String iso = oneObj.getString("iso");
                    CountryPhoneNumData data = new CountryPhoneNumData(name, code, iso);
                    JSONArray lengthAry = oneObj.optJSONArray("len");
                    if (lengthAry != null) {
                        ArrayList<Integer> lengths = new ArrayList<Integer>();
                        for (int j = 0; j < lengthAry.length(); j++) {
                            lengths.add(lengthAry.getInt(j));
                        }
                        data.lengths = lengths;
                    }
                    JSONArray prefixAry = oneObj.optJSONArray("mc");
                    if (prefixAry != null) {
                        ArrayList<String> prefixes = new ArrayList<String>();
                        for (int j = 0; j < prefixAry.length(); j++) {
                            prefixes.add(prefixAry.getString(j));
                        }
                        data.prefix = prefixes;
                    }
                    sMapCountryPhoneData.put(iso, data);
                }

            } catch (IOException e) {
                MyLog.e(e);
            } catch (JSONException e) {
                MyLog.e(e);
            } finally {
                if (null != is) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public static List<String> getCountryList() {
        ensureDataLoaded();
        ArrayList<String> countries = new ArrayList<String>(sMapCountryPhoneData.size());
        for (Entry<String, CountryPhoneNumData> oneEntry : sMapCountryPhoneData.entrySet()) {
            countries.add(oneEntry.getValue().countryName);
        }
        Collections.sort(countries);
        return countries;
    }

    public static List<CountryPhoneNumData> getCountryPhoneNumDataList() {
        ensureDataLoaded();
        ArrayList<CountryPhoneNumData> dataList = new ArrayList<CountryPhoneNumData>(
                sMapCountryPhoneData.size());
        Collection<CountryPhoneNumData> datas = sMapCountryPhoneData.values();

        for (CountryPhoneNumData data : datas) {
            dataList.add(data);
        }
        Collections.sort(dataList);
        return dataList;
    }

    public static String getCountryISOFromName(String name) {
        ensureDataLoaded();
        for (Entry<String, CountryPhoneNumData> oneEntry : sMapCountryPhoneData.entrySet()) {
            if (oneEntry.getValue().countryName.equals(name)) {
                return oneEntry.getKey();
            }
        }
        return "";
    }

    public static String getCountryISOFromPhoneNum(String phone) {
        if (!phone.startsWith("+")) {
            return "CN";
        }
        ensureDataLoaded();
        for (Entry<String, CountryPhoneNumData> oneEntry : sMapCountryPhoneData.entrySet()) {
            if (phone.startsWith("+" + oneEntry.getValue().countryCode)) {
                return oneEntry.getKey();
            }
        }
        return "";
    }

    public static String normalizeTelephoneNumber(String phoneNumber) {
        return normalizeTelephoneNumber(GlobalData.app(), phoneNumber);
    }

    public static String normalizeTelephoneNumber(Context context, String phoneNumber) {
        return normalizePhoneNumber(context, phoneNumber, false);
    }

    public static String normalizePhoneNumber(String phoneNumber) {
        return normalizePhoneNumber(GlobalData.app(), phoneNumber);
    }

    public static String normalizePhoneNumber(Context context, String phoneNumber) {
        return normalizePhoneNumber(context, phoneNumber, true);
    }

    public static String normalizePhoneNumber(Context context, String phoneNumber, boolean isMobile) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return null;
        }

        // 去掉电话号码里面的其他符号，只保留数字

        StringBuilder sb = new StringBuilder(phoneNumber.length());
        for (int iChar = 0; iChar < phoneNumber.length(); iChar++) {
            char curChar = phoneNumber.charAt(iChar);
            if (curChar == '+' && iChar == 0) {
                sb.append(curChar);
            } else if (curChar >= '0' && curChar <= '9') {
                sb.append(curChar);
            }
        }
        phoneNumber = sb.toString();

        String countryIso = CommonUtils.getCountryISO(context).toUpperCase();
        if (phoneNumber.startsWith("+86")
                || phoneNumber.startsWith("0086")
                || ((("CN".equals(countryIso)) || CommonUtils.isChineseSimCard(context))
                && !phoneNumber.startsWith("+") && !phoneNumber
                .startsWith("00"))) {
            // 对于中国的手机号，取后11为数字，前面不加国家码
            if (isMobile && !regexMoblie.matcher(phoneNumber).matches()) {
                return null;
            } else if (!regex.matcher(phoneNumber).matches()) {
                return null;
            }

            if (phoneNumber != null && phoneNumber.length() != 0) {
                if (phoneNumber.length() >= MOBILE_PHONE_LENGTH) {
                    phoneNumber = phoneNumber.substring(phoneNumber.length() - MOBILE_PHONE_LENGTH,
                            phoneNumber.length());
                }
                if (isMobile) {
                    // 只考虑手机号的最后11位\
                    if (phoneNumber.charAt(0) == '1') {
                        return phoneNumber;
                    }
                } else {
                    return phoneNumber;
                }
            }
        } else {
            // 我们在ChannelApplication中只对外国SIMCARD做INIT，现在由于中国SIMCARD也有可能去NormalizeNumber,如果没有Init,在此重新初始化
            ensureDataLoaded();

            // 如果00开头，就将00换成+
            if (phoneNumber.startsWith("00")) {
                phoneNumber = phoneNumber.replaceFirst("00", "+");
            }
            if (phoneNumber.startsWith("+")) {
                // 号码已经是一个国际号,直接返回
                phoneNumber = checkCountryNumAndProcess(phoneNumber);
                return phoneNumber;
            } else {
                if (phoneNumber.startsWith("0")) {
                    phoneNumber = phoneNumber.substring(1);
                }
                if (TextUtils.isEmpty(countryIso) || !sMapCountryPhoneData.containsKey(countryIso)) {
                    // 国家为空或该国家代码不在列表中，返回空
                    return null;
                }
                CountryPhoneNumData data = sMapCountryPhoneData.get(countryIso);
                return checkNumber(phoneNumber, data);
            }
        }

        return null;
    }

    /**
     * 根据 phoneNumber 和 找到的 国家限制DATA 查看是否符合规则
     *
     * @param phoneNumber 前面不能有+，这是国家内的号码
     * @param data
     * @return
     */
    public static String checkNumber(String phoneNumber, CountryPhoneNumData data) {
        if (phoneNumber.startsWith("+") || phoneNumber.startsWith("00") || data == null) {
            MyLog.e("phoneNumber 为空或者是 data为空 phoneNumber = " + phoneNumber + " data = "
                    + data.toString());
            return null;
        }

        if (data.lengths != null) {
            boolean matchLength = false;
            for (int i : data.lengths) {
                if (phoneNumber.length() == i) {
                    matchLength = true;
                    break;
                }
            }
            if (!matchLength) {
                return null;
            }
        }
        if (data.prefix != null) {
            boolean matchPrefix = false;
            for (String prefix : data.prefix) {
                if (prefix.startsWith("x")) {
                    int lastX = prefix.lastIndexOf("x");
                    if (lastX > phoneNumber.length())
                        continue;
                    if (phoneNumber.substring(lastX + 1)
                            .startsWith(prefix.substring(lastX + 1))) {
                        matchPrefix = true;
                        break;
                    }
                } else if (phoneNumber.startsWith(prefix)) {
                    matchPrefix = true;
                    break;
                }
            }
            if (!matchPrefix) {
                return null;
            }
        }
        StringBuilder phoneNumBuilder = new StringBuilder();
        phoneNumBuilder.append("+").append(data.countryCode).append(phoneNumber);
        return phoneNumBuilder.toString();
    }

    /**
     * 根据号码做进一步的校验，比如国家码是否存在，并且应LINA要求，除去国家码，如号码以0开头，去掉0，
     *
     * @param phoneNum 传入来的参数一定要是 +开头的，
     * @return
     */
    public static String checkCountryNumAndProcess(String phoneNum) {
        ensureDataLoaded();
        List<CountryPhoneNumData> dataList = new ArrayList<CountryPhoneNumData>();
        for (CountryPhoneNumData data : sMapCountryPhoneData.values()) {
            if (phoneNum.startsWith("+" + data.countryCode)) {
                dataList.add(data);
            }
        }
        if (dataList.size() == 0) {
            MyLog.e("没有此国际号，phoneNum = " + phoneNum);
            return null;
        }

        String num = null;
        for (CountryPhoneNumData data : dataList) {
            // 去0
            if (phoneNum.length() > data.countryCode.length() + 1
                    && phoneNum.charAt(data.countryCode.length() + 1) == '0') {
                phoneNum = "+" + data.countryCode
                        + phoneNum.substring(data.countryCode.length() + 2);
            }
            num = checkNumber(phoneNum.substring(data.countryCode.length() + 1), data);
            if (num != null) {
                break;
            }
        }
        return num;
    }

    public static CountryPhoneNumData getCounrtyPhoneDataFromIso(String iso) {
        ensureDataLoaded();
        return sMapCountryPhoneData.get(iso.toUpperCase());
    }

    // 是否是中国移动
    public static boolean isChinaMobile(final Context context) {
        final TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String operator = telManager.getSimOperator();
        return ("46000".equals(operator) || "46002".equals(operator)) || "46007".equals(operator);
    }

    // 中国联通
    public static boolean isChinaUnicom(final Context context) {
        final TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String operator = telManager.getSimOperator();
        return "46001".equals(operator);
    }

    // 中国电信
    public static boolean isChinaTelecom(final Context context) {
        final TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String operator = telManager.getSimOperator();
        return "46003".equals(operator);
    }

    public static class CountryPhoneNumData implements
            Comparable<CountryPhoneNumData> {

        public String countryName;

        public String countryCode;

        public String countryISO;

        public char index;

        public ArrayList<Integer> lengths;

        ArrayList<String> prefix;

        public CountryPhoneNumData(String name, String code, String iso) {
            countryName = name;
            countryCode = code;
            countryISO = iso;
            if (!TextUtils.isEmpty(countryName)) {
                index = name.charAt(0);
            } else {
                index = '!';
            }
        }

        @Override
        public int compareTo(CountryPhoneNumData another) {
            return countryName.compareTo(another.countryName);
        }
    }


    public static String getCountryISO(final Context context) {
        String country = getCountryISOFromSimCard(context);
        return country.toUpperCase();
    }

    /**
     * 对于没有sim卡的手机，返回的是空串
     *
     * @param context
     * @return
     */
    public static String getCountryISOFromSimCard(final Context context) {
        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }

    public static boolean isChineseSimCard(final Context context) {
        return "CN".equalsIgnoreCase(getCountryISOFromSimCard(context));
    }

    public static boolean isValidNumber(String number) {
        return number != null && TextUtils.isDigitsOnly(number)
                && number.length() == 11 && number.startsWith("1");
    }

    // 以3 4 4的格式显示的手机号
    public static boolean isValidFormattedNumber(String number) {
        return number.length() == FORMATTED_TELEPHONE_NUMBER_LENGTH && regexFormattedNumber.matcher(number).matches();
    }

    public static boolean isValidNumber(String countryCode, String number) {
        if (DEFAULT_COUNTRY_CODE.equals(countryCode)) {
            return number != null && TextUtils.isDigitsOnly(number)
                    && number.length() == 11 && number.startsWith("1");
        } else {
            return number != null && TextUtils.isDigitsOnly(number)
                    && number.length() < 11 && 8 < number.length();
        }
    }

    public static boolean isValidNumber(String countryCode, String number, ArrayList<Integer> lens) {
        if (DEFAULT_COUNTRY_CODE.equals(countryCode)) {
            return number != null && TextUtils.isDigitsOnly(number)
                    && number.length() == 11 && number.startsWith("1");
        } else {
            boolean validLen = false;
            if (lens == null || lens.isEmpty()) {
                validLen = number.length() <= 11 && 8 <= number.length();
            } else {
                validLen = lens.contains(number.length());
                MyLog.v("validLen" + validLen);
            }
            return number != null && TextUtils.isDigitsOnly(number)
                    && validLen;
        }
    }

    /**
     * 获取本地的SIM卡对应的手机号，存在问题，有些SIM卡不支持获取手机号操作
     *
     * @param context
     * @return 格式化之后的手机号
     */
    public static String fetchLocalPhoneNumber(final Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String tel = tm.getLine1Number();//手机号码
        tel = normalizePhoneNumber(tel);
        return tel;
    }

    /**
     * 从字符串中提取电话号码
     *
     * @param str
     * @return
     */
    public static String extractPhoneNumber(String str) {
        String phoneNumber = "";
        try {
            List<String> ls = new ArrayList<String>();
            Pattern verifyCodePattern = Pattern.compile("[0-9]{11,13}+");
            Matcher verifyCodeMatcher = verifyCodePattern.matcher(str);

            while (verifyCodeMatcher.find()) {
                phoneNumber = verifyCodeMatcher.group(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phoneNumber;
    }


    // 格式化：去除+86，以3 4 4的格式显示
    public static String formatPhoneNum(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        phoneNumber = phoneNumber.trim();
        boolean needFormat = true;
        for (int i = 0; i < phoneNumber.length(); i++) {
            char ch = phoneNumber.charAt(i);
            if (i == 0 && ch == '+') {
                continue;
            }
            if (ch == ' ') {
                continue;
            }
            if (ch < '0' || ch > '9') {
                needFormat = false;
                break;
            }
        }
        if (needFormat) {
            if (phoneNumber.startsWith(DEFAULT_COUNTRY_CODE)) {
                phoneNumber = phoneNumber.substring(3);
                phoneNumber = phoneNumber.trim();
            }
            if (phoneNumber.length() == 11) {
                StringBuilder sb = new StringBuilder(15);
                for (int i = 0; i < phoneNumber.length(); i++) {
                    sb.append(phoneNumber.charAt(i));
                    if (sb.length() == 3 || sb.length() == 8) {
                        sb.append(" ");
                    }
                }
                return sb.toString();
            }
        }
        return phoneNumber;
    }

    //以3...4格式显示
    public static String formatPhoneNumToSimple(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 11) {
            return "";
        }
        return formatPhoneNum(phoneNumber).substring(0, 3) + "..." + formatPhoneNum(phoneNumber).substring(formatPhoneNum(phoneNumber).length() - 4);
    }

    /**
     * 根据国际区号格式化电话号码，目前只支持格式化中国的电话号码
     *
     * @param countryCode
     * @param phoneNumber
     * @return 格式化之后的电话号码
     */
    public static String formatPhoneNum(String countryCode, String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        String num = phoneNumber.trim().replaceAll(" ", "");
        if (DEFAULT_COUNTRY_CODE.equals(countryCode)) {
            if (3 < num.length() && num.length() < 8) {
                num = new StringBuffer(num).insert(3, " ").toString();
            } else if (7 < num.length()) {
                num = new StringBuffer(num).insert(7, " ").insert(3, " ").toString();
            }
        } else {
            num = num.replaceAll(" ", "");
        }
        return num;
    }
}
