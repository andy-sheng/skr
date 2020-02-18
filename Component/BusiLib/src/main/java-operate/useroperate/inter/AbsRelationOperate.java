package useroperate.inter;

import android.text.TextUtils;
import android.view.View;

import com.common.base.BaseActivity;
import com.common.callback.Callback;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;

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

    @Override
    public IOperateHolder<UserInfoModel> getHolder() {
        return new IOperateHolder<UserInfoModel>() {
            int position;
            UserInfoModel userInfoModel;
            ExTextView mFollowTv;
            WeakReference<BaseActivity> mBaseActivityWeakReference;

            @Override
            public void init(WeakReference<BaseActivity> weakReference, View view) {
                mBaseActivityWeakReference = weakReference;
                mFollowTv = view.findViewById(R.id.operate_tv);
                mFollowTv.setText(mText);
                mFollowTv.setOnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mOnClickListener != null) {
                            mOnClickListener.clickRelationBtn(mBaseActivityWeakReference, v, position, userInfoModel, new Callback<String>() {
                                @Override
                                public void onCallback(int r, String obj) {
                                    if (r == 1) {
                                        if (!TextUtils.isEmpty(obj)) {
                                            mFollowTv.setText(obj);
                                            mFollowTv.setEnabled(false);
                                        }
                                    }
                                }
                            });
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
        void clickRelationBtn(WeakReference<BaseActivity> weakReference, View view, int pos, UserInfoModel userInfoModel, Callback<String> callback);
    }
}
