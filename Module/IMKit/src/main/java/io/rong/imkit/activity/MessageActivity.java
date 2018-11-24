package io.rong.imkit.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.module.RouterConstants;
import com.common.core.login.interceptor.JudgeLoginInterceptor;
import com.common.view.ex.ExButton;
import com.common.view.titlebar.CommonTitleBar;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.token.RCTokenManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * 消息列表会话页面
 */
@Route(path = RouterConstants.ACTIVITY_MESSAGE, extras = JudgeLoginInterceptor.NO_NEED_LOGIN)
public class MessageActivity extends BaseActivity implements RongIM.UserInfoProvider {

    CommonTitleBar commonTitleBar;
    ExButton mTestToken;
    ExButton mTestMsg;

    List<UserInfo> list;
    Uri testUri = Uri.parse("http://cms-bucket.nosdn.127.net/a2482c0b2b984dc88a479e6b7438da6020161219074944.jpeg");


    Fragment mConversationListFragment; //获取融云的会话列表对象

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.conversation_list_activity;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        commonTitleBar = (CommonTitleBar) findViewById(R.id.titlebar);
        mTestToken = (ExButton) findViewById(R.id.test_token);
        mTestMsg = (ExButton) findViewById(R.id.text_msg);

        mTestToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testGetToken();
            }
        });

        mTestMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testMsg();
            }
        });

        initUserInfo();
    }

    private void initUserInfo() {
        list = new ArrayList<>();
        list.add(new UserInfo("1001","帅哥",testUri));
        list.add(new UserInfo("1002","美女",testUri));
        RongIM.setUserInfoProvider(this,true);
    }

    //跳转到消息界面
    private void testMsg() {
        if (RongIM.getInstance() != null) {
            RongIM.getInstance().startPrivateChat(this, "1002", "私人聊天");
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    public void testGetToken() {
//        String path = Environment.getExternalStorageDirectory() + "/111.jpg";
//        File file = new File(path);
//        if (!file.exists()) {
//            U.getToastUtil().showShort("文件不存在");
//        }
//        URI uri = file.toURI();

        RCTokenManager.getInstance().getToken("1001","帅哥",testUri.toString());
//        RongIMAPIManager.getInstance().getToken("1002","美女",testUri.toString());
    }

    // 会话列表的Fragment
    private Fragment initConversationList() {
        if (mConversationListFragment == null) {
            ConversationListFragment listFragment = new ConversationListFragment();
            Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
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
    public UserInfo getUserInfo(String s) {
        for(UserInfo userInfo : list){
            if (userInfo.getUserId().equals(s)){
                return userInfo;
            }
        }
        return null;
    }
}
