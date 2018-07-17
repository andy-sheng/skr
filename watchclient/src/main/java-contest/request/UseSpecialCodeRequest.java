package request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by lan on 2018/1/25.
 */
public class UseSpecialCodeRequest extends BaseRequest {
    public UseSpecialCodeRequest(String inviteCode) {
        super(MiLinkCommand.COMMAND_USE_SPECIAL_CODE, "UseSpecialCode");
        build(inviteCode);
    }

    private void build(String code) {
        mRequest = LiveSummitProto.UseSpecialCodeReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setCode(code)
                .build();
    }

    @Override
    protected LiveSummitProto.UseSpecialCodeRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.UseSpecialCodeRsp.parseFrom(bytes);
    }
}
