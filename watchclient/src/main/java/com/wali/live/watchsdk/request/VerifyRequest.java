package com.wali.live.watchsdk.request;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.SecurityProto.VerifyAssistantReq;
import com.wali.live.proto.SecurityProto.VerifyAssistantRsp;

/**
 * Created by lan on 17/2/20.
 */
public class VerifyRequest extends BaseRequest {

    public VerifyRequest(int channelId, String packageName, String channelSecret) {
        super(MiLinkCommand.COMMAND_ACCOUNT_VERIFY_ASSISTANT, "VerifyAssistant", String.valueOf(channelId));
        generateRequest(channelId, packageName, channelSecret);
    }

    private VerifyAssistantReq.Builder generateBuilder() {
        return VerifyAssistantReq.newBuilder();
    }

    private void generateRequest(int channelId, String packageName, String channelSecret) {
        VerifyAssistantReq.Builder builder = generateBuilder()
                .setChannelId(String.valueOf(channelId))
                .setPackageName(packageName);
        if (!TextUtils.isEmpty(channelSecret)) {
            builder.setChannelSecret(channelSecret);
        }
        mRequest = builder.build();
    }

    protected VerifyAssistantRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VerifyAssistantRsp.parseFrom(bytes);
    }
}
