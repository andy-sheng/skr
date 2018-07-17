package request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

import static android.R.attr.type;

/**
 * Created by wanglinzhang on 2018/2/1.
 */

public class AddRevivalCardActReq extends BaseRequest{
    public AddRevivalCardActReq(int type, String contestId, String pkgName) {
        super(MiLinkCommand.COMMAND_ADD_REVIVAL_CARD_ACT, "AddRevivalAct");
        build(type, contestId, pkgName);
    }

    private void build(int type, String contestId, String pkgName) {
        mRequest = LiveSummitProto.AddRevivalCardActReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setType(type)
                .setContestId(contestId)
                .setGamePkgName(pkgName)
                .build();
    }

    @Override
    protected LiveSummitProto.AddRevivalCardActRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.AddRevivalCardActRsp.parseFrom(bytes);
    }
}
