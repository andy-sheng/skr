package presenter;

import com.base.mvp.IRxView;

/**
 * Created by lan on 2018/1/11.
 */
public interface IContestInviteView extends IRxView {
    void getInviteCodeSuccess(String code);

    void setInviteCodeSuccess(int revivalNum);

    void setInviteCodeFailure(int errCode);

    void useSpecialCodeSuccess(int revivalNum);

    void useSpecialCodeFailure(int errCode);
}
