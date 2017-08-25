package com.wali.live.watchsdk.component.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;

/**
 * Created by xiaolan on 2017/08/17.
 * <p>
 * Generated using create_view_with_presenter.py
 *
 * @module [TODO-COMPONENT add module]
 */
public class ExtraContainerView extends RelativeLayout implements IComponentView<ExtraContainerView.IPresenter, ExtraContainerView.IView> {
    private static final String TAG = "ExtraContainerView";

    @Nullable
    protected IPresenter mPresenter;

    private TextView mEditInfoTv;

    public ExtraContainerView(Context context) {
        super(context);
    }

    public ExtraContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtraContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

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

    private void showEditInfo() {
        if (mEditInfoTv == null) {
            mEditInfoTv = new TextView(getContext());

            mEditInfoTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, DisplayUtils.dip2px(12f));
            mEditInfoTv.setTextColor(Color.WHITE);
            mEditInfoTv.setBackgroundResource(R.drawable.round_rect_orange_bg);

            mEditInfoTv.setTranslationY(DisplayUtils.dip2px(40f));
            mEditInfoTv.setGravity(Gravity.CENTER);
            mEditInfoTv.setPadding(DisplayUtils.dip2px(15f), 0, DisplayUtils.dip2px(15f), 0);

            mEditInfoTv.setMovementMethod(LinkMovementMethod.getInstance());

            String click = getContext().getString(R.string.click_here);
            String content = getContext().getString(R.string.edit_info_tip, click);
            int start = content.indexOf(click);
            SpannableString ss = new SpannableString(content);
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    EditInfoActivity.open((Activity) getContext());
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(ds.linkColor);
                }
            }, start, start + click.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(0xffe5aa1e), start, start + click.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mEditInfoTv.setText(ss);
        }

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, DisplayUtils.dip2px(26f));
        lp.addRule(CENTER_IN_PARENT, TRUE);
        addView(mEditInfoTv, lp);
    }

    private void hideEditInfo(boolean useAnimation) {
        if (mEditInfoTv != null) {
            if (useAnimation) {
                mEditInfoTv.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        removeView(mEditInfoTv);
                    }
                }).start();
            } else {
                removeView(mEditInfoTv);
            }
        }
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) ExtraContainerView.this;
            }

            @Override
            public void showEditInfo() {
                ExtraContainerView.this.showEditInfo();
            }

            @Override
            public void hideEditInfo(boolean useAnimation) {
                ExtraContainerView.this.hideEditInfo(useAnimation);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
        void showEditInfo();

        void hideEditInfo(boolean useAnimation);
    }
}
