package io.rong.contacts.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfo;
import com.common.core.userinfo.UserInfoManager;
import com.facebook.drawee.view.SimpleDraweeView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.contacts.event.ContactsEvent;
import io.rong.contacts.adapter.FriendListAdapter;
import io.rong.contacts.utils.PinyinComparator;
import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.mention.SideBar;
import io.rong.imkit.tools.CharacterParser;

/**
 * 通讯录的 Fragment
 */
public class ContactsFragment extends BaseFragment implements View.OnClickListener {

    private SimpleDraweeView mAvatar;
    private TextView mNameTextView;
    private TextView mNoFriends;
    private TextView mUnreadTextView;
    private View mHeadView;
    private EditText mSearchEditText;
    private ListView mListView;
    private PinyinComparator mPinyinComparator;
    private SideBar mSidBar;
    /**
     * 中部展示的字母提示
     */
    private TextView mDialogTextView;

    private List<UserInfo> mFriendList;
    private List<UserInfo> mFilteredFriendList;
    /**
     * 好友列表的 mFriendListAdapter
     */
    private FriendListAdapter mFriendListAdapter;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser mCharacterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */

    private static final int CLICK_CONTACT_FRAGMENT_FRIEND = 2;

    @Override
    public int initView() {
        return R.layout.contacts_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mSearchEditText = (EditText) mRootView.findViewById(R.id.search);
        mListView = (ListView) mRootView.findViewById(R.id.listview);
        mNoFriends = (TextView) mRootView.findViewById(R.id.show_no_friend);
        mSidBar = (SideBar) mRootView.findViewById(R.id.sidrbar);
        mDialogTextView = (TextView) mRootView.findViewById(R.id.group_dialog);
        mSidBar.setTextView(mDialogTextView);
        LayoutInflater mLayoutInflater = LayoutInflater.from(getActivity());

        mHeadView = mLayoutInflater.inflate(R.layout.item_contact_list_header,
                null);
        mUnreadTextView = (TextView) mHeadView.findViewById(R.id.tv_unread);
        RelativeLayout newFriendsLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_newfriends);
        RelativeLayout groupLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_chatroom);
        RelativeLayout publicServiceLayout = (RelativeLayout) mHeadView.findViewById(R.id.publicservice);
        RelativeLayout selfLayout = (RelativeLayout) mHeadView.findViewById(R.id.contact_me_item);
        mAvatar = (SimpleDraweeView) mHeadView.findViewById(R.id.contact_me_img);
        mNameTextView = (TextView) mHeadView.findViewById(R.id.contact_me_name);
        updatePersonalUI();
        mListView.addHeaderView(mHeadView);
        mNoFriends.setVisibility(View.VISIBLE);

        selfLayout.setOnClickListener(this);
        groupLayout.setOnClickListener(this);
        newFriendsLayout.setOnClickListener(this);
        publicServiceLayout.setOnClickListener(this);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = mFriendListAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });

        mFriendList = new ArrayList<>();
        FriendListAdapter adapter = new FriendListAdapter(getActivity(), mFriendList);
        mListView.setAdapter(adapter);
        mFilteredFriendList = new ArrayList<>();
        //实例化汉字转拼音类
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = PinyinComparator.getInstance();

        updateUI();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    private void startFriendDetailsPage(UserInfo info) {
        // todo 打开个人主页
//        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
//        intent.putExtra("type", CLICK_CONTACT_FRAGMENT_FRIEND);
//        intent.putExtra("friend", friend);
//        startActivity(intent);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mDialogTextView != null) {
            mDialogTextView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr 需要过滤的 String
     */
    private void filterData(String filterStr) {
        List<UserInfo> filterDateList = new ArrayList<>();

        try {
            if (TextUtils.isEmpty(filterStr)) {
                filterDateList = mFriendList;
            } else {
                filterDateList.clear();
                for (UserInfo userInfo : mFriendList) {
                    String name = userInfo.getUserNickname();
                    String displayName = userInfo.getUserDisplayname();
                    if (!TextUtils.isEmpty(displayName)) {
                        if (name.contains(filterStr) || mCharacterParser.getSelling(name).startsWith(filterStr) || displayName.contains(filterStr) || mCharacterParser.getSelling(displayName).startsWith(filterStr)) {
                            filterDateList.add(userInfo);
                        }
                    } else {
                        if (name.contains(filterStr) || mCharacterParser.getSelling(name).startsWith(filterStr)) {
                            filterDateList.add(userInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, mPinyinComparator);
        mFilteredFriendList = filterDateList;
        mFriendListAdapter.updateListView(filterDateList);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.re_newfriends) {
            mUnreadTextView.setVisibility(View.GONE);
            // todo NewFriendListActivity页面
//                Intent intent = new Intent(getActivity(), NewFriendListActivity.class);
//                startActivityForResult(intent, 20);
        } else if (id == R.id.re_chatroom) {
            // todo GroupListActivity页面
//                startActivity(new Intent(getActivity(), GroupListActivity.class));
        } else if (id == R.id.publicservice) {
            // todo PublicServiceActivity页面
//                Intent intentPublic = new Intent(getActivity(), PublicServiceActivity.class);
//                startActivity(intentPublic);
        } else if (id == R.id.contact_me_item) {
            RongIM.getInstance().startPrivateChat(getActivity(),
                    String.valueOf(MyUserInfoManager.getInstance().getUid()),
                    MyUserInfoManager.getInstance().getNickName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsEvent.UpdateFriendEvent event) {
        if (event != null) {
            updateUI();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsEvent.UpdateRedDotEvent event) {
        if (event != null) {
            mUnreadTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsEvent.ChangeInfoEvent event) {
        updatePersonalUI();
    }

    private void updateUI() {
        UserInfoManager.getInstance().getFriends(new UserInfoManager.ResultCallback<List<UserInfo>>() {
            @Override
            public boolean onGetLocalDB(List<UserInfo> friends) {
                updateFriendsList(friends);
                return true;
            }

            @Override
            public boolean onGetServer(List<UserInfo> friends) {
                updateFriendsList(friends);
                return true;
            }
        });
    }

    private void updateFriendsList(List<UserInfo> friendsList) {
        //updateUI fragment初始化和好友信息更新时都会调用,isReloadList表示是否是好友更新时调用
        boolean isReloadList = false;
        if (mFriendList != null && mFriendList.size() > 0) {
            mFriendList.clear();
            isReloadList = true;
        }
        mFriendList = friendsList;
        if (mFriendList != null && mFriendList.size() > 0) {
            handleFriendDataForSort();
            mNoFriends.setVisibility(View.GONE);
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        // 根据a-z进行排序源数据
        Collections.sort(mFriendList, mPinyinComparator);
        if (isReloadList) {
            mSidBar.setVisibility(View.VISIBLE);
            mFriendListAdapter.updateListView(mFriendList);
        } else {
            mSidBar.setVisibility(View.VISIBLE);
            mFriendListAdapter = new FriendListAdapter(getActivity(), mFriendList);

            mListView.setAdapter(mFriendListAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mListView.getHeaderViewsCount() > 0) {
                        startFriendDetailsPage(mFriendList.get(position - 1));
                    } else {
                        startFriendDetailsPage(mFilteredFriendList.get(position));
                    }
                }
            });


            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    UserInfo bean = mFriendList.get(position - 1);
                    startFriendDetailsPage(bean);
                    return true;
                }
            });
            mSearchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                    filterData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0) {
                        if (mListView.getHeaderViewsCount() > 0) {
                            mListView.removeHeaderView(mHeadView);
                        }
                    } else {
                        if (mListView.getHeaderViewsCount() == 0) {
                            mListView.addHeaderView(mHeadView);
                        }
                    }
                }
            });
        }
    }

    private void updatePersonalUI() {
        mNameTextView.setText(MyUserInfoManager.getInstance().getNickName());
        AvatarUtils.loadAvatarByUrl(mAvatar,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());

    }

    private void handleFriendDataForSort() {
        for (UserInfo friend : mFriendList) {
            String displayName = friend.getUserDisplayname();
            String nickName = friend.getUserNickname();
            if (!TextUtils.isEmpty(displayName)) {
                String letters = replaceFirstCharacterWithUppercase(mCharacterParser.getSelling(displayName));
                friend.setLetter(letters);
            } else {
                String letters = replaceFirstCharacterWithUppercase(mCharacterParser.getSelling(nickName));
                friend.setLetter(letters);
            }
        }
    }

    private String replaceFirstCharacterWithUppercase(String spelling) {
        if (!TextUtils.isEmpty(spelling)) {
            char first = spelling.charAt(0);
            char newFirst = first;
            if (first >= 'a' && first <= 'z') {
                newFirst -= 32;
            }
            StringBuilder builder = new StringBuilder(String.valueOf(newFirst));
            if (spelling.length() > 1) {
                builder.append(spelling.substring(1, spelling.length()));
            }
            return builder.toString();
        } else {
            return "#";
        }
    }
}
