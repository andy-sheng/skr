package com.module.msg.fragment;

import com.module.msg.custom.MyMessageListAdapter;

import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.widget.adapter.MessageListAdapter;

public class MyConversationFragment extends ConversationFragment {
    @Override
    public MessageListAdapter getMessageAdapter() {
        return new MyMessageListAdapter(getActivity());
    }
}
