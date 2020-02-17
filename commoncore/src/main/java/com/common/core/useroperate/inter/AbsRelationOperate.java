package com.common.core.useroperate.inter;

import android.view.View;
import android.widget.TextView;

import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.DebounceViewClickListener;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

//当只有一个操作按钮的时候这个默认的就可以了,如果交互有别的区别就自己实现一个Operate
public abstract class AbsRelationOperate implements IOperateStub<UserInfoModel> {
    protected String mText;
    protected int mLayoutId;
    protected AbsRelationOperate.ClickListener mOnClickListener;

    public AbsRelationOperate(@NotNull String text, @NotNull int layoutId, AbsRelationOperate.ClickListener onClickListener) {
        mText = text;
        mLayoutId = layoutId;
        mOnClickListener = onClickListener;
    }

    public String getText() {
        return mText;
    }

    public int getLayoutId() {
        return mLayoutId;
    }

    @Override
    public IOperateHolder<UserInfoModel> getHolder() {
        return new IOperateHolder<UserInfoModel>() {
            int position;
            UserInfoModel userInfoModel;
            TextView operateView;
            WeakReference<BaseActivity> mBaseActivityWeakReference;

            @Override
            public void init(WeakReference<BaseActivity> weakReference, View view) {
                mBaseActivityWeakReference = weakReference;
                operateView = view.findViewById(R.id.agentweb_webview_id);
                operateView.setText(mText);
                operateView.setOnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mOnClickListener != null) {
                            mOnClickListener.click(mBaseActivityWeakReference, v, position, userInfoModel);
                        }
                    }
                });
            }

            @Override
            public void bindData(int pos, UserInfoModel model) {
                position = pos;
                userInfoModel = model;
            }
        };
    }

    public AbsRelationOperate.ClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public interface ClickListener {
        void click(WeakReference<BaseActivity> weakReference, View view, int pos, UserInfoModel userInfoModel);
    }
}
