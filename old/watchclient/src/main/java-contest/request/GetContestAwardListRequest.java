package request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by liuyanyan on 2018/1/16.
 */
public class GetContestAwardListRequest extends BaseRequest {
    public GetContestAwardListRequest(String contestId, String liveId) {
        super(MiLinkCommand.COMMAND_CONTEST_AWARD_LIST, "GetContestAwardList");
        build(contestId, liveId);
    }

    private void build(String contestId, String liveId) {
        mRequest = LiveSummitProto.GetContestAwardListReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setLimit(20)
                .setContestId(contestId)
                .setLiveid(liveId)
                .build();
    }

    @Override
    protected LiveSummitProto.GetContestAwardListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.GetContestAwardListRsp.parseFrom(bytes);
    }
}
