package com.mi.liveassistant.room.viewer;

import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.data.model.Viewer;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.proto.LiveProto;
import com.mi.liveassistant.room.viewer.callback.IViewerCallback;
import com.mi.liveassistant.room.viewer.callback.IViewerListener;
import com.mi.liveassistant.room.viewer.request.ViewerTopRequest;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/5/4.
 */
public class ViewerInfoManager implements IViewerObserver {
    private static final String TAG = ViewerInfoManager.class.getSimpleName();

    private IViewerListener mListener;

    public ViewerInfoManager() {
    }

    /**
     * 获取一个用户的信息
     */
    public void asyncViewerList(final long playerId, final String liveId, final IViewerCallback callback) {
        MyLog.d(TAG, "asyncViewerList liveId=" + liveId);
        Observable.just(0)
                .map(new Func1<Integer, LiveProto.ViewerTopRsp>() {
                    @Override
                    public LiveProto.ViewerTopRsp call(Integer integer) {
                        LiveProto.ViewerTopRsp rsp = new ViewerTopRequest(playerId, liveId).syncRsp();
                        return rsp;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.ViewerTopRsp>() {
                    @Override
                    public void call(LiveProto.ViewerTopRsp rsp) {
                        int errCode = ErrorCode.CODE_ERROR_NORMAL;
                        if (rsp != null && (errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                            callback.notifySuccess(parse(rsp));
                        } else {
                            callback.notifyFail(errCode);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
    }

    public List<Viewer> syncViewerList(long playerId, String liveId) {
        LiveProto.ViewerTopRsp rsp = new ViewerTopRequest(playerId, liveId).syncRsp();
        if (rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
            return parse(rsp);
        }
        return null;
    }

    private List<Viewer> parse(LiveProto.ViewerTopRsp rsp) {
        List<LiveCommonProto.Viewer> list = rsp.getViewerList();
        List<Viewer> result = new ArrayList();
        for (LiveCommonProto.Viewer protoViewer : list) {
            result.add(new Viewer(protoViewer));
        }
        return result;
    }

    @Override
    public void registerListener(IViewerRegister register, IViewerListener listener) {
        mListener = listener;
        register.registerObserver(this);
    }

    @Override
    public void dependOnList(List<Viewer> viewer) {
        if (mListener != null) {
            mListener.update(viewer);
        }
    }

    @Override
    public void dependOnSelf() {
        if (mListener != null) {
            mListener.updateManually();
        }
    }
}
