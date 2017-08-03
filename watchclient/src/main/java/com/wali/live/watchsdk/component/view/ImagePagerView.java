package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.image.fresco.view.TransferImageView;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.List;

/**
 * Created by xiaolan on 2017/07/17.
 *
 * @module 观看上下滑动
 */
public class ImagePagerView extends RelativeLayout
        implements IComponentView<ImagePagerView.IPresenter, ImagePagerView.IView> {
    private static final String TAG = "ImagePagerView";

    @Nullable
    protected IPresenter mPresenter;

    private TransferImageView mLastDv;
    private TransferImageView mCenterDv;
    private TransferImageView mNextDv;

    private List<RoomInfo> mRoomInfoList;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public ImagePagerView(Context context) {
        this(context, null, 0);
    }

    public ImagePagerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImagePagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.image_pager_layout, this);

        mLastDv = $(R.id.last_dv);
        mCenterDv = $(R.id.center_dv);
        mNextDv = $(R.id.next_dv);

        bindLayoutParam(mLastDv);
        bindLayoutParam(mCenterDv);
        bindLayoutParam(mNextDv);

        mLastDv.provideInitTranslationY(-GlobalData.screenHeight);
        mNextDv.provideInitTranslationY(GlobalData.screenHeight);
    }

    public void setVerticalList(List<RoomInfo> list, int position) {
        mRoomInfoList = list;

        bindTransferImage(mCenterDv, list.get(position));
        int last = position - 1;
        if (last < 0) {
            last += list.size();
        }
        bindTransferImage(mLastDv, list.get(last));
        int next = position + 1;
        if (next >= list.size()) {
            next -= list.size();
        }
        bindTransferImage(mNextDv, list.get(next));
    }

    private void bindLayoutParam(TransferImageView iv) {
        LayoutParams lp = (LayoutParams) iv.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, GlobalData.screenHeight);
        } else {
            lp.height = GlobalData.screenHeight;
        }
        iv.setLayoutParams(lp);
    }

    private void bindTransferImage(TransferImageView iv, RoomInfo info) {
        String url = info.getCoverUrl();
        if (TextUtils.isEmpty(url)) {
            url = AvatarUtils.getAvatarUrlByUidTs(info.getPlayerId(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, info.getAvatar());
        }
        AvatarUtils.loadAvatarByUrl(iv, url, false, true, R.drawable.rect_loading_bg_24292d, 320, 320);
    }

    public void switchNext(int position) {
        mLastDv.provideInitTranslationY(GlobalData.screenHeight);
        mCenterDv.provideInitTranslationY(-GlobalData.screenHeight);
        if (mCenterDv.getVisibility() == View.GONE) {
            mCenterDv.setVisibility(View.VISIBLE);
        }
        mNextDv.provideInitTranslationY(0);

        TransferImageView temp = mLastDv;
        mLastDv = mCenterDv;
        mCenterDv = mNextDv;
        mNextDv = temp;

        int next = position + 1;
        if (next >= mRoomInfoList.size()) {
            next -= mRoomInfoList.size();
        }
        bindTransferImage(mNextDv, mRoomInfoList.get(next));
    }

    public void switchLast(int position) {
        mLastDv.provideInitTranslationY(0);
        mCenterDv.provideInitTranslationY(GlobalData.screenHeight);
        if (mCenterDv.getVisibility() == View.GONE) {
            mCenterDv.setVisibility(View.VISIBLE);
        }
        mNextDv.provideInitTranslationY(-GlobalData.screenHeight);

        TransferImageView temp = mNextDv;
        mNextDv = mCenterDv;
        mCenterDv = mLastDv;
        mLastDv = temp;

        int last = position - 1;
        if (last < 0) {
            last += mRoomInfoList.size();
        }
        bindTransferImage(mLastDv, mRoomInfoList.get(last));
    }

    public void postPrepare() {
        mCenterDv.setVisibility(View.GONE);
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) ImagePagerView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy<View> {
    }
}
