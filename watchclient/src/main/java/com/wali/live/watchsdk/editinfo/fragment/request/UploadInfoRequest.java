package com.wali.live.watchsdk.editinfo.fragment.request;

import android.text.TextUtils;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.UserProto.UploadUserPropertiesReq;
import com.wali.live.proto.UserProto.UploadUserPropertiesRsp;

/**
 * Created by lan on 2017/8/15.
 */
public class UploadInfoRequest extends BaseRequest {
    private UploadUserPropertiesReq.Builder mBuilder;

    public UploadInfoRequest() {
        super(MiLinkCommand.COMMAND_UPLOAD_USER_INFO, "UploadInfo");
        mBuilder = UploadUserPropertiesReq.newBuilder()
                .setZuid(UserAccountManager.getInstance().getUuidAsLong());
    }

    public UploadInfoRequest uploadName(String name) {
        if (!TextUtils.isEmpty(name)) {
            mBuilder.setNickname(name);
        }
        mRequest = mBuilder.build();
        return this;
    }

    public UploadInfoRequest uploadGender(int gender) {
        mBuilder.setGender(gender);
        mRequest = mBuilder.build();
        return this;
    }

    public UploadInfoRequest uploadSign(String sign) {
        if (!TextUtils.isEmpty(sign)) {
            mBuilder.setSign(sign);
        }
        mRequest = mBuilder.build();
        return this;
    }

    public UploadInfoRequest uploadAvatar(long avatar, String avatarMd5) {
        if (avatar != 0) {
            mBuilder.setAvatar(avatar);
        }
        if (!TextUtils.isEmpty(avatarMd5)) {
            mBuilder.setAvatarMd5(avatarMd5);
        }
        mRequest = mBuilder.build();
        return this;
    }

    @Override
    protected GeneratedMessage parse(byte[] bytes) throws InvalidProtocolBufferException {
        return UploadUserPropertiesRsp.parseFrom(bytes);
    }
}
