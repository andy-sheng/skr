package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.watchsdk.component.view.WidgetView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyong on 2017/03/24.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 运营位操作类
 */
public class WidgetPresenter extends ComponentPresenter<WidgetView.IView>
        implements WidgetView.IPresenter {
    private static final String TAG = "WidgetPresenter";

    protected RoomBaseDataModel mMyRoomData;
    // TODO 改成不是proto的数据格式
    private List<LiveCommonProto.NewWidgetItem> mWidgetList = new ArrayList();

    public WidgetPresenter(@NonNull IComponentController componentController,
                           RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
    }

    /**
     * 设置运营位数据
     */
    public void setWidgetList(List<LiveCommonProto.NewWidgetItem> list) {
        if (list.size() <= 0) {
            return;
        }
        if (mWidgetList.containsAll(list) && mWidgetList.size() == list.size()) {
            return;
        }

        if (mWidgetList.size() > 0) {
            mWidgetList.clear();
        }
        mWidgetList.addAll(list);

        hideWidget();
        if (mWidgetList != null && mWidgetList.size() > 0) {
            mView.showWidgetView(mWidgetList);
        }
    }

    // 隐藏运营位数据
    public void hideWidget() {
        mView.hideWidgetView();
    }

    public void destroy() {
        super.destroy();
        mView.destroyView();
    }

    @Override
    protected IAction createAction() {
        return new Action();
    }

    @Override
    public long getUid() {
        return mMyRoomData != null ? mMyRoomData.getUid() : 0;
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }
}
