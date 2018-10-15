package com.wali.live.watchsdk.fans.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.dialog.DialogUtils;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.base.view.SymmetryTitleBar;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter.MemberItem;
import com.wali.live.watchsdk.fans.adapter.FansMemberManagerAdapter;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yangli on 2017/11/16.
 *
 * @module 粉丝团成员管理页视图
 */
public class FansMemberManagerView extends LinearLayout
        implements IComponentView<FansMemberManagerView.IPresenter, FansMemberManagerView.IView> {
    private static final String TAG = "FansMemberManagerView";

    private static final int MODE_NONE = -1;
    private static final int MODE_NORMAL = 0;
    private static final int MODE_BATCH_DELETE = 1;

    private int mMode = MODE_NONE;

    @Nullable
    protected IPresenter mPresenter;

    private final FansMemberManagerAdapter mAdapter = new FansMemberManagerAdapter();

    private FansGroupDetailModel mGroupDetailModel;

    private SymmetryTitleBar mTitleBar;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private View mEmptyView;
    private RecyclerView mRecyclerView;

    private final FansMemberManagerAdapter.IMemberClickListener mMemberClickListener =
            new FansMemberManagerAdapter.IMemberClickListener() {
                private static final int MENU_ITEM_ADD_MANAGER = 1;           // 设置为管理员
                private static final int MENU_ITEM_REMOVE_MANAGER = 2;        // 取消管理员
                private static final int MENU_ITEM_ADD_DEPUTY_MANAGER = 3;    // 添加副管理员
                private static final int MENU_ITEM_REMOVE_DEPUTY_MANAGER = 4; // 删除副管理员
                private static final int MENU_ITEM_KICK = 5;                  // 踢出粉丝团
                private static final int MENU_ITEM_CANCEL = 6;                // 取消

                @Override
                public void onItemClick(final MemberItem memberItem) {
                    final int myMemType = mGroupDetailModel.getMemType(), itemMemType = memberItem.getMemType();
                    if (myMemType >= itemMemType) {
                        return;
                    }
                    final List<Integer> actions = new ArrayList<>(4);
                    final List<String> names = new ArrayList<>(4);
                    if (myMemType == VFansCommonProto.GroupMemType.OWNER_VALUE) {
                        if (itemMemType == VFansCommonProto.GroupMemType.ADMIN_VALUE) {
                            names.add(getResources().getString(R.string.vfans_member_cancle_manager));
                            actions.add(MENU_ITEM_REMOVE_MANAGER);
                        } else {
                            names.add(getResources().getString(R.string.vfans_member_add_manager));
                            actions.add(MENU_ITEM_ADD_MANAGER);
                        }
                    }
                    if (myMemType == VFansCommonProto.GroupMemType.ADMIN_VALUE ||
                            myMemType == VFansCommonProto.GroupMemType.OWNER_VALUE) {
                        if (itemMemType == VFansCommonProto.GroupMemType.DEPUTY_ADMIN_VALUE) {
                            names.add(getResources().getString(R.string.vfans_member_cancle_deputy_manager));
                            actions.add(MENU_ITEM_REMOVE_DEPUTY_MANAGER);
                        } else {
                            names.add(getResources().getString(R.string.vfans_member_add_deputy_manager));
                            actions.add(MENU_ITEM_ADD_DEPUTY_MANAGER);
                        }
                    }
                    actions.add(MENU_ITEM_KICK);
                    names.add(getResources().getString(R.string.vfans_member_remove_member));
                    actions.add(MENU_ITEM_CANCEL);
                    names.add(getResources().getString(R.string.cancel));

                    final String[] nameAsArray = new String[names.size()];
                    names.toArray(nameAsArray);
                    MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getContext())
                            .setItems(nameAsArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (actions.get(which)) {
                                        case MENU_ITEM_ADD_MANAGER:
                                            mPresenter.setManager(memberItem, true);
                                            break;
                                        case MENU_ITEM_REMOVE_MANAGER:
                                            mPresenter.setManager(memberItem, false);
                                            break;
                                        case MENU_ITEM_ADD_DEPUTY_MANAGER:
                                            mPresenter.setDeputyManager(memberItem, true);
                                            break;
                                        case MENU_ITEM_REMOVE_DEPUTY_MANAGER:
                                            mPresenter.setDeputyManager(memberItem, false);
                                            break;
                                        case MENU_ITEM_KICK: {
                                            showRemoveDialog(Arrays.asList(memberItem));
                                            break;
                                        }
                                    }
                                }
                            });
                    builder.create().show();
                }

                @Override
                public void onItemSelectionChange(int cnt) {
                    mRightBtn.setEnabled(cnt > 0);
                    mRightBtn.setText(getResources().getString(R.string.vfans_member_delete, cnt));
                }
            };

    private void showRemoveDialog(final List<MemberItem> memberItems) {
        DialogUtils.showNormalDialog(
                (Activity) getContext(),
                0,
                R.string.vfans_no_member_to_delete_notify,
                R.string.ok,
                R.string.cancel,
                new DialogUtils.IDialogCallback() {
                    @Override
                    public void process(DialogInterface dialogInterface, int i) {
                        mPresenter.removeMember(memberItems);
                    }
                },
                null);
    }

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
    public final void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public FansMemberManagerView(Context context) {
        this(context, null);
    }

    public FansMemberManagerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansMemberManagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(getContext(), R.layout.fans_member_manager_view, this);

        mTitleBar = $(R.id.title_bar);
        mLeftBtn = mTitleBar.getLeftTextBtn();
        mRightBtn = mTitleBar.getRightTextBtn();
        mEmptyView = $(R.id.empty_view);
        mRecyclerView = $(R.id.recycler_view);

        mAdapter.setClickListener(mMemberClickListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (!ViewCompat.canScrollVertically(recyclerView, 1) && mPresenter != null) {
                        mPresenter.pullMore();
                    }
                }
            }
        });
        switchMode(MODE_NORMAL);
    }

    private void switchMode(int mode) {
        if (mMode == mode) {
            return;
        }
        mMode = mode;
        if (mMode == MODE_BATCH_DELETE) {
            mTitleBar.setTitle(R.string.vfans_member_title_manager_member);
            mLeftBtn.setText(R.string.cancel);
            mLeftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchMode(MODE_NORMAL);
                }
            });
            mRightBtn.setText(getResources().getString(R.string.vfans_member_delete, 0));
            mRightBtn.setEnabled(false);
            mRightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<MemberItem> selection = mAdapter.getSelectedItem();
                    if (selection != null) {
                        showRemoveDialog(selection);
                    } else {
                        ToastUtils.showToast(R.string.vfans_no_member_to_delete);
                    }
                }
            });
            mAdapter.setIsBatchDeleteMode(true);
        } else {
            mTitleBar.setTitle(R.string.vfans_member_title_manager_member);
            mLeftBtn.setText(R.string.vfans_member_title_manager_batch_delete);
            mLeftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchMode(MODE_BATCH_DELETE);
                }
            });
            mRightBtn.setText(R.string.vfans_member_title_manager_finish);
            mRightBtn.setEnabled(true);
            mRightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentNaviUtils.popFragment((FragmentActivity) getContext());
                }
            });
            mAdapter.setIsBatchDeleteMode(false);
        }
    }

    public void updateGroupDetail(FansGroupDetailModel groupDetailModel) {
        if (groupDetailModel == null) {
            return;
        }
        mGroupDetailModel = groupDetailModel;
        mAdapter.setGroupCharmLevel(groupDetailModel.getCharmLevel());
        mAdapter.setMyMemType(groupDetailModel.getMemType());
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) FansMemberManagerView.this;
            }

            @Override
            public void onUpdateDataSet(List<MemberItem> memberList) {
                if (memberList != null) {
                    mAdapter.setItemDataEx(memberList);
                }
            }

            @Override
            public void onRemoveDone(List<MemberItem> memberList) {
                if (memberList != null) {
                    mAdapter.removeSelection(memberList);
                }
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
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 拉取成员数据
         */
        void syncMemberData();

        /**
         * 拉取更多成员数据
         */
        void pullMore();

        /**
         * 设置/取消团长
         */
        void setManager(MemberItem memberItem, boolean state);

        /**
         * 设置/取消副团长
         */
        void setDeputyManager(MemberItem memberItem, boolean state);

        /**
         * 删除团成员
         */
        void removeMember(List<MemberItem> memberItems);
    }

    public interface IView extends IViewProxy {
        /**
         * 更新成员数据
         */
        void onUpdateDataSet(List<MemberItem> memberList);

        /**
         * 删除成功
         */
        void onRemoveDone(List<MemberItem> memberItems);

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
    }
}
