package com.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MD5Utils {

    MD5Utils() {

    }

    private String byte2Hex(byte b) {
        int value = (b & 0x7F) + (b < 0 ? 0x80 : 0);
        return (value < 0x10 ? "0" : "")
                + Integer.toHexString(value).toLowerCase();
    }

    public String MD5_32(String passwd) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        StringBuffer strbuf = new StringBuffer();

        //md5.update(passwd.getBytes(), 0, passwd.length());
        md5.update(passwd.getBytes(), 0, passwd.getBytes().length);
        byte[] digest = md5.digest();

        for (int i = 0; i < digest.length; i++) {
            strbuf.append(byte2Hex(digest[i]));
        }

        return strbuf.toString();
    }

    public String signReq(HashMap<String, Object> map) {
        String[] stringArray = new String[map.size()];
        {
            int i = 0;
            for (String key : map.keySet()) {
                stringArray[i++] = key;
            }
        }

        Arrays.sort(stringArray);

        StringBuffer stringBuffer = new StringBuffer();

        try {
            for (int i = 0; i < stringArray.length; i++) {
                stringBuffer.append(URLEncoder.encode(stringArray[i], "UTF-8") + "=" + URLEncoder.encode(String.valueOf(map.get(stringArray[i])), "UTF-8"));
                if (i != stringArray.length - 1) {
                    stringBuffer.append("&");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return MD5_32(stringBuffer.toString());
    }

    /**
     * 这里MD5_16取了MD5_32的中间16位
     */
    public String MD5_16(String passwd) {
        return MD5_32(passwd).subSequence(8, 24).toString();
    }


    //    public static String getMd5Digest(byte[] bytes) {
//        try {
//            MessageDigest lDigest = MessageDigest.getInstance("MD5");
//            lDigest.update(bytes);
//            BigInteger lHashInt = new BigInteger(1, lDigest.digest());
//            return String.format(Locale.US, "%1$032X", lHashInt);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
