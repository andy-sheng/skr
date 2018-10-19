package com.common.image.fresco;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.common.base.R;
import com.common.utils.U;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by linjinbin on 16/2/19.
 * Fresco ImageView自定义基类
 */
public class BaseImageView extends SimpleDraweeView {
    protected final String TAG = getTAG();

    /**
     * 获取对应的类名
     */
    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public BaseImageView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public BaseImageView(Context context) {
        super(context, new GenericDraweeHierarchyBuilder(U.app().getResources()).build());
    }

    public BaseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BaseImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setTag(R.id.fresco_view_tag, null);
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        setTag(R.id.fresco_view_tag, null);
        super.setImageDrawable(drawable);
    }

    public <IV extends BaseImageView> IV get() {
        return (IV) this;
    }
}
