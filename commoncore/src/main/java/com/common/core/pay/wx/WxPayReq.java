package com.common.core.pay.wx;

import com.common.core.pay.EPayPlatform;
import com.common.core.pay.PayBaseReq;
import com.common.core.share.ShareManager;
import com.tencent.mm.opensdk.modelpay.PayReq;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class WxPayReq extends PayBaseReq {
    static final String WXapiKey = "c2e0219f57a71313a03c15a2a3d8772e";
    final String mPartnterId = "1525798071";
    private PayReq req;
    String mOrderID;

    public WxPayReq(String prepayid, String nonceStr, String orderID) {
        mEPayPlatform = EPayPlatform.WX_PAY;

        PayReq req = new PayReq();
        String ts = System.currentTimeMillis() / 1000 + "";
        req.appId = ShareManager.WX_APP_ID;
        req.partnerId = mPartnterId;
        req.prepayId = prepayid;
        req.nonceStr = nonceStr;   //随机字符串
        req.timeStamp = ts;
        req.packageValue = "Sign=WXPay";

        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put("appid", ShareManager.WX_APP_ID);
        parameters.put("noncestr", nonceStr);
        parameters.put("package", "Sign=WXPay");
        parameters.put("partnerid", "1525798071");
        parameters.put("prepayid", prepayid);
        parameters.put("timestamp", ts);

        String characterEncoding = "UTF-8";
        String mySign = createSign(characterEncoding,parameters);

        req.sign = mySign;
        mOrderID = orderID;

        this.req = req;
    }

    public String getOrderID() {
        return mOrderID;
    }

    @SuppressWarnings("unchecked")
    public static String createSign(String characterEncoding,SortedMap<Object,Object> parameters){
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            if(null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + WXapiKey);
        String sign = MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname))
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes(charsetname)));
        } catch (Exception exception) {
        }
        return resultString;
    }

    private static final String hexDigits[] = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    public PayReq getReq() {
        return req;
    }

    public void setReq(PayReq req) {
        this.req = req;
    }


}
