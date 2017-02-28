package com.wali.live.watchsdk.task;

import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto.ViewerTopRsp;
import com.wali.live.watchsdk.request.ViewerTopRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-12-1.
 */
public class LiveTask {
    private static final String TAG = LiveTask.class.getSimpleName();

    public static Runnable viewerTop(final long uid,final String roomId, final WeakReference<IActionCallBack> callBack) {
        Runnable task = new TaskRunnable() {
            int errCode = ErrorCode.CODE_ERROR_NORMAL;
            List<ViewerModel> viewerList;

            protected Boolean doInBackground(Void... params) {
                ViewerTopRsp rsp = new ViewerTopRequest(uid, roomId).syncRsp();

                if ((rsp != null) && (errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                    viewerList = new ArrayList<>();
                    for (LiveCommonProto.Viewer protoViewer : rsp.getViewerList()) {
                        viewerList.add(new ViewerModel(protoViewer));
                    }
                    return true;
                }
                return false;
            }

            protected void onPostExecute(Boolean result) {
                if (callBack.get()!=null && callBack != null) {
                    if (result) {
                        callBack.get().processAction(MiLinkCommand.COMMAND_LIVE_VIEWER_TOP, errCode, viewerList);
                    } else {
                        callBack.get().processAction(MiLinkCommand.COMMAND_LIVE_VIEWER_TOP, errCode);
                    }
                }
            }
        };
        return task;
    }

    public static Runnable viewerTop(final RoomBaseDataModel roomData,final WeakReference<IActionCallBack> callBack) {
        Runnable task = new TaskRunnable() {
            int errCode = ErrorCode.CODE_ERROR_NORMAL;
            List<ViewerModel> viewerList;

            protected Boolean doInBackground(Void... params) {
                ViewerTopRsp rsp = new ViewerTopRequest(roomData.getUid(), roomData.getRoomId()).syncRsp();

                if ((rsp != null) && (errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                    viewerList = new ArrayList<>();
                    for (LiveCommonProto.Viewer protoViewer : rsp.getViewerList()) {
                        viewerList.add(new ViewerModel(protoViewer));
                    }
                    return true;
                }
                return false;
            }

            protected void onPostExecute(Boolean result) {
                if (callBack!=null && callBack.get() != null) {
                    if (result) {
                        callBack.get().processAction(MiLinkCommand.COMMAND_LIVE_VIEWER_TOP, errCode, roomData, viewerList);
                    } else {
                        callBack.get().processAction(MiLinkCommand.COMMAND_LIVE_VIEWER_TOP, errCode, roomData);
                    }
                }
            }
        };
        return task;
    }

}
