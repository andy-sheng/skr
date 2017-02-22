package com.wali.live.upload;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.assist.Attachment;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.AuthUploadFileProto;

public class FileUploadSenderWorker {
    private static final String TAG = FileUploadSenderWorker.class.getSimpleName();

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
    public static AuthUploadFileProto.AuthResponse getKs3AuthToken(long rid, final String httpVerb, final String contentMd5,
                                                                   final String contentType, final String date,
                                                                   final String canonicalizedHeaders, final String suffix, final int type) {

        if (TextUtils.isEmpty(httpVerb) || TextUtils.isEmpty(date)) {
            return null;
        }

        AuthUploadFileProto.AuthRequest.Builder authRequestBuilder = AuthUploadFileProto.AuthRequest.newBuilder();
        authRequestBuilder.setRid(rid);
        authRequestBuilder.setHttpVerb(httpVerb);
        authRequestBuilder.setContentMd5(contentMd5);
        authRequestBuilder.setContentType(contentType);
        //authRequestBuilder.setContentType("binary/octet-stream");
        //authRequestBuilder.setDate(date);
        authRequestBuilder.setDate("");//服务器下发时间为准
        authRequestBuilder.setCanonicalizedHeaders(canonicalizedHeaders);
        authRequestBuilder.setSuffix(suffix);


        switch(type){
            case Attachment.AUTH_TYPE_AVATAR:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.HEAD);
                break;
            case Attachment.AUTH_TYPE_FEED_BACK:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.LOG);
                break;
            case Attachment.AUTH_TYPE_PIC:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.PIC);
                break;
            case Attachment.AUTH_TYPE_ANIMATION:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.ANIMATION);
                break;
            case Attachment.AUTH_TYPE_USER_PIC:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.USER_PIC);
                break;
            case Attachment.AUTH_TYPE_USER_ID:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.USER_ID);
                break;
            case Attachment.AUTH_TYPE_USER_VIDEO:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.USER_VIDEO);
                break;
            case Attachment.AUTH_TYPE_USER_WALLPAPER:
                authRequestBuilder.setAuthType(AuthUploadFileProto.AuthType.USER_WALLPAPER);
                break;
        }

        authRequestBuilder.setVuid(UserAccountManager.getInstance().getUuidAsLong());
        final AuthUploadFileProto.AuthRequest authRequest = authRequestBuilder.build();
        MyLog.v(TAG+" getKs3AuthToken", CommonUtils.printPBDataLog(authRequest));
        try {
            //向业务服务器获取K3 request token
            PacketData data = new PacketData();
            data.setCommand(MiLinkCommand.COMMAND_ZHIBO_KS3AUTH_REQUEST);
            data.setData(authRequest.toByteArray());
            PacketData authPacketData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.UPLOAD_CONNECTION_TIMEOUT);
            return processPacketData(authPacketData);
        } catch (Exception e) {
            MyLog.v(TAG, e);
            return null;
        }
    }

    private static AuthUploadFileProto.AuthResponse processPacketData(PacketData data) {
        if (data == null) {
            return null;
        }
        MyLog.v(TAG, "processPacketData command=" + data.getCommand());
        String command = data.getCommand();
        if (TextUtils.isEmpty(command)) {
            return null;
        }
        if (command.equals(MiLinkCommand.COMMAND_ZHIBO_KS3AUTH_REQUEST)) {
            try {

                AuthUploadFileProto.AuthResponse response = AuthUploadFileProto.AuthResponse.parseFrom(data.getData());
                if (response.getErrorCode() == MiLinkConstant.ERROR_CODE_SUCCESS) {
                    MyLog.v(TAG+" getKs3AuthToken response", CommonUtils.printPBDataLog(response));
                    return response;
                } else {
                    return null;
                }
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    /**
     * 分片上传认证
     *
     * @param attId
     * @param resource
     * @param date
     * @param httpVerb
     * @param contentMd5
     * @param contentType
     * @param acl
     * @return
     */
    public static String getMultipartKs3AuthToken(long attId, String resource, String date, String httpVerb, String contentMd5, String contentType, String acl) {
        AuthUploadFileProto.MultipartAuthRequest multipartAuthRequest = AuthUploadFileProto.MultipartAuthRequest.newBuilder()
                .setRid(attId).setResource(resource).setDate(date).setHttpVerb(httpVerb).setContentMd5(contentMd5).setContentType(contentType)
                .setAcl(acl).build();

        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_ZHIBO_MULTIPART_AUTH);
        packetData.setData(multipartAuthRequest.toByteArray());
        MyLog.v(TAG+" getMultipartKs3AuthToken", CommonUtils.printPBDataLog(multipartAuthRequest));
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        if (responseData != null) {
            try {
                AuthUploadFileProto.MultipartAuthResponse response = AuthUploadFileProto.MultipartAuthResponse.parseFrom(responseData.getData());
                if (response != null && response.getErrorCode() == MiLinkConstant.ERROR_CODE_SUCCESS) {
                    MyLog.v(TAG+" getMultipartKs3AuthToken", CommonUtils.printPBDataLog(response));
                    return response.getMultipartAuthorization();
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }
}
