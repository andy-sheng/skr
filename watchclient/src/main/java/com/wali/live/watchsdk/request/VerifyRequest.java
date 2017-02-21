package com.wali.live.watchsdk.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.SecurityProto.VerifyAssistantReq;
import com.wali.live.proto.SecurityProto.VerifyAssistantRsp;

/**
 * Created by lan on 17/2/20.
 */
public class VerifyRequest extends BaseRequest {
    {
        mCommand = MiLinkCommand.COMMAND_ACCOUNT_VERIFY_ASSISTANT;
        mAction = "VerifyAssistant";
    }

    public VerifyRequest(int channelId, String packageName, String channelSecret) {
        generateRequest(channelId, packageName, channelSecret);
    }

    private VerifyAssistantReq.Builder generateBuilder() {
        return VerifyAssistantReq.newBuilder();
    }

    private void generateRequest(int channelId, String packageName, String channelSecret) {
        mRequest = generateBuilder()
                .setChannelId(String.valueOf(channelId))
                .setPackageName(packageName)
                .setChannelSecret(channelSecret)
                .build();
    }

    protected VerifyAssistantRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VerifyAssistantRsp.parseFrom(bytes);
    }
}
