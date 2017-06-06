package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailTabView;

import static com.wali.live.component.ComponentController.MSG_COMMENT_TOTAL_CNT;
import static com.wali.live.component.ComponentController.MSG_REPLAY_TOTAL_CNT;

/**
 * Created by yangli on 2017/06/02.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 详情TAB表现
 */
public class DetailTabPresenter extends ComponentPresenter<DetailTabView.IView>
        implements DetailTabView.IPresenter {
    private static final String TAG = "DetailTabPresenter";

    private RoomBaseDataModel mMyRoomData;

    public DetailTabPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel roomData) {
        super(componentController);
        mMyRoomData = roomData;
        registerAction(MSG_COMMENT_TOTAL_CNT);
        registerAction(MSG_REPLAY_TOTAL_CNT);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case MSG_COMMENT_TOTAL_CNT:
                    mView.updateCommentTotalCnt((int) params.getItem(0));
                    break;
                case MSG_REPLAY_TOTAL_CNT:
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
