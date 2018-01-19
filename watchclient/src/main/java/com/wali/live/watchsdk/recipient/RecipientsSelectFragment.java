package com.wali.live.watchsdk.recipient;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.user.User;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.recipient.adapter.RecipientsSelectRecyclerAdapter;
import com.wali.live.watchsdk.recipient.presenter.RecipientPresenter;
import com.wali.live.watchsdk.recipient.view.IndexableRecyclerView;
import com.wali.live.watchsdk.recipient.view.UserSectionIndexer;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;

/**
 * Created by zhangyuehuan on 06/11/17.
 *
 * @module 选人（支持搜索、单选和点击直接选择跳转。 注：拿掉了多选功能，助手暂时没有用到多选功能的地方，等需要了再加）
 */
public class RecipientsSelectFragment extends RxFragment implements View.OnClickListener, RecipientPresenter.IView {

    public static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";
    public static final String SELECT_MODE = "INTENT_KEY_MODE";                  //模式
    public static final String DATA_TYPE = "DATA_TYPE";                          //数据类型
    public static final String SELECT_TITLE = "SELECT_TITLE";                    //title
    public static final String INTENT_HINT_TITLE = "INTENT_HINT_TITLE";          //显示的提示title
    public static final String INTENT_SHOW_LEVEL_SEX = "INTENT_SHOW_LEVEL_SEX";  //显示性别和等级
    public static final String INTENT_SHOW_BOTH_WAY = "INTENT_SHOW_BOTH_WAY";    //关注列表是否双向
    public static final String INTENT_ENABLE_SEARCH = "INTENT_ENABLE_SEARCH";    //是否可以搜索
    public static final String INTENT_ENABLE_INDEX = "INTENT_ENABLE_INDEX";      //是否显示字母索引
    public static final String INTENT_LIVE_ROOM_ID = "INTENT_LIVE_ROOM_ID";      //房间id
    public static final String RESULT_SINGLE_OBJECT = "RESULT_SINGLE_USER";      //单选返回结果对象

    public static final int SELECT_MODE_SINGLE_CLICK = 0;
    public static final int SELECT_MODE_SINGLE_SELECT = 1;
    public static final int SELECT_MODE_MULTI = 2;

    public static final int REQUEST_CODE_RECIPIENT_SELECT = 1000;
    public static final int REQUEST_CODE_PICK_MANAGER = 1001;
    public static final int REQUEST_CODE_PICK_USER = 1002;

    private int mRequestCode;
    private boolean mSearchEnable = false;
    private String mTitle;
    private String mHintTitle;
    private boolean mShowLevelSex = false;
    private boolean mBothWay = false;
    private int mMode;
    private int mDataType;

    private BackTitleBar mTitleBar;
    private IndexableRecyclerView mRecyclerView;
    private TextView mHintTitleTv;
    private EditText mSearchEt;
    private View mSearchBar;
    private View mDeleteBtn;
    private ViewGroup mCoverView;
    private TextView mLoadingTv;
    private TextView mEmptyTv;
    private RecipientsSelectRecyclerAdapter mRecyclerAdapter;
    private UserSectionIndexer mUserSectionIndexer;
    private RecipientPresenter mRecipientPresenter;
    private RecipientsSelectRecyclerAdapter.OnItemClickListener mItemClickListener
            = new RecipientsSelectRecyclerAdapter.OnItemClickListener() {
        @Override
        public void onItemClickListener(UserListData listData) {
            if (mMode == SELECT_MODE_SINGLE_CLICK) {
                Intent intent = new Intent();
                intent.putExtra(RecipientsSelectFragment.RESULT_SINGLE_OBJECT,
                        new User(listData.userId, listData.userNickname, listData.level,
                                listData.avatar, listData.certificationType));
                finish();
                EventBus.getDefault().post(new EventClass.OnActivityResultEvent(mRequestCode,
                        Activity.RESULT_OK, intent));
            } else if (mMode == SELECT_MODE_SINGLE_SELECT) {
                mTitleBar.getRightTextBtn().setEnabled(true);
            }
        }
    };

    @Override
    public int getRequestCode() {
        return mRequestCode;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.recipients_select_fragment, container, false);
    }

    @Override
    protected void bindView() {
        initData();
        initView();
        initPresenter();
        mLoadingTv.setVisibility(View.VISIBLE);
    }

    private void initData() {
        mDataType = getArguments().getInt(RecipientsSelectFragment.DATA_TYPE, RecipientsSelectRecyclerAdapter.ITEM_TYPE_FOLLOWING);
        mRequestCode = getArguments().getInt(KEY_REQUEST_CODE);
        mMode = getArguments().getInt(RecipientsSelectFragment.SELECT_MODE, RecipientsSelectFragment.SELECT_MODE_SINGLE_CLICK);
        if (mMode > RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
            mMode = RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT;
        }
        mTitle = getArguments().getString(RecipientsSelectFragment.SELECT_TITLE);
        mHintTitle = getArguments().getString(RecipientsSelectFragment.INTENT_HINT_TITLE);
        mShowLevelSex = getArguments().getBoolean(RecipientsSelectFragment.INTENT_SHOW_LEVEL_SEX, true);
        mBothWay = getArguments().getBoolean(RecipientsSelectFragment.INTENT_SHOW_BOTH_WAY, true);
        mSearchEnable = getArguments().getBoolean(RecipientsSelectFragment.INTENT_ENABLE_SEARCH, false);
    }

    private void initPresenter() {
        mRecipientPresenter = new RecipientPresenter(mMode, this);
        mRecipientPresenter.loadDataFromServer(UserAccountManager.getInstance().getUuidAsLong(), mBothWay);
    }

    private void initView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(mTitle);
        $click(mTitleBar.getBackBtn(), this);
        mRecyclerView = $(R.id.recycler_view);
        mHintTitleTv = $(R.id.hint_title);
        mSearchEt = $(R.id.search_input_edit_text);
        mSearchBar = $(R.id.search_bar);
        mDeleteBtn = $(R.id.delete_btn);
        mCoverView = $(R.id.cover_view);
        mLoadingTv = $(mCoverView, R.id.loading);
        mEmptyTv = $(mCoverView, R.id.empty);

        if (mMode == RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
            $click(mTitleBar.getRightTextBtn(), this);
            mTitleBar.getRightTextBtn().setText(getString(R.string.ok));
            mTitleBar.getRightTextBtn().setEnabled(false);
        }

        if (!TextUtils.isEmpty(mHintTitle)) {
            mHintTitleTv.setText(mHintTitle);
            mHintTitleTv.setVisibility(View.VISIBLE);
        } else {
            mHintTitleTv.setVisibility(View.GONE);
        }

        mRecyclerAdapter = new RecipientsSelectRecyclerAdapter(mDataType, mMode);
        mRecyclerAdapter.setShowLevelSex(mShowLevelSex);
        mRecyclerAdapter.setItemClickListener(mItemClickListener);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mUserSectionIndexer = new UserSectionIndexer();
        mRecyclerView.setSectionIndexer(mUserSectionIndexer);

        if (mSearchEnable) {
            mSearchBar.setVisibility(View.VISIBLE);
            mSearchEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s.toString())) {
                        mDeleteBtn.setVisibility(View.GONE);
                    } else {
                        mDeleteBtn.setVisibility(View.VISIBLE);
                    }
                    mRecipientPresenter.doSearch(s.toString());
                }
            });
            mDeleteBtn.setOnClickListener(this);
        } else {
            mSearchBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            finish();
        } else if (i == R.id.right_text_btn) {
            Intent intent = new Intent();
            if (mMode == RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
                Object selObj = mRecyclerAdapter.getSelectItem();
                if (selObj != null && selObj instanceof UserListData) {
                    UserListData listData = (UserListData) selObj;
                    intent.putExtra(RecipientsSelectFragment.RESULT_SINGLE_OBJECT,
                            new User(listData.userId, listData.userNickname, listData.level,
                                    listData.avatar, listData.certificationType));
                }
            }
            finish();
            EventBus.getDefault().post(new EventClass.OnActivityResultEvent(mRequestCode,
                    Activity.RESULT_OK, intent));
        } else if (i == R.id.delete_btn) {
            mSearchEt.setText("");
            KeyboardUtils.hideKeyboard(getActivity(), mSearchEt);
        }
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return super.onBackPressed();
    }

    private void finish() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public boolean isStatusBarDark() {
        return true;
    }

    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    @Override
    public <T> Observable.Transformer<T, T> bindUntilEvent() {
        return bindUntilEvent();
    }

    @Override
    public void notifyDataSetChanged(List<Object> list) {
        if (list != null && list.size() > 0) {
            mCoverView.setVisibility(View.INVISIBLE);
            mRecyclerAdapter.setData(list);
            mUserSectionIndexer.setDataList(list);
            if (list != null && list.size() > 0) {
                mRecyclerView.showIndexBar();
                mRecyclerView.enableScrollListener(true);
            } else {
                mRecyclerView.hideIndexBar();
            }
        } else {
            mCoverView.setVisibility(View.VISIBLE);
            mLoadingTv.setVisibility(View.GONE);
            mEmptyTv.setVisibility(View.VISIBLE);
        }
    }
}
