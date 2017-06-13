package com.mi.liveassistant.account.upload;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.UserProto;

/**
 * 用户资料上传
 * <p/>
 * Created by wuxiaoshan on 17-3-1.
 */
public class UploadUserInfoRequest extends BaseRequest {

    public UploadUserInfoRequest(long uuid, String nickName, int gender, long avatar, String avatarMd5, boolean needUploadGender) {
        super(MiLinkCommand.COMMAND_UPLOAD_USER_INFO, "uploaduserpro");

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
                return ErrorCode.CODE_ERROR_NORMAL;
            }
        } catch (Exception e) {
            MyLog.e(e);
            return ErrorCode.CODE_ERROR_NORMAL;
        }
    }
}
