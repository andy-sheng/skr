package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by jiyangli on 17-5-15.
 */
public class WatchMenuItemView extends RelativeLayout {

    private ImageView mImgIv;
    private TextView mNameTv;
    private View mLine;
    private ImageView mUnreadIv;

    public WatchMenuItemView(Context context) {
        super(context);
        init(context);
    }

    public WatchMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WatchMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        inflate(context, R.layout.watch_menu_portrait_item, this);
        mImgIv = (ImageView) findViewById(R.id.watch_menu_protrait_item_imgPic);
        mNameTv = (TextView) findViewById(R.id.watch_menu_protrait_item_txtName);
        mLine = findViewById(R.id.watch_menu_protrait_item_imgLine);
        mUnreadIv = (ImageView) findViewById(R.id.watch_menu_protrait_item_txtUnread);
    }

    public void setImageResource(int imageResource) {
        mImgIv.setImageResource(imageResource);
    }

    public void setText(String name) {
        if (!TextUtils.isEmpty(name)) {
            mNameTv.setText(name);
        }
    }

    public void setText(@StringRes int textId) {
        mNameTv.setText(textId);
    }

    public void setUnread(int unreadCnt) {
        if (unreadCnt > 0) {
            mUnreadIv.setVisibility(View.VISIBLE);
            mUnreadIv.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.little_red_dot_number));
        } else {
            mUnreadIv.setVisibility(View.GONE);
            mUnreadIv.setBackground(null);
        }
    }

    public void setIconSize(int width, int height, int marginTop) {
        LayoutParams lp = (LayoutParams) mImgIv.getLayoutParams();
        lp.width = width;
        lp.height = height;
        lp.setMargins(0, marginTop, 0, 0);
    }

    public int getItemHeight() {
        return DisplayUtils.dip2px(80);
    }
}
