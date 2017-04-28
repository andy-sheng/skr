package com.wali.live.watchsdk.component.view.panel;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.PlusItemAdapter;
import com.wali.live.watchsdk.component.presenter.adapter.ShareItemAdapter;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import java.util.ArrayList;
import java.util.List;

import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_FACEBOOK;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_INSTAGRAM;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_MILIAO;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_MILIAO_FEEDS;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_QQ;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_QZONE;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_TWITTER;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WECHAT;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WECHAT_MOMENT;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WEIBO;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WHATSAPP;

/**
 * @module 分享面板
 * <p>
 * Created by yangli on 16-5-11.
 */
public class ShareControlPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements View.OnClickListener {
    private static final String TAG = "ShareControlPanel";

    private RecyclerView mShareGridView;
    private ShareItemAdapter mShareAdapter;
    @NonNull
    private RoomBaseDataModel mMyRoomData;

    boolean isLocalChina = CommonUtils.isLocalChina();
    int shareBtnCnt = isLocalChina ? SHARE_BTN_CNT_DOMAIN : SHARE_BTN_CNT_ABROAD;

    public static final int SHARE_BTN_CNT_DOMAIN = 9;
    public static final int SHARE_BTN_CNT_ABROAD = 6;

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

    //btn统一用SnsShareHelper
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

    public ShareControlPanel(@NonNull RelativeLayout parentView, @NonNull RoomBaseDataModel roomBaseDataModel) {
        super(parentView);
        this.mMyRoomData = roomBaseDataModel;
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

        int shareType = mMyRoomData.getShareType();
        for (int i = 0; i < SHARE_BTN_INDEX_DOMAIN.length; i++) {
            // 使用位运算判断是否支持该项分享
            if ((shareType & (1 << SHARE_BTN_INDEX_DOMAIN[i])) == 0) {
                continue;
            }
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
        int snsType = getShareType(v);
        if (snsType != -1 && SnsShareHelper.getInstance().isInstallApp(snsType)) {
            SnsShareHelper.getInstance().shareToSns(snsType, mMyRoomData);
        }
    }

    private int getShareType(View v) {
        int i = v.getId();
        if (i == R.id.weixin_friend) {
            return BTN_WECHAT;
        } else if (i == R.id.moment) {
            return BTN_WECHAT_MOMENT;
        } else if (i == R.id.QQ) {
            return BTN_QQ;
        } else if (i == R.id.qzone) {
            return BTN_QZONE;
        } else if (i == R.id.blog) {
            return BTN_WEIBO;
        } else if (i == R.id.facebook) {
            return BTN_FACEBOOK;
        } else if (i == R.id.twitter) {
            return BTN_TWITTER;
        } else if (i == R.id.instagram) {
            return BTN_INSTAGRAM;
        } else if (i == R.id.whatsapp) {
            return BTN_WHATSAPP;
        } else if (i == R.id.miliao) {
            return BTN_MILIAO;
        } else if (i == R.id.miliao_feeds) {
            return BTN_MILIAO_FEEDS;
        }
        return -1;
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
}
