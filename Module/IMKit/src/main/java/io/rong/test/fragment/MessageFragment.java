package io.rong.test.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.common.view.titlebar.CommonTitleBar;
import com.module.msg.IMessageFragment;

import java.util.ArrayList;
import java.util.List;

import io.rong.contacts.fragment.ContactsFragment;
import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class MessageFragment extends BaseFragment implements  RongIM.UserInfoProvider,IMessageFragment {

    // todo 目前给两个账号用来测试通信，待账号完善接入
    CommonTitleBar commonTitleBar;

    ExButton mTestMsg1;
    ExButton mTestMsg2;

    List<UserInfo> list;
    Uri testUri = Uri.parse("http://cms-bucket.nosdn.127.net/a2482c0b2b984dc88a479e6b7438da6020161219074944.jpeg");

    Fragment mConversationListFragment; //获取融云的会话列表对象

    @Override
    public int initView() {
        return R.layout.conversation_list_fragment;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        commonTitleBar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mTestMsg1 = (ExButton) mRootView.findViewById(R.id.test_msg1);
        mTestMsg2 = (ExButton) mRootView.findViewById(R.id.test_msg2);

        initUserInfo();

        mConversationListFragment = initConversationList();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content,mConversationListFragment);
        transaction.commit();


        mTestMsg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RongIM.getInstance() != null) {
                    RongIM.getInstance().startPrivateChat(view.getContext(), "1001", "私人聊天");
                }
            }
        });

        mTestMsg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RongIM.getInstance() != null) {
                    RongIM.getInstance().startPrivateChat(view.getContext(), "1002", "私人聊天");
                }
            }
        });

        commonTitleBar.getRightTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newParamsBuilder(getActivity(), ContactsFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        commonTitleBar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo 搜索再加
//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newParamsBuilder(MessageActivity.this, SearchFragment.class)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .build());
            }
        });
    }

    private void initUserInfo() {
        list = new ArrayList<>();
        list.add(new UserInfo("1001","帅哥",testUri));
        list.add(new UserInfo("1002","美女",testUri));
        RongIM.setUserInfoProvider(this,true);
    }


    // 会话列表的Fragment
    private Fragment initConversationList() {
        if (mConversationListFragment == null) {
            ConversationListFragment listFragment = new ConversationListFragment();
            Uri uri = Uri.parse("io.rong://" + U.getAppInfoUtils().getPackageName()).buildUpon()
                    .appendPath("conversation_list_activity")
                    .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                    .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")
                    .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//设置私聊会话是否聚合显示
                    .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")  //设置私聊会话是否聚合显示
                    .build();
            listFragment.setUri(uri);
            return listFragment;
        } else {
            return mConversationListFragment;
        }
    }

    @Override
    public UserInfo getUserInfo(String useId) {
        for(UserInfo userInfo : list){
            if (userInfo.getUserId().equals(useId)){
                return userInfo;
            }
        }
        return null;
    }
}
