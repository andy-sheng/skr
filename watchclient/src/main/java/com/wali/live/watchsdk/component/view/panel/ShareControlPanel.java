package com.wali.live.watchsdk.component.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.PlusItemAdapter;
import com.wali.live.watchsdk.component.presenter.adapter.ShareItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @module 分享面板
 * <p>
 * Created by yangli on 16-5-11.
 */
public class ShareControlPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements IComponentView<ShareControlPanel.IPresenter, ShareControlPanel.IView>, View.OnClickListener {
    private static final String TAG = "ShareControlPanel";

    @Nullable
    protected ShareControlPanel.IPresenter mPresenter;

    private RecyclerView mShareGridView;
    private ShareItemAdapter mShareAdapter;

    boolean isLocalChina = CommonUtils.isLocalChina();
    int shareBtnCnt = isLocalChina ? SHARE_BTN_CNT_DOMAIN : SHARE_BTN_CNT_ABROAD;

    public static final int SHARE_BTN_CNT_DOMAIN = 9;
    public static final int SHARE_BTN_CNT_ABROAD = 6;

    public static final int BTN_WECHAT = 0;
    public static final int BTN_WECHAT_MOMENT = 1;
    public static final int BTN_QQ = 2;
    public static final int BTN_QZONE = 3;
    public static final int BTN_WEIBO = 4;
    public static final int BTN_FACEBOOK = 5;
    public static final int BTN_TWITTER = 6;
    public static final int BTN_INSTAGRAM = 7;
    public static final int BTN_WHATSAPP = 8;
    public static final int BTN_MILIAO = 9;
    public static final int BTN_MILIAO_FEEDS = 10;
    public static final int BTN_FEEDS = 11;

    public static final int[] SHARE_ID = new int[]{
            R.id.weixin_friend,
            R.id.moment,
            R.id.QQ,
            R.id.qzone,
            R.id.blog,
            R.id.facebook,
            R.id.twitter,
            R.id.instagram,
            R.id.whatsapp,
            R.id.miliao,
            R.id.miliao_feeds,
            R.id.feeds
    };

    public static final int[] SHARE_DRAWABLE_ID = new int[]{
            R.drawable.live_audience_list_wechat,
            R.drawable.live_audience_list_pengyouquan,
            R.drawable.live_audience_list_qq,
            R.drawable.live_audience_list_qzone,
            R.drawable.live_audience_list_weibo,
            R.drawable.live_audience_list_facebook,
            R.drawable.live_audience_list_twitter,
            R.drawable.live_audience_list_instagram,
            R.drawable.web_share_whatsapp_bg,
            R.drawable.live_audience_list_miliao,
            R.drawable.live_audience_list_miliao_feeds,
            R.drawable.start_live_icon_share_feeds
    };

    public static final int[] SHARE_TV_ID = new int[]{
            R.string.weixin_friend,
            R.string.moment,
            R.string.QQ,
            R.string.qzone,
            R.string.blog,
            R.string.facebook,
            R.string.twitter,
            R.string.instagram,
            R.string.whatsapp,
            R.string.miliao,
            R.string.miliao_feeds,
            R.string.feeds
    };

    public static final int[] SHARE_BTN_INDEX_DOMAIN = new int[]{
            BTN_WECHAT,
            BTN_WECHAT_MOMENT,
            BTN_QQ,
            BTN_QZONE,
            BTN_WEIBO,
            BTN_MILIAO,
            BTN_MILIAO_FEEDS,
            BTN_FACEBOOK,
            BTN_TWITTER
    };

    //国内顺序fb，tw最后。 其他情况默认 BTN_INSTAGRAM,暂时先拿掉
    public static final int[] SHARE_BTN_INDEX_ABROAD = new int[]{
            BTN_WHATSAPP,
            BTN_FACEBOOK,
            BTN_TWITTER,
            BTN_INSTAGRAM,
            BTN_WECHAT,
            BTN_WECHAT_MOMENT
    };

    public ShareControlPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.share_control_panel;
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        initView();
    }

    private void initView() {
        mShareGridView = (RecyclerView) mContentView.findViewById(R.id.gv_share_button);
        mShareAdapter = new ShareItemAdapter(GlobalData.screenWidth / 5, GlobalData.screenWidth / 4);
        mShareAdapter.setOnClickListener(this);
        List<PlusItemAdapter.PlusItem> btnItems = new ArrayList<>();
        for (int i = 0; i < SHARE_BTN_INDEX_DOMAIN.length; i++) {
            btnItems.add(new PlusItemAdapter.PlusItem(SHARE_ID[i], SHARE_TV_ID[SHARE_BTN_INDEX_DOMAIN[i]], SHARE_DRAWABLE_ID[SHARE_BTN_INDEX_DOMAIN[i]]));
        }
        mShareAdapter.setPlusData(btnItems);
        mShareGridView.setAdapter(mShareAdapter);
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
    }

    @Override
    public void hideSelf(boolean useAnimation) {
        super.hideSelf(useAnimation);
    }

    @Override
    public void showSelf(boolean useAnimation, boolean isLandscape) {
        super.showSelf(useAnimation, isLandscape);
    }

    @Override
    protected void orientSelf() {
        super.orientSelf();
        RelativeLayout.LayoutParams layoutParams;
        //横平时候的margin
        int margin = 0;
        //分享item宽高比例
        float itemHeight = (GlobalData.screenWidth / 4f);
        if (mIsLandscape) {
            int gridWidth = GlobalData.screenWidth * shareBtnCnt / 5;
            if (gridWidth > GlobalData.screenHeight) {
                gridWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                margin = (GlobalData.screenHeight - gridWidth) / 2;
            }
            LinearLayoutManager manager = new LinearLayoutManager(mShareGridView.getContext());
            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mShareGridView.setLayoutManager(manager);
            layoutParams = new RelativeLayout.LayoutParams(gridWidth, (int) itemHeight);
            layoutParams.setMargins(margin, 0, 0, 0);
        } else {
            int layer = shareBtnCnt / 5;
            if ((shareBtnCnt % 5) > 0) {
                layer++;
            }
            mShareGridView.setLayoutManager(new GridLayoutManager(mShareGridView.getContext(), 5));
            layoutParams = new RelativeLayout.LayoutParams(GlobalData.screenWidth, (int) (layer * itemHeight));
            layoutParams.setMargins(0, 0, 0, 0);
        }
        mShareGridView.setLayoutParams(layoutParams);
        mShareAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    public ShareControlPanel.IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void showSelf() {
                ShareControlPanel.this.showSelf(true, mIsLandscape);
            }

            @Override
            public void hideSelf() {
                ShareControlPanel.this.hideSelf(true);
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                ShareControlPanel.this.onOrientation(isLandscape);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {

    }

    public interface IView extends IViewProxy, IOrientationListener {

        void showSelf();

        void hideSelf();
    }
}
