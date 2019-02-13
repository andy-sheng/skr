package com.common.webview;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.module.RouterConstants;

public class JsBridgeImpl {
    BaseActivity mBaseActivity;

    public JsBridgeImpl(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
    }

    public void openSchema(String shema){
        ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                .withString("uri", shema)
                .navigation();
    }

    public void shareUrl(String url){
//        SharePanel sharePanel = new SharePanel(ShareWebActivity.this);
//        sharePanel.setShareContent(mTitle, mDes, mUrl);
//        sharePanel.show(ShareType.URL);
    }

    public void shareImg(String url){
//        SharePanel sharePanel = new SharePanel(ShareWebActivity.this);
//        sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png", R.drawable.share_sker);
//        sharePanel.show(ShareType.IMAGE_RUL);
    }
}
