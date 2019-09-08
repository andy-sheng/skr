package com.module.msg.fragment;

import android.content.Context;

import com.module.msg.custom.MyConversationListAdapter;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;

public class MyConversationListFragment extends ConversationListFragment {

    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        return new MyConversationListAdapter(context);
    }
}
