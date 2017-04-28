package com.wali.live.watchsdk.component.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.repository.ShareRepository;
import com.wali.live.proto.ShareProto;
import com.wali.live.watchsdk.component.view.IShareView;

import java.util.ArrayList;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by zhangyuehuan on 16/7/13.
 */
public class SharePresenter extends RxLifeCyclePresenter {
    private static final String TAG = "SharePresenter";

    private ShareRepository mShareRepository;
    private IShareView mShareView;
    private CompositeSubscription mSubscriptions;
    private ArrayList<ShareProto.ChannelType> mChannelTypeArrayList;

    public SharePresenter(IShareView shareView) {
        this.mShareView = shareView;
        this.mSubscriptions = new CompositeSubscription();
        this.mShareRepository = new ShareRepository();

        mChannelTypeArrayList = new ArrayList<>();
        mChannelTypeArrayList.add(ShareProto.ChannelType.QQ);
        mChannelTypeArrayList.add(ShareProto.ChannelType.QZONE);
        mChannelTypeArrayList.add(ShareProto.ChannelType.WEIBO_SINA);
        mChannelTypeArrayList.add(ShareProto.ChannelType.WEIXIN);
        mChannelTypeArrayList.add(ShareProto.ChannelType.WEIXIN_CIRCLE);
        mChannelTypeArrayList.add(ShareProto.ChannelType.FACEBOOK);
        mChannelTypeArrayList.add(ShareProto.ChannelType.TWITTER);
        mChannelTypeArrayList.add(ShareProto.ChannelType.INSTAGRAM);
        mChannelTypeArrayList.add(ShareProto.ChannelType.WHATSAPP);
        mChannelTypeArrayList.add(ShareProto.ChannelType.MLDIALOG);
        mChannelTypeArrayList.add(ShareProto.ChannelType.MLBROADCAST);
    }

    public void updateChannelTypeArrayList(ArrayList<ShareProto.ChannelType> list) {
        mChannelTypeArrayList = list;
    }

    public void getTagTailForShare(final long uuid, final ShareProto.PeriodType periodType) {
        ShareProto.RoleType roleType = (uuid == UserAccountManager.getInstance().getUuidAsLong()) ?
                ShareProto.RoleType.ANCHOR : ShareProto.RoleType.VISITOR;
        Subscription subscription = mShareRepository.getTagTailForShare(uuid, roleType, mChannelTypeArrayList, periodType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ShareProto.GetShareTagTailRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "onError =" + e);
                    }

                    @Override
                    public void onNext(ShareProto.GetShareTagTailRsp rsp) {
                        MyLog.w(TAG, "onNext");
                        if (mShareView != null && rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            mShareView.notifyShareControlPanel(rsp.getTagTailList());
                        } else {
                            MyLog.w(TAG, "获取分享尾部文案失败 errCode=" + rsp.getRetCode());
                        }
                    }
                });
        mSubscriptions.add(subscription);
    }

    @Override
    public void destroy() {
        super.destroy();
        this.mShareView = null;
        mSubscriptions.clear();

    }
}
