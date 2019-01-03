package test.fragment;

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
import com.common.view.titlebar.CommonTitleBar;
import com.module.msg.IMessageFragment;
import com.zq.relation.fragment.RelationFragment;
import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class MessageFragment extends BaseFragment implements  RongIM.UserInfoProvider,IMessageFragment {

    CommonTitleBar commonTitleBar;

    List<UserInfo> list;

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

        mConversationListFragment = initConversationList();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content,mConversationListFragment);
        transaction.commit();

        RongIM.setUserInfoProvider(this,true);

        commonTitleBar.getRightCustomView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        // TODO: 2019/1/3 暂时屏蔽搜索
//        commonTitleBar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newParamsBuilder(MessageActivity.this, SearchFragment.class)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .build());
//            }
//        });
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
