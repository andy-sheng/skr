package useroperate.inter;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.ex.ExTextView;

import java.util.List;

public interface IOperateFriendView {

    void addInviteModelList(List<UserInfoModel> list, int oldOffset, int newOffset);

    void updateInvited(ExTextView view);

    void finishRefresh();
}
