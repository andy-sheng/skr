package request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by lan on 2018/1/12.
 */
public class GetContestInviteCodeRequest extends BaseRequest {
    public GetContestInviteCodeRequest() {
        super(MiLinkCommand.COMMAND_GET_CONTEST_INVITE_CODE, "GetContestInviteCode");
        build();
    }

    private void build() {
        mRequest = LiveSummitProto.GetContestInviteCodeReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .build();
    }

    @Override
    protected LiveSummitProto.GetContestInviteCodeRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.GetContestInviteCodeRsp.parseFrom(bytes);
    }
}
