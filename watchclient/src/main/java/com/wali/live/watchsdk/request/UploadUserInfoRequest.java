package com.wali.live.watchsdk.request;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.UserProto;

/**
 * 用户资料上传
 * <p/>
 * Created by wuxiaoshan on 17-3-1.
 */
public class UploadUserInfoRequest extends BaseLiveRequest {

    public UploadUserInfoRequest(long uuid, String nickName, int gender, long avatar, String avatarMd5, boolean needUploadGender) {
        super(MiLinkCommand.COMMAND_UPLOAD_USER_INFO, "uploaduserpro", null);
        boolean needUpload = false;
        UserProto.UploadUserPropertiesReq.Builder builder = UserProto.UploadUserPropertiesReq.newBuilder();
        builder.setZuid(uuid);
        if (!TextUtils.isEmpty(nickName)) {
            builder.setNickname(nickName);
            needUpload = true;
        }
        if (needUploadGender) {
            builder.setGender(gender);
            needUpload = true;
        }
        if (avatar != 0) {
            builder.setAvatar(avatar);
            builder.setAvatarMd5(avatarMd5);
            needUpload = true;
        }
        if (needUpload) {
            mRequest = builder.build();
        }
    }

    @Override
    protected UserProto.UploadUserPropertiesRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return UserProto.UploadUserPropertiesRsp.parseFrom(bytes);
    }

    public int sendRequest() {
        try {
            UserProto.UploadUserPropertiesRsp rsp = syncRsp();
            if (rsp != null) {
                return rsp.getRetCode();
            } else {
                return -1;
            }
        }catch (Exception e){
            MyLog.e(e);
            return -1;
        }
    }
}
