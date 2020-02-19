package useroperate.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.component.busilib.view.NickNameView;

import java.lang.ref.WeakReference;

import useroperate.inter.IOperateHolder;
import useroperate.inter.IOperateStub;

public class OperateFirendAdapter extends DiffAdapter<UserInfoModel, RecyclerView.ViewHolder> {

    OnInviteClickListener mOnInviteClickListener;
    boolean mHasSearch;

    private static final int ITEM_TYPE_NORMAL = 1;
    private static final int ITEM_TYPE_SEARCH = 2;

    Drawable mBusyCircleDrawable;
    Drawable mAIDLCircleDrawable;
    Drawable mOffLineCircleDrawable;
    Drawable mGameCircleDrawable;
    IOperateStub<UserInfoModel> mStub;

    private int mMode = UserInfoManager.RELATION.FRIENDS.getValue();

    public OperateFirendAdapter(OnInviteClickListener onInviteClickListener, boolean hasSearch, IOperateStub<UserInfoModel> stub, int mode) {
        this.mOnInviteClickListener = onInviteClickListener;
        this.mHasSearch = hasSearch;
        this.mStub = stub;
        this.mMode = mode;

        mBusyCircleDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FFC300"))
                .setCornersRadius(U.getDisplayUtils().dip2px(5))
                .build();

        mAIDLCircleDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#7ED321"))
                .setCornersRadius(U.getDisplayUtils().dip2px(5))
                .build();

        mOffLineCircleDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#8EA0A9"))
                .setCornersRadius(U.getDisplayUtils().dip2px(5))
                .build();

        mGameCircleDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FF8C9A"))
                .setCornersRadius(U.getDisplayUtils().dip2px(5))
                .build();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_SEARCH) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.operate_search_friend_item_layout, parent, false);
            SearchItmeHolder viewHolder = new SearchItmeHolder(view);
            return viewHolder;
        } else {
            View view;
            if (mStub != null) {
                view = LayoutInflater.from(parent.getContext()).inflate(mStub.getViewLayout(), parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.relation_view_holder_item, parent, false);
            }

            ItemHolder viewHolder = new ItemHolder(view, mMode);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mHasSearch) {
            if (position == 0) {

            } else {
                UserInfoModel model = mDataList.get(position - 1);
                ItemHolder itemHolder = (ItemHolder) holder;
                itemHolder.bind(position, model);
            }
        } else {
            UserInfoModel model = mDataList.get(position);
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.bind(position, model);

        }
    }

    @Override
    public int getItemCount() {
        if (mHasSearch) {
            return mDataList.size() + 1;
        }
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mHasSearch && position == 0) {
            return ITEM_TYPE_SEARCH;
        }
        return ITEM_TYPE_NORMAL;
    }

    private class SearchItmeHolder extends RecyclerView.ViewHolder {

        RelativeLayout mContent;

        public SearchItmeHolder(View itemView) {
            super(itemView);
            mContent = (RelativeLayout) itemView.findViewById(R.id.content);

            mContent.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mOnInviteClickListener != null) {
                        mOnInviteClickListener.onClickSearch();
                    }
                }
            });
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ConstraintLayout mContent;
        AvatarView mAvatarIv;
        NickNameView mNickNameTv;

        ExTextView mSendTv;
        ExTextView mStatusTv;
        ExTextView mIntimacyTv;

        IOperateHolder<UserInfoModel> mIOperateHolder;

        int mMode;
        int position;
        UserInfoModel userInfoModel;

        public ItemHolder(View itemView, int mode) {
            super(itemView);

            this.mMode = mode;
            mContent = itemView.findViewById(R.id.content);
            mAvatarIv = itemView.findViewById(R.id.avatar_iv);
            mNickNameTv = itemView.findViewById(R.id.nickname_tv);
            mSendTv = itemView.findViewById(R.id.send_tv);
            mStatusTv = itemView.findViewById(R.id.status_tv);
            mIntimacyTv = itemView.findViewById(R.id.intimacy_tv);

//            mFollowTv.setOnClickListener(new DebounceViewClickListener() {
//                @Override
//                public void clickValid(View v) {
//                    if (recyclerOnItemClickListener != null) {
//                        recyclerOnItemClickListener.onItemClicked(mFollowTv, position, userInfoModel);
//                    }
//                }
//            });
//
//            mContent.setOnClickListener(new DebounceViewClickListener() {
//                @Override
//                public void clickValid(View v) {
//                    if (recyclerOnItemClickListener != null) {
//                        recyclerOnItemClickListener.onItemClicked(mContent, position, userInfoModel);
//                    }
//                }
//            });

            mIOperateHolder = mStub.getHolder();
            WeakReference weakReference = new WeakReference<BaseActivity>((BaseActivity) itemView.getContext());
            mIOperateHolder.init(weakReference, itemView);
        }

        public void bind(int position, UserInfoModel userInfoModel) {
            this.position = position;
            this.userInfoModel = userInfoModel;

            mNickNameTv.setAllStateText(userInfoModel);
            mAvatarIv.bindData(userInfoModel);

            // 只是关心在线和离线
            if (mMode != UserInfoManager.RELATION.FANS.getValue()) {
                if (userInfoModel.getStatus() >= UserInfoModel.EF_ONLINE) {
                    mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                    mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.greendot, 0, 0, 0);
                    mStatusTv.setVisibility(View.VISIBLE);
                    mStatusTv.setText(userInfoModel.getStatusDesc());
                } else if (userInfoModel.getStatus() == UserInfoModel.EF_OFFLINE) {
                    mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                    mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.graydot, 0, 0, 0);
                    mStatusTv.setVisibility(View.VISIBLE);
                    mStatusTv.setText(userInfoModel.getStatusDesc());
                } else {
                    mStatusTv.setVisibility(View.GONE);
                }
            } else {
                mStatusTv.setVisibility(View.GONE);
            }

            if (mMode == UserInfoManager.RELATION.FRIENDS.getValue() && userInfoModel.hasIntimacy()) {
                mIntimacyTv.setVisibility(View.VISIBLE);
                mIntimacyTv.setText("亲密度 " + userInfoModel.getIntimacy());
            } else {
                mIntimacyTv.setVisibility(View.GONE);
            }
            mIOperateHolder.bindData(position, userInfoModel);
        }
    }

    public interface OnInviteClickListener {
        void onClick(UserInfoModel model, ExTextView view);

        default void onClickSearch() {

        }
    }
}