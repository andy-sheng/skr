package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.thornbirds.component.presenter.IEventPresenter;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.FansMemberManagerFragment;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter.MemberItem;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.sixin.PopComposeMessageFragment;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;

import java.util.List;

import static com.wali.live.component.view.Utils.$click;

/**
 * Created by yangli on 2017/11/13.
 *
 * @module 粉丝团成员页视图
 */
public class FansMemberView extends RelativeLayout
        implements IComponentView<FansMemberView.IPresenter, FansMemberView.IView> {
    private static final String TAG = "FansMemberView";

    @Nullable
    protected IPresenter mPresenter;

    private final FansMemberAdapter mAdapter = new FansMemberAdapter();
    private boolean mHasInflated = false;

    private FansGroupDetailModel mGroupDetailModel;

    private View mEmptyView;
    private RecyclerView mRecyclerView;

    private View mManagerMemberArea; // view in view stub

    private final FansMemberAdapter.IMemberClickListener mMemberClickListener =
            new FansMemberAdapter.IMemberClickListener() {
                @Override
                public void onItemClick(MemberItem item) {
                    mPresenter.showPersonalInfo(item.getUuid());
                }

                @Override
                public void onClickFocus(MemberItem item) {
                    mPresenter.fellowUser(item);
                }

                @Override
                public void onClickSixin(SixinTarget target) {
                    PopComposeMessageFragment.open((BaseActivity) getContext(), target, true);
                }
            };

    private final Runnable mHideLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            MyLog.d(TAG, "mHideLoadingRunnable");
            mAdapter.hideLoading();
            mEmptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        }
    };

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public FansMemberView(Context context) {
        this(context, null);
    }

    public FansMemberView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansMemberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void inflateContent() {
        if (mHasInflated) {
            return;
        }
        mHasInflated = true;
        inflate(getContext(), R.layout.fans_member_view, this);

        mEmptyView = $(R.id.empty_view);
        mRecyclerView = $(R.id.recycler_view);

        mAdapter.setClickListener(mMemberClickListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                        mPresenter.pullMore();
                    }
                }
            }
        });

        updateGroupDetail(mGroupDetailModel);
        mPresenter.startPresenter();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        inflateContent();
    }

    public void destroy() {
        mPresenter.stopPresenter();
        mPresenter.destroy();
    }

    public void updateGroupDetail(FansGroupDetailModel groupDetailModel) {
        if (groupDetailModel == null) {
            return;
        }
        mGroupDetailModel = groupDetailModel;
        mAdapter.setGroupCharmLevel(groupDetailModel.getCharmLevel());
        if (mHasInflated) {
            switch (groupDetailModel.getMemType()) {
                case VFansCommonProto.GroupMemType.OWNER_VALUE:
                case VFansCommonProto.GroupMemType.ADMIN_VALUE:
                case VFansCommonProto.GroupMemType.DEPUTY_ADMIN_VALUE:
                    if (mManagerMemberArea == null) {
                        ViewStub viewStub = $(R.id.manager_view_stub);
                        mManagerMemberArea = viewStub.inflate();
                        $click($(R.id.manage_vfans_member_btn), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FansMemberManagerFragment.openFragment((BaseActivity) getContext(),
                                        mGroupDetailModel, mAdapter.getItemData());
                            }
                        });
                    }
                    mManagerMemberArea.setVisibility(VISIBLE);
                    break;
                default:
                    if (mManagerMemberArea != null) {
                        mManagerMemberArea.setVisibility(GONE);
                    }
                    break;
            }
        }
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) FansMemberView.this;
            }

            @Override
            public void onNewDataSet(List<MemberItem> memberList) {
                mAdapter.addItemData(memberList);
            }

            @Override
            public void onUpdateDataSet(List<MemberItem> memberList) {
                mAdapter.setItemData(memberList);
            }

            @Override
            public void onLoadingStarted() {
                MyLog.d(TAG, "onLoadingStarted");
                mAdapter.showLoading();
                mRecyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
                removeCallbacks(mHideLoadingRunnable);
                mEmptyView.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingDone(boolean hasMore) {
                MyLog.d(TAG, "onLoadingDone");
                mAdapter.onLoadingDone(hasMore);
                if (mAdapter.isEmpty() || hasMore) {
                    postDelayed(mHideLoadingRunnable, 400);
                }
            }

            @Override
            public void onLoadingFailed() {
                MyLog.d(TAG, "onLoadingFailed");
                mAdapter.onLoadingFailed();
                postDelayed(mHideLoadingRunnable, 400);
            }

            @Override
            public void onFellowDone(MemberItem item) {
                mAdapter.onItemDataUpdated(item);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter extends IEventPresenter<IView> {
        /**
         * 拉取成员数据
         */
        void syncMemberData();

        /**
         * 拉取更多成员数据
         */
        void pullMore();

        /**
         * 显示个人信息页
         */
        void showPersonalInfo(long userId);

        /**
         * 关注用户
         */
        void fellowUser(MemberItem item);

    }

    public interface IView extends IViewProxy {
        /**
         * 拉取到成员数据
         */
        void onNewDataSet(List<MemberItem> memberList);

        /**
         * 更新成员数据
         */
        void onUpdateDataSet(List<MemberItem> memberList);

        /**
         * 拉取开始
         */
        void onLoadingStarted();

        /**
         * 拉取结束
         */
        void onLoadingDone(boolean hasMore);

        /**
         * 拉取失败
         */
        void onLoadingFailed();

        /**
         * 关注成功
         */
        void onFellowDone(MemberItem item);
    }
}
