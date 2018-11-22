package com.common.core.upload;

import android.text.TextUtils;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.wali.live.proto.AuthUpload.AuthRequest;
import com.wali.live.proto.AuthUpload.AuthResponse;
import com.wali.live.proto.AuthUpload.AuthType;

public class UploadServerApi {
    public final static String TAG = "UploadServerApi";

    /**
     * 授权客户端对ks3的API操作
     *
     * @param httpVerb
     * @param contentMd5
     * @param contentType
     * @param date                 "Wed, 25 Mar 2015 03:50:50 GMT"
     * @param canonicalizedHeaders
     * @return
     */
    public static AuthResponse getKs3AuthToken(long rid, final String httpVerb, final String contentMd5,
                                               final String contentType, final String date,
                                               final String canonicalizedHeaders, final String suffix, final AuthType type) {

        if (TextUtils.isEmpty(httpVerb) || TextUtils.isEmpty(date)) {
            return null;
        }

        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.setRid(rid);
        authRequestBuilder.setHttpVerb(httpVerb);
        authRequestBuilder.setContentMd5(contentMd5);
        authRequestBuilder.setContentType(contentType);
        //authRequestBuilder.setContentType("binary/octet-stream");
        //authRequestBuilder.setDate(date);
        authRequestBuilder.setDate("");//服务器下发时间为准
        authRequestBuilder.setCanonicalizedHeaders(canonicalizedHeaders);
        authRequestBuilder.setSuffix(suffix);

        authRequestBuilder.setAuthType(type);

        authRequestBuilder.setVuid(UserAccountManager.getInstance().getUuidAsLong());
        final AuthRequest authRequest = authRequestBuilder.build();
        try {
            //向业务服务器获取K3 request token
//            PacketData data = new PacketData();
//            data.setCommand("zhibo.mfas.auth");
//            data.setData(authRequest.toByteArray());
//            PacketData authPacketData = MiLinkClientAdapter.getInstance().sendSync(data, 10*1000);
//            return processPacketData(authPacketData);
        } catch (Exception e) {
            MyLog.v(TAG, e);

        }
        return null;
    }

//    private static AuthResponse processPacketData(PacketData data) {
//        if (data == null) {
//            return null;
//        }
//        MyLog.v(TAG, "processPacketData command=" + data.getCommand());
//        String command = data.getCommand();
//        if (TextUtils.isEmpty(command)) {
//            return null;
//        }
//        if (command.equals("zhibo.mfas.auth")) {
//            try {
//
//                AuthResponse response = AuthResponse.parseFrom(data.getData());
//                if (response.getErrorCode() == 0) {
//                    return response;
//                } else {
//                    return null;
//                }
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 分片上传认证
//     * todo 等服务器支持
//     * @param attId
//     * @param resource
//     * @param date
//     * @param httpVerb
//     * @param contentMd5
//     * @param contentType
//     * @param acl
//     * @param bucketName
//     * @return
//     */
//    public static String getMultipartKs3AuthToken(long attId, String resource, String date, String httpVerb, String contentMd5, String contentType, String acl, String bucketName) {
//        MultipartAuthRequest multipartAuthRequest = new MultipartAuthRequest.Builder()
//                .setRid(attId).setResource(resource).setDate(date).setHttpVerb(httpVerb).setContentMd5(contentMd5).setContentType(contentType)
//                .setAcl(acl).build();
//
//        PacketData packetData = new PacketData();
//        packetData.setCommand("zhibo.mfas.multipartauth");
//        packetData.setData(multipartAuthRequest.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, 10*1000);
//        if (responseData != null) {
//            try {
//                MultipartAuthResponse response = MultipartAuthResponse.parseFrom(responseData.getData());
//                if (response != null && response.getErrorCode() == 0) {
//                    return response.getMultipartAuthorization();
//                }
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//        }
//        return null;
//    }
}
